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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class DependencyManager implements SynchronousBundleListener {
    private static final Logger log = LoggerFactory.getLogger(DependencyManager.class);

    private final BundleContext bundleContext;
    private final Collection<Repository> repositories;

    public DependencyManager(@ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext,
                             @ParamReference(name = "Repositories", namingType = "Repository") Collection<Repository> repositories) {
        this.bundleContext = bundleContext;
        this.repositories = repositories;
        bundleContext.addBundleListener(this);
    }

    public void bundleChanged(BundleEvent bundleEvent) {
        int eventType = bundleEvent.getType();
        if (eventType == BundleEvent.INSTALLED) {
            installed(bundleEvent.getBundle());
        }
    }

    private void installed(Bundle bundle) {
        URL info = bundle.getEntry("META-INF/geronimo-plugin.xml");
        if (info != null) {
            log.info("found geronimo-plugin.xml for bundle " + bundle);
            try {
                InputStream in = info.openStream();
                try {
                    PluginType pluginType = PluginXmlUtil.loadPluginMetadata(in);
                    PluginArtifactType pluginArtifactType = pluginType.getPluginArtifact().get(0);
                    List<DependencyType> dependencies = pluginArtifactType.getDependency();
                    List<Bundle> bundles = new ArrayList<Bundle>();
                    for (DependencyType dependencyType : dependencies) {
                        log.info("installing artifact: " + dependencyType);
                        Artifact artifact = dependencyType.toArtifact();
                        String location = locateBundle(artifact);
                        for (Bundle test: bundleContext.getBundles()) {
                            if (location.equals(test.getLocation())) {
                                continue;
                            }
                        }
                        try {
                            bundles.add(bundleContext.installBundle(location));
                        } catch (BundleException e) {
                            log.warn("Could not install bundle for artifact: " + artifact, e);
                        }
                    }
                    for (Bundle b : bundles) {
                        try {
                            b.start();
                        } catch (BundleException e) {
                            log.warn("Could not start bundle: " + b, e);
                        }
                    }
                } catch (Throwable e) {
                    log.warn("Could not read geronimo metadata for bundle: " + bundle, e);
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                //??
            }
        } else {
            log.info("did not find geronimo-plugin.xml for bundle " + bundle);
        }
    }

    private String locateBundle(Artifact configurationId) throws NoSuchConfigException, IOException, InvalidConfigException {
        if (System.getProperty("geronimo.build.car") == null) {
            return "mvn:" + configurationId.getGroupId() + "/" + configurationId.getArtifactId() + "/" + configurationId.getVersion() + ("jar".equals(configurationId.getType())?  "": "/" + configurationId.getType());
        }
        for (Repository repo : repositories) {
            if (repo.contains(configurationId)) {
                return "reference:file://" + repo.getLocation(configurationId).getAbsolutePath();
            }
        }
        throw new NoSuchConfigException(configurationId);
    }

}
