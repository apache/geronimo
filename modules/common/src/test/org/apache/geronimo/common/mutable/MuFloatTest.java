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
 * Unit test for {@link MuFloat} class.
 *
 * @version $Revision: 1.2 $ $Date: 2003/09/03 18:00:17 $
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
