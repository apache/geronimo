/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.plugin.car;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ImportType;

import org.apache.maven.model.Dependency;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

//
// TODO: Rename to PreparePlanMojo
//

/**
 * Add dependencies to a plan and process with velocity
 * 
 * @goal prepare-plan
 *
 * @version $Rev$ $Date$
 */
public class PlanProcessorMojo
    extends AbstractCarMojo
{
    private static final String ENVIRONMENT_LOCAL_NAME = "environment";

    private static final QName ENVIRONMENT_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.1", "environment");

    /**
     * @parameter expression="${basedir}/src/plan"
     * @required
     */
    private File sourceDir;

    /**
     * @parameter expression="${project.build.directory}/plan"
     * @required
     */
    private File targetDir;

    /**
     * @parameter default-value="plan.xml"
     * @required
     */
    private String planFileName;

    /**
     * @parameter expression="${project.build.directory}/plan/plan.xml"
     * @required
     */
    private File targetFile;

    //
    // FIXME: Resolve what to do about this comment...
    //
    // This is needed for ${pom.currentVersion} and will be removed when
    // we move to a full m2 build
    //

    private VelocityContext createContext() {
        VelocityContext context = new VelocityContext();
        Map pom = new HashMap();
        pom.put("groupId", project.getGroupId());
        pom.put("artifactId", project.getArtifactId());
        pom.put("currentVersion", project.getVersion());
        context.put("pom", pom);

        // Load properties, It inherits them all!
        Properties props = project.getProperties();
        for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            String value = props.getProperty(key);

            log.debug("Setting " + key + "=" + value);
            context.put(key, value);
        }

        return context;
    }

    protected void doExecute() throws Exception {
        //
        // FIXME: Do not need velocity here, we only need to filter,
        //        could use resources plugin to do this for us, or
        //        implement what resources plugin does here
        //
        
        VelocityContext context = createContext();

        VelocityEngine velocity = new VelocityEngine();
        velocity.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, sourceDir.getAbsolutePath());
        velocity.init();

        Template template = velocity.getTemplate(planFileName);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        String plan = writer.toString();

        XmlObject doc = XmlObject.Factory.parse(plan);
        XmlCursor xmlCursor = doc.newCursor();
        LinkedHashSet dependencies = toDependencies();
        Artifact configId = new Artifact(project.getGroupId(), project.getArtifactId(), project.getVersion(), "car");

        try {
            mergeEnvironment(xmlCursor, configId, dependencies);
            
            if (targetDir.exists()) {
                if (!targetDir.isDirectory()) {
                    throw new RuntimeException("TargetDir: " + this.targetDir + " exists and is not a directory");
                }
            }
            else {
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

    void mergeEnvironment(final XmlCursor xmlCursor, final Artifact configId, final LinkedHashSet dependencies) {
        xmlCursor.toFirstContentToken();
        xmlCursor.toFirstChild();
        QName childName = xmlCursor.getName();
        Environment oldEnvironment;

        if (childName != null && childName.getLocalPart().equals(ENVIRONMENT_LOCAL_NAME)) {
            convertElement(xmlCursor, ENVIRONMENT_QNAME.getNamespaceURI());
            XmlObject xmlObject = xmlCursor.getObject();
            EnvironmentType environmentType = (EnvironmentType) xmlObject.copy().changeType(EnvironmentType.type);
            oldEnvironment = EnvironmentBuilder.buildEnvironment(environmentType);
            xmlCursor.removeXml();
        }
        else {
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

    private LinkedHashSet toDependencies() {
        List artifacts = project.getDependencies();
        LinkedHashSet dependencies = new LinkedHashSet();

        Iterator iterator = artifacts.iterator();
        while (iterator.hasNext()) {
            //Artifact artifact = (Artifact) iterator.next();
            Dependency dependency = (Dependency) iterator.next();
            //Dependency dependency = artifact.getDependency();
            org.apache.geronimo.kernel.repository.Dependency geronimoDependency = toGeronimoDependency(dependency);

            if (geronimoDependency != null) {
                dependencies.add(geronimoDependency);
            }
        }

        return dependencies;
    }

    private static org.apache.geronimo.kernel.repository.Dependency toGeronimoDependency(final Dependency dependency) {
        Artifact artifact = toGeronimoArtifact(dependency);
        String type = dependency.getType();
        String scope = dependency.getScope();
        String groupId = dependency.getGroupId();

        //!"org.apache.geronimo.specs".equals(groupId) jacc spec needed in plan.xml
        if ("jar".equalsIgnoreCase(type) && !"junit".equals(groupId)) {
            if (dependency.getVersion() != null) {
                artifact = new Artifact(
                    artifact.getGroupId(),
                    artifact.getArtifactId(),
                    dependency.getVersion(),
                    artifact.getType());
            }
            return new org.apache.geronimo.kernel.repository.Dependency(artifact, ImportType.CLASSES);
        }
        else if ("car".equalsIgnoreCase(type) && ("runtime").equalsIgnoreCase(scope)) {
            return new org.apache.geronimo.kernel.repository.Dependency(artifact, ImportType.SERVICES);
        }
        else if ("car".equalsIgnoreCase(type) && ("compile".equalsIgnoreCase(scope))) {
            return new org.apache.geronimo.kernel.repository.Dependency(artifact, ImportType.CLASSES);
        }
        else if ("car".equalsIgnoreCase(type) && (scope == null)) { //parent
            return new org.apache.geronimo.kernel.repository.Dependency(artifact, ImportType.ALL);
        }
        else {
            // not one of ours
            return null;
        }
    }

    private static Artifact toGeronimoArtifact(final Dependency dependency) {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        String version = null;
        String type = dependency.getType();

        return new Artifact(groupId, artifactId, version, type);
    }

    interface Inserter {
        ArtifactType insert(EnvironmentType environmentType);
    }
}
