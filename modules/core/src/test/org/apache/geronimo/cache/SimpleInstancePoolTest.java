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
