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

import com.nephele.service.config.UploadPathConfig;

import java.io.FileNotFoundException;
import java.util.List;

public interface IndexCache {

    /**
     * Store a single items indexed under a specific UploadPathConfig
     * @param p An upload path location
     * @param toIndex IndexedFileMetadata object to store in cache
     */
    void store(UploadPathConfig p, List<IndexedFileMetadata> toIndex);

    /**
     * Store a single items indexed under a specific UploadPathConfig
     * @param p An upload path location
     * @param toIndex IndexedFileMetadata object to store in cache
     */
    void store(UploadPathConfig p, IndexedFileMetadata toIndex);

    /**
     * Get all items indexed under a specific UploadPathConfig
     * @param p An upload path location
     * @return List of Indexed File Metadata objects
     */
    List<IndexedFileMetadata> get(String p);

    /**
     * Get a single cached file
     * @param uploadPathConfigPath An upload path location
     * @param filename the full path of the file under the upload path config location
     * @return The indexed File Metadata object that describes the
     */
    IndexedFileMetadata get(String uploadPathConfigPath, String filename) throws FileNotFoundException;

}
