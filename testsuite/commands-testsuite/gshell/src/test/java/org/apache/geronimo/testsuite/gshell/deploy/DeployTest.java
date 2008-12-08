/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.testsuite.gshell.deploy;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.geronimo.testsupport.commands.CommandTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class DeployTest extends CommandTestSupport {
        
    private static final String UP = "-u system -w manager";
    
    protected String execute(String[] args) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<String> cmd = new ArrayList<String>();    
        cmd.addAll(Arrays.asList("-c"));
        if (args != null) {
            cmd.addAll(Arrays.asList(args));
        }
        execute(CommandTestSupport.GSH, cmd, null, baos);
        return baos.toString();
    }
    
    @Test
    public void testListAllModules() throws Exception {
        String[] args = new String[]{ "deploy/list-modules " + UP };
 
        String output = execute(args);

        if (output.indexOf("org.apache.geronimo.framework/j2ee-system") < 0) {
            Assert.fail("deploy/list-modules failed : " + output);
        }
    }
    
    @Test
    public void testListStartedModules() throws Exception {
        String[] args = new String[]{ "deploy/list-modules " + UP + " --started" };
 
        String output = execute(args);

        if (output.indexOf("org.apache.geronimo.framework/j2ee-system") < 0) {
            Assert.fail("deploy/list-modules --started failed : " + output);
        }
        if (output.indexOf("org.apache.geronimo.configs/client-corba-yoko") > 0) {
            Assert.fail("deploy/list-modules --started failed : " + output);
        }
    }
    
    @Test
    public void testListStoppedModules() throws Exception {
        String[] args = new String[]{ "deploy/list-modules " + UP + " --stopped" };
 
        String output = execute(args);

        if (output.indexOf("org.apache.geronimo.framework/j2ee-system") > 0) {
            Assert.fail("deploy/list-modules --stopped failed : " + output);
        }
        if (output.indexOf("org.apache.geronimo.configs/client-corba-yoko") < 0) {
            Assert.fail("deploy/list-modules --stopped failed : " + output);
        }
    }
    
    public void testListAllPlugins() throws Exception {
        //todo this testcase fails due to needing to select a repo
	/*
        String[] args = new String[]{"deploy/list-plugins " + UP};
 
        String output = execute(args);

        if (output.indexOf("org.apache.geronimo.framework/j2ee-system") < 0) {
                Assert.fail("deploy/list-modules failed : " + output);
        }
	*/
    }

}
