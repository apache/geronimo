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
package org.apache.geronimo.naming.java;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/21 17:41:07 $
 */
public class ContextBuilderTest extends TestCase {
    private ComponentContextBuilder builder;

    public void testFreeze() {
        ReadOnlyContext context = builder.getContext();
        assertTrue(context.isFrozen());
        try {
            builder.addEnvEntry(null, null, null);
            fail();
        } catch (IllegalStateException e) {
            // ok
        } catch (NamingException e) {
            fail();
        }
        try {
            builder.addUserTransaction(null);
            fail();
        } catch (IllegalStateException e) {
            // ok
        } catch (NamingException e) {
            fail();
        }
    }

    public void testEnvEntries() throws Exception {
        String stringVal = "Hello World";
        Character charVal = new Character('H');
        Byte byteVal = new Byte((byte)12);
        Short shortVal = new Short((short)12345);
        Integer intVal = new Integer(12345678);
        Long longVal = new Long(1234567890123456L);
        Float floatVal = new Float(123.456);
        Double doubleVal = new Double(12345.6789);
        Boolean booleanVal = Boolean.TRUE;
        builder.addEnvEntry("string", String.class.getName(), stringVal);
        builder.addEnvEntry("char", Character.class.getName(), charVal.toString());
        builder.addEnvEntry("byte", Byte.class.getName(), byteVal.toString());
        builder.addEnvEntry("short", Short.class.getName(), shortVal.toString());
        builder.addEnvEntry("int", Integer.class.getName(), intVal.toString());
        builder.addEnvEntry("long", Long.class.getName(), longVal.toString());
        builder.addEnvEntry("float", Float.class.getName(), floatVal.toString());
        builder.addEnvEntry("double", Double.class.getName(), doubleVal.toString());
        builder.addEnvEntry("boolean", Boolean.class.getName(), booleanVal.toString());

        ReadOnlyContext context = builder.getContext();
        Set actual = new HashSet();
        for (NamingEnumeration e = context.listBindings("env");e.hasMore();) {
            NameClassPair pair = (NameClassPair) e.next();
            actual.add(pair.getName());
        }
        Set expected = new HashSet(Arrays.asList(new String[] {"string", "char", "byte", "short", "int", "long", "float", "double", "boolean"}));
        assertEquals(expected, actual);
        assertEquals(stringVal, context.lookup("env/string"));
        assertEquals(charVal, context.lookup("env/char"));
        assertEquals(byteVal, context.lookup("env/byte"));
        assertEquals(shortVal, context.lookup("env/short"));
        assertEquals(intVal, context.lookup("env/int"));
        assertEquals(longVal, context.lookup("env/long"));
        assertEquals(floatVal, context.lookup("env/float"));
        assertEquals(doubleVal, context.lookup("env/double"));
        assertEquals(booleanVal, context.lookup("env/boolean"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        builder = new ComponentContextBuilder();
    }
}
