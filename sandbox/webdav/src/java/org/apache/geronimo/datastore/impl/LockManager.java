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

import org.apache.geronimo.datastore.GFile;

/**
 * Manages locks on GFile.  
 *
 * @version $Rev$ $Date$
 */
public class LockManager
{

    /**
     * Registers a lock for the provided GFile.
     * 
     * TODO pass a LockInfo
     * 
     * @param aFile GFile to be locked.
     */
    public void registerLock(GFile aFile) {
    }
    
    /**
     * Unregisters a lock for the provided GFile.
     * 
     * @param aFile GFile to be unlocked.
     */
    public void unregisterLock(GFile aFile) {
    }
    
}
