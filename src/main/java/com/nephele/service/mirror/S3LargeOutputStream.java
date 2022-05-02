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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class S3LargeOutputStream extends OutputStream {
    private static final Logger logger = LoggerFactory.getLogger(S3LargeOutputStream.class);

    private final S3Client client;
    private final String uploadId;
    private final String bucketName;
    private final String key;
    private final TargetMirrorMetadata targetMetadata;
    private int partNumber = 1;

    private final List<CompletedPart> partList;

    private boolean closed = false;

    private byte[] buf;
    private int count = 0;


    public S3LargeOutputStream(S3Client client, String bucketName, String key, TargetMirrorMetadata targetMetadata) {
        this.client = client;
        this.bucketName = bucketName;
        this.key = key;
        this.targetMetadata = targetMetadata;
        partList = new ArrayList<>();
        buf = new byte[5*1024*1024];
        Arrays.fill(buf, (byte)0);


        // create initial upload
        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                .metadata(this.targetMetadata.toMap())
                .bucket(bucketName)
                .key(key)
                .build();

        CreateMultipartUploadResponse response = client.createMultipartUpload(createMultipartUploadRequest);
        uploadId = response.uploadId();
        logger.debug("Created upload id for new multipart upload: {}", uploadId);
    }

    @Override
    public void write(int b) {
        if(count >= buf.length){
            writeToS3(buf);
            Arrays.fill(buf, (byte)0);
            count = 0;
        }

        buf[count] = (byte)b;
        count++;

        if(count == buf.length){
            writeToS3(buf);
            Arrays.fill(buf, (byte)0);
            count = 0;
        }
    }


    private void writeToS3(byte[] b){
        ensureOpen();
        UploadPartRequest uploadPartRequest1 = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber).build();
        String etag1 = client.uploadPart(uploadPartRequest1, RequestBody.fromBytes(b)).eTag();
        CompletedPart part = CompletedPart.builder().partNumber(partNumber).eTag(etag1).build();
        partList.add(part);
        partNumber += 1;
    }


    @Override
    public void close() throws IOException {
        // will want to make sure not to upload 0 bytes if no data exists.
        if (count == 0 && partList.size() == 0){
            // We have no data to upload, don't upload anything.
            closed = true;
            return;
        }

        // make sure we get left over data
        if (count > 0){
            byte[] leftOvers = Arrays.copyOfRange(buf, 0, count);
            writeToS3(leftOvers);
        }

        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(partList)
                .build();

        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                CompleteMultipartUploadRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .uploadId(uploadId)
                        .multipartUpload(completedMultipartUpload)
                        .build();

        client.completeMultipartUpload(completeMultipartUploadRequest);

        closed = true;
    }

    private void ensureOpen() {
        if (this.closed) {
            throw new RuntimeException("Stream writer has already been closed, cannot continue to write");
        }
    }
}



//        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
//                "your_access_key_id",
//                "your_secret_access_key");

//        S3Client s3Client = S3Client.builder()
//                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
//                .endpointOverride(URI.create(""))
//                .build();

