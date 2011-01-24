/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.deployment.util.osgi;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.system.configuration.DependencyManager;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.xbean.osgi.bundle.util.BundleDescription.ExportPackage;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class OSGiBuildContext {

    private static final Logger logger = LoggerFactory.getLogger(OSGiBuildContext.class);

    private List<String> hiddenImportPackageNamePrefixes;

    private Set<String> hiddenImportPackageNames;

    private DependencyManager dependencyManager;

    private Environment environment;

    private boolean inverseClassLoading;

    private boolean clientModule;

    private ArtifactResolver clientArtifactResolver;

    public OSGiBuildContext(Environment environment, List<String> hiddenImportPackageNamePrefixes, Set<String> hiddenImportPackageNames, DependencyManager dependencyManager,
            boolean inverseClassLoading) {
        this.hiddenImportPackageNamePrefixes = hiddenImportPackageNamePrefixes;
        this.hiddenImportPackageNames = hiddenImportPackageNames;
        this.dependencyManager = dependencyManager;
        this.environment = environment;
        this.inverseClassLoading = inverseClassLoading;
    }

    private Map<String, Object> mergeAttributes = new HashMap<String, Object>();

    public Object getAttribute(String name) {
        return mergeAttributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        mergeAttributes.put(name, value);
    }

    public boolean isInverseClassLoading() {
        return inverseClassLoading;
    }

    public boolean isHiddenExportPackage(ExportPackage exportPackage) {
        String packageName = exportPackage.getName();
        if (hiddenImportPackageNames.contains(packageName)) {
            return true;
        }
        for (String hiddenImportPackageNamePrefix : hiddenImportPackageNamePrefixes) {
            if (packageName.startsWith(hiddenImportPackageNamePrefix)) {
                return true;
            }
        }
        return false;
    }

    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Set<ExportPackage> getEffectExportPackages(Long bundleId) {
        Set<ExportPackage> exportPackages = new HashSet<ExportPackage>(dependencyManager.getExportedPackages(bundleId));
        for (Iterator<ExportPackage> it = exportPackages.iterator(); it.hasNext();) {
            ExportPackage exportPackage = it.next();
            if (isHiddenExportPackage(exportPackage)) {
                it.remove();
            }
        }
        return exportPackages;
    }

    public Artifact resolveArtifact(Artifact artifact) {
        if (clientModule) {
            try {
                return clientArtifactResolver.resolveInClassLoader(artifact);
            } catch (MissingDependencyException e) {
                logger.warn("Fail to resovle artifact " + artifact + " with client artifact resolver", e);
                return null;
            }
        }
        return artifact;
    }

    public Set<Long> getFullDependentBundleIds(Bundle bundle) {
        if (clientModule) {
            return getFullClientDependentBundleIds(bundle.getBundleId());
        }
        return dependencyManager.getFullDependentBundleIds(bundle);
    }

    private Set<Long> getFullClientDependentBundleIds(long bundleId) {
        Artifact artifact = dependencyManager.getArtifact(bundleId);
        if (artifact == null) {
            return Collections.emptySet();
        }
        Artifact resolvedDependentArtifact = resolveArtifact(artifact);
        if (resolvedDependentArtifact == null) {
            return Collections.emptySet();
        }
        Set<Long> dependentBundleIds = new HashSet<Long>();
        searchFullClientDependentBundleIds(resolvedDependentArtifact, dependentBundleIds);
        return dependentBundleIds;

    }

    private void searchFullClientDependentBundleIds(Artifact resolvedClientArtifact, Set<Long> dependentBundleIds) {
        if (resolvedClientArtifact == null) {
            return;
        }
        Bundle resolvedBundle = dependencyManager.getBundle(resolvedClientArtifact);
        if (resolvedBundle == null || dependentBundleIds.contains(resolvedBundle.getBundleId())) {
            return;
        }
        PluginArtifactType pluginArtifact = dependencyManager.getCachedPluginMetadata(resolvedBundle);
        if (pluginArtifact != null) {
            for (DependencyType dependency : pluginArtifact.getDependency()) {
                Artifact resolvedDependentArtifact = resolveArtifact(dependency.toArtifact());
                if (resolvedDependentArtifact == null) {
                    continue;
                }
                searchFullClientDependentBundleIds(resolvedDependentArtifact, dependentBundleIds);
            }
        }
        dependentBundleIds.add(resolvedBundle.getBundleId());
    }

    public boolean isClientModule() {
        return clientModule;
    }

    public void setClientModule(boolean clientModule) {
        this.clientModule = clientModule;
    }

    public ArtifactResolver getClientArtifactResolver() {
        return clientArtifactResolver;
    }

    public void setClientArtifactResolver(ArtifactResolver clientArtifactResolver) {
        this.clientArtifactResolver = clientArtifactResolver;
    }
}
