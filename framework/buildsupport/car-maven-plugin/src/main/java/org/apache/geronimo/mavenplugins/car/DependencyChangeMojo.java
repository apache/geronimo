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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Comparator;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.xml.sax.SAXException;

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
    private boolean warnOnDependencyChange;

    /**
     * Whether to show changed dependencies in log
     * @parameter
     */
    private boolean logDependencyChanges;

    /**
     * Whether to overwrite dependencies.xml if it has changed
     * @parameter
     */
    private boolean overwriteChangedDependencies;

    /**
     * Location of existing dependency file.
     *
     * @parameter expression="${basedir}/src/main/history/dependencies.xml"
     * @required
     */
    private File dependencyFile;

    /**
     * Location of filtered dependency file.
     *
     * @parameter expression="${basedir}/target/history/dependencies.xml"
     * @required
     */
    private File filteredDependencyFile;

    /**
     * Configuration of use of maven dependencies.  If missing or if value element is false, use the explicit list in the car-maven-plugin configuration.
     * If present and true, use the maven dependencies in the current pom file of scope null, runtime, or compile.  In addition, the version of the maven
     * dependency can be included or not depending on the includeVersion element.
     *
     * @parameter
     */
    private UseMavenDependencies useMavenDependencies = new UseMavenDependencies(true, false, true);

    public void execute() throws MojoExecutionException, MojoFailureException {
        UseMavenDependencies useMavenDependencies = new UseMavenDependencies(true, false, this.useMavenDependencies.isUseTransitiveDependencies());

        try {
            Collection<DependencyType> dependencies = toDependencies(this.dependencies, useMavenDependencies, false);
            for (DependencyType test: dependencies) {
                test.setStart(null);
                test.setImport(null);
            }
            Collection<DependencyType> added = new LinkedHashSet<DependencyType>(dependencies);
            PluginArtifactType removed = new PluginArtifactType();
            if (dependencyFile.exists()) {
                //filter dependencies file
                filter(dependencyFile, filteredDependencyFile);
                //read dependency types, convert to dependenciees, compare.
                FileReader in = new FileReader(filteredDependencyFile);
                try {
                    PluginArtifactType pluginArtifactType = PluginXmlUtil.loadPluginArtifactMetadata(in);
                    for (DependencyType test: pluginArtifactType.getDependency()) {
                        boolean t1 = added.contains(test);
                        int s1 = added.size();
                        boolean t2 = added.remove(test);
                        int s2 = added.size();
                        if (t1 != t2) {
                            getLogger().warn("dependencies.contains: " + t1 + ", dependencies.remove(test): " + t2);
                        }
                        if (t1 == (s1 == s2)) {
                            getLogger().warn("dependencies.contains: " + t1 + ", size before: " + s1 + ", size after: " + s2);
                        }
                        if (!t2) {
                            removed.getDependency().add(test);
                        }
                    }

                } catch (Exception e) {
                    getLogger().warn("Could not read dependencies.xml file at " + dependencyFile, e);
                } finally {
                    in.close();
                }
                File treeListing = saveTreeListing();
                if (!added.isEmpty() || !removed.getDependency().isEmpty()) {
                    saveDependencyChanges(added, removed, treeListing);
                    if (overwriteChangedDependencies) {
                        writeDependencies(toPluginArtifactType(dependencies), dependencyFile);
                    }
                }
            } else {
                writeDependencies(toPluginArtifactType(dependencies),  dependencyFile);
            }
        } catch (MojoFailureException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("Could not write dependency history info", e);
        }
    }

    protected void saveDependencyChanges(Collection<DependencyType> dependencies, PluginArtifactType removed, File treeListing)
            throws Exception {
        File addedFile = new File(filteredDependencyFile.getParentFile(), "dependencies.added.xml");
        PluginArtifactType added = toPluginArtifactType(dependencies);
        writeDependencies(added,  addedFile);

        File removedFile = new File(filteredDependencyFile.getParentFile(), "dependencies.removed.xml");
        writeDependencies(removed,  removedFile);

        StringWriter out = new StringWriter();
        out.write("Dependencies have changed:\n");
        if (!added.getDependency().isEmpty()) {
            out.write("\tAdded dependencies are saved here: " + addedFile.getAbsolutePath() + "\n");
            if (logDependencyChanges) {
                PluginXmlUtil.writePluginArtifact(added, out);
            }
        }
        if (!removed.getDependency().isEmpty()) {
            out.write("\tRemoved dependencies are saved here: " + removedFile.getAbsolutePath() + "\n");
            if (logDependencyChanges) {
                PluginXmlUtil.writePluginArtifact(removed, out);
            }
        }
        out.write("\tTree listing is saved here: " + treeListing.getAbsolutePath() + "\n");
        out.write("Delete " + dependencyFile.getAbsolutePath()
                + " if you are happy with the dependency changes.");

        if (warnOnDependencyChange) {
            getLog().warn(out.toString());
        } else {
            throw new MojoFailureException(out.toString());
        }
    }

    protected File saveTreeListing() throws IOException {
        File treeListFile = new File(filteredDependencyFile.getParentFile(), "treeListing.txt");
        OutputStream os = new FileOutputStream(treeListFile);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        try {
            writer.write(treeListing);
        } finally {
            writer.close();
        }
        return treeListFile;
    }

    private PluginArtifactType toPluginArtifactType(Collection<DependencyType> dependencies) throws IOException, XMLStreamException, JAXBException {
        PluginArtifactType pluginArtifactType = new PluginArtifactType();
        for (DependencyType dependency: dependencies) {
            pluginArtifactType.getDependency().add(dependency);
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
        Collections.sort(pluginArtifactType.getDependency(), new Comparator<DependencyType>() {

            public int compare(DependencyType a, DependencyType b) {
                int result = a.getGroupId().compareTo(b.getGroupId());
                if (result != 0) return result;
                result = a.getArtifactId().compareTo(b.getArtifactId());
                if (result != 0) return result;
                return getType(a).compareTo(getType(b));
            }

            private String getType(DependencyType a) {
                return a.getType() == null? "jar": a.getType();
            }
        });
        try {
            PluginXmlUtil.writePluginArtifact(pluginArtifactType, out);
        } finally {
            out.close();
        }
    }
}
