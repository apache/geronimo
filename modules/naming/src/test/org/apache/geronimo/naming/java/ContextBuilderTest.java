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
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:57 $
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
