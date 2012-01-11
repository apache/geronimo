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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.aries.util.tracker.RecursiveBundleTracker;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.AbstractRepository;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.apache.xbean.osgi.bundle.util.BundleDescription.ExportPackage;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.xbean.osgi.bundle.util.HeaderParser;
import org.apache.xbean.osgi.bundle.util.HeaderParser.HeaderElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@Component(immediate = true)
@Service //for OsgiMetaDataBuilder
public class DependencyManager implements OsgiMetaDataProvider {

    private static final Logger log = LoggerFactory.getLogger(DependencyManager.class);

    private BundleContext bundleContext;

    @Reference(name = "repository", referenceInterface = Repository.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    private final Collection<Repository> repositories = new LinkedHashSet<Repository>();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private RepositoryAdmin repositoryAdmin;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ArtifactResolver artifactResolver;

    private final Map<Long, PluginArtifactType> pluginMap = Collections.synchronizedMap(new WeakHashMap<Long, PluginArtifactType>());

    private final Map<Long, Set<Long>> dependentBundleIdsMap = new ConcurrentHashMap<Long, Set<Long>>();

    private final Map<Long, Set<Long>> fullDependentBundleIdsMap = new ConcurrentHashMap<Long, Set<Long>>();

    private final Map<Long, Set<ExportPackage>> bundleExportPackagesMap = new HashMap<Long, Set<ExportPackage>>();

    private final Map<Artifact, Bundle> artifactBundleMap = new ConcurrentHashMap<Artifact, Bundle>();

    private final Map<Long, Artifact> bundleIdArtifactMap = new ConcurrentHashMap<Long, Artifact>();

    //configuration activator
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private Kernel kernel;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ConfigurationManager configurationManager;

    private RecursiveBundleTracker bt;

    //used in tests
    protected DependencyManager(BundleContext bundleContext,
            Collection<Repository> repositories,
            ArtifactResolver artifactResolver) {
        this.bundleContext = bundleContext;
        this.repositories.addAll(repositories);
        this.artifactResolver = artifactResolver;
    }

    public DependencyManager() {
    }

    public void setArtifactResolver(ArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }

    public void unsetArtifactResolver(ArtifactResolver artifactResolver) {
        if (this.artifactResolver == artifactResolver) {
            this.artifactResolver = null;
        }
    }

    public void setRepositoryAdminr(RepositoryAdmin repositoryAdmin) {
        this.repositoryAdmin = repositoryAdmin;
    }

    public void unsetRepositoryAdmin(RepositoryAdmin repositoryAdmin) {
        if (this.repositoryAdmin == repositoryAdmin) {
            this.repositoryAdmin = null;
        }
    }

    public void bindRepository(Repository repository) {
        repositories.add(repository);
    }

    public void unbindRepository(Repository repository) {
        repositories.remove(repository);
    }

    public void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    public void unsetKernel(Kernel kernel) {
        if (this.kernel == kernel) {
            this.kernel = null;
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void unsetConfigurationManager(ConfigurationManager configurationManager) {
        if (this.configurationManager == configurationManager) {
            this.configurationManager = null;
        }
    }


    @Activate
    public void doStart(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;
        int stateMask = Bundle.INSTALLED | Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING | Bundle.UNINSTALLED;
        bt = new RecursiveBundleTracker(bundleContext, stateMask, new BundleTrackerCustomizer() {
            @Override
            public Object addingBundle(Bundle bundle, BundleEvent bundleEvent) {
                if (bundleEvent == null) {
                    // existing bundles first added to the tracker with no event change
                    installed(bundle);
                    //make sure existing bundle is at least starting
                    if (bundle.getBundleContext() != null) {
                        start(bundle);
                    }
                } else {
                    bundleChanged(bundleEvent);
                }

                return bundle;
            }

            @Override
            public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
                if (bundleEvent == null) {
                    // existing bundles first added to the tracker with no event change
                    return;
                } else {
                    bundleChanged(bundleEvent);
                }
            }

            @Override
            public void removedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
                if (bundleEvent == null) {
                    // existing bundles first added to the tracker with no event change
                    return;
                } else {
                    bundleChanged(bundleEvent);
                }
            }
        });
        bt.open();
    }

    @Deactivate
    public void doStop() throws Exception {
        bt.close();
        //Some clean up work
        pluginMap.clear();
        dependentBundleIdsMap.clear();
        fullDependentBundleIdsMap.clear();
        bundleExportPackagesMap.clear();
        artifactBundleMap.clear();
        bundleIdArtifactMap.clear();
    }

    public void bundleChanged(BundleEvent bundleEvent) {
        int eventType = bundleEvent.getType();
        //TODO Need to optimize the codes, as we will not receive the INSTALLED event after the cache is created
        if (eventType == BundleEvent.INSTALLED || eventType == BundleEvent.RESOLVED) {
            installed(bundleEvent.getBundle());
        } else if (eventType == BundleEvent.STARTING) {
            starting(bundleEvent.getBundle());
            start(bundleEvent.getBundle());
        } else if (eventType == BundleEvent.STOPPING) {
            stop(bundleEvent.getBundle());
        } else if (eventType == BundleEvent.UNINSTALLED) {
            uninstall(bundleEvent.getBundle());
        }
    }

    @Override
    public Set<ExportPackage> getExportedPackages(Bundle bundle) {
        return getExportedPackages(bundle.getBundleId());
    }

    @Override
    public Set<ExportPackage> getExportedPackages(long bundleId) {
        synchronized (bundleExportPackagesMap) {
            Set<ExportPackage> exportPackages = bundleExportPackagesMap.get(bundleId);
            if (exportPackages == null) {
                exportPackages = getExportPackagesInternal(bundleContext.getBundle(bundleId));
                bundleExportPackagesMap.put(bundleId, exportPackages);
            }
            return exportPackages;
        }
    }

    public List<Bundle> getDependentBundles(Bundle bundle) {
        Set<Long> dependentBundleIds = getDependentBundleIds(bundle);
        if (dependentBundleIds.size() == 0) {
            return Collections.<Bundle> emptyList();
        }
        List<Bundle> dependentBundles = new ArrayList<Bundle>(dependentBundleIds.size());
        for (Long dependentBundleId : dependentBundleIds) {
            Bundle b = bundleContext.getBundle(dependentBundleId);
            if (b!=null) {
                dependentBundles.add(b);
            }
        }
        return dependentBundles;
    }

    public Set<Long> getDependentBundleIds(Bundle bundle) {
        Set<Long> dependentBundleIds = dependentBundleIdsMap.get(bundle.getBundleId());
        return dependentBundleIds == null ? Collections.<Long> emptySet() : new HashSet<Long>(dependentBundleIds);
    }

    public Set<Bundle> getFullDependentBundles(Bundle bundle) {
        return getFullDependentBundles(bundle.getBundleId());
    }

    public Set<Bundle> getFullDependentBundles(long bundleId) {
        Set<Long> fullDependentBundleIds = getFullDependentBundleIds(bundleId);
        if (fullDependentBundleIds.size() == 0) {
            return Collections.<Bundle> emptySet();
        }
        Set<Bundle> dependentBundles = new HashSet<Bundle>(fullDependentBundleIds.size());
        for (Long dependentBundleId : fullDependentBundleIds) {
            Bundle b = bundleContext.getBundle(dependentBundleId);
            if (b!=null) {
                dependentBundles.add(b);
            }
        }
        return dependentBundles;
    }
    
    public Set<Long> getFullDependentBundleIds(Bundle bundle) {
        return getFullDependentBundleIds(bundle.getBundleId());
    }

    @Override
    public Set<Long> getFullDependentBundleIds(long bundleId) {
        Set<Long> fullDependentBundleIds = fullDependentBundleIdsMap.get(bundleId);
        return fullDependentBundleIds == null ? Collections.<Long> emptySet() : new HashSet<Long>(fullDependentBundleIds);
    }

    @Override
    public Bundle getBundle(Artifact artifact) {
        if (!artifact.isResolved()) {
            try {
                if (artifactResolver != null) {
                    artifact = artifactResolver.resolveInClassLoader(artifact);
                }
            } catch (MissingDependencyException e) {
            }
        }
        return artifactBundleMap.get(artifact);
    }

    @Override
    public Artifact getArtifact(long bundleId) {
        return bundleIdArtifactMap.get(bundleId);
    }

    public Artifact toArtifact(String installationLocation) {
        if (installationLocation == null) {
            return null;
        }
        if (installationLocation.startsWith("mvn:")) {
            String[] artifactFragments = installationLocation.substring(4).split("[/]");
            if (artifactFragments.length < 2) {
                return null;
            }
            return new Artifact(artifactFragments[0], artifactFragments[1], artifactFragments.length > 2 ? artifactFragments[2] : "",
                    artifactFragments.length > 3 && artifactFragments[3].length() > 0 ? artifactFragments[3] : "jar");
        } else if(installationLocation.startsWith("reference:file://")) {
            //TODO a better way for this ???
            installationLocation = installationLocation.substring("reference:file://".length());
            for (Repository repo : repositories) {
                if (repo instanceof AbstractRepository) {
                    File rootFile = ((AbstractRepository) repo).getRootFile();
                    if (installationLocation.startsWith(rootFile.getAbsolutePath())) {
                        String artifactString = installationLocation.substring(rootFile.getAbsolutePath().length());
                        if (artifactString.startsWith(File.separator)) {
                            artifactString = artifactString.substring(File.separator.length());
                        }
                        String[] filePathFragments = artifactString.split("[" + (File.separator.equals("\\") ? "\\\\" : File.separator) + "]");
                        if (filePathFragments.length >= 4) {
                            StringBuilder groupId = new StringBuilder(filePathFragments[0]);
                            for (int i = 1; i <= filePathFragments.length - 4; i++) {
                                groupId.append(".").append(filePathFragments[i]);
                            }
                            return new Artifact(groupId.toString(), filePathFragments[filePathFragments.length - 3], filePathFragments[filePathFragments.length - 2],
                                    filePathFragments[filePathFragments.length - 1].substring(filePathFragments[filePathFragments.length - 1].lastIndexOf('.') + 1));
                        }
                    }
                }
            }
        }
        return null;
    }

    public void updatePluginMetadata(Bundle bundle) {
        Long bundleId = bundle.getBundleId();
        dependentBundleIdsMap.remove(bundleId);
        fullDependentBundleIdsMap.remove(bundleId);
        pluginMap.remove(bundleId);
        PluginArtifactType pluginArtifactType = getCachedPluginMetadata(bundle);
        if (pluginArtifactType != null) {
            List<DependencyType> dependencies = pluginArtifactType.getDependency();
            Set<Long> dependentBundleIds = new HashSet<Long>();
            Set<Long> fullDependentBundleIds = new HashSet<Long>();
            try {
                for (DependencyType dependencyType : dependencies) {
                    Artifact artifact = dependencyType.toArtifact();
                    Bundle dependentBundle = getBundle(artifact);
                    if(dependentBundle == null) {
                        log.warn("Dependent artifact " + artifact + " could not be resolved, it will be ignored");
                        continue;
                    }
                    Long dependentBundleId = dependentBundle.getBundleId();
                    dependentBundleIds.add(dependentBundleId);
                    if (fullDependentBundleIds.add(dependentBundleId)) {
                        Set<Long> parentDependentBundleIds = fullDependentBundleIdsMap.get(dependentBundleId);
                        if (parentDependentBundleIds != null) {
                            fullDependentBundleIds.addAll(parentDependentBundleIds);
                        }
                    }
                }
                fullDependentBundleIdsMap.put(bundle.getBundleId(), fullDependentBundleIds);
                dependentBundleIdsMap.put(bundle.getBundleId(), dependentBundleIds);
            } catch (Exception e) {
                log.error("Could not update bundle dependecy", e);
            }
        }
    }

    public static void updatePluginMetadata(BundleContext bundleContext, Bundle bundle) {
        ServiceReference serviceReference = null;
        try {
            serviceReference = bundleContext.getServiceReference(DependencyManager.class.getName());
            DependencyManager dependencyManager = null;
            if (serviceReference != null) {
                dependencyManager = (DependencyManager) bundleContext.getService(serviceReference);
                dependencyManager.updatePluginMetadata(bundle);
            }
        } finally {
            if (serviceReference != null) {
                bundleContext.ungetService(serviceReference);
            }
        }
    }

    private PluginArtifactType addArtifactBundleEntry(Bundle bundle) {
        PluginArtifactType pluginArtifactType = getCachedPluginMetadata(bundle);
        Artifact artifact;
        if (pluginArtifactType == null) {
            artifact = toArtifact(bundle.getLocation());
        } else {
            artifact = pluginArtifactType.getModuleId().toArtifact();
        }
        if (artifact != null) {
            artifactBundleMap.put(artifact, bundle);
            bundleIdArtifactMap.put(bundle.getBundleId(), artifact);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("fail to resovle artifact from the bundle location " + bundle.getLocation());
            }
        }
        return pluginArtifactType;
    }

    private void removeArtifactBundleEntry(Bundle bundle) {
        Artifact artifact = bundleIdArtifactMap.remove(bundle.getBundleId());
        if (artifact != null) {
            artifactBundleMap.remove(artifact);
        }
    }

    private Set<ExportPackage> getExportPackagesInternal(Bundle bundle) {
        ServiceReference reference = null;
        try {
            reference = bundleContext.getServiceReference(PackageAdmin.class.getName());
            if (reference == null) {
                log.warn("No PackageAdmin service is found, fail to get export packages of " + bundle.getLocation());
                return Collections.<ExportPackage> emptySet();
            }
            String exportPackageHeader = (String)bundle.getHeaders().get(Constants.EXPORT_PACKAGE);
            Map<String, HeaderElement> nameVersionExportPackageMap = new HashMap<String, HeaderElement>();
            if (exportPackageHeader != null) {
                List<HeaderElement> headerElements = HeaderParser.parseHeader(exportPackageHeader);
                for (HeaderElement headerElement : headerElements) {
                    String version = headerElement.getAttribute(Constants.VERSION_ATTRIBUTE);
                    if (version == null) {
                        headerElement.addAttribute(Constants.VERSION_ATTRIBUTE, "0.0.0");
                        nameVersionExportPackageMap.put(headerElement.getName() + "0.0.0", headerElement);
                    } else {
                        nameVersionExportPackageMap.put(headerElement.getName() + new Version(version).toString(), headerElement);
                    }
                }
            }
            PackageAdmin packageAdmin = (PackageAdmin) bundleContext.getService(reference);
            ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(bundle);
            if (exportedPackages != null) {
                Set<ExportPackage> exportPackageNames = new HashSet<ExportPackage>();
                for (ExportedPackage exportedPackage : exportedPackages) {
                    HeaderElement headerElement = nameVersionExportPackageMap.get(exportedPackage.getName() + exportedPackage.getVersion());
                    if (headerElement != null) {
                        exportPackageNames.add(new ExportPackage(headerElement.getName(), headerElement.getAttributes(), headerElement.getDirectives()));
                    }
                }
                return exportPackageNames;
            }
            return Collections.<ExportPackage> emptySet();
        } finally {
            if (reference != null) {
                bundleContext.ungetService(reference);
            }
        }
    }

    private PluginArtifactType getPluginMetadata(Bundle bundle) {
        PluginArtifactType pluginArtifactType = null;
        InputStream in = null;
        try {
            URL info = bundle.getEntry("META-INF/geronimo-plugin.xml");
            if (info != null) {
                if (log.isDebugEnabled()) {
                    log.debug("found geronimo-plugin.xml for bundle " + bundle);
                }
                in = info.openStream();
            } else if (bundle.getBundleContext() != null) {
                File pluginMetadataFile = bundle.getBundleContext().getDataFile("geronimo-plugin.xml");
                if (pluginMetadataFile.exists()) {
                    in = new FileInputStream(pluginMetadataFile);
                }
            }
            if (in != null) {
                PluginType pluginType = PluginXmlUtil.loadPluginMetadata(in);
                pluginArtifactType = pluginType.getPluginArtifact().get(0);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("did not find geronimo-plugin.xml for bundle " + bundle);
                }
            }
        } catch (Throwable e) {
            log.warn("Could not read geronimo metadata for bundle: " + bundle, e);
        } finally {
            IOUtils.close(in);
        }
        return pluginArtifactType;
    }

    private void uninstall(Bundle bundle) {
        removeArtifactBundleEntry(bundle);
        dependentBundleIdsMap.remove(bundle.getBundleId());
        fullDependentBundleIdsMap.remove(bundle.getBundleId());
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

    @Override
    public PluginArtifactType getCachedPluginMetadata(Bundle bundle) {
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

    public void installed(Bundle bundle) {
        if (bundleIdArtifactMap.containsKey(bundle.getBundleId())) {
            return;
        }
        PluginArtifactType pluginArtifactType = addArtifactBundleEntry(bundle);
        if (pluginArtifactType == null) {
            return;
        }
        List<DependencyType> dependencies = pluginArtifactType.getDependency();
        Set<Long> dependentBundleIds = new HashSet<Long>();
        Set<Long> fullDependentBundleIds = new HashSet<Long>();
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
                    Bundle installedDependentBundle = bundleContext.installBundle(location);
                    long installedDependentBundleId = installedDependentBundle.getBundleId();
                    dependentBundleIds.add(installedDependentBundleId);
                    if (fullDependentBundleIds.add(installedDependentBundleId)) {
                        Set<Long> parentDependentBundleIds = fullDependentBundleIdsMap.get(installedDependentBundleId);
                        if (parentDependentBundleIds != null) {
                            fullDependentBundleIds.addAll(parentDependentBundleIds);
                        }
                    }
                } catch (BundleException e) {
                    log.warn("Could not install bundle for artifact: " + artifact + " from location "  + location +  " due to " + e.getMessage());
                }
            }
            fullDependentBundleIdsMap.put(bundle.getBundleId(), fullDependentBundleIds);
            dependentBundleIdsMap.put(bundle.getBundleId(), dependentBundleIds);
        } catch (Exception e) {
            log.error("Could not install bundle dependency", e);
        }
    }

    private void starting(Bundle bundle) {
        PluginArtifactType pluginArtifactType = getCachedPluginMetadata(bundle);
        if (pluginArtifactType != null) {
            List<Bundle> bundles = new ArrayList<Bundle>();
            List<DependencyType> dependencies = pluginArtifactType.getDependency();
            boolean dependencyHierarchyBuildingRequired = !dependentBundleIdsMap.containsKey(bundle.getBundleId());
            Set<Long> dependentBundleIds = null;
            Set<Long> fullDependentBundleIds = null;
            if (dependencyHierarchyBuildingRequired) {
                dependentBundleIds = new HashSet<Long>();
                fullDependentBundleIds = new HashSet<Long>();
            }
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
                    Bundle b = null;
                    try {
                        b = bundleContext.installBundle(location);
                    } catch (BundleException e) {
                        if (e.getType() == 9) {
                            String name = e.getMessage().substring(e.getMessage().indexOf(":") + 2);
                            for (Bundle test: bundleContext.getBundles()) {
                                if (name.startsWith(test.getSymbolicName()) && name.endsWith(test.getVersion().toString())) {
                                    b = test;
                                    break;
                                }
                            }
                        }
                        if (b == null) {
                            throw e;
                        }
                    }
                    if (dependencyHierarchyBuildingRequired) {
                        long startingBundleId = b.getBundleId();
                        dependentBundleIds.add(startingBundleId);
                        if (fullDependentBundleIds.add(startingBundleId)) {
                            Set<Long> parentDependentBundleIds = fullDependentBundleIdsMap.get(startingBundleId);
                            if (parentDependentBundleIds != null) {
                                fullDependentBundleIds.addAll(parentDependentBundleIds);
                            }
                        }
                    }
                    if (b.getState() != Bundle.ACTIVE ) {
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

                if (dependencyHierarchyBuildingRequired) {
                    fullDependentBundleIdsMap.put(bundle.getBundleId(), fullDependentBundleIds);
                    dependentBundleIdsMap.put(bundle.getBundleId(), dependentBundleIds);
                }
            } catch (Exception e) {
                log.error("Could not install bundle dependecy", e);
            }
        }
    }

    private String locateBundle(Artifact configurationId) throws NoSuchConfigException, IOException, InvalidConfigException {
        if (System.getProperty("geronimo.build.car") == null) {
            return "mvn:" + configurationId.getGroupId() + "/" + configurationId.getArtifactId() + "/" + configurationId.getVersion()
                    + ("jar".equals(configurationId.getType()) ? "" : "/" + configurationId.getType());
        }
        if (System.getProperty("geronimo.build.car") != null) {
            for (Repository repo : repositories) {
                if (repo.contains(configurationId)) {
                    return "reference:file://" + repo.getLocation(configurationId).getAbsolutePath();
                }
            }
        }
        return "mvn:" + configurationId.getGroupId() + "/" + configurationId.getArtifactId() + "/" + configurationId.getVersion()
                + ("jar".equals(configurationId.getType()) ? "" : "/" + configurationId.getType());
//        throw new NoSuchConfigException(configurationId);
    }

    //Activator replacement

    public void start(Bundle bundle) {
        URL url = bundle.getEntry("META-INF/config.ser");
        if (url != null) {
            InputStream in = null;
            try {
                in = url.openStream();
                //TODO there are additional consistency checks in RepositoryConfigurationStore that we should use.
                ConfigurationData data = ConfigurationUtil.readConfigurationData(in);
                data.setBundleContext(bundle.getBundleContext());
                configurationManager.loadConfiguration(data);
//            Artifact id = data.getId();
                //            manager.startConfiguration(id);
            } catch (Exception e) {
                log.warn("Exception trying to load configuration bundle " + bundle, e);
//            throw e;
            } finally {
                if (in != null)
                    try {
                        in.close();
                    } catch (Exception e) {
                    }
            }
        }
    }

    public void stop(Bundle bundle) {
        try {
            Artifact id = bundleIdArtifactMap.get(bundle.getBundleId());
            if (id == null) {
                return;
            }
            AbstractName name = Configuration.getConfigurationAbstractName(id);
            //TODO investigate how this is called and whether just stopping/unloading the configuration gbean will
            //leave the configuration model in a consistent state.  We might need a shutdown flag set elsewhere to avoid
            //overwriting the load attribute in config.xml. This code mimics the shutdown hook in KernelConfigurationManager
            //see https://issues.apache.org/jira/browse/GERONIMO-4909
            try {
                kernel.stopGBean(name);
            } catch (GBeanNotFoundException e) {
            } catch (InternalKernelException e) {
            } catch (IllegalStateException e) {
            }
            try {
                kernel.unloadGBean(name);
            } catch (GBeanNotFoundException e) {
            } catch (InternalKernelException e) {
            } catch (IllegalStateException e) {
            }
            //TODO this code is more symmetrical with start, but currently sets the load attribute to false in config.xml,
            //which prevents restarting the server.
            //            ConfigurationManager manager = ConfigurationUtil.getConfigurationManager(kernel);
            //            manager.unloadConfiguration(id);
        } catch (InvalidConfigException e) {
            log.warn("cannot stop Configuration for bundle " + bundle.getBundleId());
        }
    }


}
