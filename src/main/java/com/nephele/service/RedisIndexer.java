package com.nephele.service;

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

import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nephele.service.box.BoxClient;
import com.nephele.service.box.BoxClientFactory;
import com.nephele.service.config.ConfigService;
import com.nephele.service.config.SynchronizationConfig;
import com.nephele.service.config.UploadPathConfig;
import com.nephele.service.indexcache.IndexedFileMetadata;
import com.nephele.service.s3.S3Path;
import io.quarkus.redis.client.RedisClient;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class RedisIndexer {
    private static final Logger logger = LoggerFactory.getLogger(BoxClientFactory.class);
    private static final ZoneId utc = ZoneId.of("UTC");

    private final BoxClient client;
    private ZonedDateTime todayDateTime;

    private final String[] infoParameters = {"id", "type", "content_created_at", "content_modified_at", "created_at",
            "etag", "modified_at", "name", "path_collection", "sha1", "size"};

    @Inject
    ConfigService configService;

    @ConfigProperty(name = "s3mirror.mode.index")
    Boolean indexMode;

    @Inject
    RedisClient redisClient;

    @Inject
    ObjectMapper om;

    public RedisIndexer() {
        client = new BoxClient(new BoxClientFactory().createClient());
        LocalDateTime today = LocalDateTime.of(LocalDate.now(utc), LocalTime.of(0,0,0));
        todayDateTime = ZonedDateTime.of(today, utc);
    }

    @Scheduled(cron="0 0/7 * * * ?")
    public void indexMain() throws JsonProcessingException {
        if(indexMode){
            index();
        } else {
            logger.info("Skipping indexing. Deployment not in indexing mode.");
        }
    }

    public void index() throws JsonProcessingException {
        logger.info("Starting Indexing job");

        updateCurrentDateTime();

        // get paths
        SynchronizationConfig config = configService.getConf();

        // index paths
        // initialize / ensure init paths
        for(UploadPathConfig p : config.getSynchronizationPaths()){
            if(!client.isValidBoxId(p.getBox_directory_id())){
                String id = client.getID(p.getPath());
                if(id == null){
                    logger.warn("Id for upload config item could not be found: {}", p.getPath());
                }
                p.setBox_directory_id(id);
            }
        }

        // get files
        // push to cache
        for(UploadPathConfig p : config.getSynchronizationPaths()) {
            List<IndexedFileMetadata> l = indexPath(p);
            // delete current redis index before indexing
            redisClient.del(List.of(p.getPath()));
            for (IndexedFileMetadata i : l) {
                redisClient.hset(List.of(p.getPath(), i.getBoxPath(), om.writeValueAsString(i)));
            }
        }
        logger.info("Finished indexing job");
    }

    public List<IndexedFileMetadata> indexPath(UploadPathConfig p) {
        List<IndexedFileMetadata> res = new ArrayList<>();
        if (p.getBox_directory_id() == null){
            logger.info("Upload Path config didn't have a box id: {}", p.getPath());
            return List.of();
        }

        BoxItem item = client.itemOf(p.getBox_directory_id());
        BoxItem.Info info = item.getInfo();

        if(client.boxTypeIsFile(info)){
            res.add(toIndexedObjFromInfo(info));
            return res;
        }

        // else we have a folder and we want to BFS all the way down.
        BoxFolder pBaseFolder = (BoxFolder) item;
        Queue<BoxItem.Info> frontier = new ArrayDeque<>();

        logger.info("Initialize the Frontier");
        // init frontier
        for(BoxItem.Info childInfo : pBaseFolder.getChildren(infoParameters)){
            frontier.add(childInfo);
        }

        // upload root files only
        if(p.always_upload_root_files){
            logger.info("Add all files in root regardless of time offset under: {}", info.getName());
            List<IndexedFileMetadata> alwaysUploadFile = new ArrayList<>();
            for(BoxItem.Info itemInfo : frontier) {
                if(client.boxTypeIsFile(itemInfo)) {
                    logger.debug("Adding always upload file in root {}", itemInfo.getName());
                    IndexedFileMetadata obj = toIndexedObjFromInfo(itemInfo);
                    alwaysUploadFile.add(obj);
                }
            }

            res.addAll(alwaysUploadFile);
        }

        if(!p.recursive){
            logger.info("Not looking for files to upload past top level. Config set to no recursion for {}", info.getName());
            return res;
        }

        ZonedDateTime referenceTime = todayDateTime.minusDays(p.last_mod_time_delta_days);
        logger.debug("Reference time for configured indexing for {} is {}", info.getName(), referenceTime);

        // start BFS
        logger.debug("Starting BFS");
        while(!frontier.isEmpty()){
            BoxItem.Info currInfo = frontier.poll();

            ZonedDateTime lastModTime = ZonedDateTime.ofInstant(currInfo.getModifiedAt().toInstant(), utc);

            // if lastModTime < reference Time skip
            if(lastModTime.compareTo(referenceTime) < 0){
                continue;
            }

            if(client.boxTypeIsFile(currInfo)){
                logger.info("Adding file to index result list: {}", currInfo.getName());
                res.add(toIndexedObjFromInfo(currInfo));
            } else {
                logger.debug("Adding folder contents to frontier. Folder name: {}", currInfo.getName());
                BoxFolder f = (BoxFolder) client.itemOf(currInfo);
                f.getChildren(infoParameters).forEach(frontier::add);
            }
        }

        return res;
    }

    private void updateCurrentDateTime(){
        LocalDateTime today = LocalDateTime.of(LocalDate.now(utc), LocalTime.of(0,0,0));
        todayDateTime = ZonedDateTime.of(today, utc);
    }


    private IndexedFileMetadata toIndexedObjFromInfo(BoxItem.Info info){
        String sha1 = "";
        if(client.boxTypeIsFile(info)){
            sha1 =  ((BoxFile.Info)info).getSha1();
        }

        Date modTime = info.getContentModifiedAt();
        OffsetDateTime lastModTime = OffsetDateTime.ofInstant(modTime.toInstant(), utc);

        // build box path
        List<BoxItem.Info> pathToObject = new ArrayList<>(info.getPathCollection());
        pathToObject.remove(0);
        pathToObject.add(info);
        String boxPath = pathToObject.stream().map(BoxItem.Info::getName).collect(Collectors.joining("/"));

        String s3Path = null;
        try {
            s3Path = S3Path.objectKeyFromPath(boxPath);
        } catch (Exception e) {
            logger.warn("Failure to translate path to s3 path for path {}", boxPath, e);
            s3Path = "";
        }

        return new IndexedFileMetadata(boxPath, info.getID(), s3Path, lastModTime, info.getSize(), sha1);
    }

}
