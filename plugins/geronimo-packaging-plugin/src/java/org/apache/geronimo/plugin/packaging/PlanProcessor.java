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
import org.apache.maven.project.Dependency;
import org.apache.maven.repository.Artifact;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * @version $Rev:  $ $Date:  $
 */
public class PlanProcessor {
    private static final String IMPORT_PROPERTY = "geronimo.import";
    private static final QName IMPORT_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.0", "import");
    private static final String INCLUDE_PROPERTY = "geronimo.include";
    private static final QName INCLUDE_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.0", "include");
    private static final String DEPENDENCY_PROPERTY = "geronimo.dependency";
    private static final QName DEPENDENCY_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.0", "dependency");

    private List artifacts;
    private String sourceDir;
    private String targetDir;
    private String planFile;
    private String targetFile;
    private Context context;

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

    public void execute() throws Exception, XmlException {
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


            insertPlanElements(xmlCursor, IMPORT_PROPERTY, IMPORT_QNAME);
            insertPlanElements(xmlCursor, INCLUDE_PROPERTY, INCLUDE_QNAME);
            insertPlanElements(xmlCursor, DEPENDENCY_PROPERTY, DEPENDENCY_QNAME);

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
    }

    private void insertPlanElements(XmlCursor xmlCursor, String artifactProperty, QName elementQName) {
        if (xmlCursor.toNextSibling(elementQName)) {
            while(xmlCursor.toNextSibling(elementQName));
            xmlCursor.toEndToken();
            xmlCursor.toNextToken();
        }
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            Dependency dependency = (Dependency) artifact.getDependency();
            if ("true".equals(dependency.getProperty(artifactProperty))) {
                String groupId = dependency.getGroupId();
                String artifactId = dependency.getArtifactId();
                String version = dependency.getVersion();
                String type = dependency.getType();
                org.apache.geronimo.deployment.xbeans.DependencyType dependencyType = org.apache.geronimo.deployment.xbeans.DependencyType.Factory.newInstance();
                dependencyType.setGroupId(groupId);
                dependencyType.setArtifactId(artifactId);
                dependencyType.setVersion(version);
                if (type != null && !"jar".equals(type)) {
                    dependencyType.setType(type);
                }

                xmlCursor.beginElement(elementQName);
                XmlCursor element = dependencyType.newCursor();
                try {
                    element.copyXmlContents(xmlCursor);
                } finally {
                    element.dispose();
                }
                xmlCursor.toNextToken();
            }
        }
    }
}
