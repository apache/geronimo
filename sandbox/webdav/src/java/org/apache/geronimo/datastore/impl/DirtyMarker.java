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
 * Mix-in interface tracking the life-cycle of a component.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:16 $
 */
public interface DirtyMarker
{

    /**
     * Is the component dirty.
     * 
     * @return true if dirty and false otherwise.  
     */
    public boolean isDirty();
    public void setIsDirty(boolean aDirtyFlag);
    
    /**
     * Is the component new.
     * 
     * @return true if new and false otherwise.
     */
    public boolean isNew();
    public void setIsNew(boolean aNewFlag);
    
    /**
     * Is the component to be removed.
     * 
     * @return true if the component is to be removed.
     */
    public boolean isDelete();
    public void setIsDelete(boolean aDeleteFlag);
    
}
