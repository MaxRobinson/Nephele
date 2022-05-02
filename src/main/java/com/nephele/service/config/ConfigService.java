package com.nephele.service.config;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class ConfigService {
    private static final Logger LOG = Logger.getLogger(ConfigService.class);

    private static final String config = ".s3uploader/config.yml";
    private static final ObjectMapper om = new ObjectMapper(new YAMLFactory());
    private final SynchronizationConfig conf;

    public ConfigService() throws IOException {
        conf = this.load();
    }

    public SynchronizationConfig getConf() {
        return conf;
    }

    private SynchronizationConfig load() throws IOException {
        InputStream is = getConfigFile();
        return om.readValue(is, SynchronizationConfig.class);
    }

    private InputStream getConfigFile() {
        InputStream res = null;
        Path home = Paths.get(System.getProperty("user.home"));
        Path userConfig = home.resolve(config);

        try {
            res = new FileInputStream(userConfig.toFile());
        } catch (FileNotFoundException e) {
            LOG.warn("No deployment configuration found in user directory under" + config + ". Falling back to application property file");
        }

        if (res == null) {
            res =  Thread.currentThread().getContextClassLoader().getResourceAsStream("config.yml");
        }

        return res;
    }
}
