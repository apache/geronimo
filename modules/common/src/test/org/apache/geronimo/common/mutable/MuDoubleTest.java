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
import org.apache.geronimo.common.NotCoercibleException;

/**
 * Unit test for {@link MuDouble} class.
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:57:05 $
 */
public class MuDoubleTest extends TestCase {

    public void testConstructors() {
        double d = 10;
        MuDouble muDouble = new MuDouble();
        assertEquals(0, muDouble.get(), 0);

        muDouble = new MuDouble(d);
        assertEquals(d, muDouble.get(), 0);

        muDouble = new MuDouble(new Integer(10));
        assertEquals(10, muDouble.get(), 0);
    }

    public void testCommit() {
        double assumed = 10;
        double newVal = 20;

        MuDouble muDouble = new MuDouble(10);
        assertTrue(muDouble.commit(assumed, newVal));
        assertFalse(muDouble.commit(assumed, newVal));
    }

    public void testSwap() {
        MuDouble six = new MuDouble(6);
        MuDouble nine = new MuDouble(9);

        assertEquals(9, nine.swap(nine), 0);
        assertEquals(9, six.swap(nine), 0);
        assertEquals(9, six.get(), 0);
    }

    public void testPlusMinusMultiDivide() {
        MuDouble muDouble = new MuDouble(9);
        double val = 2;

        assertEquals(11, muDouble.add(val), 0);
        assertEquals(11, muDouble.get(), 0);

        assertEquals(9, muDouble.subtract(val), 0);
        assertEquals(9, muDouble.get(), 0);

        assertEquals(18, muDouble.multiply(val), 0);
        assertEquals(18, muDouble.get(), 0);

        assertEquals(9, muDouble.divide(val), 0);
        assertEquals(9, muDouble.get(), 0);
    }

    public void testNegate() {
        MuDouble muDouble = new MuDouble(9);
        assertEquals(-9, muDouble.negate(), 0);
    }

    public void testCompare() {
        double lesser = 1.2;
        double equal = 2.3;
        double greater = 3.4;

        MuDouble muDouble = new MuDouble(equal);

        assertTrue(muDouble.compareTo(equal) == 0);
        assertTrue(muDouble.compareTo(lesser) > 0);
        assertTrue(muDouble.compareTo(greater) < 0);

        assertTrue(muDouble.compareTo(new MuDouble(2.3)) == 0);
        assertTrue(muDouble.compareTo(new MuDouble(5.0)) < 0);
        assertTrue(muDouble.compareTo(new MuDouble(1.2)) > 0);
        try {
            muDouble.compareTo(new String());
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
        }
    }

    public void testEquals() {
        MuDouble muDouble = new MuDouble(5.0);

        assertTrue(muDouble.equals(muDouble));
        assertTrue(muDouble.equals(new MuDouble(5.0)));
        assertFalse(muDouble.equals(null));
        assertFalse(muDouble.equals(new String()));
    }

    public void testMisc() {
        MuDouble muDouble = new MuDouble(10);

        assertEquals("10.0", muDouble.toString());
        assertEquals(muDouble.hashCode(), (new MuDouble(10)).hashCode());
    }

    public void testSetObject() {
        MuDouble muDouble = new MuDouble();

        muDouble.setValue(new Integer(5));
        assertEquals(new Double(5), muDouble.getValue());
        assertEquals(5, muDouble.get(), 0);

        muDouble.setValue(new Double(5.5));
        assertEquals(new Double(5.5), muDouble.getValue());
        assertEquals(5.5, muDouble.get(), 0);

        muDouble = new MuDouble();
        muDouble.set(2.2);
        assertEquals(2.2, muDouble.get(), 0);

        try {
            muDouble.setValue(new String("5.5"));
            fail("Expected NotCoercibleException");
        } catch (NotCoercibleException ignore) {
        }
    }
}
