package com.nephele.service.box;

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

import com.box.sdk.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BoxClient {
    private static final Logger logger = LoggerFactory.getLogger(BoxClient.class);
    private static final ObjectMapper om = new ObjectMapper();
    static {
        om.registerModule(new JavaTimeModule());
        om.registerModule(new BlackbirdModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private final BoxClientFactory clientFactory;
    private final BoxItemFactory itemFactory;
    private BoxDeveloperEditionAPIConnection api;

    private final String[] infoParameters = {"id", "type", "content_created_at", "content_modified_at", "created_at",
            "etag", "modified_at", "name", "path_collection", "sha1", "size"};

    private static final ZoneId utc = ZoneId.of("UTC");

    public BoxClient(BoxDeveloperEditionAPIConnection api) {
        clientFactory = new BoxClientFactory();
        itemFactory = new BoxItemFactory();
        this.api = api;
    }

    public boolean isValidBoxId(String id) {
        if( id == null){
            return false;
        }
        try {
            BoxFolder f = new BoxFolder(getApi(), id);
            f.getInfo();
            return true;
        } catch (BoxAPIException ex) {
            return false;
        }
    }

    public String getID(String path) {
        BoxItem item = findItem(path);
        if(item == null){
            logger.error("Box Path to Index could not be found. Throwing exception. Failing init config.");
            return null;
        }

        BoxItem.Info info = item.getInfo();
        return info.getID();
    }

    public BoxItem findItem(String path){
        Path p = Paths.get(path);
        List<String> pathParts = new ArrayList<>();

        for(int i = 0; i < p.getNameCount(); i++){
            pathParts.add(p.getName(i).toString());
        }

        BoxDeveloperEditionAPIConnection api = getApi();
        BoxItem currentBoxItem = new BoxFolder(api, "0");
        String boxItemType = "folder";
        BoxItem.Info currentInfo = currentBoxItem.getInfo();

        for (String part : pathParts) {

            if ("file".equals(boxItemType)) {
                logger.warn("Found a file rather than a folder during file search and tried to continue file search;" +
                        "Returning that file was not found. ");
                return null;
            }

            List<BoxItem.Info> childrenInfo = getChildrenInFolder((BoxFolder) currentBoxItem);
            List<BoxItem.Info> d = childrenInfo.stream().filter(x -> part.equals(x.getName())).collect(Collectors.toList());
            if (d.size() <= 0) {
                // We didn't find anything at the current level that matched
                logger.info("Did not find file or folder during folder traversal at level " + currentInfo.getName() +
                        " looking for part: " + part);
                return null;
            } else {
                currentInfo = d.get(0);
                boxItemType = currentInfo.getType();
                currentBoxItem = itemFactory.createBoxItem(currentInfo, api);
            }
        }

        return currentBoxItem;
    }

    public List<BoxItem.Info> getChildrenInFolder(BoxFolder folder) {
        List<BoxItem.Info> res = new ArrayList<>();
        for(BoxItem.Info child: folder.getChildren()){
            res.add(child);
        }

        return res;
    }

    public BoxItem itemOf(String id){
        return  itemFactory.createBoxItem(id, api);
    }

    public BoxItem itemOf(BoxItem.Info info){
        return  itemFactory.createBoxItem(info, api);
    }

    public boolean boxTypeIsFile(BoxItem.Info info){
        return "file".equals(info.getType());
    }

    public BoxDeveloperEditionAPIConnection getApi() {
        if(api == null){
            long start = System.currentTimeMillis();

            api = clientFactory.createClient();
            long end = System.currentTimeMillis();
            logger.warn("Time to authenticate / create client = {}ms", (end - start));
        }
        return api;
    }

}
