package com.nephele.service.s3;

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

import org.eclipse.microprofile.config.ConfigProvider;

import java.nio.file.Path;
import java.nio.file.Paths;

public class S3Path {

    public final static String ROOT_PATTERN = ConfigProvider.getConfig().getValue("s3mirror.root_pattern", String.class);

    private String bucket;
    private String objectKey;


    public S3Path() {
    }

    public S3Path(String bucket, String objectKey) {
        setBucket(bucket);
        this.objectKey = objectKey;
    }

    public static S3Path fromPath(String path) {
        S3Path s3Path = new S3Path();
        if(ROOT_PATTERN.equals(path.substring(0, ROOT_PATTERN.length()))){

            String pathSubString = path.substring(ROOT_PATTERN.length());
            Path mainPath = Paths.get(pathSubString);

            if(mainPath.getNameCount() <= 1){
                // Path is not long enough for a bucket and a value
                throw new RuntimeException("Path is not long enough to specify bucket and file path");
            }

            s3Path.setBucket(mainPath.subpath(0, 1).toString());
            s3Path.setObjectKey(mainPath.subpath(1, mainPath.getNameCount()).toString());
        }
        return s3Path;
    }

    public static String objectKeyFromPath(String pathToTranslate) throws Exception {
        if(ROOT_PATTERN.equals(pathToTranslate.substring(0, ROOT_PATTERN.length()))){

            String pathSubString = pathToTranslate.substring(ROOT_PATTERN.length());
            Path mainPath = Paths.get(pathSubString);
            return mainPath.subpath(1, mainPath.getNameCount()).toString();
        }
        throw new Exception("Path Cannot be translated to S3");
    }

    //<editor-fold desc="Getter Setters">
    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket.toLowerCase().replace(" ", "-");
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }
    //</editor-fold>

    public String asPath() {
        Path result = Paths.get("");
        if(bucket != null){
            result = result.resolve(bucket);
        }
        if(objectKey != null) {
            result = result.resolve(objectKey);
        }
        return result.toString();
    }

}
