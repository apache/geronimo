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

package org.apache.geronimo.datastore.impl.remote.messaging;

/**
 * @version $Revision: 1.1 $ $Date: 2004/03/16 14:48:59 $
 */
public class IndexedMap {

    private static final Object RESERVED = new Object();
    private final Object[] map;
    private int lastAllocatedIndex;
    
    public IndexedMap(int aSize) {
        if ( aSize < 1 ) {
            throw new IllegalArgumentException("Size is not valid.");
        }
        map = new Object[aSize];
        lastAllocatedIndex = 0;  
    }
    
    public synchronized int allocateId() {
        for (int i=lastAllocatedIndex; i < map.length; i++) {
            if ( null == map[i] ) {
                lastAllocatedIndex = i;
                map[i] = RESERVED;
                return i;
            }
        }
        for (int i=0; i < lastAllocatedIndex; i++) {
            if ( null == map[i] ) {
                lastAllocatedIndex = i;
                map[i] = RESERVED;
                return i;
            }
        }
        throw new RuntimeException("Map is fulled.");
    }
    
    public Object get(int anId) {
        return map[anId];
    }
    
    public void remove(int anId) {
        map[anId] = null;
    }
    
    public void put(int anId, Object anObject) {
        map[anId] = anObject;
    }
    
}
