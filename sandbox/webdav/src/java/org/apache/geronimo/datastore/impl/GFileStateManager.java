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

import org.apache.geronimo.datastore.GFile;

/**
 * StateManager for a GFile.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:16 $
 */
class GFileStateManager
    implements StateManager
{
    
    /**
     * GFile associated to this state.
     */
    private GFile file;
    
    /**
     * DAO to access the data store.
     */
    private final GFileDAO fileDAO;
    
    /**
     * Dirty marker delegate.
     */
    private final DirtyMarker dirtyMarker;

    private GFileDelegateImpl delegate;
    
    /**
     * Creates a StateManager for the provided GFile. Interactions with the
     * data store will be performed by the provided DAO.
     * 
     * @param aFile GFile.
     * @param aFileDAO DAO to be used to query/update the data store.
     * @param aDirtyMarker Initial life cycle state of the provided file.
     */
    public GFileStateManager(GFileDAO aFileDAO, DirtyMarker aDirtyMarker) {
        if ( null == aFileDAO ) {
            throw new IllegalArgumentException("DAO is required.");
        } else if ( null == aDirtyMarker ) {
            throw new IllegalArgumentException("DirtyMarker is required.");
        }
        fileDAO = aFileDAO;
        dirtyMarker = aDirtyMarker;
    }
    
    void setGFileDelegate(GFileDelegate aDelegate) {
        delegate = (GFileDelegateImpl) aDelegate;
    }
    
    GFileDelegate getGFileDelegate() {
        return delegate;
    }
    
    void setGFile(GFile aFile) {
        file = aFile;
        delegate.setGFile(aFile);
    }
    
    public void prepare() throws IOException {
        GFileTO fileTO = new GFileTO(file);
        // Depending on the state of the GFile, one creates, updates or delete
        // the GFile from the data store.
        if ( isNew() ) {
            fileDAO.create(fileTO);
        } else if ( isDirty() ) {
            fileDAO.update(fileTO);
        } else if ( isDelete() ) {
            fileDAO.delete(fileTO.getPath());
        }
    }

    public void flush() {
        fileDAO.flush();
        // The memory-state is now in sync with the data store. One resets the
        // state.
        dirtyMarker.setIsDelete(false);
        dirtyMarker.setIsDirty(false);
        dirtyMarker.setIsNew(false);
    }
    
    public void unflush() {
        fileDAO.unflush();
    }

    public boolean isDirty() {
        return dirtyMarker.isDirty();
    }

    public void setIsDirty(boolean aDirtyFlag) {
        dirtyMarker.setIsDirty(aDirtyFlag);
    }

    public boolean isNew() {
        return dirtyMarker.isNew();
    }

    public void setIsNew(boolean aNewFlag) {
        dirtyMarker.setIsNew(aNewFlag);
    }

    public boolean isDelete() {
        return dirtyMarker.isDelete();
    }

    public void setIsDelete(boolean aDeleteFlag) {
        dirtyMarker.setIsDelete(aDeleteFlag);
    }

}
