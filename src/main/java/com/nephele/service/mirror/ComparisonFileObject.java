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

import software.amazon.awssdk.services.s3.model.S3Object;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;


public class ComparisonFileObject {
    private String bucket;
    private String objectKey;
    private Long size;
    private Instant lastModified;
    private TargetMirrorMetadata targetMirrorMetadata;

    public ComparisonFileObject() {
    }


    public static ComparisonFileObject from(String bucket, S3Object s3Object) {
        return from(bucket, s3Object, Map.of());
    }

    public static ComparisonFileObject from(String bucket, S3Object s3Object, Map<String, String> metadata) {
        ComparisonFileObject file = new ComparisonFileObject();
        if (s3Object != null) {
            file.setObjectKey(s3Object.key());
            file.setSize(s3Object.size());
            file.lastModified = s3Object.lastModified();
            file.bucket = bucket;
        }
        file.targetMirrorMetadata = TargetMirrorMetadata.fromMap(metadata);
        return file;
    }

    //<editor-fold desc="Getter Setter">
    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public Long getSize() {
        return size;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public ComparisonFileObject setObjectKey(String objectKey) {
        this.objectKey = objectKey;
        return this;
    }

    public ComparisonFileObject setSize(Long size) {
        this.size = size;
        return this;
    }

    public ComparisonFileObject setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public TargetMirrorMetadata getTargetMirrorMetadata() {
        return targetMirrorMetadata;
    }

    public void setTargetMirrorMetadata(TargetMirrorMetadata targetMirrorMetadata) {
        this.targetMirrorMetadata = targetMirrorMetadata;
    }
    //</editor-fold>

    public String getIdentifier(){
        return Paths.get(getBucket()).resolve(getObjectKey()).toString();
    }
}
