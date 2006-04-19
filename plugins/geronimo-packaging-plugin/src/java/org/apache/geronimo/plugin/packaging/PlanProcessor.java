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
package org.apache.geronimo.plugin.packaging;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.tags.velocity.JellyContextAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.maven.project.Dependency;
import org.apache.maven.repository.Artifact;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import javax.xml.namespace.QName;

import java.io.File;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class PlanProcessor {
    private static Log log = LogFactory.getLog(PlanProcessor.class);

    private static final String IMPORT_PROPERTY = "geronimo.import";
//    private static final String INCLUDE_PROPERTY = "geronimo.include";
    private static final String DEPENDENCY_PROPERTY = "geronimo.dependency";
    private static final String REFERENCE_PROPERTY = "geronimo.reference";
    private static final String ENVIRONMENT_LOCAL_NAME = "environment";
    private static final QName ENVIRONMENT_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.1", "environment");

    private List artifacts;
    private String sourceDir;
    private String targetDir;
    private String planFile;
    private String targetFile;
    private Context context;
    private String groupId;
    private String artifactId;
    private String version;

    public List getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List artifacts) {
        this.artifacts = artifacts;
    }

    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    public String getTargetDir() {
        return targetDir;
    }

    public void setTargetDir(String targetDir) {
        this.targetDir = targetDir;
    }

    public String getPlanFile() {
        return planFile;
    }

    public void setPlanFile(String planFile) {
        this.planFile = planFile;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    public void setContext(JellyContext context) {
        this.context = new JellyContextAdapter(context);
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void execute() throws Exception, XmlException {
        try {
            if (artifacts == null) {
                throw new RuntimeException("Artifacts not supplied");
            }
            if (targetDir == null) {
                throw new RuntimeException("No target directory supplied");
            }
            if (planFile == null) {
                throw new RuntimeException("No source plan supplied");
            }
            if (targetFile == null) {
                throw new RuntimeException("No target plan supplied");
            }

            File sourceD = new File(sourceDir);
            VelocityEngine velocity = new VelocityEngine();
            velocity.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, sourceD.getAbsolutePath());
            velocity.init();
            Template template = velocity.getTemplate(planFile);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);

            String plan = writer.toString();

            LinkedHashSet dependencies = toDependencies();
            org.apache.geronimo.kernel.repository.Artifact configId = new org.apache.geronimo.kernel.repository.Artifact(groupId, artifactId, version, "car");
            XmlObject doc = XmlObject.Factory.parse(plan);
            XmlCursor xmlCursor = doc.newCursor();
            try {

                mergeEnvironment(xmlCursor, configId, dependencies);

                File targetDir = new File(this.targetDir);
                if (targetDir.exists()) {
                    if (!targetDir.isDirectory()) {
                        throw new RuntimeException("TargetDir: " + this.targetDir + " exists and is not a directory");
                    }
                } else {
                    targetDir.mkdirs();
                }
                File output = new File(targetFile);
                XmlOptions xmlOptions = new XmlOptions();
                xmlOptions.setSavePrettyPrint();
                doc.save(output, xmlOptions);
            } finally {
                xmlCursor.dispose();
            }
        } catch (Exception e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw e;
        }
    }

    void mergeEnvironment(XmlCursor xmlCursor, org.apache.geronimo.kernel.repository.Artifact configId, LinkedHashSet dependencies) {
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
        } finally {
            element.dispose();
        }
    }

    private void convertElement(XmlCursor cursor, String namespace) {
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
        } finally {
            end.dispose();
        }
    }

    private LinkedHashSet toDependencies() {
        LinkedHashSet dependencies = new LinkedHashSet();
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            Dependency dependency = artifact.getDependency();
            org.apache.geronimo.kernel.repository.Dependency geronimoDependency = toGeronimoDependency(dependency);
            if (geronimoDependency != null) {
                dependencies.add(geronimoDependency);
            }
        }
        return dependencies;
    }

    private static org.apache.geronimo.kernel.repository.Dependency toGeronimoDependency(Dependency dependency) {
        org.apache.geronimo.kernel.repository.Artifact artifact = toGeronimoArtifact(dependency);
        if ("true".equals(dependency.getProperty(DEPENDENCY_PROPERTY))) {
            return new org.apache.geronimo.kernel.repository.Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.CLASSES);
        } else if ("true".equals(dependency.getProperty(REFERENCE_PROPERTY))) {
            return new org.apache.geronimo.kernel.repository.Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.SERVICES);
        } else if ("true".equals(dependency.getProperty(IMPORT_PROPERTY))) {
            return new org.apache.geronimo.kernel.repository.Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.ALL);
        } else {
            // not one of ours
            return null;
        }
    }

    private static org.apache.geronimo.kernel.repository.Artifact toGeronimoArtifact(Dependency dependency) {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        String version = null;
        String type = dependency.getType();
        return new org.apache.geronimo.kernel.repository.Artifact(groupId, artifactId, version, type);
    }

    interface Inserter {
        ArtifactType insert(EnvironmentType environmentType);
    }


}
