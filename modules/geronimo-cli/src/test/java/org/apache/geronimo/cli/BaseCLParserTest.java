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

import junit.framework.TestCase;

import org.apache.geronimo.cli.CLParserException;

/**
 * @version $Rev:385659 $ $Date: 2007-03-07 14:40:07 +1100 (Wed, 07 Mar 2007) $
 */
public class BaseCLParserTest extends TestCase {

    private CLParser parser;

    @Override
    protected void setUp() throws Exception {
        parser = new BaseCLParser(System.out);
    }
    
    public void testHelp() throws Exception {
        parser.parse(new String[] {"--help"});
        assertTrue(parser.isHelp());
        
        parser.parse(new String[] {"-h"});
        assertTrue(parser.isHelp());
    }

    public void testVerbose() throws Exception {
        parser.parse(new String[0]);
        assertFalse(parser.isVerboseInfo());
        
        parser.parse(new String[] {"--verbose"});
        assertTrue(parser.isVerboseInfo());
        
        parser.parse(new String[] {"-v"});
        assertTrue(parser.isVerboseInfo());
    }
    
    public void testVeryVerbose() throws Exception {
        parser.parse(new String[0]);
        assertFalse(parser.isVerboseDebug());
        
        parser.parse(new String[] {"--veryverbose"});
        assertTrue(parser.isVerboseDebug());
        
        parser.parse(new String[] {"-vv"});
        assertTrue(parser.isVerboseDebug());
    }
    
    public void testVeryVeryVerbose() throws Exception {
        parser.parse(new String[0]);
        assertFalse(parser.isVerboseDebug());
        
        parser.parse(new String[] {"--veryveryverbose"});
        assertTrue(parser.isVerboseTrace());
        
        parser.parse(new String[] {"-vvv"});
        assertTrue(parser.isVerboseTrace());
    }
    
    public void testMultiVerboseNotAllowed() throws Exception {
        try {
            parser.parse(new String[] {"-v", "-vv"});
            fail();
        } catch (CLParserException e) {
        }
        
        try {
            parser.parse(new String[] {"-v", "-vvv"});
            fail();
        } catch (CLParserException e) {
        }
        
        try {
            parser.parse(new String[] {"-vv", "-vvv"});
            fail();
        } catch (CLParserException e) {
        }
    }
    
}
