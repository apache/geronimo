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

/**
 * DirtyMarker implementation.
 *
 * @version $Rev$ $Date$
 */
public class DirtyMarkerImpl
    implements DirtyMarker
{

    private boolean isDirty;
    private boolean isNew;
    private boolean isDelete;
    
    public boolean isDirty() {
        return isDirty;
    }

    public void setIsDirty(boolean aDirtyFlag) {
        if ( (isNew || isDelete) && aDirtyFlag ) {
            throw new IllegalArgumentException("Wrong state");
        }
        isDirty = aDirtyFlag;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean aNewFlag) {
        if ( (isDirty || isDelete) && aNewFlag ) {
            throw new IllegalArgumentException("Wrong state");
        }
        isNew = aNewFlag;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setIsDelete(boolean aDeleteFlag) {
        if ( (isDirty || isNew) && aDeleteFlag ) {
            throw new IllegalArgumentException("Wrong state");
        }
        isDelete = aDeleteFlag;
    }

}
