package com.nephele.resources.redis;

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

import com.nephele.service.indexcache.IndexedFileMetadata;
import com.nephele.service.indexcache.RedisIndexCache;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("cache/redis")
public class RedisCacheResource {

    @Inject
    RedisIndexCache redisCache;

    @GET
    public Response main(){
        redisCache.get("status");
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("retrive")
    @Produces(MediaType.APPLICATION_JSON)
    public List<IndexedFileMetadata> blah(@QueryParam("UploadPathConfigPath") String p, @QueryParam("filename") String fname){
        if(fname == null){
            return redisCache.get(p);
        } else {
            IndexedFileMetadata f = redisCache.get(p, fname);
            if(f == null){
                return List.of();
            }
            return List.of(f);
        }

    }
}
