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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.geronimo.datastore.GFile;

/**
 * GFileDelegate implementation.
 * 
 * @version $Revision: 1.2 $ $Date: 2004/05/11 12:24:59 $
 */
class GFileDelegateImpl
    implements GFileDelegate
{

    /**
     * Lock manager to be used to lock GFile.
     */
    private final LockManager lockManager;
    
    /**
     * DAO to query the data store.
     */
    private final GFileDAO gFileDAO;
    
    /**
     * GFile owning this delegate. 
     */
    private GFile gFile;
    
    /**
     * Stores the state of the GFile when the owning GFile has been bound to
     * this delegate. Instead of querying each time the data-store, one 
     * uses the state stored by this TO.
     */
    private GFileTO fileTO;
    
    public GFileDelegateImpl(GFileDAO aFileDAO, LockManager aLockManager) {
        if ( null == aFileDAO ) {
            throw new IllegalArgumentException("DAO is required");
        } else if ( null == aLockManager ) {
            throw new IllegalArgumentException("LockManager is required.");
        }
        gFileDAO = aFileDAO;
        lockManager = aLockManager;
    }
    
    void setGFile(GFile aFile) {
        gFile = aFile;
        load();
    }
    
    public boolean exists() {
        return fileTO.exists();
    }

    public boolean isDirectory() {
        return fileTO.isDirectory();
    }

    public boolean isFile() {
        return fileTO.isFile();
    }

    public String[] listFiles() {
        throw new RuntimeException("Not implemented");
    }

    public void lock() {
        lockManager.registerLock(gFile);
    }

    public void unlock() {
        lockManager.unregisterLock(gFile);
    }

    public InputStream getInputStream() throws IOException {
        return gFileDAO.getInputStream(gFile.getPath());
    }

    public Map getProperties() {
        return Collections.unmodifiableMap(fileTO.getProperties());
    }
    
    public Map getPropertiesByName(Collection aCollOfNames) {
        Map properties = fileTO.getProperties(); 
        Map tmpProperties;
        synchronized(properties) {
            tmpProperties = new HashMap(properties);
        }
        Map foundProps = new HashMap();
        for (Iterator iter = aCollOfNames.iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            Object value = tmpProperties.get(name);
            if ( null != value ) {
                foundProps.put(name, value);
            }
        }
        return foundProps;
    }

    public void addProperty(String aName, String aValue) {
        Map properties = fileTO.getProperties(); 
        synchronized (properties) {
            properties.put(aName, aValue);
        }
    }

    public void removeProperty(String aName) {
        Map properties = fileTO.getProperties(); 
        synchronized (properties) {
            properties.remove(aName);
        }
    }

    /**
     * Loads the state (the properties) of the GFile owning this delegate.
     */
    private void load() {
        try {
            fileTO = gFileDAO.read(gFile.getPath());
        } catch (DAOException e) {
            throw new RuntimeException("Can not load GFile.", e);
        }
    }

}
