/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.configuration.condition;

import org.apache.geronimo.testsupport.TestSupport;

/**
 * Unit tests for the {@link JexlConditionParser} class.
 *
 * @version $Rev$ $Date$
 */
public class JexlConditionParserTest
    extends TestSupport
{
    private JexlConditionParser parser;

    protected void setUp() throws Exception {
        parser = new JexlConditionParser();
    }

    protected void tearDown() throws Exception {
        parser = null;
    }

    public void testTrue() throws Exception {
        boolean result = parser.evaluate("true");
        assertTrue(result);
    }

    public void testFalse() throws Exception {
        boolean result = parser.evaluate("false");
        assertTrue(!result);
    }

    public void testJavaIs1_1() throws Exception {
        //
        // Assume we never run in a 1.1 jvm
        //
        assertTrue(!SystemUtils.IS_JAVA_1_1);
        boolean result = parser.evaluate("!java.is1_1");
        assertTrue(result);
    }

    public void testJavaIs1_5() throws Exception {
        boolean result = parser.evaluate("java.is1_5 " + (SystemUtils.IS_JAVA_1_5 ? "==" : "!=") + " true");
        assertTrue(result);
    }

    public void testNonBooleanResult() throws Exception {
        try {
            parser.evaluate("java");
            fail();
        }
        catch (ConditionParserException e) {
            // expected
        }
    }

    public void testBadSyntax() throws Exception {
        try {
            parser.evaluate("a b c d e f");
            fail();
        }
        catch (ConditionParserException e) {
            // expected
        }
    }

    public void testInvalidVariableRef() throws Exception {
        try {
            parser.evaluate("nosuchvar");
            fail();
        }
        catch (ConditionParserException e) {
            // expected
        }
    }
}
