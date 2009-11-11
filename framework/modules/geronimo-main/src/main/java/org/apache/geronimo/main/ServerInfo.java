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

package org.apache.geronimo.main;

import java.io.File;
import java.net.URI;

public class ServerInfo {
    
    private File base;
    private URI baseURI;
    private File baseServer;
    private URI baseServerURI;
    
    public ServerInfo(File geronimoHome, File geronimoBase) {
        this.base = geronimoHome;
        this.baseURI = base.toURI();
        this.baseServer = geronimoBase;
        this.baseServerURI = baseServer.toURI();
    }
    
    public String resolvePath(String filename) {
        return resolve(filename).getAbsolutePath();
    }

    public String resolveServerPath(String filename) {
        return resolveServer(filename).getAbsolutePath();
    }
    
    public File resolve(String filename) {
        return resolveWithBase(base, filename);
    }

    public File resolveServer(String filename) {
        return resolveWithBase(baseServer, filename);
    }

    public URI resolve(URI uri) {
        return baseURI.resolve(uri);
    }

    public URI resolveServer(URI uri) {
        return baseServerURI.resolve(uri);
    }
    
    private File resolveWithBase(File baseDir, String filename) {
        File file = new File(filename);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(baseDir, filename);
    }

    public File getBase() {
        return base;
    }
    
    public File getBaseServer() {
        return baseServer;
    }
    
}
