/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.mavenplugins.testsuite;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

import org.apache.geronimo.genesis.MojoSupport;
import org.apache.geronimo.genesis.ant.AntHelper;

import org.apache.maven.project.MavenProject;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.taskdefs.XmlProperty;

/**
 * Create a surefire xml file by concatenating the surefire xmls of the child poms that are invoked using maven-maven-plugin:invoke
 *
 * @goal generate-surefire-xml
 *
 * @version $Rev$ $Date$
 */
public class SurefireXMLGeneratorMojo
    extends MojoSupport
{
    /**
     * @component
     */
    protected AntHelper ant;

    /**
     * @parameter default-value="${project.build.directory}/surefire-reports"
     * @read-only
     */
     private File currentReportsDirectory;

     /**
     * @parameter default-value="${project.basedir}"
     * @read-only
     */
     private File currentBaseDirectory;

     /**
     * @parameter default-value="${project.parent.basedir}/target/surefire-reports"
     * @read-only
     */
     private File parentReportsDirectory;

    //
    // MojoSupport Hooks
    //

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project = null;

    protected MavenProject getProject() {
        return project;
    }

    protected void init() throws MojoExecutionException, MojoFailureException {
        super.init();

        ant.setProject(getProject());
    }

    protected void doExecute() throws Exception {

        if (currentReportsDirectory == null) {
            log.info("No surefire-reports directory here");
            return;
        }

        String parent_tests = "0";
        String parent_skipped = "0";
        String parent_errors = "0";
        String parent_failures = "0";
        String parent_time = "0";

        String artifactName = FileUtils.filename(currentBaseDirectory.getAbsolutePath());
        parentReportsDirectory.mkdirs();
        File parentSurefireXMLFile = new File(parentReportsDirectory, "TEST-" + artifactName + ".xml");

        ArrayList xmlFiles = (ArrayList) FileUtils.getFiles(currentReportsDirectory, "TEST*.xml", null);
        for (int i=0; i < xmlFiles.size(); i++) {
            File xmlFile = (File) xmlFiles.get(i);
            log.info("Loading surefire xml for xmlproperty: " + xmlFile.getAbsolutePath());

            String prefix = String.valueOf(System.currentTimeMillis());
            loadXMLProperty(xmlFile, prefix);

            String tests = ant.getAnt().getProperty(prefix + ".testsuite.tests");
            String skipped = ant.getAnt().getProperty(prefix + ".testsuite.skipped");
            String errors = ant.getAnt().getProperty(prefix + ".testsuite.errors");
            String failures = ant.getAnt().getProperty(prefix + ".testsuite.failures");
            String time = ant.getAnt().getProperty(prefix + ".testsuite.time");
            log.debug("tests=" + tests + "; skipped=" + skipped + ", errors=" + errors + ", failures=" + failures + ", time=" + time);

            if (parentSurefireXMLFile.exists()) {
                log.info("Loading parent surefire xml for xmlproperty");
                String parentPrefix = "parent" + prefix;
                loadXMLProperty(parentSurefireXMLFile, parentPrefix);

                parent_tests = ant.getAnt().getProperty(parentPrefix + ".testsuite.tests");
                parent_skipped = ant.getAnt().getProperty(parentPrefix + ".testsuite.skipped");
                parent_errors = ant.getAnt().getProperty(parentPrefix + ".testsuite.errors");
                parent_failures = ant.getAnt().getProperty(parentPrefix + ".testsuite.failures");
                parent_time = ant.getAnt().getProperty(parentPrefix + ".testsuite.time");
                log.debug("tests=" + parent_tests + "; skipped=" + parent_skipped + ", errors=" + parent_errors + ", failures=" + parent_failures + ", time=" + parent_time);
            }

            int testsNum = Integer.parseInt(parent_tests) + Integer.parseInt(tests);
            int skippedNum = Integer.parseInt(parent_skipped) + Integer.parseInt(skipped);
            int errorsNum = Integer.parseInt(parent_errors) + Integer.parseInt(errors);
            int failuresNum = Integer.parseInt(parent_failures) + Integer.parseInt(failures);
            float timeNum = Float.parseFloat(parent_time) + Float.parseFloat(time);

            writeParentXML(testsNum,skippedNum,errorsNum,failuresNum,timeNum,artifactName,parentSurefireXMLFile);
        }
    }



    /**
     * http://ant.apache.org/manual/CoreTasks/xmlproperty.html
     */
    private void loadXMLProperty(File src, String prefix) {
        XmlProperty xmlProperty = (XmlProperty)ant.createTask("xmlproperty");
        xmlProperty.setFile(src);
        if (prefix != null) {
            xmlProperty.setPrefix(prefix);
        }
        xmlProperty.setCollapseAttributes(true);
        xmlProperty.execute();
    }


    /**
     * (over)writes the surefire xml file in the parent's surefire-reports dir
     */
    private void writeParentXML(int testsNum,int skippedNum,int errorsNum,
                                int failuresNum, float timeNum, 
                                String artifactName, File parentSurefireXMLFile ) throws IOException {

        final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        String testSuite = "<testsuite errors=\"" + errorsNum + "\" skipped=\"" + skippedNum + "\" tests=\"" + testsNum + "\" time=\"" + timeNum + "\" failures=\"" + failuresNum + "\" name=\"" + artifactName + "#.\"/>";

        String parentSurefireXMLFileName = parentSurefireXMLFile.getAbsolutePath();

        log.debug(testSuite);

        FileUtils.fileWrite(parentSurefireXMLFileName, header + "\n" + testSuite);

        return;
    }

}
