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

package org.apache.geronimo.common.mutable;

import junit.framework.TestCase;

/**
 * Unit test for {@link MuByte} class.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/30 20:59:16 $
 */
public class MuByteTest
    extends TestCase
{
    public void testDefaultConstructor() {
        MuByte muByte = new MuByte();
        assertEquals(0,muByte.get());
    }

    public void testPrimitiveConstructor() {
        byte b = 127;
        MuByte muByte = new MuByte(b);
        assertEquals(127,muByte.get());
    }

    public void testObjectConstructor() {
        Object obj = new Integer(10);
        MuByte muByte = new MuByte(obj);
        assertEquals(10,muByte.get());
    }

    public void testCommit() {
        byte b = 10;
        MuByte muByte = new MuByte(b);

        byte assumed = 10;
        byte newVal = 20;
        assertTrue(muByte.commit(assumed,newVal));
        assertFalse(muByte.commit(assumed,newVal));
    }

    public void testSwap() {
        MuByte six = new MuByte(new Integer(6));
        MuByte nine = new MuByte(new Integer(9));

        assertEquals(9,nine.swap(nine));
        assertEquals(9,six.swap(nine));
        assertEquals(9,six.get());
    }

    public void testIncDec() {
        MuByte muByte = new MuByte(new Integer(5));

        assertEquals(6,muByte.increment());
        assertEquals(6,muByte.get());

        assertEquals(5,muByte.decrement());
        assertEquals(5,muByte.get());
    }

    public void testPlusMinusMultiDivide() {
        byte val = 2;
        MuByte muByte = new MuByte(new Integer(9));

        assertEquals(11,muByte.add(val));
        assertEquals(11,muByte.get());

        assertEquals(9,muByte.subtract(val));
        assertEquals(9,muByte.get());

        assertEquals(18,muByte.multiply(val));
        assertEquals(18,muByte.get());

        assertEquals(9,muByte.divide(val));
        assertEquals(9,muByte.get());
    }

    public void testLogicalOperations() {
        byte value;

        MuByte muByte = new MuByte(new Integer(2));

        assertEquals(-2,muByte.negate());
        assertEquals(2,muByte.negate());

        assertEquals(-3,muByte.complement());
        assertEquals(2,muByte.complement());

        value = 7;
        assertEquals(2,muByte.and(value));

        value = 5;
        assertEquals(7,muByte.or(value));

        value = 3;
        assertEquals(4,muByte.xor(value));
    }

    public void testShift() {
        MuByte muByte = new MuByte(new Integer(2));

        assertEquals(8,muByte.shiftLeft(2));
        assertEquals(4,muByte.shiftRight(1));
        assertEquals(2,muByte.shiftRightZero(1));
    }

    public void testCompare() {
        byte equal = 2;
        byte greater = 3;
        byte lesser = 1;
        MuByte muByte = new MuByte(new Integer(2));

        assertEquals(0,muByte.compareTo(equal));
        assertTrue(muByte.compareTo(greater) < 0);
        assertTrue(muByte.compareTo(lesser) > 0);

        muByte.compareTo(muByte);
    }

    public void testEquals() {
        MuByte two = new MuByte(new Integer(2));
        MuByte due = new MuByte(new Integer(2));
        Integer integerTwo = new Integer(2);

        assertTrue(two.equals(due));
        assertTrue(two.equals(two));
        assertFalse(two.equals(integerTwo));
    }

    public void testMutable() {
        MuByte muByte = new MuByte(new Integer(2));
        muByte.setValue(new Integer(10));
        assertEquals(10,muByte.get());

        byte b = 10;
        Byte byteObj = new Byte(b);
        assertEquals(byteObj,muByte.getValue());

        byte max = 127;
        assertEquals(10,muByte.set(max));
        assertEquals(max,muByte.get());
    }

    public void testMisc() {
        MuByte muByte1 = new MuByte(new Integer(2));
        MuByte muByte2 = new MuByte(new Integer(2));

        assertEquals(new String("2"),muByte1.toString());
        assertEquals(muByte1.hashCode(),muByte2.hashCode());
    }
}
