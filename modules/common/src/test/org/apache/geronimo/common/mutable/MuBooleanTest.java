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

package org.apache.geronimo.common.mutable;

import junit.framework.TestCase;

/**
 * Unit test for {@link MuBoolean} class.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:05 $
 */
public class MuBooleanTest
    extends TestCase
{
    public void testDefaultConstructor() {
        MuBoolean mutable = new MuBoolean();
        assertFalse(mutable.booleanValue());
    }
    
    public void testPrimativeConstructorTrue() {
        MuBoolean mutable = new MuBoolean(true);
        assertTrue(mutable.booleanValue());
    }
    
    public void testPrimativeConstructorFalse() {
        MuBoolean mutable = new MuBoolean(false);
        assertFalse(mutable.booleanValue());
    }
    
    public void testObjectConstructorNull() {
        MuBoolean mutable = new MuBoolean(null);
        assertFalse(mutable.booleanValue());
    }
    
    public void testObjectConstructorMuBoolean() {
        MuBoolean control = new MuBoolean(true);
        MuBoolean mutable = new MuBoolean(control);
        assertEquals(mutable.booleanValue(), control.booleanValue());
    }
    
    public void testObjectConstructorBoolean() {
        Boolean control = Boolean.TRUE;
        MuBoolean mutable = new MuBoolean(control);
        assertEquals(mutable.booleanValue(), control.booleanValue());
    }
    
    public void testObjectConstructorObject() {
        Object control = new byte[0];
        MuBoolean mutable = new MuBoolean(control);
        assertTrue(mutable.booleanValue());
    }
    
    public void testStringConstructor() {
        MuBoolean mutable = new MuBoolean("TRUE");
        assertTrue(mutable.booleanValue());
    }
    
    public void testSetPrimative() {
        MuBoolean first = new MuBoolean(false);
        assertFalse(first.booleanValue());
        boolean old = first.set(true);
        assertFalse(old);
        assertTrue(first.booleanValue());
    }
    
    public void testSetBoolean() {
        MuBoolean first = new MuBoolean(Boolean.FALSE);
        assertFalse(first.booleanValue());
        boolean old = first.set(Boolean.TRUE);
        assertFalse(old);
        assertTrue(first.booleanValue());
    }
    
    public void testSetMuBoolean() {
        MuBoolean first = new MuBoolean(new MuBoolean(false));
        assertFalse(first.booleanValue());
        boolean old = first.set(new MuBoolean(true));
        assertFalse(old);
        assertTrue(first.booleanValue());
    }
    
    public void testGet() {
        MuBoolean mutable = new MuBoolean(true);
        assertTrue(mutable.get());
        mutable.set(false);
        assertFalse(mutable.get());
    }
    
    public void testCommit() {
        MuBoolean mutable = new MuBoolean(true);
        boolean success = mutable.commit(false, true);
        assertFalse(success);
        assertTrue(mutable.booleanValue());
        
        success = mutable.commit(true, false);
        assertTrue(success);
        assertFalse(mutable.booleanValue());
    }
    
    public void testSwap() {
        MuBoolean t = new MuBoolean(true);
        MuBoolean f = new MuBoolean(false);
        
        boolean newT = t.swap(f);
        assertFalse(newT);
        assertFalse(t.booleanValue());
        assertTrue(f.booleanValue());
    }
    
    public void testComplement() {
        MuBoolean t = new MuBoolean(true);
        boolean c = t.complement();
        assertFalse(c);
        assertFalse(t.booleanValue());
    }
    
    public void testAnd() {
        MuBoolean t = new MuBoolean(true);
        boolean and = t.and(true);
        assertTrue(and);
        assertTrue(t.booleanValue());
        
        and = t.and(false);
        assertFalse(and);
        assertFalse(t.booleanValue());
    }
    
    public void testOr() {
        MuBoolean t = new MuBoolean(false);
        
        boolean or = t.or(false);
        assertFalse(or);
        assertFalse(t.booleanValue());

        or = t.or(true);
        assertTrue(or);
        assertTrue(t.booleanValue());
    }
    
    public void testXor() {
        MuBoolean t = new MuBoolean(false);

        boolean xor = t.xor(false);
        assertFalse(xor);
        assertFalse(t.booleanValue());

        xor = t.xor(true);
        assertTrue(xor);
        assertTrue(t.booleanValue());
    }
}
