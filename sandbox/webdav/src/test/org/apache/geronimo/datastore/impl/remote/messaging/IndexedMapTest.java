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

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/16 14:48:59 $
 */
public class IndexedMapTest extends TestCase {

    private IndexedMap map;
    private int size;
    
    protected void setUp() throws Exception {
        size = 10;
        map = new IndexedMap(size);
    }
    
    public void testAllocate() throws Exception {
        for(int i = 0; i < size; i++) {
            map.allocateId();
        }
        try {
            map.allocateId();
            fail("Map should be full.");
        } catch (RuntimeException e) {
        }
        
    }
    
    public void testGetPut() {
        Object value = new Object();
        int id = map.allocateId();
        map.put(id, value);
        assertEquals(value, map.get(id));
    }
    
}
