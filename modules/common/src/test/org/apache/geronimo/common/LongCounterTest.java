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
 * Unit test for {@link LongCounter} class.
 *
 * @version $Rev$ $Date$
 */
public class LongCounterTest
    extends TestCase
{
    public void testIncrement() {
        LongCounter c = new LongCounter();
        
        assertEquals(0, c.getCount());
        
        c.increment();
        c.increment();
        
        assertEquals(2, c.getCount());
    }

    public void testDecrement() {
        LongCounter c = new LongCounter(2);
        
        assertEquals(2, c.getCount());
        
        c.decrement();
        c.decrement();
        
        assertEquals(0, c.getCount());
    }
    
    public void testReset() {
        LongCounter c = new LongCounter(2);
        
        assertEquals(2, c.getCount());
        
        c.increment();
        c.increment();
        
        assertEquals(4, c.getCount());
        
        c.reset();
        
        assertEquals(0, c.getCount());
    }
    
    public void testEquals() {
        LongCounter first = new LongCounter();
        LongCounter second = new LongCounter(2);
        
        assertFalse(first.equals(second));
        
        first.increment();
        first.increment();
        
        assertTrue(first.equals(second));
    }
    
    public void testEqualsNull() {
        LongCounter first = new LongCounter();
        
        assertFalse(first.equals(null));
    }

    public void testEqualsOther() {
        LongCounter first = new LongCounter();
        
        assertFalse(first.equals(new Object()));
    }
    
    public void testMakeDirectionalIncreasing() {
        LongCounter increasing = LongCounter.makeDirectional(new LongCounter(), true);
        
        assertEquals(0, increasing.getCount());
        
        increasing.increment();
        increasing.increment();

        assertEquals(2, increasing.getCount());
        
        try {
            increasing.decrement();
            fail();
        } catch(UnsupportedOperationException ex){
            // success
        }
    }
    
    public void testMakeDirectionalDecreasing() {
        LongCounter decreasing = LongCounter.makeDirectional(new LongCounter(2), false);
        
        assertEquals(2, decreasing.getCount());
        
        decreasing.decrement();
        decreasing.decrement();

        assertEquals(0, decreasing.getCount());
        
        try {
            decreasing.increment();
            fail();
        } catch(UnsupportedOperationException ex){
            // success
        }
    }
}
