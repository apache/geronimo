/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.cache;

import junit.framework.TestCase;

public class SimpleInstanceCacheTestCase extends TestCase {
    protected InstanceCache cache;
    Object key;
    Object value;

    public SimpleInstanceCacheTestCase(String name) {
        super(name);
    }

    public void setUp() {
        key = new Object();
        value = new Object();
        cache = new SimpleInstanceCache();
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

    public void tearDown() {
        cache = null;
        key = null;
        value = null;
    }

}