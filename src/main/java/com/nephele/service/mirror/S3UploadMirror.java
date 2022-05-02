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

import com.nephele.service.s3.S3Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class S3UploadMirror implements UploadMirror {
    private static final Logger logger = LoggerFactory.getLogger(S3UploadMirror.class);

    @Inject
    S3Client s3;


    @Override
    public boolean upload(String path, InputStream inputStream, long contentLength) {
        S3Path s3Path = null;
        try {
            s3Path = S3Path.fromPath(path);
        } catch (Exception e) {
            return false;
        }

        PutObjectRequest putRequest = PutObjectRequest.builder()
                                        .bucket(s3Path.getBucket())
                                        .key(s3Path.getObjectKey())
                                        .build();

        s3.putObject(putRequest, RequestBody.fromInputStream(inputStream, contentLength));
        return true;
    }

    @Override
    public List<ComparisonFileObject> getComparableFiles(String path) throws Exception {
        final S3Path s3Path = S3Path.fromPath(path);

        ListObjectsV2Request listRequest = null;

        listRequest = ListObjectsV2Request.builder()
                .bucket(s3Path.getBucket())
                .prefix(s3Path.getObjectKey())
                .maxKeys(1000)
                .build();

//        HeadObjectRequest r = HeadObjectRequest.builder()
//                .bucket(s3Path.getBucket())
//                .key(s3Path.getObjectKey())
//                .build();
//
//        HeadObjectResponse resp = client.headObject(r);
//        resp.metadata();

        List<ComparisonFileObject> res = new ArrayList<>();
        ListObjectsV2Response result;
        do {

            result = s3.listObjectsV2(listRequest);
            String token = result.nextContinuationToken();

            for(S3Object obj : result.contents()){
                HeadObjectRequest r = HeadObjectRequest.builder()
                        .bucket(s3Path.getBucket())
                        .key(obj.key())
                        .build();

                HeadObjectResponse resp = s3.headObject(r);
                res.add(ComparisonFileObject.from(s3Path.getBucket(), obj, resp.metadata()));
            }

            listRequest = ListObjectsV2Request.builder()
                    .bucket(s3Path.getBucket())
                    .prefix(s3Path.getObjectKey())
                    .maxKeys(1000)
                    .continuationToken(token)
                    .build();

        } while (result.isTruncated());

        return res;
    }


    public ComparisonFileObject getComparableFile(String path) throws Exception {

        final S3Path s3Path = S3Path.fromPath(path);

        ListObjectsV2Request listRequest = null;
        listRequest = ListObjectsV2Request.builder()
                .bucket(s3Path.getBucket())
                .prefix(s3Path.getObjectKey())
                .maxKeys(1)
                .build();

        HeadObjectRequest r = HeadObjectRequest.builder()
                .bucket(s3Path.getBucket())
                .key(s3Path.getObjectKey())
                .build();

        try {
            HeadObjectResponse resp = s3.headObject(r);
            ListObjectsV2Response result = s3.listObjectsV2(listRequest);

            S3Object res = result.contents().get(0);

            return ComparisonFileObject.from(s3Path.getBucket(), res, resp.metadata());
        } catch(NoSuchKeyException ex) {
            return null;
        }
    }
}
