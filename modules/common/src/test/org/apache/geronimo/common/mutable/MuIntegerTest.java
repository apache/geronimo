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
 * Unit test for {@link MuInteger} class.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:27 $
 */
public class MuIntegerTest extends TestCase {

    public void testConstructors() {

        MuInteger muInteger = new MuInteger();
        assertEquals(0, muInteger.get());

        muInteger = new MuInteger(1);
        assertEquals(1, muInteger.get());

        muInteger = new MuInteger(new Integer(1));
        assertEquals(1, muInteger.get());
    }

    public void testCommit() {
        int assumed = 10;
        int newVal = 20;

        MuInteger muInteger = new MuInteger(10);
        assertTrue(muInteger.commit(assumed, newVal));
        assertFalse(muInteger.commit(assumed, newVal));
    }

    public void testSwap() {
        MuInteger six = new MuInteger(6);
        MuInteger nine = new MuInteger(9);

        assertEquals(9, nine.swap(nine));
        assertEquals(9, six.swap(nine));
        assertEquals(9, six.get());
    }

    public void testPlusMinusMultiDivide() {
        MuInteger muInteger = new MuInteger(9);
        int val = 2;

        assertEquals(11, muInteger.add(val));
        assertEquals(11, muInteger.get());

        assertEquals(9, muInteger.subtract(val));
        assertEquals(9, muInteger.get());

        assertEquals(18, muInteger.multiply(val));
        assertEquals(18, muInteger.get());

        assertEquals(9, muInteger.divide(val));
        assertEquals(9, muInteger.get());
    }

    public void testIncDec() {
        MuInteger muInteger = new MuInteger(5);
        assertEquals(6, muInteger.increment());
        assertEquals(6, muInteger.get());

        assertEquals(5, muInteger.decrement());
        assertEquals(5, muInteger.get());
    }

    public void testBooleanOperations() {
        MuInteger muInteger = new MuInteger(9);
        assertEquals(-9, muInteger.negate(), 0);

        muInteger.set(9);
        assertEquals(~9, muInteger.complement());

        muInteger.set(6);
        assertEquals(6 | 9, muInteger.or(9));

        muInteger.set(9);
        assertEquals(9 & 6, muInteger.and(6));

        muInteger.set(6);
        assertEquals(6 ^ 9, muInteger.xor(9));

        muInteger.set(9);
        assertEquals(9 << 6,muInteger.shiftLeft(6));

        muInteger.set(6);
        assertEquals(6 >> 9,muInteger.shiftRight(9));

        muInteger.set(9);
        assertEquals(9 >>> 6,muInteger.shiftRightZero(6));
    }

    public void testCompare() {
        int lesser = 1;
        int equal = 2;
        int greater = 3;

        MuInteger muInteger = new MuInteger(equal);

        assertTrue(muInteger.compareTo(equal) == 0);
        assertTrue(muInteger.compareTo(lesser) > 0);
        assertTrue(muInteger.compareTo(greater) < 0);

        assertTrue(muInteger.compareTo(new MuInteger(2)) == 0);
        assertTrue(muInteger.compareTo(new MuInteger(5)) < 0);
        assertTrue(muInteger.compareTo(new MuInteger(0)) > 0);
        try {
            muInteger.compareTo(new String());
            fail("Expected ClassCastException");
        } catch (ClassCastException ignore) {
        }
    }

    public void testEquals() {
        MuInteger muInteger = new MuInteger(5);

        assertTrue(muInteger.equals(muInteger));
        assertTrue(muInteger.equals(new MuInteger(5)));
        assertFalse(muInteger.equals(null));
        assertFalse(muInteger.equals(new String()));
    }

    public void testMisc() {
        MuInteger muInteger1 = new MuInteger(5);
        MuInteger muInteger2 = new MuInteger(new Integer(5));

        assertTrue(muInteger1.equals(muInteger2));
        assertEquals("5",muInteger2.toString());

        assertEquals(muInteger1.hashCode(),muInteger2.hashCode());
    }

    public void testSetObject() {
        MuInteger muInteger = new MuInteger();

        muInteger.setValue(new Integer(5));
        assertEquals(new Integer(5), muInteger.getValue());
        assertEquals(5, muInteger.get());

        muInteger.setValue(new Integer(5));
        assertEquals(new Integer(5), muInteger.getValue());
        assertEquals(5, muInteger.get());

        muInteger = new MuInteger();
        muInteger.set(2);
        assertEquals(2, muInteger.get());

        try {
            muInteger.setValue(new String("5"));
            fail("Expected NotCoercibleException");
        } catch (NotCoercibleException ignore) {
        }
    }
}