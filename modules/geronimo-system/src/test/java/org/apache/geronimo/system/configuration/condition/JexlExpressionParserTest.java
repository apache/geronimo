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
 * Unit tests for the {@link JexlExpressionParser} class.
 *
 * @version $Rev$ $Date$
 */
public class JexlExpressionParserTest
        extends TestSupport
{
    protected JexlExpressionParser parser;
    
    /**
     * Set up instance variables required by this test case.
     */
    protected void setUp() {
        parser = new JexlExpressionParser();
    }
    
    /**
     * Tear down instance variables required by this test case.
     */
    protected void tearDown() {
        parser = null;
    }
    
    
    public void testDefault() throws Exception {
        String value = "${java.home}";
        String result = parser.parse(value);
        assertEquals(result, System.getProperty("java.home"));
    }
    
    public void testSubst() throws Exception {
        String value = "BEFORE${java.home}AFTER";
        String result = parser.parse(value);
        assertEquals(result, "BEFORE" + System.getProperty("java.home") + "AFTER");
    }
    
    public void testVariable() throws Exception {
        String myvar = "this is my variable";
        parser.getVariables().put("my.var", myvar);
        
        String value = "${my.var}";
        String result = parser.parse(value);
        assertEquals(result, myvar);
    }
    
    public void testFlatVariable() throws Exception {
        String myvar = "this is my variable";
        parser.getVariables().put("my.var", myvar);
        parser.getVariables().put("my", "not used");
        
        String value = "${my.var}";
        String result = parser.parse(value);
        assertEquals(result, myvar);
    }
    
    public void testSyntax() throws Exception {
        String value = "${java.home";
            
        try {
            String result = parser.parse(value);
            assertTrue("Should have thrown an exception", false);
        }
        catch (Exception ignore) {}
    }
}
