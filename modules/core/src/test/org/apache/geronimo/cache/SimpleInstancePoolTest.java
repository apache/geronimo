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

public final class SimpleInstancePoolTest extends TestCase {

    private static final int POOL_SIZE = 10;
    private static final boolean HARD_LIMIT = true;

    private InstanceFactory factory;
    private SimpleInstancePool pool;

    public SimpleInstancePoolTest(String name) {
        super(name);
    }
    
    public void setUp() {
        factory = new TestObjectFactory();
        pool = new SimpleInstancePool(factory, POOL_SIZE, HARD_LIMIT);
        pool.startPooling();
    }

    public void testGetMaxSize() throws Exception {
        assertTrue("Pool max size should be " + POOL_SIZE,
                   pool.getMaxSize() == POOL_SIZE);
    }

    public void testIsHardLimit() throws Exception {
        assertTrue("Use hard limit should be " + HARD_LIMIT,
                   pool.isHardLimit() && HARD_LIMIT);
    }
    
    public void testFill() throws Exception {
        pool.fill();
        assertTrue("Pool filled size should match " + POOL_SIZE,
                   pool.getSize() == POOL_SIZE);
    }
    
    public void testAcquireAndRelease() throws Exception {
        Object o1 = pool.acquire();
        Object o2 = pool.acquire();
        assertNotNull("Acquired object should not be null", o1);
        assertNotNull("Acquired object should not be null", o2);
        assertNotSame("Two acquired objects should not be identical", o1, o2);
        assertTrue("Allocated size should be 2", pool.getAllocatedSize() == 2);
        pool.release(o1);
        pool.release(o2);
        assertTrue("After release, allocated size should be zero",
                   pool.getAllocatedSize() == 0);
    }
    
    public void tearDown() {
        pool.stopPooling();
        pool = null;
        factory = null;
    }
    
    public final class TestObjectFactory implements InstanceFactory {
        
        public Object createInstance() {
            return new Object();
        }

        public void destroyInstance(Object instance) {
        }
    }
}
