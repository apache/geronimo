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

package org.apache.geronimo.cache;

import junit.framework.TestCase;

/**
 * Base class for InstaceCache implementation tests.
 * Descendants should override the {@link AbstractInstanceCacheTest#setUp()} method,
 * create instance of specific InstaceCache implementation and store it to protected <code>cache</code> variable.
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:30 $
 */
public abstract class AbstractInstanceCacheTest extends TestCase {
    protected InstanceCache cache;
    Object key;
    Object value;

    public AbstractInstanceCacheTest(String string) {
        super(string);
    }

    public void setUp() {
        key = new Object();
        value = new Object();
    }

    public void tearDown() {
        key = null;
        value = null;
    }

    /**
     * Tests that an object
     * 1) can be put to the cache as active
     * 2) is considered active
     * 3) cant be retreived from it
     * @throws Exception if an exception happens while testing
     */
    public void testPutGetActive() throws Exception {
        cache.putActive(key, value);
        assertTrue("Object is in fact active", cache.isActive(key));
        Object value1 = cache.get(key);
        assertNotNull("Object returned is not null", value1);
        assertEquals("Object returned is the same as the object inserted", value, value1);
    }

    /**
     * Tests that an object
     * 1) can be put to the cache as inactive
     * 2) is not considered active
     * 3) can be retreived from it
     * 4) becomes active after retreival
     * @throws Exception if an exception happens while testing
     */
    public void testPutGetInactive() throws Exception {
        cache.putInactive(key, value);
        assertFalse("Object is in fact NOT active", cache.isActive(key));
        Object value1 = cache.get(key);
        assertNotNull("Object returned is not null", value1);
        assertEquals("Object returned is the same as the object inserted", value, value1);
        assertTrue("Object has become active", cache.isActive(key));
    }

    /**
     * Tests that an object
     * 1) can be put to the cache as active
     * 2) is considered active
     * 3) can be put again as inactive
     * 4) becomes inactive
     * @throws Exception if an exception happens while testing
     */
    public void testPutInactiveAfterActive() throws Exception {
        cache.putActive(key, value);
        assertTrue("Object is in fact active", cache.isActive(key));
        cache.putInactive(key, value);
        assertFalse("Object has becode inactive", cache.isActive(key));
    }

    /**
     * Tests that an object
     * 1) can be put to the cache as inactive
     * 2) is not considered active
     * 3) can be put again as active
     * 4) becomes active
     * @throws Exception if an exception happens while testing
     */
    public void testPutActiveAfterInactive() throws Exception {
        cache.putInactive(key, value);
        assertFalse("Object is in fact NOT active", cache.isActive(key));
        cache.putActive(key, value);
        assertTrue("Object has becode active", cache.isActive(key));
    }

    /**
     * Tests that an object
     * 1) can be put as active
     * 2) the same object is returned while removing
     * 3) actually removed
     * @throws Exception
     */
    public void testRemoveActive() throws Exception {
        cache.putActive(key,value);
        assertTrue("Object is in fact active", cache.isActive(key));
        Object value1 = cache.remove(key);
        assertNotNull("Object is found",value1);
        assertEquals("Object is the same as object inserted",value,value1);
        Object value2 = cache.get(key);
        assertNull("Object is in fact removed",value2);
    }

    /**
     * Tests that an object
     * 1) can be put as inactive
     * 2) the same object is returned while removing
     * 3) actually removed
     * @throws Exception
     */
    public void testRemoveInactive() throws Exception {
        cache.putInactive(key,value);
        assertFalse("Object is in fact NOT active", cache.isActive(key));
        Object value1 = cache.remove(key);
        assertNotNull("Object is found",value1);
        assertEquals("Object is the same as object inserted",value,value1);
        Object value2 = cache.get(key);
        assertNull("Object is in fact removed",value2);
    }

    /**
     * Tests that an object
     * 1) can be put as active
     * 2) can be peeked
     * 3) stays active
     * @throws Exception
     */
    public void testPeekActive() throws Exception {
        cache.putActive(key, value);
        assertTrue("Object is in fact active", cache.isActive(key));
        Object value1 = cache.get(key);
        assertNotNull("Object returned is not null", value1);
        assertEquals("Object returned is the same as the object inserted", value, value1);
        assertTrue("Object has NOT become inactive after peek", cache.isActive(key));
    }

    /**
     * Tests that an object
     * 1) can be put as inactive
     * 2) can be peeked
     * 3) stays inactive
     * @throws Exception
     */
    public void testPeekInactive() throws Exception {
        cache.putInactive(key, value);
        assertFalse("Object is in fact NOT active", cache.isActive(key));
        Object value1 = cache.peek(key);
        assertNotNull("Object returned is not null", value1);
        assertEquals("Object returned is the same as the object inserted", value, value1);
        assertFalse("Object has NOT become active after peek", cache.isActive(key));
    }
}
