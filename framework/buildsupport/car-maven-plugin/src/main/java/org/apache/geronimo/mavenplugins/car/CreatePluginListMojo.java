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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Maven2Repository;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.xml.sax.SAXException;

/**
 * Creates or replaces a geronimo-plugins.xml catalog of geronimo plugins in the local maven repository.  Although geronimo-plugins.xml is
 * maintained automatically when you build geronimo plugins locally, this is useful if you have downloaded plugins from elsewhere or
 * corrupted the geronimo-plugins.xml file.  This must be run explcitly using the command line
 * mvn org.apache.geronimo.buildsupport:car-maven-plugin:create-pluginlist
 *
 * @version $Rev$ $Date$
 * @goal create-pluginlist
 */
public class CreatePluginListMojo extends AbstractCarMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        String path = getArtifactRepository().getBasedir();
        File baseDir = new File(path);

        ListableRepository repository = new Maven2Repository(baseDir);
        try {
            PluginListType pluginList = createPluginListForRepositories(repository,  path);
            File outFile = new File(baseDir, "geronimo-plugins.xml");
            Writer out = new FileWriter(outFile, false);
            try {
                PluginXmlUtil.writePluginList(pluginList, out);
            } finally {
                out.close();
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Could not create plugin list", e);
        }
    }

    public PluginListType createPluginListForRepositories(ListableRepository repository, String repoName) throws NoSuchStoreException {
        Map<PluginType, PluginType> pluginMap = new HashMap<PluginType, PluginType>();
        SortedSet<Artifact> configs = repository.list();
        for (Artifact configId : configs) {
            PluginType data = getPluginMetadata(repository, configId);

            if (data != null) {
                PluginType key = PluginInstallerGBean.toKey(data);
                PluginType existing = pluginMap.get(key);
                if (existing == null) {
                    pluginMap.put(key, data);
                } else {
                    existing.getPluginArtifact().addAll(data.getPluginArtifact());
                }
            }
        }
        PluginListType pluginList = new PluginListType();
        pluginList.getPlugin().addAll(pluginMap.values());
        pluginList.getDefaultRepository().add(repoName);
        return pluginList;
    }

    private PluginType getPluginMetadata(ListableRepository repository, Artifact configId) {
        File dir = repository.getLocation(configId);
        if (!dir.isFile() || !dir.canRead()) {
            getLog().error("Cannot read artifact dir " + dir.getAbsolutePath());
            throw new IllegalStateException("Cannot read artifact dir " + dir.getAbsolutePath());
        }
        if (dir.toString().endsWith(".pom")) {
            return null;
        }
        try {
            JarFile jar = new JarFile(dir);
            try {
                ZipEntry entry = jar.getEntry("META-INF/geronimo-plugin.xml");
                if (entry == null) {
                    return null;
                }
                InputStream in = jar.getInputStream(entry);
                try {
                    PluginType pluginType = PluginXmlUtil.loadPluginMetadata(in);
                    if (pluginType.getPluginArtifact().isEmpty()) {
                        return null;
                    }
                    return pluginType;
                } finally {
                    in.close();
                }
            } finally {
                jar.close();
            }
        } catch (ZipException e) {
            //not a zip file, ignore
        } catch (SAXException e) {
            getLog().error("Unable to read JAR file " + dir.getAbsolutePath(), e);
        } catch (XMLStreamException e) {
            getLog().error("Unable to read JAR file " + dir.getAbsolutePath(), e);
        } catch (JAXBException e) {
            getLog().error("Unable to read JAR file " + dir.getAbsolutePath(), e);
        } catch (IOException e) {
            getLog().error("Unable to read JAR file " + dir.getAbsolutePath(), e);
        }
        return null;
    }


}
