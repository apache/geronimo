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

package org.apache.geronimo.messaging.replication;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 *
 * @version $Rev$ $Date$
 */
public class SimpleReplicatedMapTest extends TestCase
{
    
    private SimpleReplicatedMap master;
    private SimpleReplicatedMap slave;
    
    protected void setUp() throws Exception {
        master = new SimpleReplicatedMap();
        slave = new SimpleReplicatedMap();
        master.addUpdateListener(new EventForwarder());
    }
    
    public void testMergeWithUpdate1() throws Exception {
        Integer key = new Integer(1);
        Object value = new Object();
        master.put(key, value);
        
        assertTrue(slave.containsKey(key));
        assertEquals(value, slave.get(key));
    }

    public void testMergeWithUpdate2() throws Exception {
        Integer key = new Integer(1);
        Object value = new Object();
        master.put(key, value);
        master.clear();
        
        assertTrue(slave.isEmpty());
    }

    public void testMergeWithUpdate3() throws Exception {
        Integer key = new Integer(1);
        Object value = new Object();
        master.put(key, value);
        master.remove(key);
        
        assertTrue(slave.isEmpty());
    }

    public void testMergeWithUpdate4() throws Exception {
        Map temp = new HashMap();
        Integer key = new Integer(1);
        Object value = new Object();
        temp.put(key, value);
        key = new Integer(2);
        value = new Object();
        temp.put(key, value);

        master.putAll(temp);
        
        assertEquals(2, slave.size());
    }
    
    public class EventForwarder implements UpdateListener {

        public void fireUpdateEvent(UpdateEvent anEvent) {
            try {
                slave.mergeWithUpdate(anEvent);
            } catch (ReplicationException e) {
                e.printStackTrace();
            }
        }
        
    }
    
}
