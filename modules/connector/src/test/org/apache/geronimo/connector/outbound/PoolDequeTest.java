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

package org.apache.geronimo.connector.outbound;

import junit.framework.TestCase;


/**
 * PoolDequeTest.java
 *
 *
 * Created: Fri Oct 10 12:00:47 2003
 *
 * @version 1.0
 */
public class PoolDequeTest extends TestCase {

    private static final int MAX_SIZE = 10;

    public PoolDequeTest(String name) {
        super(name);
    } // PoolDequeTest constructor


    public void testFill() throws Exception {
        SinglePoolConnectionInterceptor.PoolDeque pool = new SinglePoolConnectionInterceptor.PoolDeque(MAX_SIZE);
        for (int i = 0; i < MAX_SIZE; i++) {
            pool.addLast(new ManagedConnectionInfo(null, null));
        }
    }

    public void testFillAndEmptyFirst() throws Exception {
        SinglePoolConnectionInterceptor.PoolDeque pool = new SinglePoolConnectionInterceptor.PoolDeque(MAX_SIZE);
        for (int i = 0; i < MAX_SIZE; i++) {
            pool.addLast(new ManagedConnectionInfo(null, null));
        }
        ManagedConnectionInfo[] mcis = new ManagedConnectionInfo[MAX_SIZE];
        for (int i = 0; i < MAX_SIZE; i++) {
            mcis[i] = pool.removeFirst();
        }
        assertTrue("Expected pool to be empty!", pool.isEmpty());

        for (int i = 0; i < MAX_SIZE; i++) {
            pool.addFirst(mcis[i]);
        }

    }

    public void testFillAndEmptyLast() throws Exception {
        SinglePoolConnectionInterceptor.PoolDeque pool = new SinglePoolConnectionInterceptor.PoolDeque(MAX_SIZE);
        ManagedConnectionInfo[] mcis = new ManagedConnectionInfo[MAX_SIZE];
        for (int i = 0; i < MAX_SIZE; i++) {
            mcis[i] = new ManagedConnectionInfo(null, null);
            pool.addLast(mcis[i]);
        }

        for (int i = MAX_SIZE - 1; i >= 0; i--) {
            assertTrue("Expected to get corresponding MCI from pool", mcis[i] == pool.peekLast());
            assertTrue("Expected to get corresponding MCI from pool", mcis[i] == pool.removeLast());
        }
        assertTrue("Expected pool to be empty!", pool.isEmpty());
    }

    public void testRemove() throws Exception {
        SinglePoolConnectionInterceptor.PoolDeque pool = new SinglePoolConnectionInterceptor.PoolDeque(MAX_SIZE);
        ManagedConnectionInfo[] mcis = new ManagedConnectionInfo[MAX_SIZE];
        for (int i = 0; i < MAX_SIZE; i++) {
            mcis[i] = new ManagedConnectionInfo(null, null);
            pool.addLast(mcis[i]);
        }

        for (int i = 0; i < MAX_SIZE; i++) {
            assertTrue("Expected to find MCI in pool", pool.remove(mcis[i]));
        }
        assertTrue("Expected pool to be empty!", pool.isEmpty());
    }

} // PoolDequeTest
