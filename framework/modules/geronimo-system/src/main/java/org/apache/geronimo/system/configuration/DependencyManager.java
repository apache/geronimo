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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.osgi.BundleUtils;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
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

    private final RepositoryAdmin repositoryAdmin;

    private final ArtifactResolver artifactResolver;

    private final Map<Bundle, PluginArtifactType> pluginMap =
        Collections.synchronizedMap(new WeakHashMap<Bundle, PluginArtifactType>());

    public DependencyManager(@ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext,
                             @ParamReference(name = "Repositories", namingType = "Repository") Collection<Repository> repositories,
                             @ParamReference(name="ArtifactResolver", namingType = "ArtifactResolver") ArtifactResolver artifactResolver) {
        this.bundleContext = bundleContext;
        this.repositories = repositories;
        this.artifactResolver = artifactResolver;
        bundleContext.addBundleListener(this);
        ServiceReference ref = bundleContext.getServiceReference(RepositoryAdmin.class.getName());
        repositoryAdmin = ref == null? null: (RepositoryAdmin) bundleContext.getService(ref);
    }

    public void bundleChanged(BundleEvent bundleEvent) {
        int eventType = bundleEvent.getType();
        if (eventType == BundleEvent.INSTALLED) {
            installed(bundleEvent.getBundle());
        } else if (eventType == BundleEvent.STARTING) {
            starting(bundleEvent.getBundle());
        }
    }

    private PluginArtifactType getPluginMetadata(Bundle bundle) {
        PluginArtifactType pluginArtifactType = null;
        URL info = bundle.getEntry("META-INF/geronimo-plugin.xml");
        if (info != null) {
            log.info("found geronimo-plugin.xml for bundle " + bundle);
            InputStream in = null;
            try {
                in = info.openStream();
                PluginType pluginType = PluginXmlUtil.loadPluginMetadata(in);
                pluginArtifactType = pluginType.getPluginArtifact().get(0);
            } catch (Throwable e) {
                log.warn("Could not read geronimo metadata for bundle: " + bundle, e);
            } finally {
                if (in != null) {
                    try { in.close(); } catch (IOException e) {}
                }
            }
        } else {
            log.info("did not find geronimo-plugin.xml for bundle " + bundle);
        }
        return pluginArtifactType;
    }

    private void installRepository(Bundle bundle) {
        if (repositoryAdmin != null) {
            URL info = bundle.getEntry("OSGI-INF/obr/repository.xml");
            if (info != null) {
                log.info("found repository.xml for bundle " + bundle);
                try {
                    repositoryAdmin.addRepository(info);
                } catch (Exception e) {
                    log.info("Error adding respository.xml for bundle " + bundle, e);
                }
            } else {
                log.info("did not find respository.xml for bundle " + bundle);
            }
        }
    }

    private PluginArtifactType getCachedPluginMetadata(Bundle bundle) {
        PluginArtifactType pluginArtifactType = pluginMap.get(bundle);
        if (pluginArtifactType == null) {
            pluginArtifactType = getPluginMetadata(bundle);
            if (pluginArtifactType != null) {
                pluginMap.put(bundle, pluginArtifactType);
            }
            //take this opportunity to install obr repo fragment
            // installRepository(bundle);
        }
        return pluginArtifactType;
    }

    private void installed(Bundle bundle) {
        PluginArtifactType pluginArtifactType = getCachedPluginMetadata(bundle);
        if (pluginArtifactType != null) {
            List<DependencyType> dependencies = pluginArtifactType.getDependency();
            try {
                for (DependencyType dependencyType : dependencies) {
                    log.info("Installing artifact: " + dependencyType);
                    Artifact artifact = dependencyType.toArtifact();
                    if (artifactResolver != null) {
                        artifact = artifactResolver.resolveInClassLoader(artifact);
                    }
                    String location = locateBundle(artifact);
                    try {
                        bundleContext.installBundle(location);
                    } catch (BundleException e) {
                        log.warn("Could not install bundle for artifact: " + artifact, e);
                    }
                }
            } catch (Exception e) {
                log.error("Could not install bundle dependecy", e);
            }
        }
    }

    private void starting(Bundle bundle) {
        PluginArtifactType pluginArtifactType = getCachedPluginMetadata(bundle);
        if (pluginArtifactType != null) {
            List<Bundle> bundles = new ArrayList<Bundle>();
            List<DependencyType> dependencies = pluginArtifactType.getDependency();
            try {
                for (DependencyType dependencyType : dependencies) {
                    log.info("Starting artifact: " + dependencyType);
                    Artifact artifact = dependencyType.toArtifact();
                    if (artifactResolver != null) {
                        artifact = artifactResolver.resolveInClassLoader(artifact);
                    }
                    String location = locateBundle(artifact);
                    Bundle b = bundleContext.installBundle(location);
                    if (b.getState() != Bundle.ACTIVE) {
                        bundles.add(b);
                    }
                }

                for (Bundle b : bundles) {
                    if (BundleUtils.canStart(b)) {
                        try {
                            b.start(Bundle.START_TRANSIENT);
                        } catch (BundleException e) {
                            log.warn("Could not start bundle: " + b, e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Could not install bundle dependecy", e);
            }
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
