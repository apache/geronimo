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
import org.apache.geronimo.common.NotCoercibleException;

/**
 * Unit test for {@link MuDouble} class.
 *
 * @version $Revision: 1.3 $ $Date: 2003/09/03 18:00:17 $
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
