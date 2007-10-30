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
package org.apache.geronimo.cli.client;

import org.apache.geronimo.cli.CLParserException;

import junit.framework.TestCase;

/**
 * @version $Rev:385659 $ $Date: 2007-03-07 14:40:07 +1100 (Wed, 07 Mar 2007) $
 */
public class ClientCLParserTest extends TestCase {

    private ClientCLParser parser;

    @Override
    protected void setUp() throws Exception {
        parser = new ClientCLParser(System.out);
    }
    
    public void testNoApplicationClientConfigurationFails() throws Exception {
        try {
            parser.parse(new String[] {"-v"});
            fail();
        } catch (CLParserException e) {
        }
    }
    
    public void testGetApplicationClientConfiguration() throws Exception {
        String configName = "configName";
        parser.parse(new String[] {"-v", configName, "arg1", "arg2"});
        assertEquals(configName, parser.getApplicationClientConfiguration());
    }

    public void testGetApplicationClientArgs() throws Exception {
        String arg1 = "arg1";
        String arg2 = "arg2";
        parser.parse(new String[] {"-v", "configName", arg1, arg2});
        String[] args = parser.getApplicationClientArgs();
        assertEquals(2, args.length);
        assertEquals(arg1, args[0]);
        assertEquals(arg2, args[1]);
    }
    
}
