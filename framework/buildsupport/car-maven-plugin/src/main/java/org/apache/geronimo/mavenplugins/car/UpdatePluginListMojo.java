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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;

import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Maintain the geronimo-plugins.xml catalog in the local maven repository by merging in the geronimo-plugin.xml from the current project.
 *
 * @version $Rev$ $Date$
 * @goal update-pluginlist
 */
public class UpdatePluginListMojo extends AbstractCarMojo {

    /**
     * Location of the (just generated) plugin metadata file to merge into the geronimo-plugins.xml catalog in the local maven repository.
     *
     * @parameter expression="${project.build.directory}/resources/META-INF/geronimo-plugin.xml"
     * @required
     */
    protected File targetFile;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            InputStream min = new FileInputStream(targetFile);
            PluginType plugin;
            try {
                plugin = PluginXmlUtil.loadPluginMetadata(min);
            } finally {
                min.close();
            }

            String path = getArtifactRepository().getBasedir();
            File baseDir = new File(path);

            File outFile = new File(baseDir, "geronimo-plugins.xml");
            PluginListType pluginList;
            if (outFile.exists()) {
                InputStream in = new FileInputStream(outFile);
                try {
                    pluginList = PluginXmlUtil.loadPluginList(in);
                } finally {
                    in.close();
                }
            } else {
                pluginList = new PluginListType();
                pluginList.getDefaultRepository().add(path);
            }

            updatePluginList(plugin, pluginList);
            Writer out = new FileWriter(outFile, false);
            try {
                PluginXmlUtil.writePluginList(pluginList, out);
            } finally {
                out.close();
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Could not update plugin list", e);
        }
    }

    public void updatePluginList(PluginType plugin, PluginListType pluginList) throws NoSuchStoreException {
        PluginType key = PluginInstallerGBean.toKey(plugin);
        PluginArtifactType instance = plugin.getPluginArtifact().get(0);
        Artifact id = PluginInstallerGBean.toArtifact(instance.getModuleId());
        boolean foundKey = false;
        boolean foundModule = false;
        for (Iterator<PluginType> pit = pluginList.getPlugin().iterator(); pit.hasNext();) {
            PluginType test = pit.next();
            for (Iterator<PluginArtifactType> it = test.getPluginArtifact().iterator(); it.hasNext();) {
                PluginArtifactType testInstance = it.next();
                Artifact testId = PluginInstallerGBean.toArtifact(testInstance.getModuleId());
                if (id.equals(testId)) {
                    //if the module id appears anywhere, remove that instance
                    //however, this would cause plugin without plugin artifact
                    it.remove();
                    foundModule = true;
                }
            }
            PluginType testKey = PluginInstallerGBean.toKey(test);
            if (key.equals(testKey)) {
                foundKey = true;
                //if the name, category, author, description, pluginGroup, licence, url match, then add this instance to current pluginType
                test.getPluginArtifact().add(instance);
            }
            if(!foundKey && foundModule) {
                //remove the old plugin off pluginList if it exists as we 'll add a new one
                //otherwise we'll leave plugin without plugin artifact on the plugin catalog.
                if (test.getPluginArtifact().size() == 0) {
                    pit.remove();
                }              
            }
        }
        if (!foundKey) {
            //did not find a matching key
            pluginList.getPlugin().add(plugin);
        }
    }

}
