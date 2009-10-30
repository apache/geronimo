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
package org.apache.geronimo.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.agical.rmock.extension.junit.RMockTestCase;

/**
 * @version $Rev:385659 $ $Date: 2007-03-07 14:40:07 +1100 (Wed, 07 Mar 2007) $
 */
public class AbstractCLITest extends RMockTestCase {

    private CLParser parser;
    private String[] args;
    private MockCLI mockCLI;

    @Override
    protected void setUp() throws Exception {
        parser = (CLParser) mock(CLParser.class);
        args = new String[0];
        mockCLI = new MockCLI(args, parser);
    }
    
    public void testParseExceptionReturn1() throws Exception {
        parser.parse(args);
        modify().throwException(new CLParserException("desc"));
        
        parser.displayHelp();
        startVerification();
        
        int status = mockCLI.executeMain();
        assertEquals(1, status);
    }
    
    public void testHelpOptionReturn0() throws Exception {
        parser.parse(args);

        parser.isHelp();
        modify().returnValue(true);
        parser.displayHelp();
        startVerification();
        
        int status = mockCLI.executeMain();
        assertEquals(0, status);
    }
    
    public void testCommandExecutionReturn0() throws Exception {
        parser.parse(args);

        parser.isHelp();
        startVerification();
        
        int status = mockCLI.executeMain();
        assertEquals(0, status);
    }
    
    private static class MockCLI extends AbstractCLI {

        private final CLParser parser;

        protected MockCLI(String[] args, CLParser parser) {
            super(args, new PrintStream(new ByteArrayOutputStream()));
            this.parser = parser;
        }

        @Override
        protected CLParser getCLParser() {
            return parser;
        }
        
        @Override
        protected boolean executeCommand(CLParser parser) {
            return true;
        }
        
    }
    
}
