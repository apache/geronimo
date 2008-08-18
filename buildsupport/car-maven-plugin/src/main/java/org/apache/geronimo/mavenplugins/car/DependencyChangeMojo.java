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


package org.apache.geronimo.mavenplugins.car;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.bind.JAXBException;

import org.apache.geronimo.system.plugin.PluginXmlUtil;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Check if the dependencies have changed
 *
 * @version $Rev$ $Date$
 * @goal verify-no-dependency-change
 * @requiresDependencyResolution runtime
 */
public class DependencyChangeMojo extends AbstractCarMojo {

    /**
     * Dependencies explicitly listed in the car-maven-plugin configuration
     *
     * @parameter
     */
    private List<Dependency> dependencies = Collections.emptyList();


    /**
     * Whether to fail on changed dependencies
     * @parameter
     */
    private boolean failOnChange=true;

    /**
     * Location of existing dependency file.
     *
     * @parameter expression="${basedir}/src/main/history/dependencies.xml"
     * @required
     */
    private File dependencyFile;

    /**
     * Configuration of use of maven dependencies.  If missing or if value element is false, use the explicit list in the car-maven-plugin configuration.
     * If present and true, use the maven dependencies in the current pom file of scope null, runtime, or compile.  In addition, the version of the maven
     * dependency can be included or not depending on the includeVersion element.
     *
     * @parameter
     */
    private UseMavenDependencies useMavenDependencies;

    public void execute() throws MojoExecutionException, MojoFailureException {
        boolean useTransitiveDependencies = useMavenDependencies == null || useMavenDependencies.isUseTransitiveDependencies();
        UseMavenDependencies useMavenDependencies = new UseMavenDependencies(true, false, useTransitiveDependencies);

        try {
            Collection<Dependency> dependencies = toDependencies(this.dependencies, useMavenDependencies);
            if (dependencyFile.exists()) {
                //read dependency types, convert to dependenciees, compare.
                FileReader in = new FileReader(dependencyFile);
                try {
                    PluginArtifactType pluginArtifactType = PluginXmlUtil.loadPluginArtifactMetadata(in);
                    PluginArtifactType removed = new PluginArtifactType();
                    for (DependencyType test: pluginArtifactType.getDependency()) {
                        Dependency testDependency = Dependency.newDependency(test);
                        if (!dependencies.remove(testDependency)) {
                            removed.getDependency().add(test);
                        }
                    }
                    if (!dependencies.isEmpty() || !removed.getDependency().isEmpty()) {
                        File addedFile = new File(dependencyFile.getParentFile(), "dependencies.added.xml");
                        PluginArtifactType added = toPluginArtifactType(dependencies);
                        writeDependencies(added,  addedFile);
                        File removedFile = new File(dependencyFile.getParentFile(), "dependencies.removed.xml");
                        writeDependencies(removed,  removedFile);
                        if (failOnChange) {
                            StringWriter out = new StringWriter();
                            out.write("Dependencies have changed:\n");
                            if (!added.getDependency().isEmpty()) {
                                out.write("added:\n");
                                PluginXmlUtil.writePluginArtifact(added, out);
                            }
                            if (!removed.getDependency().isEmpty()) {
                                out.write("removed:\n");
                                PluginXmlUtil.writePluginArtifact(removed, out);
                            }
                            throw new MojoFailureException(out.toString());
                        }
                    }
                } finally {
                    in.close();
                }
            } else {
                writeDependencies(toPluginArtifactType(dependencies),  dependencyFile);
            }
        } catch (MojoFailureException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("Could not read or write dependency history info", e);
        }
    }

    private PluginArtifactType toPluginArtifactType(Collection<Dependency> dependencies) throws IOException, XMLStreamException, JAXBException {
        PluginArtifactType pluginArtifactType = new PluginArtifactType();
        for (Dependency dependency: dependencies) {
            pluginArtifactType.getDependency().add(dependency.toDependencyType());
        }
        return pluginArtifactType;
    }

    private void writeDependencies(PluginArtifactType pluginArtifactType, File file) throws IOException, XMLStreamException, JAXBException {
        pluginArtifactType.setModuleId(getModuleId());
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        } else if (!parent.isDirectory()) {
            throw new IOException("expected dependencies history directory is not a directory: " + parent);
        }
        FileWriter out = new FileWriter(file);
        try {
            PluginXmlUtil.writePluginArtifact(pluginArtifactType, out);
        } finally {
            out.close();
        }
    }
}
