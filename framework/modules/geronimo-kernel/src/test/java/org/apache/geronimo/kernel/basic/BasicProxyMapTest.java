/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.kernel.basic;

import junit.framework.TestCase;

public class BasicProxyMapTest extends TestCase {
    private BasicProxyMap map;
    
    public void testPut() {
        Object key1 = new Object();
        Object value1 = new Object();
        Object key2 = new Object();
        Object value2 = new Object();

        // test expected start conditions
        assertEquals(0,map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get(key1));
        assertFalse(map.containsKey(key1));
        assertFalse(map.containsValue(value1));
        
        // test put
        assertNull(map.put(key1, value1));
        assertEquals(1,map.size());
        assertFalse(map.isEmpty());
        assertSame(value1, map.put(key1,value2));
        assertSame(value2, map.put(key1,value1));
        assertNull(map.put(key2, value2));
        assertEquals(2,map.size());
    }
    
    public void testContains() {
        Object key1 = new Object();
        Object value1 = new Object();
        Object key2 = new Object();
        Object value2 = new Object();

        map.put(key1,value1);
        map.put(key2, value2);
        
        assertTrue(map.containsKey(key1));
        assertTrue(map.containsValue(value1));
        assertTrue(map.containsKey(key2));
        assertTrue(map.containsValue(value2));
        assertFalse(map.containsKey(new Object()));
        assertFalse(map.containsValue(new Object()));
    }
    
    public void testGet() {
        Object key1 = new Object();
        Object value1 = new Object();
        Object key2 = new Object();
        Object value2 = new Object();

        map.put(key1,value1);
        map.put(key2, value2);
        
        assertSame(value1,map.get(key1));
        assertSame(value2,map.get(key2));
        assertNull(map.get(new Object()));
    }
    
    public void testRemove() {
        Object key1 = new Object();
        Object value1 = new Object();
        Object key2 = new Object();
        Object value2 = new Object();

        map.put(key1,value1);
        map.put(key2, value2);

        assertSame(value1,map.remove(key1));
        assertEquals(1,map.size());
        assertFalse(map.isEmpty());
        assertFalse(map.containsKey(key1));
        assertFalse(map.containsValue(value1));
        assertNull(map.get(key1));
        assertNull(map.put(key1, value1));
    }
    
    public void testClear() {
        Object key1 = new Object();
        Object value1 = new Object();
        Object key2 = new Object();
        Object value2 = new Object();

        map.put(key1,value1);
        map.put(key2, value2);
        map.clear();
        
        assertEquals(0,map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get(key1));
        assertFalse(map.containsKey(key1));
        assertFalse(map.containsValue(value1));
    }
    
    public void testIdentitySemantic() {
        Object value1 = new Object();
        Object value2 = new Object();

        class IdentityTest1 {
            public int hashCode() { return 1; }
            public boolean equals(Object o) { return true; }
        }
        class IdentityTest2 {
            public int hashCode() { return 1; }
            public boolean equals(Object o) { return false; }
        }
        
        Object key1 = new IdentityTest1();
        Object key2 = new IdentityTest1();
        
        assertNull(map.put(key1, value1));
        assertNull(map.get(key2));
        assertNull(map.put(key2, value2));
        assertSame(value1, map.get(key1));
        assertSame(value2, map.get(key2));
        map.clear();

        key1 = new IdentityTest2();
        key2 = new IdentityTest2();
        
        assertNull(map.put(key1, value1));
        assertNull(map.get(key2));
        assertNull(map.put(key2, value2));
        assertSame(value1, map.get(key1));
        assertSame(value2, map.get(key2));
    }
    
    public void testWeakSemantic() {
        int MAX = 256;
        Object [] keys = new Object[MAX];
        Object [] values = new Object[MAX];
        for (int i=0;i<MAX;i++) {
            keys[i] = new Object(); values[i] = new Object();
            assertNull(map.put(keys[i],values[i]));
        }
        for (int i=0;i<MAX;i++) {
            assertEquals(values[i], map.get(keys[i]));
        }
        
        // now add a number of entries that will be available for GCing
        for (int i=0;i<MAX;i++) {
            assertNull(map.put(new Object(), new Object()));
        }
        System.gc();
        try {Thread.sleep(1000);} catch (InterruptedException ie) {}
        assertEquals(MAX,map.size());
        for (int i=0;i<MAX;i++) {
            assertSame(values[i], map.get(keys[i]));
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        map = new BasicProxyMap();
    }    
}
