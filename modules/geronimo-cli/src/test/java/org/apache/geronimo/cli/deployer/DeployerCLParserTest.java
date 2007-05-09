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
package org.apache.geronimo.cli.deployer;

import org.apache.geronimo.cli.CLParserException;

import junit.framework.TestCase;

/**
 * @version $Rev:385659 $ $Date: 2007-03-07 14:40:07 +1100 (Wed, 07 Mar 2007) $
 */
public class DeployerCLParserTest extends TestCase {

    private DeployerCLParser parser;

    @Override
    protected void setUp() throws Exception {
        parser = new DeployerCLParser(System.out);
    }

    public void testNoOptionsSet() throws Exception {
        String moduleId = "modulID";
        parser.parse(new String[] {"start", moduleId});
        assertNull(parser.getURI());
        assertNull(parser.getHost());
        assertNull(parser.getPort());
        assertNull(parser.getDriver());
        assertNull(parser.getUser());
        assertNull(parser.getPassword());
        assertFalse(parser.isSyserr());
        assertFalse(parser.isOffline());
        
        assertSame(StartCommandMetaData.META_DATA, parser.getCommandMetaData());
        CommandArgs commandArgs = parser.getCommandArgs();
        assertEquals(1, commandArgs.getArgs().length);
        assertEquals(moduleId, commandArgs.getArgs()[0]);
    }

    public void testURI() throws Exception {
        String uri = "uri";
        parser.parse(new String[] {"-U", uri, "start", "dummyID"});
        assertEquals(uri, parser.getURI());
        
        parser.parse(new String[] {"--uri", uri, "start", "dummyID"});
        assertEquals(uri, parser.getURI());
    }

    public void testHost() throws Exception {
        String host = "host";
        parser.parse(new String[] {"--host", host, "start", "dummyID"});
        assertEquals(host, parser.getHost());
    }
    
    public void testPort() throws Exception {
        String port = "1";
        parser.parse(new String[] {"--port", port, "start", "dummyID"});
        assertEquals(new Integer(1), parser.getPort());
        
        try {
            parser.parse(new String[] {"--port", "notAnInteger", "start", "dummyID"});
            fail();
        } catch (CLParserException e) {
        }
    }
    
    public void testDriver() throws Exception {
        String driver = "driver.jar";
        parser.parse(new String[] {"--driver", driver, "start", "dummyID"});
        assertEquals(driver, parser.getDriver());

        parser.parse(new String[] {"-d", driver, "start", "dummyID"});
        assertEquals(driver, parser.getDriver());
    }
    
    public void testUser() throws Exception {
        String user = "username";
        parser.parse(new String[] {"--user", user, "start", "dummyID"});
        assertEquals(user, parser.getUser());

        parser.parse(new String[] {"-u", user, "start", "dummyID"});
        assertEquals(user, parser.getUser());
    }
    
    public void testPassword() throws Exception {
        String password = "password";
        parser.parse(new String[] {"--password", password, "start", "dummyID"});
        assertEquals(password, parser.getPassword());

        parser.parse(new String[] {"-p", password, "start", "dummyID"});
        assertEquals(password, parser.getPassword());
    }
    
    public void testSyserr() throws Exception {
        parser.parse(new String[] {"--syserr", "start", "dummyID"});
        assertTrue(parser.isSyserr());

        parser.parse(new String[] {"-s", "start", "dummyID"});
        assertTrue(parser.isSyserr());
    }

    public void testOffline() throws Exception {
        parser.parse(new String[] {"--offline", "start", "dummyID"});
        assertTrue(parser.isOffline());

        parser.parse(new String[] {"-o", "start", "dummyID"});
        assertTrue(parser.isOffline());
    }

}
