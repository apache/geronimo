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

package org.apache.geronimo.testsuite.jaxws;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.geronimo.testsupport.commands.CommandTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class JAXWSToolsTest extends CommandTestSupport {

    protected String executeWsGen(String[] args) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<String> cmd = new ArrayList<String>();
        cmd.addAll(Arrays.asList("wsgen"));
        if (args != null) {
            cmd.addAll(Arrays.asList(args));
        }
        execute("jaxws-tools", cmd, null, baos);
        return baos.toString();
    }
    
    protected String executeWsImport(String[] args) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<String> cmd = new ArrayList<String>();
        cmd.addAll(Arrays.asList("wsimport"));
        if (args != null) {
            cmd.addAll(Arrays.asList(args));
        }
        execute("jaxws-tools", cmd, null, baos);
        return baos.toString();
    }
        
    @Test
    public void testWsGen() throws Exception {
        String projectDir = System.getProperty("project.directory");
        String targetDir = System.getProperty("project.build.directory");
        
        File outputDir = createUniqueDirectory(targetDir, "wsgen-");
        File testClassesDir = new File(projectDir, "test-classes");
        
        String[] args = new String[]{ "-verbose", "-keep", "-d", outputDir.getAbsolutePath(), "-cp", testClassesDir.getAbsolutePath(), "org.apache.geronimo.testsuite.jaxws.Greeter" };
 
        String output = executeWsGen(args);
     
        checkGeneratedFiles(outputDir, output);
    }
    
    @Test
    public void testWsImport() throws Exception {
        String projectDir = System.getProperty("project.directory");
        String targetDir = System.getProperty("project.build.directory");
        
        File outputDir = createUniqueDirectory(targetDir, "wsimport-");        
        File wsdlFile = new File(projectDir, "../src/test/java/org/apache/geronimo/testsuite/jaxws/greeter_control.wsdl");
        
        String[] args = new String[]{ "-verbose", "-keep", "-d", outputDir.getAbsolutePath(), wsdlFile.getAbsolutePath() };
        
        String output = executeWsImport(args);

        checkGeneratedFiles(outputDir, output);
    }

    private void checkGeneratedFiles(File outputDir, String output) {
        String packageName = "org.apache.greeter_control.types.";
        packageName = packageName.replace('.', File.separatorChar);
        List<String> expectedFiles = Arrays.asList("GreetMe.java",  "GreetMeOneWay.java", "GreetMeResponse.java", "SayHi.java", "SayHiResponse.java");
                                      
        for (String file : expectedFiles) {
            file = packageName + file;
            Assert.assertTrue(output.indexOf(file) > 0, "Expected " + file + " in " + output);
            File sourceFile = new File(outputDir, file);
            Assert.assertTrue(sourceFile.exists(), "Source file " + file + " does not exist");
        }
    }
    
    private File createUniqueDirectory(String targetDir, String prefix) {
        File dir = new File(targetDir, prefix + System.currentTimeMillis());
        if (!dir.mkdir()) {
            throw new RuntimeException("Failed to create unqiue directory " + dir);
        }
        return dir;
    }
               
}
