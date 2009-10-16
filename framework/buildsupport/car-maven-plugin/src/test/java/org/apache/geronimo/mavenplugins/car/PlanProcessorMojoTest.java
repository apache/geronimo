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
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.apache.maven.shared.filtering.DefaultMavenResourcesFiltering;
import org.apache.maven.shared.filtering.DefaultMavenFileFilter;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @version $Rev$ $Date$
 */
public class PlanProcessorMojoTest extends PlexusTestCase {

    private PlanProcessorMojo processorMojo;
    private File sourceDir;
    private File filteredPlanDir;

    protected void setUp() throws Exception {
        super.setUp();
        processorMojo = new PlanProcessorMojo();
        processorMojo.useMavenDependencies = new UseMavenDependencies(false, false, false);
        Model model = new Model();
        MavenProject mavenProject = new MavenProject(model);
        mavenProject.setGroupId("dummy-group");
        mavenProject.setArtifactId("dummy-artifact-id");
        mavenProject.setVersion("dummy-version");
        processorMojo.project = mavenProject;
        File basedir = getBaseDir();
        sourceDir = new File(basedir, "src/test/resources");
        processorMojo.targetDir = new File(basedir, "target/PlanProcessorMojoTest");
        filteredPlanDir = new File(processorMojo.targetDir, "filteredPlan");
        processorMojo.mavenFileFilter = (MavenFileFilter) lookup( MavenFileFilter.class.getName(), "default" );
    }
    
    public void testEmptyPlanProcessing() throws Exception {
        String planName = "empty-plan.xml";
        processorMojo.sourceFile = new File(sourceDir, planName);
        processorMojo.filteredPlanFile = new File(filteredPlanDir, planName);
        processorMojo.targetFile = new File(processorMojo.targetDir, "actual-" + planName);
        
        processorMojo.execute();
        
        assertResultingPlan(planName);
    }

    public void testNoEnvironmentPlanProcessing() throws Exception {
        String planName = "no-env-plan.xml";
        processorMojo.sourceFile = new File(sourceDir, planName);
        processorMojo.filteredPlanFile = new File(filteredPlanDir, planName);
        processorMojo.targetFile = new File(processorMojo.targetDir, "actual-" + planName);
        
        processorMojo.execute();
        
        assertResultingPlan(planName);
    }

    private void assertResultingPlan(String planName) throws Exception {
        InputStream expectedIn = new FileInputStream(new File(sourceDir, "expected-" + planName));
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

    protected final File getBaseDir() {
        File dir;

        // If ${basedir} is set, then honor it
        String tmp = System.getProperty("basedir");
        if (tmp != null) {
            dir = new File(tmp);
        }
        else {
            // Find the directory which this class (or really the sub-class of TestSupport) is defined in.
            String path = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();

            // We expect the file to be in target/test-classes, so go up 2 dirs
            dir = new File(path).getParentFile().getParentFile();

            // Set ${basedir} which is needed by logging to initialize
            System.setProperty("basedir", dir.getPath());
        }

        // System.err.println("Base Directory: " + dir);

        return dir;
    }

}
