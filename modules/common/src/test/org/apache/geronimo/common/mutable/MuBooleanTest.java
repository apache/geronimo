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

/**
 * Unit test for {@link MuBoolean} class.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/30 20:59:16 $
 */
public class MuBooleanTest
    extends TestCase
{
    public void testDefaultConstructor() {
        MuBoolean mutable = new MuBoolean();
        assertFalse(mutable.booleanValue());
    }
    
    public void testPrimativeConstructorTrue() {
        MuBoolean mutable = new MuBoolean(true);
        assertTrue(mutable.booleanValue());
    }
    
    public void testPrimativeConstructorFalse() {
        MuBoolean mutable = new MuBoolean(false);
        assertFalse(mutable.booleanValue());
    }
    
    public void testObjectConstructorNull() {
        MuBoolean mutable = new MuBoolean(null);
        assertFalse(mutable.booleanValue());
    }
    
    public void testObjectConstructorMuBoolean() {
        MuBoolean control = new MuBoolean(true);
        MuBoolean mutable = new MuBoolean(control);
        assertEquals(mutable.booleanValue(), control.booleanValue());
    }
    
    public void testObjectConstructorBoolean() {
        Boolean control = Boolean.TRUE;
        MuBoolean mutable = new MuBoolean(control);
        assertEquals(mutable.booleanValue(), control.booleanValue());
    }
    
    public void testObjectConstructorObject() {
        Object control = new byte[0];
        MuBoolean mutable = new MuBoolean(control);
        assertTrue(mutable.booleanValue());
    }
    
    public void testStringConstructor() {
        MuBoolean mutable = new MuBoolean("TRUE");
        assertTrue(mutable.booleanValue());
    }
    
    public void testSetPrimative() {
        MuBoolean first = new MuBoolean(false);
        assertFalse(first.booleanValue());
        boolean old = first.set(true);
        assertFalse(old);
        assertTrue(first.booleanValue());
    }
    
    public void testSetBoolean() {
        MuBoolean first = new MuBoolean(Boolean.FALSE);
        assertFalse(first.booleanValue());
        boolean old = first.set(Boolean.TRUE);
        assertFalse(old);
        assertTrue(first.booleanValue());
    }
    
    public void testSetMuBoolean() {
        MuBoolean first = new MuBoolean(new MuBoolean(false));
        assertFalse(first.booleanValue());
        boolean old = first.set(new MuBoolean(true));
        assertFalse(old);
        assertTrue(first.booleanValue());
    }
    
    public void testGet() {
        MuBoolean mutable = new MuBoolean(true);
        assertTrue(mutable.get());
        mutable.set(false);
        assertFalse(mutable.get());
    }
    
    public void testCommit() {
        MuBoolean mutable = new MuBoolean(true);
        boolean success = mutable.commit(false, true);
        assertFalse(success);
        assertTrue(mutable.booleanValue());
        
        success = mutable.commit(true, false);
        assertTrue(success);
        assertFalse(mutable.booleanValue());
    }
    
    public void testSwap() {
        MuBoolean t = new MuBoolean(true);
        MuBoolean f = new MuBoolean(false);
        
        boolean newT = t.swap(f);
        assertFalse(newT);
        assertFalse(t.booleanValue());
        assertTrue(f.booleanValue());
    }
    
    public void testComplement() {
        MuBoolean t = new MuBoolean(true);
        boolean c = t.complement();
        assertFalse(c);
        assertFalse(t.booleanValue());
    }
    
    public void testAnd() {
        MuBoolean t = new MuBoolean(true);
        boolean and = t.and(true);
        assertTrue(and);
        assertTrue(t.booleanValue());
        
        and = t.and(false);
        assertFalse(and);
        assertFalse(t.booleanValue());
    }
    
    public void testOr() {
        MuBoolean t = new MuBoolean(false);
        
        boolean or = t.or(false);
        assertFalse(or);
        assertFalse(t.booleanValue());

        or = t.or(true);
        assertTrue(or);
        assertTrue(t.booleanValue());
    }
    
    public void testXor() {
        MuBoolean t = new MuBoolean(false);

        boolean xor = t.xor(false);
        assertFalse(xor);
        assertFalse(t.booleanValue());

        xor = t.xor(true);
        assertTrue(xor);
        assertTrue(t.booleanValue());
    }
}
