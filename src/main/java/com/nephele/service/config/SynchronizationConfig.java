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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SynchronizationConfig {
    @JsonProperty("root_dir")
    public String rootDirectory;
    @JsonProperty("dev_instance")
    public boolean devInstance;

    @JsonProperty("upload")
    public List<UploadPathConfig> synchronizationPaths;

    //<editor-fold desc="Constructor">

    public SynchronizationConfig() {
        this.rootDirectory = null;
        this.devInstance = true;
        this.synchronizationPaths = List.of();
    }

    public SynchronizationConfig(String rootDirectory, boolean devInstance, List<UploadPathConfig> synchronizationPaths) {
        this.rootDirectory = rootDirectory;
        this.devInstance = devInstance;
        this.synchronizationPaths = synchronizationPaths;
    }

    //</editor-fold>

    //<editor-fold desc="Accessors">
    public String getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public boolean getDevInstance() {
        return devInstance;
    }

    public void setDevInstance(boolean devInstance) {
        this.devInstance = devInstance;
    }

    public List<UploadPathConfig> getSynchronizationPaths() {
        return synchronizationPaths;
    }

    public void setSynchronizationPaths(List<UploadPathConfig> synchronizationPaths) {
        this.synchronizationPaths = synchronizationPaths;
    }
    //</editor-fold>
}
