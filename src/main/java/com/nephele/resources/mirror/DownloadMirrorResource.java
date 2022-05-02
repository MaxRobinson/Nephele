package com.nephele.resources.mirror;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nephele.config.S3SourceEnum;
import com.nephele.service.indexcache.IndexedFileMetadata;
import com.nephele.service.indexcache.RedisIndexCache;
import com.nephele.service.mirror.BoxDownloadMirror;
import com.nephele.service.mirror.MirrorMain;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/mirror")
public class DownloadMirrorResource {
    private static final Logger logger = LoggerFactory.getLogger(DownloadMirrorResource.class);

    @ConfigProperty(name = "s3mirror.mode.mirror")
    Boolean mirrorMode;

    @ConfigProperty(name = "s3mirror.source")
    S3SourceEnum source;

    @ConfigProperty(name = "s3mirror.target")
    S3SourceEnum target;

    @Inject
    BoxDownloadMirror downloadMirror;

    @Inject
    MirrorMain main;

    @Inject
    RedisIndexCache redisIndexCache;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String status() throws Exception {
        if(mirrorMode){
            main.mirror();
            return "Success";
        } else {
            return "Not mirroring. Service is not in mirror mode";
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] downloadFileByID(@QueryParam("id") String id) throws JsonProcessingException {
        byte[] res = downloadMirror.getByID(id);
        return res;
    }

    @POST
    @Path("/toMirror")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public List<IndexedFileMetadata> getFilesToMirror(String path) throws Exception {
        return main.getItemsToMirror(path);
    }


    @GET
    @Path("/mirrorItem")
//    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String mirrorItem(@QueryParam("rootPath") String p, @QueryParam("spec") String specificFile) {
        IndexedFileMetadata i = redisIndexCache.get(p, specificFile);
        main.mirrorSingleItem(i);
        return "Successful";
    }

}
