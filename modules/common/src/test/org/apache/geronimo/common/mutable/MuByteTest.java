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

package org.apache.geronimo.common.mutable;

import junit.framework.TestCase;

/**
 * Unit test for {@link MuByte} class.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:27 $
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
