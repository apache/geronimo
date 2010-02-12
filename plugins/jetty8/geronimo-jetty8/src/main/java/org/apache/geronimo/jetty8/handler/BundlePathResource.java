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
package org.apache.geronimo.jetty8.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jetty.util.resource.Resource;

public class BundlePathResource extends Resource {

    private URL url;
    private String[] resources;
    
    public BundlePathResource(URL url, Enumeration<String> paths) {
        this.url = url;
        String baseName = removeSlash(url.getPath());
        List<String> list = new ArrayList<String>();
        while(paths.hasMoreElements()) {
            String path = getRelativeName(baseName, paths.nextElement());
            list.add(path);            
        }
        this.resources = list.toArray(new String [list.size()]);
    }
    
    private static String removeSlash(String name) {
        return name.startsWith("/") ? name.substring(1): name;        
    }
    
    private static String getRelativeName(String base, String name) {
        if (base != null && name.startsWith(base)) {
            return name.substring(base.length());
        } else {
            return name;
        }
    }
    
    public boolean exists() {
        return true;
    }
    
    public boolean isDirectory() {
        return true;
    }
    
    public long lastModified() {
        return -1;
    }

    public long length() {
        return -1;
    }
    
    public String[] list() {
        return resources;
    }
        
    public String getName() {
        return url.toExternalForm();
    }
    
    public void release() {
    }
       
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(new byte[] {});
    }

    public URL getURL() {
        return url;
    }

    public File getFile() throws IOException {
        return null;
    }
    
    public OutputStream getOutputStream() throws IOException, SecurityException {
        throw new IOException( "Output not supported");
    }

    public boolean delete() throws SecurityException {  
        throw new SecurityException( "Delete not supported");
    }

    public boolean renameTo(Resource dest) throws SecurityException {    
        throw new SecurityException( "RenameTo not supported");
    }
    
    public Resource addPath(String arg0) throws IOException, MalformedURLException {
        return null;
    }
    
    public boolean isContainedIn(Resource arg0) throws MalformedURLException {
        return false;
    }
    
}
