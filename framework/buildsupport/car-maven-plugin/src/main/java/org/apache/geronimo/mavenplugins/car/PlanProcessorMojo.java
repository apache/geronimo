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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

//
// TODO: Rename to PreparePlanMojo
//

/**
 * Add module id and dependencies to a plan and process with velocity
 *
 * @version $Rev$ $Date$
 * @goal prepare-plan
 * @requiresDependencyResolution runtime
 */
public class PlanProcessorMojo
        extends AbstractCarMojo {
    private static final String ENVIRONMENT_LOCAL_NAME = "environment";

    private static final QName ENVIRONMENT_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "environment");

    /**
     * Location of unproccesed plan, normally missing moduleId and dependencies.
     *
     * @parameter expression="${basedir}/src/main/plan/plan.xml"
     * @required
     */
    protected File sourceFile = null;

    /**
     * Directory to put the processed plan in.
     *
     * @parameter expression="${project.build.directory}/work"
     * @required
     */
    protected File targetDir = null;

    /**
     * XXX
     *
     * @parameter expression="${project.build.directory}/work/plan.xml"
     * @required
     */
    protected File targetFile = null;

    /**
     * Dependencies explicitly listed in the car-maven-plugin configuration
     *
     * @parameter
     */
    private List<Dependency> dependencies = Collections.emptyList();

    /**
     * Configuration of use of maven dependencies.  If missing or if value element is false, use the explicit list in the car-maven-plugin configuration.
     * If present and true, use the maven dependencies in the current pom file of scope null, runtime, or compile.  In addition, the version of the maven
     * dependency can be included or not depending on the includeVersion element.
     *
     * @parameter
     */
    UseMavenDependencies useMavenDependencies = new UseMavenDependencies(true, false, true);

    /**
     * we copy the plan here for filtering, then add env stuff.
     *
     * @parameter expression="${project.build.directory}/work/filteredplan.xml"
     * @required
     */
    protected File filteredPlanFile;

    /**
     * whether this is a boot bundle (starts kernel, and the config) or a normal plugin bundle.
     * @parameter
     */
    private boolean boot;


    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!sourceFile.exists()) {
            getLog().info("No plan found, plugin will have no classloader");
            return;
        }
        try {
            filter(sourceFile, filteredPlanFile);

            XmlObject doc = XmlObject.Factory.parse(filteredPlanFile);
            XmlCursor xmlCursor = doc.newCursor();
            LinkedHashSet<org.apache.geronimo.kernel.repository.Dependency> dependencies = toKernelDependencies(useMavenDependencies);
            Artifact configId = new Artifact(project.getGroupId(), project.getArtifactId(), project.getVersion(), "car");

            try {
                mergeEnvironment(xmlCursor, configId, dependencies);

                if (targetDir.exists()) {
                    if (!targetDir.isDirectory()) {
                        throw new RuntimeException("TargetDir: " + this.targetDir + " exists and is not a directory");
                    }
                } else {
                    targetDir.mkdirs();
                }

                XmlOptions xmlOptions = new XmlOptions();
                xmlOptions.setSavePrettyPrint();
                doc.save(targetFile, xmlOptions);

                if (getLog() != null) {
                    getLog().info("Generated: " + targetFile);
                }
            }
            finally {
                xmlCursor.dispose();
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Could not process plan", e);
        }
    }

    void mergeEnvironment(final XmlCursor xmlCursor, final Artifact configId, final LinkedHashSet<org.apache.geronimo.kernel.repository.Dependency> dependencies) {
        moveToFirstStartElement(xmlCursor);

        boolean atLeastOneChild = xmlCursor.toFirstChild();
        if (!atLeastOneChild) {
            // this is an empty element. Move to EndToken such XmlCursor.beginElement inserts an element inside it.
            xmlCursor.toEndToken();
        }
        QName childName = xmlCursor.getName();
        Environment oldEnvironment;

        if (childName != null && childName.getLocalPart().equals(ENVIRONMENT_LOCAL_NAME)) {
            convertElement(xmlCursor, ENVIRONMENT_QNAME.getNamespaceURI());
            XmlObject xmlObject = xmlCursor.getObject();
            EnvironmentType environmentType = (EnvironmentType) xmlObject.copy().changeType(EnvironmentType.type);
            oldEnvironment = EnvironmentBuilder.buildEnvironment(environmentType);
            xmlCursor.removeXml();
        } else {
            oldEnvironment = new Environment();
        }

        Environment newEnvironment = new Environment();
        newEnvironment.setConfigId(configId);
        newEnvironment.setDependencies(dependencies);

        EnvironmentBuilder.mergeEnvironments(oldEnvironment, newEnvironment);
        EnvironmentType environmentType = EnvironmentBuilder.buildEnvironmentType(oldEnvironment);

        xmlCursor.beginElement(ENVIRONMENT_QNAME);
        XmlCursor element = environmentType.newCursor();

        try {
            element.copyXmlContents(xmlCursor);
        }
        finally {
            element.dispose();
        }
    }

    private void moveToFirstStartElement(XmlCursor xmlCursor) throws AssertionError {
        xmlCursor.toStartDoc();
        xmlCursor.toFirstChild();
        while (!xmlCursor.currentTokenType().isStart()) {
            if (!xmlCursor.toNextSibling()) {
                break;
            }
        }
        if (!xmlCursor.currentTokenType().isStart()) {
            throw new AssertionError("Cannot find first start element");
        }
    }

    private void convertElement(final XmlCursor cursor, final String namespace) {
        cursor.push();
        XmlCursor end = cursor.newCursor();

        try {
            end.toCursor(cursor);
            end.toEndToken();

            while (cursor.hasNextToken() && cursor.isLeftOf(end)) {
                if (cursor.isStart()) {
                    if (!namespace.equals(cursor.getName().getNamespaceURI())) {
                        cursor.setName(new QName(namespace, cursor.getName().getLocalPart()));
                    }
                }

                cursor.toNextToken();
            }

            cursor.pop();
        }
        finally {
            end.dispose();
        }
    }

    protected LinkedHashSet<org.apache.geronimo.kernel.repository.Dependency> toKernelDependencies(UseMavenDependencies useMavenDependencies) throws InvalidDependencyVersionException, ArtifactResolutionException, ProjectBuildingException, MojoExecutionException {
        LinkedHashSet<org.apache.geronimo.kernel.repository.Dependency> kernelDependencies = new LinkedHashSet<org.apache.geronimo.kernel.repository.Dependency>();
        LinkedHashSet<DependencyType> dependencies = toDependencies(this.dependencies, useMavenDependencies, true);
        for (DependencyType dependency: dependencies) {
            kernelDependencies.add(Dependency.toKernelDependency(dependency));
        }
        return kernelDependencies;
    }


    interface Inserter {
        ArtifactType insert(EnvironmentType environmentType);
    }
}
