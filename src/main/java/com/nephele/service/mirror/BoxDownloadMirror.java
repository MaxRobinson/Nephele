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

import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.ProgressListener;
import com.nephele.service.box.BoxClient;
import com.nephele.service.box.BoxClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@ApplicationScoped
public class BoxDownloadMirror implements DownloadMirror {
    private static final Logger logger = LoggerFactory.getLogger(BoxDownloadMirror.class);

    private final BoxDeveloperEditionAPIConnection api;
    private final BoxClient client;

    public BoxDownloadMirror() {
        api = new BoxClientFactory().createClient();
        client = new BoxClient(api);
    }

    @Override
    public void getByID(String id, OutputStream outputStream) {
        BoxFile file = new BoxFile(api, id);
        file.download(outputStream);
//        try {
//            outputStream.close();
//        } catch (IOException e) {
//            logger.error("Error closing bytes stream.", e);
//        }
    }

    @Override
    public byte[] getByID(String id) {
        BoxFile file = new BoxFile(api, id);
        BoxFile.Info infoSize = file.getInfo("size");

        int who = (int)infoSize.getSize();
        ByteArrayOutputStream output = new ByteArrayOutputStream(who);

        file.download(output, new ProgressListener() {
            @Override
            public void onProgressChanged(long numBytes, long totalBytes) {
                logger.debug("Download progress. New bytes {}, total bytes {}", numBytes, totalBytes);
            }
        });

        byte[] res = output.toByteArray();

        try {
            output.close();
        } catch (IOException e) {
            logger.error("Error closing bytes stream.", e);
        }
        return res;
    }

    @Override
    public byte[] getByPath(String path) {
        return getByID(client.getID(path));
    }

    @Override
    public boolean matchesMetadata(String path, TargetMirrorMetadata metadata) {
        // To do: Actually implement this will real logic
        return true;
    }
}
