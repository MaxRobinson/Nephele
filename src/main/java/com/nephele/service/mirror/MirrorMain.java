package com.nephele.service.mirror;

/*-
 * #%L
 * NepheleService
 * %%
 * Copyright (C) 2020 - 2022 Max Robinson
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.nephele.config.S3SourceEnum;
import com.nephele.service.config.ConfigService;
import com.nephele.service.config.SynchronizationConfig;
import com.nephele.service.config.UploadPathConfig;
import com.nephele.service.indexcache.IndexedFileMetadata;
import com.nephele.service.indexcache.RedisIndexCache;
import com.nephele.service.s3.S3Path;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static software.amazon.awssdk.regions.Region.US_EAST_1;

@ApplicationScoped
public class MirrorMain {
    private static final Logger logger = LoggerFactory.getLogger(MirrorMain.class);

    private static final AtomicBoolean activelyMirroring = new AtomicBoolean(false);
    protected final int PART_SIZE = 5 * 1024 * 1024;

    @Inject
    S3Client s3Client;

    @Inject
    protected S3UploadMirror uploadMirror;

    @Inject
    protected RedisIndexCache indexCache;

    @ConfigProperty(name = "s3mirror.mode.mirror")
    Boolean mirrorMode;

    @ConfigProperty(name = "s3mirror.source")
    S3SourceEnum source;

    @ConfigProperty(name = "s3mirror.s3.access-key-id")
    String targetAccessKeyId;
    @ConfigProperty(name = "s3mirror.s3.secret-access-key")
    String secretAccessKey;
    @ConfigProperty(name = "s3mirror.s3.endpoint-override")
    String endpointOverride;

    @Inject
    protected ConfigService configService;

    private DownloadMirror downloadMirror;

    public MirrorMain() {
    }

    @Scheduled(cron="{s3mirror.schedule.mirror}")
    public void mirrorMain() throws Exception {
        ensureInit();
        if(mirrorMode){
            if(activelyMirroring.compareAndSet(false, true)){
                try {
                    mirror();
                } catch(Exception | OutOfMemoryError ex){
                    logger.error("Error while mirroring. Stopping mirroring and turning running flag to false", ex);
                } finally {
                    activelyMirroring.compareAndSet(true, false);
                }
            } else {
                logger.info("Mirror is already running, skipping this cron start of mirroring");
            }

        } else {
            logger.info("Skipping mirroring. Deployment not in mirroring mode.");
        }
    }

    public void mirror() throws Exception {
        ensureInit();
        SynchronizationConfig config = configService.getConf();
        for(UploadPathConfig p : config.getSynchronizationPaths()){
            logger.info("Mirroring configured Upload path {}", p.getPath());
            List<IndexedFileMetadata> itemsToMirror = getItemsToMirror(p.getPath());
            uploadItemsToTarget(itemsToMirror);
            logger.info("Mirriong complete for Upload path {}", p.getPath());
        }
        logger.info("Mirroring complete");
    }

    /**
     * Does the transfer from one place to the next
     * @param itemsToUpload
     */
    private void uploadItemsToTarget(List<IndexedFileMetadata> itemsToUpload) {
        ensureInit();
        for(IndexedFileMetadata metadata : itemsToUpload){
            try {
                mirrorSingleItem(metadata);
            } catch (S3Exception ex) {
                // don't stop mirroring all files just cause 1 had a problem.
                // will continually halt all mirroring in the list if we don't catch at least some type of error.
                logger.error("Error Mirroring {}", metadata.getBoxPath(), ex);
                logger.error("Continuing on to the next item.");
            }
        }
    }

    public void mirrorSingleItem(IndexedFileMetadata metadata) {
        ensureInit();
        S3Path s3Path = S3Path.fromPath(metadata.getBoxPath());

        TargetMirrorMetadata targetMetadata = new TargetMirrorMetadata();
        targetMetadata.setSourceLastModTime(metadata.getLastModTime().toInstant());
        targetMetadata.setSourceSha1(metadata.getSha1());

        OutputStream s3outputStream;
        if(metadata.getSize() <= PART_SIZE){
            s3outputStream = new S3OneShotOutputStream(s3Client, s3Path.getBucket(), s3Path.getObjectKey(), targetMetadata);
        } else {
            s3outputStream = new S3LargeOutputStream(s3Client, s3Path.getBucket(), s3Path.getObjectKey(), targetMetadata);
        }

        try {
            logger.info("Mirroring file {} : {}", metadata.getBoxPath(), metadata.getBoxId());
            if(source == S3SourceEnum.box) {
                // Want to check that the metadata matches indexed metadata
                if(downloadMirror.matchesMetadata(metadata.getBoxId(), targetMetadata)) {
                    downloadMirror.getByID(metadata.getBoxId(), s3outputStream);
                } else {
                    logger.info("Index metadata does not current source metadata. Not uploading {}", metadata.getBoxPath());
                }
            } else {
                if(downloadMirror.matchesMetadata(metadata.getBoxPath(), targetMetadata)){
                    downloadMirror.getByID(metadata.getBoxPath(), s3outputStream);
                }
                else {
                    logger.info("Index metadata does not match current source metadata. Not uploading {}", metadata.getBoxPath());
                }
            }
        } catch (OutOfMemoryError ex){
            logger.error("OOM Error while downloading or uploading: {}", metadata.getBoxPath(), ex);
        } catch (NoSuchKeyException ex) {
            logger.warn("No Such Key Exception trying to mirror: {}:{}", s3Path.getBucket(), s3Path.getObjectKey());
        } finally {
            try {
                s3outputStream.close();
            } catch (IOException e) {
                logger.error("Error closing input stream", e);
            }
        }
    }

    private void ensureInit(){
        if(downloadMirror != null){
            return;
        }

        if (source == S3SourceEnum.box) {
            downloadMirror = new BoxDownloadMirror();
        } else {
            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                    targetAccessKeyId,
                    secretAccessKey);

            S3Client s3TargetClient = S3Client.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                    .endpointOverride(URI.create(endpointOverride))
                    .region(US_EAST_1)
                    .httpClientBuilder(UrlConnectionHttpClient.builder())
                    .build();
            downloadMirror = new S3DownloadMirror(s3TargetClient);
        }
    }

//    /**
//     * Does the transfer from one place to the next
//     * @param itemsToUpload
//     */
//    private void uploadItemsToTarget(List<IndexedFileMetadata> itemsToUpload) {
//        for(IndexedFileMetadata metadata : itemsToUpload){
//            InputStream i = new ByteArrayInputStream(new byte[0]);
//            try {
//                logger.info("Downloading file {} : {}", metadata.getBoxPath(), metadata.getBoxId());
//                byte[] content = downloadMirror.getByID(metadata.getBoxId());
//                logger.info("Uploading file {} : {}", metadata.getBoxPath(), metadata.getBoxId());
//                i = new ByteArrayInputStream(content);
//                uploadMirror.upload(metadata.getBoxPath(), i, content.length);
//            } catch (OutOfMemoryError ex){
//                logger.error("OOM Error while downloading or uploading: {}", metadata.getBoxPath(), ex);
//            } finally {
//                try {
//                    i.close();
//                } catch (IOException e) {
//                    logger.error("Error closing input stream", e);
//                }
//            }
//        }
//    }

    public List<IndexedFileMetadata> getItemsToMirror(String path) throws Exception {
        S3Path s3Path = S3Path.fromPath(path);

        List<IndexedFileMetadata> indexedItems =  indexCache.get(path);
        List<ComparisonFileObject> targetComparableFiles = new ArrayList<>(indexedItems.size());
//        List<ComparisonFileObject> targetComparableFiles = uploadMirror.getComparableFiles(path);

        // get head of object
        for(IndexedFileMetadata i : indexedItems){
            ComparisonFileObject compObject = uploadMirror.getComparableFile(i.getBoxPath());
            if (compObject != null){
                targetComparableFiles.add(compObject);
            }
        }


        List<IndexedFileMetadata> itemsToUpload = getItemsToMirror(indexedItems, targetComparableFiles);
        return itemsToUpload;
    }

    private List<IndexedFileMetadata> getItemsToMirror(List<IndexedFileMetadata> indexedFileMetadata,
                                                       List<ComparisonFileObject> filesInTargetMirror) throws Exception {

        HashMap<String, ComparisonFileObject> targetMap = new HashMap<>();
        for(ComparisonFileObject f : filesInTargetMirror){
            targetMap.put(f.getIdentifier(), f);
        }

        List<IndexedFileMetadata> res = new ArrayList<>();
        for(IndexedFileMetadata indexedFile : indexedFileMetadata){
            S3Path s3Path = S3Path.fromPath(indexedFile.getBoxPath());
            String spath = s3Path.asPath();
            ComparisonFileObject comparisonFileObject = targetMap.get(spath);

            if(comparisonFileObject == null){
                // file not in target, upload
                res.add(indexedFile);
            } else {

                Instant comparisonFileLastModTime;
                TargetMirrorMetadata meta = comparisonFileObject.getTargetMirrorMetadata();
                if(meta.hasLastModTime()){
                    comparisonFileLastModTime = meta.getSourceLastModTime();
                } else {
                    comparisonFileLastModTime = comparisonFileObject.getLastModified();
                }

                if(indexedFile.getLastModTime().toInstant().compareTo(comparisonFileLastModTime) > 0) {
                    res.add(indexedFile);
                }
            }
        }

        return res;
    }

}
