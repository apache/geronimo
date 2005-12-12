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
package org.apache.geronimo.plugin.dependency;

import java.util.List;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;

import org.apache.geronimo.deployment.xbeans.ServiceDocument;
import org.apache.geronimo.deployment.xbeans.ServiceType;
import org.apache.maven.project.Dependency;
import org.apache.maven.repository.Artifact;
import org.apache.xmlbeans.XmlOptions;

/**
 * @version $Rev$ $Date$
 */
public class GenerateServiceXml {

    private static final String DEPENDENCY_PROPERTY = "geronimo.dependency";

    private List artifacts;
    private String targetDir;

    public List getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List artifacts) {
        this.artifacts = artifacts;
    }


    public String getTargetDir() {
        return targetDir;
    }

    public void setTargetDir(String targetDir) {
        this.targetDir = targetDir;
    }

    public void execute() throws IOException {
        if (artifacts == null) {
            throw new RuntimeException("Artifacts not supplied");
        }
        if (targetDir == null) {
            throw new RuntimeException("No target directory supplied");
        }
        ServiceDocument serviceDocument = ServiceDocument.Factory.newInstance();
        ServiceType serviceType = serviceDocument.addNewService();
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            Dependency dependency = (Dependency) artifact.getDependency();
            if ("true".equals(dependency.getProperty(DEPENDENCY_PROPERTY))) {
                String groupId = dependency.getGroupId();
                String artifactId = dependency.getArtifactId();
                String version = dependency.getVersion();
                String type = dependency.getType();
                org.apache.geronimo.deployment.xbeans.DependencyType dependencyType = serviceType.addNewDependency();
                dependencyType.setGroupId(groupId);
                dependencyType.setArtifactId(artifactId);
                dependencyType.setVersion(version);
                if (type != null && !"jar".equals(type)) {
                    dependencyType.setType(type);
                }
            }
        }

        if (serviceType.getDependencyArray().length > 0) {
            File targetDir = new File(this.targetDir);
            if (targetDir.exists()) {
                if (!targetDir.isDirectory()) {
                    throw new RuntimeException("TargetDir: " + this.targetDir + " exists and is not a directory");
                }
            } else {
                targetDir.mkdirs();
            }
            File output = new File(targetDir, "geronimo-service.xml");
            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setSavePrettyPrint();
            serviceDocument.save(output, xmlOptions);
        }
    }
}
