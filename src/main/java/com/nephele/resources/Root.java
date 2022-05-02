package com.nephele.resources;

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

import com.nephele.service.config.ConfigService;
import com.nephele.service.config.SynchronizationConfig;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


@Path("/")
public class Root {

    @Inject
    ConfigService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Instant> dateTest() {
        return Map.of("Time", Instant.now());
    }

    @GET
    @Path("config")
    @Produces(MediaType.APPLICATION_JSON)
    public SynchronizationConfig hello() {
        return service.getConf();
    }

    @GET
    @Path("instance-config")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> instanceConfig() {
        Map<String, Object> res = new HashMap<>();
        Config c = ConfigProvider.getConfig();
        for(String p : ConfigProvider.getConfig().getPropertyNames()){
            c.getOptionalValue(p, String.class).ifPresent(s -> res.put(p, s));
        }
        return res;
    }

    @GET
    @Path("status")
    @Produces(MediaType.TEXT_PLAIN)
    public String status(){
        return "running";
    }

    @GET
    @Path("mem")
    @Produces(MediaType.TEXT_PLAIN)
    public String mem(){
        int mb = 1024*1024;
        Runtime runtime = Runtime.getRuntime();

        return "Max: " + runtime.maxMemory()/mb + " Total: " + runtime.totalMemory()/mb + " Free: " + runtime.freeMemory()/mb + " Used: " + (runtime.totalMemory() - runtime.freeMemory())/mb;
    }
}
