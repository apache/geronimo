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

package org.apache.geronimo.mavenplugins.car;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.geronimo.testsupport.TestSupport;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;

/**
 * @version $Rev$ $Date$
 */
public class PlanProcessorMojoTest extends TestSupport {

    private PlanProcessorMojo processorMojo;

    protected void setUp() throws Exception {
        processorMojo = new PlanProcessorMojoTester();
        Model model = new Model();
        MavenProject mavenProject = new MavenProject(model);
        mavenProject.setGroupId("dummy-group");
        mavenProject.setArtifactId("dummy-artifact-id");
        mavenProject.setVersion("dummy-version");
        processorMojo.project = mavenProject;
        processorMojo.sourceDir = new File(BASEDIR, "src/test/resources");
        processorMojo.targetDir = new File(BASEDIR, "target/PlanProcessorMojoTest");
    }
    
    public void testEmptyPlanProcessing() throws Exception {
        String planName = "empty-plan.xml";
        processorMojo.planFileName = planName;
        processorMojo.targetFile = new File(processorMojo.targetDir, "actual-" + planName);
        
        processorMojo.doExecute();
        
        assertResultingPlan(planName);
    }

    public void testNoEnvironmentPlanProcessing() throws Exception {
        String planName = "no-env-plan.xml";
        processorMojo.planFileName = planName;
        processorMojo.targetFile = new File(processorMojo.targetDir, "actual-" + planName);
        
        processorMojo.doExecute();
        
        assertResultingPlan(planName);
    }

    private void assertResultingPlan(String planName) throws Exception {
        InputStream expectedIn = new FileInputStream(new File(processorMojo.sourceDir, "expected-" + planName));
        InputStream actualIn = new FileInputStream(new File(processorMojo.targetDir, "actual-" + planName));
        
        int read;
        while (-1 == (read = expectedIn.read())) {
            int actualRead = actualIn.read();
            if (-1 == actualRead) {
                fail();
            }
            assertEquals(read, actualRead);
        }
    }
    
    private class PlanProcessorMojoTester extends PlanProcessorMojo {
        public PlanProcessorMojoTester() {
            log = new SystemStreamLog();
        }
    }
}
