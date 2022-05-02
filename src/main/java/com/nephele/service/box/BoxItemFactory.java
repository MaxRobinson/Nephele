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

import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

public class BoxItemFactory {

    private final BoxClientFactory clientFactory;

    public BoxItemFactory(){
        clientFactory = new BoxClientFactory();
    }

    public BoxItem createBoxItem(BoxItem.Info info, BoxDeveloperEditionAPIConnection api){
        BoxItem res;
        if(api == null){
            api = clientFactory.createClient();
        }

        switch (info.getType()){
            case "folder":
                res = new BoxFolder(api, info.getID());
                break;
            case "file":
                res = new BoxFile(api, info.getID());
                break;
            default:
                res = null;
        }

        return res;
    }

    public BoxItem createBoxItem(String boxId, BoxDeveloperEditionAPIConnection api){
        BoxItem res = null;
        if(api == null){
            api = clientFactory.createClient();
        }


        try {
            BoxFolder f = new BoxFolder(api, boxId);
            f.getInfo("type");
            res = f;
        } catch (Exception ex ) {
        }

        if( res != null) {
            return res;
        }

        try {
            BoxFile f = new BoxFile(api, boxId);
            f.getInfo("type");
            res = f;
        } catch (Exception ex) {
            res = null;
        }

        return res;
    }
}
