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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.apache.xbean.osgi.bundle.util.BundleDescription.ExportPackage;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean
@OsgiService
public class DependencyManager implements SynchronousBundleListener {
    private static final Logger log = LoggerFactory.getLogger(DependencyManager.class);

    private final BundleContext bundleContext;
    private final Collection<Repository> repositories;

    private final RepositoryAdmin repositoryAdmin;

    private final ArtifactResolver artifactResolver;

    private final Map<Long, PluginArtifactType> pluginMap =
            Collections.synchronizedMap(new WeakHashMap<Long, PluginArtifactType>());

    private final Map<Long, Set<Long>> dependentBundleIdsMap = new ConcurrentHashMap<Long, Set<Long>>();
    private final Map<Long, Set<ExportPackage>> bundleExportPackagesMap = new ConcurrentHashMap<Long, Set<ExportPackage>>();
    private final Map<Artifact, Bundle> artifactBundleMap = new ConcurrentHashMap<Artifact, Bundle>();

    public DependencyManager(@ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext,
                             @ParamReference(name = "Repositories", namingType = "Repository") Collection<Repository> repositories,
                             @ParamReference(name="ArtifactResolver", namingType = "ArtifactResolver") ArtifactResolver artifactResolver) {
        this.bundleContext = bundleContext;
        this.repositories = repositories;
        this.artifactResolver = artifactResolver;
        bundleContext.addBundleListener(this);
        ServiceReference ref = bundleContext.getServiceReference(RepositoryAdmin.class.getName());
        repositoryAdmin = ref == null? null: (RepositoryAdmin) bundleContext.getService(ref);
        //init installed bundles
        for (Bundle bundle : bundleContext.getBundles()) {
            installed(bundle);
        }
        //Check the car who loads me ...
        try {
            PluginArtifactType pluginArtifact = getCachedPluginMetadata(bundleContext.getBundle());
            if (pluginArtifact != null) {
                Set<Long> dependentBundleIds = new HashSet<Long>();
                for (DependencyType dependency : pluginArtifact.getDependency()) {
                    Bundle dependentBundle = getBundle(dependency.toArtifact());
                    if (dependentBundle != null) {
                        dependentBundleIds.add(dependentBundle.getBundleId());
                    }
                }
                dependentBundleIdsMap.put(bundleContext.getBundle().getBundleId(), dependentBundleIds);
            }
        } catch (Exception e) {
            log.error("Fail to read the dependency info from bundle " + bundleContext.getBundle().getLocation());
        }
    }

    public void bundleChanged(BundleEvent bundleEvent) {
        int eventType = bundleEvent.getType();
        //TODO Need to optimize the codes, as we will not receive the INSTALLED event after the cache is created
        if (eventType == BundleEvent.INSTALLED || eventType == BundleEvent.RESOLVED) {
            installed(bundleEvent.getBundle());
        } else if (eventType == BundleEvent.STARTING) {
            starting(bundleEvent.getBundle());
        } else if (eventType == BundleEvent.UNINSTALLED) {
            uninstall(bundleEvent.getBundle());
        }
    }

    public Set<ExportPackage> getExportedPackages(Bundle bundle) {
        Set<ExportPackage> exportPackages = bundleExportPackagesMap.get(bundle.getBundleId());
        if (exportPackages == null) {
            exportPackages = getExportPackagesInternal(bundle);
            bundleExportPackagesMap.put(bundle.getBundleId(), exportPackages);
        }
        return exportPackages;
    }

    public List<Bundle> getDependentBundles(Bundle bundle) {
        Set<Long> dependentBundleIds = dependentBundleIdsMap.get(bundle.getBundleId());
        if (dependentBundleIds == null || dependentBundleIds.size() == 0) {
            return Collections.<Bundle> emptyList();
        }
        List<Bundle> dependentBundles = new ArrayList<Bundle>(dependentBundleIds.size());
        for (Long dependentBundleId : dependentBundleIds) {
            dependentBundles.add(bundleContext.getBundle(dependentBundleId));
        }
        return dependentBundles;
    }

    public Bundle getBundle(Artifact artifact) {
        if(!artifact.isResolved()) {
            try {
                if (artifactResolver != null) {
                    artifact = artifactResolver.resolveInClassLoader(artifact);
                }
            } catch (MissingDependencyException e) {
            }
        }
        return artifactBundleMap.get(artifact);
    }

    public Artifact toArtifact(String installationLocation) {
        if (installationLocation == null || !installationLocation.startsWith("mvn:")) {
            return null;
        }
        String[] artifactFragments = installationLocation.substring(4).split("[/]");
        if(artifactFragments.length < 2) {
            return null;
        }
        return new Artifact(artifactFragments[0], artifactFragments[1], artifactFragments.length > 2 ? artifactFragments[2] : "" ,artifactFragments.length > 3 && artifactFragments[3].length() > 0 ?artifactFragments[3] : "jar" );
    }

    private void addArtifactBundleEntry(Bundle bundle) {
        Artifact artifact = toArtifact(bundle.getLocation());
        if (artifact != null) {
            artifactBundleMap.put(artifact, bundle);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("fail to resovle artifact from the bundle location " + bundle.getLocation());
            }
        }
    }

    private void removeArtifactBundleEntry(Bundle bundle) {
        Artifact artifact = toArtifact(bundle.getLocation());
        if (artifact != null) {
            artifactBundleMap.remove(artifact);
        }
    }

    private Set<ExportPackage> getExportPackagesInternal(Bundle bundle) {
        ServiceReference reference = null;
        try {
            reference = bundleContext.getServiceReference(PackageAdmin.class.getName());
            PackageAdmin packageAdmin = (PackageAdmin) bundleContext.getService(reference);
            ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(bundle);
            if (exportedPackages != null) {
                Set<ExportPackage> exportPackageNames = new HashSet<ExportPackage>();
                for (ExportedPackage exportedPackage : exportedPackages) {
                    Map<String, String> attributes = new HashMap<String, String>();
                    attributes.put(Constants.VERSION_ATTRIBUTE, exportedPackage.getVersion().toString());
                    exportPackageNames.add(new ExportPackage(exportedPackage.getName(), attributes, Collections.<String, String>emptyMap()));
                }
                return exportPackageNames;
            }
            return Collections.<ExportPackage>emptySet();
        } finally {
            if (reference != null) {
                bundleContext.ungetService(reference);
            }
        }
    }

    private PluginArtifactType getPluginMetadata(Bundle bundle) {
        PluginArtifactType pluginArtifactType = null;
        URL info = bundle.getEntry("META-INF/geronimo-plugin.xml");
        if (info != null) {
            if (log.isDebugEnabled()) {
                log.debug("found geronimo-plugin.xml for bundle " + bundle);
            }
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
            log.debug("did not find geronimo-plugin.xml for bundle " + bundle);
        }
        return pluginArtifactType;
    }

    private void uninstall(Bundle bundle) {
        removeArtifactBundleEntry(bundle);
        dependentBundleIdsMap.remove(bundle.getBundleId());
        pluginMap.remove(bundle.getBundleId());
    }

    private void installRepository(Bundle bundle) {
        if (repositoryAdmin != null) {
            URL info = bundle.getEntry("OSGI-INF/obr/repository.xml");
            if (info != null) {
                if (log.isDebugEnabled()) {
                    log.debug("found repository.xml for bundle " + bundle);
                }
                try {
                    repositoryAdmin.addRepository(info);
                } catch (Exception e) {
                    log.info("Error adding respository.xml for bundle " + bundle, e);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("did not find respository.xml for bundle " + bundle);
                }
            }
        }
    }

    private PluginArtifactType getCachedPluginMetadata(Bundle bundle) {
        PluginArtifactType pluginArtifactType = pluginMap.get(bundle.getBundleId());
        if (pluginArtifactType == null) {
            pluginArtifactType = getPluginMetadata(bundle);
            if (pluginArtifactType != null) {
                pluginMap.put(bundle.getBundleId(), pluginArtifactType);
            }
            //take this opportunity to install obr repo fragment
            // installRepository(bundle);
        }
        return pluginArtifactType;
    }

    private void installed(Bundle bundle) {
        addArtifactBundleEntry(bundle);
        PluginArtifactType pluginArtifactType = getCachedPluginMetadata(bundle);
        if (pluginArtifactType != null) {
            List<DependencyType> dependencies = pluginArtifactType.getDependency();
            Set<Long> dependentBundleIds = new HashSet<Long>();
            try {
                for (DependencyType dependencyType : dependencies) {
                    if (log.isDebugEnabled()) {
                        log.debug("Installing artifact: " + dependencyType);
                    }
                    Artifact artifact = dependencyType.toArtifact();
                    if (artifactResolver != null) {
                        artifact = artifactResolver.resolveInClassLoader(artifact);
                    }
                    String location = locateBundle(artifact);
                    try {
                        Bundle installedBundle = bundleContext.installBundle(location);
                        if (dependentBundleIds.add(installedBundle.getBundleId())) {
                            Set<Long> parentDependentBundleIds = dependentBundleIdsMap.get(installedBundle
                                    .getBundleId());
                            if (parentDependentBundleIds != null) {
                                dependentBundleIds.addAll(parentDependentBundleIds);
                            }
                        }
                    } catch (BundleException e) {
                        log.warn("Could not install bundle for artifact: " + artifact, e);
                    }
                }
                dependentBundleIdsMap.put(bundle.getBundleId(), dependentBundleIds);
            } catch (Exception e) {
                log.error("Could not install bundle dependency", e);
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
                    if (log.isDebugEnabled()) {
                        log.debug("Starting artifact: " + dependencyType);
                    }
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
