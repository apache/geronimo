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
 * Unit test for {@link MuFloat} class.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:05 $
 */
public class MuFloatTest extends TestCase {

    public void testConstructors() {
        MuFloat muFloat = new MuFloat();
        assertEquals(0, muFloat.get(), 0);

        muFloat = new MuFloat(5.5f);
        assertEquals(5.5f, muFloat.get(),0);

        muFloat = new MuFloat(new Double(3.6));
        assertEquals(3.6f,muFloat.get(),0);
    }

    public void testCommit() {
        float assumed = 10;
        float newVal = 20;

        MuFloat muFloat = new MuFloat(10);
        assertTrue(muFloat.commit(assumed, newVal));
        assertFalse(muFloat.commit(assumed, newVal));
    }

    public void testSwap() {
        MuFloat six = new MuFloat(6);
        MuFloat nine = new MuFloat(9);

        assertEquals(9, nine.swap(nine), 0);
        assertEquals(9, six.swap(nine), 0);
        assertEquals(9, six.get(), 0);
    }

    public void testPlusMinusMultiDivide() {
        MuFloat muFloat = new MuFloat(9);

        float val = 2;

        assertEquals(11, muFloat.add(val), 0);
        assertEquals(11, muFloat.get(), 0);

        assertEquals(9, muFloat.subtract(val), 0);
        assertEquals(9, muFloat.get(), 0);

        assertEquals(18, muFloat.multiply(val), 0);
        assertEquals(18, muFloat.get(), 0);

        assertEquals(9, muFloat.divide(val), 0);
        assertEquals(9, muFloat.get(), 0);
    }

    public void testNegate() {
        MuFloat muFloat = new MuFloat(9);
        assertEquals(-9, muFloat.negate(), 0);
    }

    public void testCompare() {
        float lesser = 1.2f;
        float equal = 2.3f;
        float greater = 3.4f;

        MuFloat muFloat = new MuFloat(equal);

        assertTrue(muFloat.compareTo(equal) == 0);
        assertTrue(muFloat.compareTo(lesser) > 0);
        assertTrue(muFloat.compareTo(greater) < 0);

        assertTrue(muFloat.compareTo(new MuFloat(2.3f)) == 0);
        assertTrue(muFloat.compareTo(new MuFloat(5.0f)) < 0);
        assertTrue(muFloat.compareTo(new MuFloat(1.2f)) > 0);

        try {
            muFloat.compareTo(new String());
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
        }
    }

    public void testEquals() {
        MuFloat muFloat = new MuFloat(5.5f);

        assertTrue(muFloat.equals(muFloat));
        assertTrue(muFloat.equals(new MuFloat(5.5f)));
        assertFalse(muFloat.equals(null));
        assertFalse(muFloat.equals(new String()));
    }

   public void testMisc() {
        MuFloat muFloat = new MuFloat(10);

        assertEquals("10.0", muFloat.toString());
        assertEquals(muFloat.hashCode(), (new MuFloat(10)).hashCode());
    }

    public void testSetObject() {
        MuFloat muFloat = new MuFloat();
        muFloat.setValue(new Integer(5));

        assertEquals(new Float(5), muFloat.getValue());
        assertEquals(5, muFloat.get(), 0);

        muFloat.setValue(new Double(5.5));

        assertEquals(new Float(5.5), muFloat.getValue());
        assertEquals(5.5, muFloat.get(), 0);

        muFloat = new MuFloat();
        muFloat.set(2.2f);
        assertEquals(2.2f, muFloat.get(), 0);

        muFloat.setValue(new String("5.5"));
        assertEquals(5.5f,muFloat.get(),0);

        try {
            muFloat.setValue(new String("Thirty Six.Five Five"));
            fail("Expected NotCoercibleException");
        } catch (NotCoercibleException ignore) {
        }

        try {
            muFloat.setValue(new Object());
            fail("Expected NotCoercibleException");
        }
        catch (NotCoercibleException ignore) {
        }
    }
}
