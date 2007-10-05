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

package org.apache.geronimo.mavenplugins.testsuite;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

import org.codehaus.mojo.pluginsupport.MojoSupport;
import org.codehaus.mojo.pluginsupport.ant.AntHelper;

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
     * @parameter default-value="${project.build.directory}"
     * @read-only
     */
    private File currentBuildDirectory;

    private File currentReportsDirectory;

    /**
     * @parameter default-value="${project.basedir}"
     * @read-only
     */
    private File currentBaseDirectory;

    /**
     * @parameter default-value="${project.parent.basedir}/target"
     * @read-only
     */
    private File parentBuildDirectory;

    private File parentReportsDirectory;

    /**
     * Sometimes it is necessary to generate the surefire xml in a grand parent project because
     * the current pom was not invoked by the maven-maven-plugin by a parent project.
     * Such a parent project whose packaging is set to pom will not transfer the surefire data to it's parent.
     * So we will directly write to the grandparent's surefire-reports dir.
     * 
     * @parameter default-value="false"
     */
    private boolean grandParent;

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

    protected MavenProject getProject()
    {
        return project;
    }

    protected void init() throws MojoExecutionException, MojoFailureException {
        super.init();

        ant.setProject(getProject());

        currentReportsDirectory = new File(currentBuildDirectory, "surefire-reports");
        if (grandParent) {
            log.debug(getProject().getParent().getParent().getArtifactId() + " -- " + getProject().getParent().getParent().getBasedir().getAbsolutePath());
            parentBuildDirectory = new File(getProject().getParent().getParent().getBasedir(), "target");
        }
        parentReportsDirectory = new File(parentBuildDirectory, "surefire-reports");
        log.info("Updating directory: " + parentReportsDirectory.getAbsolutePath() );
    }

    protected void doExecute() throws Exception {
        if ( !currentReportsDirectory.exists() )
        {
            log.info("No surefire-reports directory here");
            return;
        }

        String parent_tests = "0";
        String parent_skipped = "0";
        String parent_errors = "0";
        String parent_failures = "0";
        String parent_time = "0";

        String artifactName = FileUtils.filename(currentBaseDirectory.getAbsolutePath());
        if ( !parentReportsDirectory.exists() )
        {
            parentReportsDirectory.mkdirs();
        }
        File parentSurefireXMLFile = new File(parentReportsDirectory, "TEST-" + artifactName + ".xml");
        if ( parentSurefireXMLFile.exists() )
        {
            parentSurefireXMLFile.delete();
        }

        if (grandParent) {
            artifactName = project.getParent().getBasedir().getName() + "@" + artifactName;
        }

        ArrayList xmlFiles = (ArrayList) FileUtils.getFiles(currentReportsDirectory, "TEST*.xml", null);
        for ( int i=0; i < xmlFiles.size(); i++ )
        {
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

            if ( parentSurefireXMLFile.exists() )
            {
                log.info("Loading parent surefire xml for xmlproperty: " + parentSurefireXMLFile.getAbsolutePath() );
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
            float timeNum = getTime(parent_time) + getTime(time);

            writeParentXML(testsNum,skippedNum,errorsNum,failuresNum,timeNum,artifactName,parentSurefireXMLFile);
        }
    }

    private float getTime(String time) {
        time = time.replaceAll(",","");
        return Float.parseFloat(time);
    }

    /**
     * http://ant.apache.org/manual/CoreTasks/xmlproperty.html
     */
    private void loadXMLProperty(File src, String prefix)
    {
        XmlProperty xmlProperty = (XmlProperty)ant.createTask("xmlproperty");
        xmlProperty.setFile(src);
        if ( prefix != null )
        {
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
