package com.nephele.resources.box;

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

import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxItem;
import com.nephele.service.config.ConfigService;
import com.nephele.service.box.BoxClient;
import com.nephele.service.box.BoxClientFactory;
import io.quarkus.redis.client.RedisClient;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.stream.Collectors;

@Path("/box")
public class BoxActions {

    @Inject
    ConfigService configService;

    @Inject
    RedisClient redisClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String status() {
        BoxDeveloperEditionAPIConnection api = new BoxClientFactory().createClient();
        return "Successfully Connected to Box";
    }

    @POST
    @Path("find")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String findFile(String filePath) {
        System.out.println(filePath);
        BoxClient client = new BoxClient(new BoxClientFactory().createClient());
        BoxItem item = client.findItem(filePath);

        if(item == null){
            return null;
        }
        BoxItem.Info info = item.getInfo();
        return info.getName() + " ID:" + info.getID() + " Type:" + info.getType() + " Size:" + info.getSize() +
                " PathInfo:" +
                info.getPathCollection().stream().map(BoxItem.Info::getName).collect(Collectors.toList()).toString();
    }
}
