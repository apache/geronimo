/* ====================================================================
 *   Copyright 2001-2004 The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * ====================================================================
 */

package org.apache.geronimo.maven.xpom;

import java.io.File;
import java.util.List;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.maven.jelly.tags.BaseTagSupport;
import org.apache.maven.project.Dependency;
import org.apache.maven.project.Project;
import org.apache.maven.repository.Artifact;
import org.apache.maven.repository.DefaultArtifactFactory;

public class AddDependency extends BaseTagSupport {


    private Dependency dependency;

    public AddDependency() {
        dependency = new Dependency();
    }

    public void setProperty(String combinedProperty) {
        if (combinedProperty == null) {
            return;
        }
        dependency.addProperty("property:" + combinedProperty);
    }

    public void setProperties(String combinedProperties) {

        if (combinedProperties == null) {
            return;
        }

        String[] strings = combinedProperties.split(",");
        for (int i = 0; i < strings.length; i++) {
            setProperty(strings[i].trim());
        }
    }

    public void setGroupId(String s) {
        if (s == null) {
            return;
        }
        dependency.setGroupId(s);
    }

    public void setArtifactId(String s) {
        if (s == null) {
            return;
        }
        dependency.setArtifactId(s);
    }

    public void setVersion(String s) {
        if (s == null) {
            return;
        }
        dependency.setVersion(s);
    }

    public void setType(String s) {
        if (s == null) {
            return;
        }
        dependency.setType(s);
    }

    public void setJar(String s) {
        if (s == null) {
            return;
        }
        dependency.setJar(s);
    }

    public void setUrl(String s) {
        if (s == null) {
            return;
        }
        dependency.setUrl(s);
    }

    public void setId(String s) {
        if (s == null) {
            return;
        }

        dependency.setId(s);
    }

    public String getGroupId() {
        return dependency.getGroupId();
    }

    public String getId() throws IllegalStateException {
        return dependency.getId();
    }

    public String getArtifactId() {
        return dependency.getArtifactId();
    }

    public String getVersion() {
        return dependency.getVersion();
    }

    public String getJar() {
        return dependency.getJar();
    }

    public String getUrl() {
        return dependency.getUrl();
    }

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        try {
            if (dependency.getJar() != null){
                Artifact artifact = DefaultArtifactFactory.createArtifact(dependency);
                // Munge the paths
                File jarFile = new File(dependency.getJar());
                artifact.setPath(jarFile.getAbsolutePath());
                dependency.setJar(jarFile.getName());
                // Add artifact and dependency to project
                Project project = getMavenContext().getProject();
                project.addDependency(dependency);
                project.getArtifacts().add(artifact);
            } else {
                Project project = getMavenContext().getProject();
                project.addDependency(dependency);
                project.buildArtifactList();
            }
        } catch (Exception e) {
            throw new JellyTagException(e);
        }
    }
}
