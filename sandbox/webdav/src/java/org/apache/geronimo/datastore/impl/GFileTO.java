/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.datastore.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.geronimo.datastore.GFile;

/**
 * Transfer object for a GFile. It is used between a GFileStateManager and
 * a DAO to transfer the state of a GFile.
 *
 * @version $Rev$ $Date$
 */
public class GFileTO
{

    /**
     * Path.
     */
    private final String path;
    
    /**
     * Properties.
     */
    private final Map properties;
    
    /**
     * Content.
     */
    private InputStream inputStream;
    
    /**
     * Is a directory?
     */
    private boolean isDirectory;
    
    /**
     * Is a file?
     */
    private boolean isFile;
    
    /**
     * Does exists?
     */
    private boolean exists;
    
    /**
     * Creates a TO defining a GFile.
     * 
     * @param aPath path of the GFile.
     * @param aMapOfProp Properties.
     * @param anIn Content of the GFile.
     */
    public GFileTO(String aPath, Map aMapOfProp, InputStream anIn) {
        path = aPath;
        properties = aMapOfProp;
        inputStream = anIn;
    }
    
    /**
     * Creates a TO from a GFile.
     * 
     * @param aFile GFile whose state should be extracted.
     * @throws IOException Indicates that the properties of the GFile can not
     * be retrieved.
     */
    public GFileTO(GFile aFile) throws IOException {
        this(aFile.getPath(), aFile.getProperties(), aFile.getContent());
    }
    
    public String getPath() {
        return path;
    }
    
    public Map getProperties() {
        return properties;
    }
    
    public InputStream getStream() {
        return inputStream;
    }

    public boolean isDirectory() {
        return isDirectory;
    }
    
    public boolean isFile() {
        return isFile;
    }
    
    public boolean exists() {
        return exists;
    }
    
    public void setExists(boolean aIsExists) {
        exists = aIsExists;
    }

    public void setDirectory(boolean aIsDirectory) {
        isDirectory = aIsDirectory;
    }

    public void setFile(boolean aIsFile) {
        isFile = aIsFile;
    }

}
