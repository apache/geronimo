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
 * Unit test for {@link MuShort} class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/27 18:09:52 $
 */
public class MuShortTest extends TestCase {

    public void testConstructors() {

        MuShort muShort = new MuShort();
        assertEquals(0, muShort.get());

        short s = 1;

        muShort = new MuShort(s);
        assertEquals(1, muShort.get());

        muShort = new MuShort(new Short(s));
        assertEquals(1, muShort.get());
    }

    public void testCommit() {
        short assumed = 10;
        short newVal = 20;

        MuShort muShort = new MuShort((short) 10);
        assertTrue(muShort.commit(assumed, newVal));
        assertFalse(muShort.commit(assumed, newVal));
    }

    public void testSwap() {

        MuShort six = new MuShort((short) 6);
        MuShort nine = new MuShort((short) 9);

        assertEquals(9, nine.swap(nine));
        assertEquals(9, six.swap(nine));
        assertEquals(9, six.get());
    }

    public void testPlusMinusMultiDivide() {
        MuShort muShort = new MuShort((short) 9);
        short val = 2;

        assertEquals(11, muShort.add(val));
        assertEquals(11, muShort.get());

        assertEquals(9, muShort.subtract(val));
        assertEquals(9, muShort.get());

        assertEquals(18, muShort.multiply(val));
        assertEquals(18, muShort.get());

        assertEquals(9, muShort.divide(val));
        assertEquals(9, muShort.get());
    }

    public void testIncDec() {
        MuShort muShort = new MuShort((short) 5);
        assertEquals(6, muShort.increment());
        assertEquals(6, muShort.get());

        assertEquals(5, muShort.decrement());
        assertEquals(5, muShort.get());
    }

    public void testBooleanOperations() {
        MuShort muShort = new MuShort((short) 9);
        assertEquals(-9, muShort.negate());

        muShort.set((short) 9);
        assertEquals(~9, muShort.complement());

        muShort.set((short) 6);
        assertEquals(6 | 9, muShort.or((short) 9));

        muShort.set((short) 9);
        assertEquals(9 & 6, muShort.and((short) 6));

        muShort.set((short) 6);
        assertEquals(6 ^ 9, muShort.xor((short) 9));

        muShort.set((short) 9);
        assertEquals(9 << 6, muShort.shiftLeft((short) 6));

        muShort.set((short) 6);
        assertEquals(6 >> 9, muShort.shiftRight(9));

        muShort.set((short) 9);
        assertEquals(9 >>> 6, muShort.shiftRightZero(6));
    }

    public void testCompare() {
        short lesser = 1;
        short equal = 2;
        short greater = 3;

        MuShort muShort = new MuShort(equal);

        assertTrue(muShort.compareTo(equal) == 0);
        assertTrue(muShort.compareTo(lesser) > 0);
        assertTrue(muShort.compareTo(greater) < 0);

        assertTrue(muShort.compareTo(new MuShort((short) 2)) == 0);
        assertTrue(muShort.compareTo(new MuShort((short) 5)) < 0);
        assertTrue(muShort.compareTo(new MuShort((short) 0)) > 0);
        try {
            muShort.compareTo(new String());
            fail("Expected ClassCastException");
        } catch (ClassCastException ignore) {
        }
    }

    public void testEquals() {
        MuShort muShort = new MuShort((short) 5);

        assertTrue(muShort.equals(muShort));
        assertTrue(muShort.equals(new MuShort((short) 5)));
        assertFalse(muShort.equals(null));
        assertFalse(muShort.equals(new String()));
    }

    public void testMisc() {
        MuShort muShort1 = new MuShort((short) 5);
        MuShort muShort2 = new MuShort(new Short((short) 5));

        assertTrue(muShort1.equals(muShort2));
        assertEquals("5", muShort2.toString());

        assertEquals(muShort1.hashCode(), muShort2.hashCode());
    }

    public void testSetObject() {
        MuShort muShort = new MuShort();

        muShort.setValue(new Short((short) 5));
        assertEquals(new Short((short) 5), muShort.getValue());
        assertEquals(5, muShort.get());

        muShort.setValue(new Short((short) 5));
        assertEquals(new Short((short) 5), muShort.getValue());
        assertEquals(5, muShort.get());

        muShort = new MuShort();
        muShort.set((short) 2);
        assertEquals(2, muShort.get());

        try {
            muShort.setValue(new String("5"));
            fail("Expected NotCoercibleException");
        } catch (NotCoercibleException ignore) {
        }
    }
}