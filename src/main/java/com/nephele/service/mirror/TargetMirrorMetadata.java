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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Used for storing metadata into target mirror locations.
 * Will enable comparision of mirrored files to source files using source file metadata.
 * This will help with any time based or checksum based difference calculations.
 */
public class TargetMirrorMetadata {
    @JsonIgnore
    private static final ObjectMapper om = new ObjectMapper();
    static {
        om.registerModule(new JavaTimeModule());
        om.registerModule(new BlackbirdModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // UTC
    @JsonProperty("sourcelastmodtime")
    private Instant sourceLastModTime;
    @JsonProperty("sourcesha1")
    private String sourceSha1;

    public TargetMirrorMetadata() {

    }

    public TargetMirrorMetadata(Instant sourceLastModTime, String sourceSha1) {
        this.sourceLastModTime = sourceLastModTime;
        this.sourceSha1 = sourceSha1;
    }

    public Map<String, String> toMap(){
        Map<String, String> res = new HashMap<>();
        if(sourceLastModTime != null){
            res.put("sourcelastmodtime", sourceLastModTime.toString());
        } else {
            res.put("sourcelastmodtime", null);
        }
        res.put("sourcesha1", sourceSha1);

        return res;
    }

    public static TargetMirrorMetadata fromMap(Map<String, String> metadata){
        return om.convertValue(metadata, TargetMirrorMetadata.class);
    }

    public boolean hasLastModTime(){
        return sourceLastModTime != null;
    }

    public boolean hasSha1(){
        return sourceSha1 != null;
    }

    //<editor-fold desc="Getter and Setters">
    public Instant getSourceLastModTime() {
        return sourceLastModTime;
    }

    public void setSourceLastModTime(Instant sourceLastModTime) {
        this.sourceLastModTime = sourceLastModTime;
    }

    public String getSourceSha1() {
        return sourceSha1;
    }

    public void setSourceSha1(String sourceSha1) {
        this.sourceSha1 = sourceSha1;
    }
    //</editor-fold>

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetMirrorMetadata that = (TargetMirrorMetadata) o;
        return Objects.equals(sourceLastModTime, that.sourceLastModTime) && Objects.equals(sourceSha1, that.sourceSha1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceLastModTime, sourceSha1);
    }
}
