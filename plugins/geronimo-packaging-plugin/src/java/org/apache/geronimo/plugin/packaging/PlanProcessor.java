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

import java.io.File;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;

import org.apache.commons.jelly.tags.velocity.JellyContextAdapter;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.project.Dependency;
import org.apache.maven.repository.Artifact;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.ClassloaderType;

/**
 * @version $Rev$ $Date$
 */
public class PlanProcessor {
    private static Log log = LogFactory.getLog(PlanProcessor.class);

    private static final String IMPORT_PROPERTY = "geronimo.import";
    private static final String INCLUDE_PROPERTY = "geronimo.include";
    private static final String DEPENDENCY_PROPERTY = "geronimo.dependency";
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

            XmlObject doc = XmlObject.Factory.parse(plan);
            XmlCursor xmlCursor = doc.newCursor();
            try {

                xmlCursor.toFirstContentToken();
                xmlCursor.toFirstChild();
                QName childName = xmlCursor.getName();
                EnvironmentType environmentType;
                if (childName.equals(ENVIRONMENT_QNAME)) {
                    XmlObject xmlObject = xmlCursor.getObject();
                    System.out.println("Expected EnvironmentType, actual: " + xmlObject.getClass().getName());
                    System.out.println(xmlObject.toString());
                    environmentType = (EnvironmentType) xmlObject.copy().changeType(EnvironmentType.type);
                    xmlCursor.removeXml();
                } else {
                    environmentType = EnvironmentType.Factory.newInstance();
                    xmlCursor.beginElement(ENVIRONMENT_QNAME);
                    XmlCursor element = environmentType.newCursor();
                    try {
                        element.copyXmlContents(xmlCursor);
                    } finally {
                        element.dispose();
                    }
                }

                ArtifactType configId = environmentType.getConfigId();
                if (configId == null) {
                    configId = environmentType.addNewConfigId();
                    configId.setGroupId(groupId);
                    configId.setArtifactId(artifactId);
                    configId.setVersion(version);
                    configId.setType("car");
                }

                insertPlanElements(environmentType, IMPORT_PROPERTY, new Inserter() {

                    public ArtifactType insert(EnvironmentType environmentType) {
                        ClassloaderType classloaderType = environmentType.getClassloader();
                        if (classloaderType == null) {
                            classloaderType = environmentType.addNewClassloader();
                        }
                        return classloaderType.addNewImport();
                    }
                });
                insertPlanElements(environmentType, INCLUDE_PROPERTY, new Inserter() {

                    public ArtifactType insert(EnvironmentType environmentType) {
                        ClassloaderType classloaderType = environmentType.getClassloader();
                        if (classloaderType == null) {
                            classloaderType = environmentType.addNewClassloader();
                        }
                        return classloaderType.addNewInclude();
                    }
                });
                insertPlanElements(environmentType, DEPENDENCY_PROPERTY, new Inserter() {

                    public ArtifactType insert(EnvironmentType environmentType) {
                        ClassloaderType classloaderType = environmentType.getClassloader();
                        if (classloaderType == null) {
                            classloaderType = environmentType.addNewClassloader();
                        }
                        return classloaderType.addNewDependency();
                    }
                });

                xmlCursor.beginElement(ENVIRONMENT_QNAME);
                XmlCursor element = environmentType.newCursor();
                try {
                    element.copyXmlContents(xmlCursor);
                } finally {
                    element.dispose();
                }

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
            log.error(e.getClass().getName()+": "+e.getMessage(), e);
            throw e;
        }
    }

    private void insertPlanElements(EnvironmentType environmentType, String artifactProperty, Inserter inserter) {
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            Dependency dependency = artifact.getDependency();
            if ("true".equals(dependency.getProperty(artifactProperty))) {
                String groupId = dependency.getGroupId();
                String artifactId = dependency.getArtifactId();
                String version = dependency.getVersion();
                String type = dependency.getType();
                ArtifactType artifactType = inserter.insert(environmentType);
                artifactType.setGroupId(groupId);
                artifactType.setArtifactId(artifactId);
                artifactType.setVersion(version);
                if (type != null && !"jar".equals(type)) {
                    artifactType.setType(type);
                }

            }
        }
    }

    interface Inserter {
        ArtifactType insert(EnvironmentType environmentType);
    }


}
