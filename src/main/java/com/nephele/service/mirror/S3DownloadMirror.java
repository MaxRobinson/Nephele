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
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.OutputStream;

public class S3DownloadMirror implements DownloadMirror {
    private static final Logger logger = LoggerFactory.getLogger(S3DownloadMirror.class);

    private final S3Client client;

    public S3DownloadMirror(S3Client client){
        this.client = client;
    }


    @Override
    public byte[] getByID(String id) {
        S3Path s3Path = S3Path.fromPath(id);


        GetObjectRequest who = GetObjectRequest.builder()
                .bucket(s3Path.getBucket())
                .key(s3Path.getObjectKey())
                .build();

        ResponseBytes<GetObjectResponse> fullObject = client.getObjectAsBytes(who);
        return fullObject.asByteArray();
    }

    @Override
    public void getByID(String id, OutputStream outputStream) {
        S3Path s3Path = S3Path.fromPath(id);

        GetObjectRequest who = GetObjectRequest.builder()
                .bucket(s3Path.getBucket())
                .key(s3Path.getObjectKey())
                .build();
        GetObjectResponse fullObject = client.getObject(who,
                ResponseTransformer.toOutputStream(outputStream));
    }

    @Override
    public byte[] getByPath(String path) {
        return new byte[0];
    }

    @Override
    public boolean matchesMetadata(String path, TargetMirrorMetadata expectedMetadata) {
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
            HeadObjectResponse resp = client.headObject(r);
            ListObjectsV2Response result = client.listObjectsV2(listRequest);

            S3Object res = result.contents().get(0);
            ComparisonFileObject resComparisonObject = ComparisonFileObject.from(s3Path.getBucket(), res, resp.metadata());

            return expectedMetadata.equals(resComparisonObject.getTargetMirrorMetadata());
        } catch(NoSuchKeyException ex) {
            logger.warn("Attempting to match metadata on a file that can't be found {}", path);
        }
        return false;
    }
}
