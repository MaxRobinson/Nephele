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


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadPathConfig {
    public String path;
    public boolean recursive;

    public int last_mod_time_delta_days;
    public boolean dated_directories;
    public boolean always_upload_root_files;
    public String box_directory_id;

    public UploadPathConfig() {
    }

    public UploadPathConfig(String path, boolean recursive, int last_mod_time_delta_days, boolean dated_directories, boolean always_upload_root_files, String box_directory_id) {
        this.path = path;
        this.recursive = recursive;
        this.last_mod_time_delta_days = last_mod_time_delta_days;
        this.dated_directories = dated_directories;
        this.always_upload_root_files = always_upload_root_files;
        this.box_directory_id = box_directory_id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public int getLast_mod_time_delta_days() {
        return last_mod_time_delta_days;
    }

    public void setLast_mod_time_delta_days(int last_mod_time_delta_days) {
        this.last_mod_time_delta_days = last_mod_time_delta_days;
    }

    public boolean isDated_directories() {
        return dated_directories;
    }

    public void setDated_directories(boolean dated_directories) {
        this.dated_directories = dated_directories;
    }

    public boolean isAlways_upload_root_files() {
        return always_upload_root_files;
    }

    public void setAlways_upload_root_files(boolean always_upload_root_files) {
        this.always_upload_root_files = always_upload_root_files;
    }

    public String getBox_directory_id() {
        return box_directory_id;
    }

    public void setBox_directory_id(String box_directory_id) {
        this.box_directory_id = box_directory_id;
    }
}
