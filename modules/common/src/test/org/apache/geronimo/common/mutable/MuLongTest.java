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
import org.apache.geronimo.common.NotCoercibleException;


/**
 * Unit test for {@link MuLong} class.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:27 $
 */
public class MuLongTest extends TestCase {
    public void testConstructors() {

        MuLong muLong = new MuLong();
        assertEquals(0, muLong.get());

        muLong = new MuLong(1);
        assertEquals(1, muLong.get());

        muLong = new MuLong(new Long(1));
        assertEquals(1, muLong.get());
    }

    public void testCommit() {
        long assumed = 10;
        long newVal = 20;

        MuLong muLong = new MuLong(10);
        assertTrue(muLong.commit(assumed, newVal));
        assertFalse(muLong.commit(assumed, newVal));
    }

    public void testSwap() {
        MuLong six = new MuLong(6);
        MuLong nine = new MuLong(9);

        assertEquals(9, nine.swap(nine));
        assertEquals(9, six.swap(nine));
        assertEquals(9, six.get());
    }

    public void testPlusMinusMultiDivide() {
        MuLong muLong = new MuLong(9);
        long val = 2;

        assertEquals(11, muLong.add(val));
        assertEquals(11, muLong.get());

        assertEquals(9, muLong.subtract(val));
        assertEquals(9, muLong.get());

        assertEquals(18, muLong.multiply(val));
        assertEquals(18, muLong.get());

        assertEquals(9, muLong.divide(val));
        assertEquals(9, muLong.get());
    }

    public void testIncDec() {
        MuLong muLong = new MuLong(5);
        assertEquals(6, muLong.increment());
        assertEquals(6, muLong.get());

        assertEquals(5, muLong.decrement());
        assertEquals(5, muLong.get());
    }

    public void testBooleanOperations() {
        MuLong muLong = new MuLong(9);
        assertEquals(-9, muLong.negate());

        muLong.set(9);
        assertEquals(~9, muLong.complement());

        muLong.set(6);
        assertEquals(6 | 9, muLong.or(9));

        muLong.set(9);
        assertEquals(9 & 6, muLong.and(6));

        muLong.set(6);
        assertEquals(6 ^ 9, muLong.xor(9));

        muLong.set(9);
        assertEquals(9 << 6,muLong.shiftLeft(6));

        muLong.set(6);
        assertEquals(6 >> 9,muLong.shiftRight(9));

        muLong.set(9);
        assertEquals(9 >>> 6,muLong.shiftRightZero(6));
    }

    public void testCompare() {
        long lesser = 1;
        long equal = 2;
        long greater = 3;

        MuLong muLong = new MuLong(equal);

        assertTrue(muLong.compareTo(equal) == 0);
        assertTrue(muLong.compareTo(lesser) > 0);
        assertTrue(muLong.compareTo(greater) < 0);

        assertTrue(muLong.compareTo(new MuLong(2)) == 0);
        assertTrue(muLong.compareTo(new MuLong(5)) < 0);
        assertTrue(muLong.compareTo(new MuLong(0)) > 0);
        try {
            muLong.compareTo(new String());
            fail("Expected ClassCastException");
        } catch (ClassCastException ignore) {
        }
    }

    public void testEquals() {
        MuLong muLong = new MuLong(5);

        assertTrue(muLong.equals(muLong));
        assertTrue(muLong.equals(new MuLong(5)));
        assertFalse(muLong.equals(null));
        assertFalse(muLong.equals(new String()));
    }

    public void testMisc() {
        MuLong muLong1 = new MuLong(5);
        MuLong muLong2 = new MuLong(new Long(5));

        assertTrue(muLong1.equals(muLong2));
        assertEquals("5",muLong2.toString());

        assertEquals(muLong1.hashCode(),muLong2.hashCode());
    }

    public void testSetObject() {
        MuLong muLong = new MuLong();

        muLong.setValue(new Long(5));
        assertEquals(new Long(5), muLong.getValue());
        assertEquals(5, muLong.get());

        muLong.setValue(new Long(5));
        assertEquals(new Long(5), muLong.getValue());
        assertEquals(5, muLong.get());

        muLong = new MuLong();
        muLong.set(2);
        assertEquals(2, muLong.get());

        try {
            muLong.setValue(new String("5"));
            fail("Expected NotCoercibleException");
        } catch (NotCoercibleException ignore) {
        }
    }
}