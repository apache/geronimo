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
 * Unit test for {@link MuInteger} class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/27 18:09:52 $
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