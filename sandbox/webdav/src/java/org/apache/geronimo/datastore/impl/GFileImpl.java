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
import java.util.Collection;
import java.util.Map;

import org.apache.geronimo.datastore.GFile;
import org.apache.geronimo.messaging.reference.Referenceable;

/**
 * GFile implementation.
 *
 * @version $Rev$ $Date$
 */
public class GFileImpl
    implements GFile, Referenceable
{
    
    /**
     * File path.
     */
    private String path;
    
    /**
     * New content of the file.
     */
    private InputStream content;
    
    /**
     * State manager of this GFile.
     */
    private GFileStateManager stateManager;
    
    /**
     * GFileDelegate related to the state manager of this GFile.
     */
    private GFileDelegate delegate;
    
    /**
     * Creates a GFile having the specified path.
     * 
     * @param aPath Path of this file.
     * @param aManager StateManager tracking the state of this instance.
     */
    public GFileImpl(String aPath, GFileStateManager aManager) {
        if ( null == aPath ) {
            throw new IllegalArgumentException("Path is required.");
        } else if ( null == aManager ) {
            throw new IllegalArgumentException("StateManager is required.");
        }
        path = aPath;
        stateManager = aManager;
        stateManager.setGFile(this);
        delegate = stateManager.getGFileDelegate();
        content = UNSET;
    }

    GFileStateManager getStateManager() {
        return stateManager;
    }
    
    public String getPath() {
        return path;
    }

    public boolean exists() throws IOException {
        return delegate.exists();
    }

    public boolean isDirectory() throws IOException {
        return delegate.isDirectory();
    }

    public boolean isFile() throws IOException {
        return delegate.isFile();
    }

    public String[] listFiles() throws IOException {
        return delegate.listFiles();
    }

    public void lock() throws IOException {
        delegate.lock();
    }

    public void unlock() throws IOException {
        delegate.unlock();
    }

    public Map getProperties() throws IOException {
        return delegate.getProperties();
    }

    public void setContent(InputStream anIn) {
        content = anIn;
    }

    public InputStream getContent() {
        return content;
    }

    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    public Map getPropertiesByName(Collection aCollOfNames) throws IOException {
        return delegate.getPropertiesByName(aCollOfNames);
    }

    public void addProperty(String aName, String aValue) throws IOException {
        delegate.addProperty(aName, aValue);
    }
    
    public void removeProperty(String aName) throws IOException {
        delegate.removeProperty(aName);
    }
    
}
