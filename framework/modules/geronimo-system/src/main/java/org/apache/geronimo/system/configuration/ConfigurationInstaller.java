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


package org.apache.geronimo.system.configuration;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.ConfigXmlContentType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.apache.geronimo.system.plugin.model.PropertyType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:$ $Date:$
 */
@Component(metatype = true)
public class ConfigurationInstaller {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationInstaller.class);

    private final Executor executor = Executors.newCachedThreadPool();

    private BundleTracker bt;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private PluginAttributeStore pluginAttributeStore;

    public void setPluginAttributeStore(PluginAttributeStore pluginAttributeStore) {
        this.pluginAttributeStore = pluginAttributeStore;
    }

    public void unsetPluginAttributeStore(PluginAttributeStore pluginAttributeStore) {
        if (pluginAttributeStore == this.pluginAttributeStore) {
            this.pluginAttributeStore = null;
        }
    }

    @Activate
    public void start(BundleContext bundleContext) {
        bt = new BundleTracker(bundleContext, Bundle.INSTALLED, new ConfigurationBundleTrackerCustomizer());
        bt.open();

    }

    @Deactivate
    public void stop() {
        bt.close();
        bt = null;
    }

    private class ConfigurationBundleTrackerCustomizer implements BundleTrackerCustomizer {

        @Override
        public Object addingBundle(Bundle bundle, BundleEvent bundleEvent) {
            installConfiguration(bundle);
            return null;
        }

        @Override
        public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
        }

        @Override
        public void removedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
        }
    }

    private void installConfiguration(Bundle bundle) {
        URL configSerURL = bundle.getEntry("META-INF/geronimo-plugin.xml");
        if (configSerURL == null) {
            return;
        }
        Installer loader = new Installer(configSerURL);
        loader.run();
//        try {
//            executor.execute(loader);
//        } catch (RejectedExecutionException e) {
//            logger.info("could not execute in thread pool", e);
//            loader.run();
//        }

    }

    private class Installer implements Runnable {

        private URL pluginXmlURL;

        public Installer(URL pluginXmlURL) {
            this.pluginXmlURL = pluginXmlURL;


        }

        private boolean equals(String server, String serverName) {
            return server == null? serverName == null || serverName.isEmpty(): server.equals(serverName);
        }

        private Artifact toArtifact(ArtifactType moduleId) {
            String groupId = moduleId.getGroupId();
            String artifactId = moduleId.getArtifactId();
            String version = moduleId.getVersion();
            String type = moduleId.getType();
            return new Artifact(groupId, artifactId, version, type);
        }


        @Override
        public void run() {
            try {
                InputStream in = pluginXmlURL.openStream();
                try {
                    PluginType pluginType = PluginXmlUtil.loadPluginMetadata(in);
                    PluginArtifactType pluginArtifactType = pluginType.getPluginArtifact().get(0);
                    List<PropertyType> substitutions = pluginArtifactType.getConfigSubstitution();
                    Map<String, String> subMap = new HashMap<String, String>();
                    for (PropertyType propertyType: substitutions) {
                        if (equals(propertyType.getServer(), pluginAttributeStore.getServerName())) {
                            subMap.put(propertyType.getKey(), propertyType.getValue());
                        }
                    }
                    pluginAttributeStore.addConfigSubstitutions(subMap);
                    List<ConfigXmlContentType> xmls = pluginArtifactType.getConfigXmlContent();
                    for (ConfigXmlContentType xml: xmls) {
                        if (equals(xml.getServer(), pluginAttributeStore.getServerName())) {
                            pluginAttributeStore.setModuleGBeans(toArtifact(pluginArtifactType.getModuleId()), xml.getGbean(), xml.isLoad(), xml.getCondition());
                        }
                    }
                } finally {
                    in.close();
                }
            } catch (Exception e) {
                logger.info("Could not install plugin xml", e);
            }
        }
    }


}
