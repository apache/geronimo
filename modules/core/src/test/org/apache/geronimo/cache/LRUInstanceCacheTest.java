/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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


/**
 * Tests the {@link LRUInstanceCache} implementation of {@link InstanceCache} interface.
 * Adds tests for {@link LRUInstanceCache#size()} and {@link LRUInstanceCache#run(LRURunner)} methods to base set provided by {@link AbstractInstanceCacheTest}
 * @see AbstractInstanceCacheTest
 * @version $Rev$ $Date$
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
