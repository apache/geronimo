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

package org.apache.geronimo.common;

import junit.framework.TestCase;

/**
 * Unit test for {@link Counter} class.
 *
 * @version $Rev$ $Date$
 */
public class CounterTest
    extends TestCase
{
    public void testIncrement() {
        Counter c = new Counter();
        
        assertEquals(0, c.getCount());
        
        c.increment();
        c.increment();
        
        assertEquals(2, c.getCount());
    }

    public void testDecrement() {
        Counter c = new Counter(2);
        
        assertEquals(2, c.getCount());
        
        c.decrement();
        c.decrement();
        
        assertEquals(0, c.getCount());
    }
    
    public void testReset() {
        Counter c = new Counter(2);
        
        assertEquals(2, c.getCount());
        
        c.increment();
        c.increment();
        
        assertEquals(4, c.getCount());
        
        c.reset();
        
        assertEquals(0, c.getCount());
    }
    
    public void testEquals() {
        Counter first = new Counter();
        Counter second = new Counter(2);
        
        assertFalse(first.equals(second));
        
        first.increment();
        first.increment();
        
        assertTrue(first.equals(second));
    }
    
    public void testEqualsNull() {
        Counter first = new Counter();
        
        assertFalse(first.equals(null));
    }

    public void testEqualsOther() {
        Counter first = new Counter();
        
        assertFalse(first.equals(new Object()));
    }
    
    public void testMakeDirectionalIncreasing() {
        Counter increasing = Counter.makeDirectional(new Counter(), true);
        
        assertEquals(0, increasing.getCount());
        
        increasing.increment();
        increasing.increment();

        assertEquals(2, increasing.getCount());
        
        try {
            increasing.decrement();
            fail();
        } catch (UnsupportedOperationException ex) {
            // success
        }
    }
    
    public void testMakeDirectionalDecreasing() {
        Counter decreasing = Counter.makeDirectional(new Counter(2), false);
        
        assertEquals(2, decreasing.getCount());
        
        decreasing.decrement();
        decreasing.decrement();

        assertEquals(0, decreasing.getCount());
        
        try {
            decreasing.increment();
            fail();
        } catch (UnsupportedOperationException ex) {
            // success
        }
    }
    
    public void testUpAndDown() {
        Counter c = new Counter();
        
        assertEquals(0, c.getCount());
        assertEquals(1, c.increment());
        assertEquals(2, c.increment());
        assertEquals(2, c.getCount());
        assertEquals(1, c.decrement());
        assertEquals(2, c.increment());
        assertEquals(1, c.decrement());
        assertEquals(0, c.decrement());
        assertEquals(0, c.getCount());
    }
}
