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

import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.IAccessTokenCache;
import com.box.sdk.InMemoryLRUAccessTokenCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class BoxClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(BoxClientFactory.class);
    private static final ObjectMapper om = new ObjectMapper();

    private static String clientId;
    private static String clientSecret;
    private static String enterpriseId;
    private static String publicKeyId;
    private static String privateKey;
    private static String passphrase;

    /**
     * Get a client from the client factory.
     */
    public BoxDeveloperEditionAPIConnection createClient() {
        if(!initialized()){
            try {
                init();
            } catch (IOException e) {
                logger.error("Cannot initialize box client from config file.", e);
                throw new RuntimeException(e);
            }
        }
        return authenticate();
    }


    /**
     * Simple check if we have loaded our config already
     */
    private boolean initialized(){
        return clientId != null && !"".equals(clientId);
    }

    public void init() throws IOException {
        URL res = Thread.currentThread().getContextClassLoader().getResource("box_jwt_config.json");
        if (res == null){
            throw new RuntimeException("Cannot load box jwt auth file");
        }

        try {
            JsonNode config = om.readTree(res);
            JsonNode settings = config.get("boxAppSettings");

            clientId = settings.get("clientID").asText();
            clientSecret = settings.get("clientSecret").asText();
            JsonNode appAuth = settings.get("appAuth");
            publicKeyId = appAuth.get("publicKeyID").asText();
            privateKey = appAuth.get("privateKey").asText();
            passphrase = appAuth.get("passphrase").asText();
            enterpriseId = config.get("enterpriseID").asText();
        } catch (NullPointerException e) {
            logger.error("Problem parsing Box JWT Auth file");
            throw new RuntimeException(e);
        }
    }

    private BoxDeveloperEditionAPIConnection authenticate() {
        //It is a best practice to use an access token cache to prevent unneeded requests to Box for access tokens.
        //For production applications it is recommended to use a distributed cache like Memcached or Redis, and to
        //implement IAccessTokenCache to store and retrieve access tokens appropriately for your environment.
        IAccessTokenCache accessTokenCache = new InMemoryLRUAccessTokenCache(10);

        BoxConfig boxConfig = new BoxConfig(clientId, clientSecret, enterpriseId, publicKeyId, privateKey, passphrase);
        return BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);
    }
}
