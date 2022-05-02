package com.nephele.service.indexcache;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import com.nephele.service.config.UploadPathConfig;
import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ApplicationScoped
public class RedisIndexCache implements IndexCache {
    private static final Logger logger = LoggerFactory.getLogger(RedisIndexCache.class);
    private static final ObjectMapper om = new ObjectMapper();
    static {
        om.registerModule(new JavaTimeModule());
        om.registerModule(new BlackbirdModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    RedisClient redisClient;

    public RedisIndexCache(RedisClient redisClient) {
        this.redisClient = redisClient;
    }


    @Override
    public void store(UploadPathConfig p, List<IndexedFileMetadata> toIndex) {

    }

    @Override
    public void store(UploadPathConfig p, IndexedFileMetadata toIndex) {
        try {
            redisClient.hset(List.of(p.getPath(), toIndex.getBoxPath(), om.writeValueAsString(toIndex)));
        } catch (JsonProcessingException e) {
            logger.error("Problem caching item: {}", toIndex, e);
        }
    }

    @Override
    public List<IndexedFileMetadata> get(String p) {
        Response response = redisClient.hgetall(p);
        List<IndexedFileMetadata> result = new ArrayList<>();
        Iterator<Response> responseIterator = response.iterator();
        int i = 0;
        while(responseIterator.hasNext()){
            Response r = responseIterator.next();
            i++;
            if(i%2 != 0){
                continue;
            }

            String responseValue = r.toString();
            try {
                IndexedFileMetadata f = om.readValue(responseValue, IndexedFileMetadata.class);
                result.add(f);
            } catch (JsonProcessingException e) {
                logger.info("Failure in desceralizing Indexed File Object for {} {}", p, e);
            }
        }

        return result;
    }

    @Override
    public IndexedFileMetadata get(String uploadPathConfigPath, String filename) {
        Response response = redisClient.hget(uploadPathConfigPath, filename);
        if(response == null){
            return null;
        }

        try {
            // Should only have 2 items in it since it's an hget
            // could come back with nothing though...
            String responseValue = response.toString();
            return om.readValue(responseValue, IndexedFileMetadata.class);
        } catch (JsonProcessingException e) {
            logger.info("Failure in desceralizing Indexed File Object for {} {}", uploadPathConfigPath, filename, e);
            return null;
        }
    }
}
