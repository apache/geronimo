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
 * Unit test for {@link Primitives} class.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:27 $
 */
public class PrimitivesTest
    extends TestCase
{
    public void testEqualsByteArrayNullValid() {
        byte[] b = {(byte)1, (byte)2, (byte)3};
        assertFalse(Primitives.equals(null, b));
    }
    
    public void testEqualsByteArrayValidNull() {
        byte[] b = {(byte)1, (byte)2, (byte)3};
        assertFalse(Primitives.equals(b, null));
    }
    
    public void testEqualsByteArrayUnequalLength() {
        byte[] first = {(byte)1, (byte)2, (byte)3};
        byte[] second = {(byte)1, (byte)2};
        assertFalse(Primitives.equals(first, second));
    }
    
    public void testEqualsByteArrayUnequal() {
        byte[] first = {(byte)1, (byte)2, (byte)3};
        byte[] second = {(byte)3, (byte)1, (byte)2};
        assertFalse(Primitives.equals(first, second));
    }        
    
    public void testEqualsByteArrayEqual() {
        byte[] first = {(byte)1, (byte)2, (byte)3};
        byte[] second = {(byte)1, (byte)2, (byte)3};
        assertTrue(Primitives.equals(first, second));
    }        
    
    public void testEqualsByteArraySame() {
        byte[] b = {(byte)1, (byte)2, (byte)3};
        assertTrue(Primitives.equals(b, b));
    }
    
    public void testEqualsSubByteArrayNullValid() {
        byte[] b = {(byte)1, (byte)2, (byte)3};
        assertFalse(Primitives.equals(null, 0, b, 0, b.length));
    }
    
    public void testEqualsSubByteArrayValidNull() {
        byte[] b = {(byte)1, (byte)2, (byte)3};
        assertFalse(Primitives.equals(b, 0, null, 0, b.length));
    }
    
    public void testEqualsSubByteArrayUnequalLength() {
        byte[] first = {(byte)1, (byte)2, (byte)3};
        byte[] second = {(byte)1, (byte)2};
        assertFalse(Primitives.equals(first, 1, second, 0, second.length));
        assertTrue(Primitives.equals(first, 0, second, 0, second.length));
    }
    
    public void testEqualsSubByteArrayNegativeOffset() {
        byte[] first = {(byte)1, (byte)2, (byte)3};
        byte[] second = {(byte)3, (byte)1, (byte)2};
        assertFalse(Primitives.equals(first, -1, second, 0, second.length));
        assertFalse(Primitives.equals(first, 0, second, -1, second.length));
    }
    
    public void testEqualsDouble() {
        assertTrue(Primitives.equals(1.0, 1.0));
        assertFalse(Primitives.equals(0.0, Double.MIN_VALUE));
    }        
    
    public void testEqualsFloat() {
        assertTrue(Primitives.equals(1.0f, 1.0f));
        assertFalse(Primitives.equals(0.0f, Float.MIN_VALUE));
    }
    
    public void testToInt() {
        assertEquals(1, Primitives.toInt(1L));
        
        try {
            Primitives.toInt(Long.MIN_VALUE);
            fail();
        } catch(DataConversionException ex){
            // success
        }        
        
        try {
            Primitives.toInt(Long.MAX_VALUE);
            fail();
        } catch(DataConversionException ex){
            // success
        }
    }        
}
