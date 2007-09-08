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
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
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
 */
public class PlanProcessorMojo
        extends AbstractCarMojo {
    private static final String ENVIRONMENT_LOCAL_NAME = "environment";

    private static final QName ENVIRONMENT_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "environment");

    /**
     * Location of unproccesed plan, normally missing moduleId and dependencies.
     *
     * @parameter expression="${basedir}/src/main/plan"
     * @required
     */
    protected File sourceDir = null;

    /**
     * Directory to put the processed plan in.
     *
     * @parameter expression="${project.build.directory}/resources/META-INF"
     * @required
     */
    protected File targetDir = null;

    /**
     * Name of the unprocessed source and processed target plan file.
     *
     * @parameter default-value="plan.xml"
     * @required
     */
    protected String planFileName = null;

    /**
     * XXX
     *
     * @parameter expression="${project.build.directory}/resources/META-INF/plan.xml"
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
    private UseMavenDependencies useMavenDependencies;

    private VelocityContext createContext() {
        VelocityContext context = new VelocityContext();

        // Load properties, It inherits them all!
        Properties props = project.getProperties();
        for (Object o : props.keySet()) {
            String key = (String) o;
            String value = props.getProperty(key);

            log.debug("Setting " + key + "=" + value);
            context.put(key, value);
        }

        context.put("pom", project);

        return context;
    }

    protected void doExecute() throws Exception {
        //
        // FIXME: Do not need velocity here, we only need to filter,
        //        could use resources plugin to do this for us, or
        //        implement what resources plugin does here
        //
        //        Also velocity does not handle property expansion of expressions like
        //        ${foo.bar} to the value of the "foo.bar" property :-(
        //
        //        Might be better of just hand rolling something...
        //

        VelocityContext context = createContext();

        VelocityEngine velocity = new VelocityEngine();
        velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, sourceDir.getAbsolutePath());

        // Don't spit out any logs
        velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.NullLogSystem");
        velocity.init();

        Template template = velocity.getTemplate(planFileName);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        String plan = writer.toString();

        XmlObject doc = XmlObject.Factory.parse(plan);
        XmlCursor xmlCursor = doc.newCursor();
        LinkedHashSet<org.apache.geronimo.kernel.repository.Dependency> dependencies = toDependencies();
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

            log.info("Generated: " + targetFile);
        }
        finally {
            xmlCursor.dispose();
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

    private LinkedHashSet<org.apache.geronimo.kernel.repository.Dependency> toDependencies() {
        LinkedHashSet<org.apache.geronimo.kernel.repository.Dependency> dependencies = new LinkedHashSet<org.apache.geronimo.kernel.repository.Dependency>();

        if (useMavenDependencies == null || !useMavenDependencies.isValue()) {
            for (Dependency dependency : this.dependencies) {
                org.apache.geronimo.kernel.repository.Dependency gdep = dependency.toDependency();
                dependencies.add(gdep);
            }
        } else {
            List<org.apache.maven.model.Dependency> includedDependencies = project.getOriginalModel().getDependencies();
            List<org.apache.maven.model.Dependency> artifacts = project.getDependencies();
            for (org.apache.maven.model.Dependency dependency : includedDependencies) {
                dependency = resolveDependency(dependency, artifacts);
                if (includeDependency(dependency)) {
                    org.apache.geronimo.kernel.repository.Dependency gdep = toGeronimoDependency(dependency, useMavenDependencies.isIncludeVersion());
                    dependencies.add(gdep);
                }
            }
        }

        return dependencies;
    }


    private static org.apache.geronimo.kernel.repository.Dependency toGeronimoDependency(final org.apache.maven.model.Dependency dependency, boolean includeVersion) {
        Artifact artifact = toGeronimoArtifact(dependency, includeVersion);
        return new org.apache.geronimo.kernel.repository.Dependency(artifact, ImportType.ALL);
    }

    private static Artifact toGeronimoArtifact(final org.apache.maven.model.Dependency dependency, boolean includeVersion) {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        String version = includeVersion ? dependency.getVersion() : null;
        String type = dependency.getType();

        return new Artifact(groupId, artifactId, version, type);
    }


    interface Inserter {
        ArtifactType insert(EnvironmentType environmentType);
    }
}
