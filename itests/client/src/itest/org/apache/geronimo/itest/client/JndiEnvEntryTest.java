/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.itest.client;

import javax.naming.InitialContext;

import junit.framework.TestCase;

public class JndiEnvEntryTest extends TestCase {


    public void testLookupStringEntry() throws Exception {
        InitialContext ctx = new InitialContext();
        assertNotNull("The InitialContext is null", ctx);

        String expected = new String("1");
        String actual = (String) ctx.lookup("java:comp/env/entry/String");

        assertNotNull("The String looked up is null", actual);
        assertEquals(expected, actual);
    }

    public void testLookupDoubleEntry() throws Exception {
        InitialContext ctx = new InitialContext();
        assertNotNull("The InitialContext is null", ctx);

        Double expected = new Double(1.0D);
        Double actual = (Double) ctx.lookup("java:comp/env/entry/Double");

        assertNotNull("The Double looked up is null", actual);
        assertEquals(expected, actual);
    }

    public void testLookupLongEntry() throws Exception {
        InitialContext ctx = new InitialContext();
        assertNotNull("The InitialContext is null", ctx);

        Long expected = new Long(1L);
        Long actual = (Long) ctx.lookup("java:comp/env/entry/Long");

        assertNotNull("The Long looked up is null", actual);
        assertEquals(expected, actual);
    }

    public void testLookupFloatEntry() throws Exception {
        InitialContext ctx = new InitialContext();
        assertNotNull("The InitialContext is null", ctx);

        Float expected = new Float(1.0F);
        Float actual = (Float) ctx.lookup("java:comp/env/entry/Float");

        assertNotNull("The Float looked up is null", actual);
        assertEquals(expected, actual);
    }

    public void testLookupIntegerEntry() throws Exception {
        InitialContext ctx = new InitialContext();
        assertNotNull("The InitialContext is null", ctx);

        Integer expected = new Integer(1);
        Integer actual = (Integer) ctx.lookup("java:comp/env/entry/Integer");

        assertNotNull("The Integer looked up is null", actual);
        assertEquals(expected, actual);
    }

    public void testLookupShortEntry() throws Exception {
        InitialContext ctx = new InitialContext();
        assertNotNull("The InitialContext is null", ctx);

        Short expected = new Short((short) 1);
        Short actual = (Short) ctx.lookup("java:comp/env/entry/Short");

        assertNotNull("The Short looked up is null", actual);
        assertEquals(expected, actual);
    }

    public void testLookupBooleanEntry() throws Exception {
        InitialContext ctx = new InitialContext();
        assertNotNull("The InitialContext is null", ctx);

        Boolean expected = new Boolean(true);
        Boolean actual = (Boolean) ctx.lookup("java:comp/env/entry/Boolean");

        assertNotNull("The Boolean looked up is null", actual);
        assertEquals(expected, actual);
    }

    public void testLookupByteEntry() throws Exception {
        InitialContext ctx = new InitialContext();
        assertNotNull("The InitialContext is null", ctx);

        Byte expected = new Byte((byte) 1);
        Byte actual = (Byte) ctx.lookup("java:comp/env/entry/Byte");

        assertNotNull("The Byte looked up is null", actual);
        assertEquals(expected, actual);
    }

}
