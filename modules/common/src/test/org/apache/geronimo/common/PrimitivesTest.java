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
package org.apache.geronimo.common;

import junit.framework.TestCase;

/**
 * Unit test for {@link Primitives} class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/27 08:55:27 $
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
