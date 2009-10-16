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
package org.apache.geronimo.cli.daemon;

import junit.framework.TestCase;

import org.apache.geronimo.cli.CLParserException;

/**
 * @version $Rev:385659 $ $Date: 2007-03-07 14:40:07 +1100 (Wed, 07 Mar 2007) $
 */
public class DaemonCLParserTest extends TestCase {

    private DaemonCLParser parser;

    @Override
    protected void setUp() throws Exception {
        parser = new DaemonCLParser(System.out);
    }
    
    public void testOverride() throws Exception {
        parser.parse(new String[0]);
        String[] override = parser.getOverride();
        assertNull(override);
        
        parser.parse(new String[] {"-o", "module1", "module2"});
        override = parser.getOverride();
        assertEquals(2, override.length);
        
        parser.parse(new String[] {"--override", "module1", "module2"});
        override = parser.getOverride();
        assertEquals(2, override.length);
    }

    public void testLong() throws Exception {
        parser.parse(new String[0]);
        assertFalse(parser.isLongProgress());

        parser.parse(new String[] {"--long"});
        assertTrue(parser.isLongProgress());
        
        parser.parse(new String[] {"-l"});
        assertTrue(parser.isLongProgress());
    }
    
    public void testQuiet() throws Exception {
        parser.parse(new String[0]);
        assertFalse(parser.isNoProgress());
        
        parser.parse(new String[] {"--quiet"});
        assertTrue(parser.isNoProgress());
        
        parser.parse(new String[] {"-q"});
        assertTrue(parser.isNoProgress());
    }
    
    public void testQuietAndLongNotAllowed() throws Exception {
        try {
            parser.parse(new String[] {"--quiet", "--long"});
            fail();
        } catch (CLParserException e) {
        }
    }
    
}
