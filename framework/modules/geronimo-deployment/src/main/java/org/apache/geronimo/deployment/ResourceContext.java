/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public interface ResourceContext {
    void addIncludeAsPackedJar(URI targetPath, JarFile jarFile) throws IOException;

    void addInclude(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException;

    void addInclude(URI targetPath, URL source) throws IOException;

    void addInclude(URI targetPath, File source) throws IOException;

    void addFile(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException;

    void addFile(URI targetPath, URL source) throws IOException;

    void addFile(URI targetPath, File source) throws IOException;

    void addFile(URI targetPath, String source) throws IOException;

    void addFile(URI targetPath, byte[] contents) throws IOException;
    
    File getTargetFile(URI targetPath);
    
    URL getTargetURL(URI targetPath);
    
    void flush() throws IOException;
}