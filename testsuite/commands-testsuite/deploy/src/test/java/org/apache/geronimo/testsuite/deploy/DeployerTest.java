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

package org.apache.geronimo.testsuite.deploy;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.geronimo.testsupport.commands.CommandTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class DeployerTest extends CommandTestSupport {

    protected String execute(String[] args) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<String> cmd = new ArrayList<String>();
        cmd.addAll(Arrays.asList("--user", "system", "--password", "manager"));
        if (args != null) {
            cmd.addAll(Arrays.asList(args));
        }
        execute(CommandTestSupport.DEPLOY, cmd, null, baos);
        return baos.toString();
    }
        
    @Test
    public void testListAllModules() throws Exception {
        String[] args = new String[]{ "list-modules" };
 
        String output = execute(args);

        if (output.indexOf(getDefaultStartedModuleName()) < 0) {
            Assert.fail("list-modules failed : " + output);
        }
    }
    
    @Test
    public void testListStartedModules() throws Exception {
        String[] args = new String[]{ "list-modules", "--started" };
 
        String output = execute(args);

        if (output.indexOf(getDefaultStartedModuleName()) < 0) {
            Assert.fail("list-modules failed : " + output);
        }
        if (output.indexOf(getDefaultStoppedModuleName()) > 0) {
            Assert.fail("list-modules failed : " + output);
        }
    }
    
    @Test
    public void testListStoppedModules() throws Exception {
        String[] args = new String[]{ "list-modules", "--stopped" };
 
        String output = execute(args);

        if (output.indexOf(getDefaultStartedModuleName()) > 0) {
            Assert.fail("deploy/list-modules failed : " + output);
        }
        if (output.indexOf(getDefaultStoppedModuleName()) < 0) {
            Assert.fail("deploy/list-modules failed : " + output);
        }
    }
    
    @Test
    public void testListTargets() throws Exception {
        String[] args = new String[]{ "list-targets" };

        String output = execute(args);

        if (output.indexOf("j2eeType=ConfigurationStore,name=Local") < 0) {
            Assert.fail("deploy/list-targets failed : " + output);
        }
    }

    protected String getDefaultStartedModuleName() {
        return "org.apache.geronimo.framework/rmi-naming";
    }
    
    protected String getDefaultStoppedModuleName() {
        return "org.apache.geronimo.framework/offline-deployer";
    }
    
}
