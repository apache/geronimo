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
 * Unit test for {@link MuLong} class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/27 18:09:52 $
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