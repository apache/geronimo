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


/**
 * Tests the {@link LRUInstanceCache} implementation of {@link InstanceCache} interface.
 * Adds tests for {@link LRUInstanceCache#size()} and {@link LRUInstanceCache#run(LRURunner)} methods to base set provided by {@link AbstractInstanceCacheTest}
 * @see AbstractInstanceCacheTest
 * @version $Revision: 1.1 $ $Date: 2003/08/13 01:47:12 $
 */
public class LRUInstanceCacheTest extends AbstractInstanceCacheTest {

    public LRUInstanceCacheTest(String name) {
        super(name);
    }

    public void setUp() {
        cache = new LRUInstanceCache();
        super.setUp();
    }

    public void tearDown() {
        super.tearDown();
        cache = null;
    }

    /**
     * Tests that the size is calculated as a sum of number of active and inactive keys
     * @throws Exception
     */
    public void testSize() throws Exception {
        LRUInstanceCache lruInstanceCache = (LRUInstanceCache) cache;
        assertEquals("Size of empty cache is in fact 0", lruInstanceCache.size(), 0);
        lruInstanceCache.putActive(key, value);
        assertEquals("Cache size with one and only one active element is in fact 1", lruInstanceCache.size(), 1);
        lruInstanceCache.putInactive(key, value);
        assertEquals("Cache size with one and only one inactive element is in fact 1", lruInstanceCache.size(), 1);
        Object key2 = new Object();
        Object value2 = new Object();
        lruInstanceCache.putInactive(key2, value2);
        assertEquals("Cache size with two inactive elements is in fact 2", lruInstanceCache.size(), 2);
        lruInstanceCache.putActive(key2, value2);
        assertEquals("Cache size with one active and one inactive elements is in fact 2", lruInstanceCache.size(), 2);
        lruInstanceCache.putActive(key, value);
        assertEquals("Cache size with two active elements is in fact 2", lruInstanceCache.size(), 2);
        lruInstanceCache.remove(key2);
        assertEquals("Cache size after removal of second element is in fact 1", lruInstanceCache.size(), 1);
        lruInstanceCache.remove(key);
        assertEquals("Cache size after removal the last element is in fact 0", lruInstanceCache.size(), 0);
    }

    /**
     * Tests that on an empty cache runner is not invoked and that if runner is not supposed to run it is not run.
     * @throws Exception
     */

    public void testRunnerEmpty() throws Exception {
        MockLRURunner mockLRURunner = new MockLRURunner();
        LRUInstanceCache lruInstanceCache = (LRUInstanceCache) cache;
        mockLRURunner.init(100);
        lruInstanceCache.run(mockLRURunner);
        assertEquals("With empty cache LRURunner that was not supposed to run at all was indeed not invoked", mockLRURunner.getInvoked(), 0);
        lruInstanceCache.putInactive(key, value);
        mockLRURunner.init(0);
        lruInstanceCache.run(mockLRURunner);
        assertEquals("With non-empty cache LRURunner that was not supposed to run at all was indeed not invoked", mockLRURunner.getInvoked(), 0);
    }

    /**
     * Tests that the LRURunner traverses the whole list and is asked to remove all elements if they are all scheduled
     * @throws Exception
     */
    public void testRunnerAll() throws Exception {
        MockLRURunner mockLRURunner = new MockLRURunner();
        mockLRURunner.init(100);
        LRUInstanceCache lruInstanceCache = (LRUInstanceCache) cache;
        Object key1 = new Object();
        Object value1 = new Object();
        Object key2 = new Object();
        Object value2 = new Object();
        Object key3 = new Object();
        Object value3 = new Object();
        lruInstanceCache.putInactive(key, value);
        lruInstanceCache.putInactive(key1, value1);
        lruInstanceCache.putInactive(key2, value2);
        lruInstanceCache.putInactive(key3, value3);
        assertEquals("After adding 4 inactive entries the size of cache is in fact 4", lruInstanceCache.size(), 4);
        lruInstanceCache.run(mockLRURunner);
        assertEquals("For 4 inactive elements the LRURunner was invoked 4 times", mockLRURunner.getInvoked(), 4);
        assertEquals("For 0 inactive elements scheduled to be removed LRURunner removed none", mockLRURunner.getElementsRemoved(), 0);
        assertEquals("For 4 inactive elements in cache, 0 scheduled to be removed after the run of LRURunner the size of the cache has not been affected", lruInstanceCache.size(), 4);
        mockLRURunner.init(100);
        mockLRURunner.addElementToRemove(key);
        mockLRURunner.addElementToRemove(key1);
        mockLRURunner.addElementToRemove(key2);
        mockLRURunner.addElementToRemove(key3);
        lruInstanceCache.run(mockLRURunner);
        assertEquals("For 4 inactive elements scheduled to be removed the LRURunner was invoked 4 times", mockLRURunner.getInvoked(), 4);
        assertEquals("For 4 inactive elements scheduled to be removed the LRURunner removed 4 ", mockLRURunner.getElementsRemoved(), 4);
        assertEquals("For 4 inactive elements in cache, 4 scheduled to be removed after the run of LRURunner the size of the cache is 0", lruInstanceCache.size(), 0);
    }

    /**
     * Tests that LRURunner is not asked to remove already removed elements again.
     * @throws Exception
     */

    public void testRunnerAgain() throws Exception {
        MockLRURunner mockLRURunner = new MockLRURunner();
        LRUInstanceCache lruInstanceCache = (LRUInstanceCache) cache;
        Object key1 = new Object();
        Object value1 = new Object();
        Object key2 = new Object();
        Object value2 = new Object();
        Object key3 = new Object();
        Object value3 = new Object();
        lruInstanceCache.putInactive(key, value);
        lruInstanceCache.putInactive(key1, value1);
        lruInstanceCache.putInactive(key2, value2);
        lruInstanceCache.putInactive(key3, value3);
        assertEquals("After adding 4 inactive entries the size of cache is in fact 4", lruInstanceCache.size(), 4);

        mockLRURunner.init(100);
        mockLRURunner.addElementToRemove(key1);
        mockLRURunner.addElementToRemove(key2);
        lruInstanceCache.run(mockLRURunner);
        assertEquals("For 4 inactive elements the LRURunner was invoked 4 times", mockLRURunner.getInvoked(), 4);
        assertEquals("For 2 inactive elements scheduled to be removed LRURunner removed 2", mockLRURunner.getElementsRemoved(), 2);
        assertEquals("For 4 inactive elements in cache, 2 scheduled to be removed after the run of LRURunner the size of the cache is 2", lruInstanceCache.size(), 2);

        mockLRURunner.init(100);
        mockLRURunner.addElementToRemove(key1);
        mockLRURunner.addElementToRemove(key2);
        lruInstanceCache.run(mockLRURunner);
        assertEquals("For 2 elements in the cache, 2 removed inactive elements scheduled again to be removed the LRURunner was invoked 2 times", mockLRURunner.getInvoked(), 2);
        assertEquals("For 2 elements in the cache, 2 removed inactive elements scheduled again to be removed the LRURunner removed 0 ", mockLRURunner.getElementsRemoved(), 0);
        assertEquals("For 2 elements in the cache, 2 removed inactive elements scheduled again to be removed after the run of LRURunner the size of the cache is 2", lruInstanceCache.size(), 2);
    }

    /**
     * Tests that if runner doesn't want to continue traverse the list it won't be asked.
     * @throws Exception
     */
    public void testRunnerPartial() throws Exception {
        MockLRURunner mockLRURunner = new MockLRURunner();
        LRUInstanceCache lruInstanceCache = (LRUInstanceCache) cache;
        Object key1 = new Object();
        Object value1 = new Object();
        Object key2 = new Object();
        Object value2 = new Object();
        Object key3 = new Object();
        Object value3 = new Object();
        lruInstanceCache.putInactive(key, value);
        lruInstanceCache.putInactive(key1, value1);
        lruInstanceCache.putInactive(key2, value2);
        lruInstanceCache.putInactive(key3, value3);
        assertEquals("After adding 4 inactive entries the size of cache is in fact 4", lruInstanceCache.size(), 4);

        mockLRURunner.init(2);
        mockLRURunner.addElementToRemove(key1);
        mockLRURunner.addElementToRemove(key3);
        lruInstanceCache.run(mockLRURunner);
        assertEquals("For 4 inactive elements the LRURunner that was supposed to be invoked 2 times was invoked 2 times", mockLRURunner.getInvoked(), 2);
        assertEquals("For 2 inactive elements scheduled and only 1 that was supposed to be removed because the LRURunner wants to get invoked only twice, only one is in fact removed", mockLRURunner.getElementsRemoved(), 1);
        assertEquals("For 2 inactive elements scheduled and only 1 that was supposed to be removed because the LRURunner wants to get invoked only twice, after the run of LRURunner the size of the cache is 3", lruInstanceCache.size(), 3);

    }

    /**
     * Tests that LRURunner operates on the inactive elements only
     * @throws Exception
     */
    public void testRunnerOnActive() throws Exception {
        MockLRURunner mockLRURunner = new MockLRURunner();
        LRUInstanceCache lruInstanceCache = (LRUInstanceCache) cache;
        Object key1 = new Object();
        Object value1 = new Object();
        Object key2 = new Object();
        Object value2 = new Object();
        Object key3 = new Object();
        Object value3 = new Object();
        lruInstanceCache.putActive(key, value);
        lruInstanceCache.putActive(key1, value1);
        lruInstanceCache.putActive(key2, value2);
        lruInstanceCache.putActive(key3, value3);
        assertEquals("After adding 4 active entries the size of cache is in fact 4", lruInstanceCache.size(), 4);

        mockLRURunner.init(100);
        mockLRURunner.addElementToRemove(key1);
        mockLRURunner.addElementToRemove(key2);
        lruInstanceCache.run(mockLRURunner);
        assertEquals("For 4 active elements in cache, 2 active elements erroneousdly scheduled to be removed, the LRURunner was invoked 0 times", mockLRURunner.getInvoked(), 0);
        assertEquals("For 4 active elements in cache, 2 active elements erroneousdly scheduled to be removed, LRURunner removed 0", mockLRURunner.getElementsRemoved(), 0);
        assertEquals("For 4 active elements in cache, 2 active elements erroneousdly scheduled to be removed, after the run of LRURunner the size of the cache is 4", lruInstanceCache.size(), 4);
    }

}
