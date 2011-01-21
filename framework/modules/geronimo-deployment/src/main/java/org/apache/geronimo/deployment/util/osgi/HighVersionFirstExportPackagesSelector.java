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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.system.configuration.DependencyManager;
import org.apache.xbean.osgi.bundle.util.BundleDescription.ExportPackage;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class HighVersionFirstExportPackagesSelector implements ExportPackagesSelector {

    private static final Logger logger = LoggerFactory.getLogger(HighVersionFirstExportPackagesSelector.class);

    @Override
    public Map<Long, Set<ExportPackage>> select(OSGiBuildContext context) {
        Map<Long, Set<ExportPackage>> bundleIdExportPackages = new HashMap<Long, Set<ExportPackage>>();
        Map<String, Version> packageNameVersionMap = new HashMap<String, Version>();
        DependencyManager dependencyManager = context.getDependencyManager();
        for (Dependency dependency : context.getEnvironment().getDependencies()) {
            Bundle dependentBundle = dependencyManager.getBundle(dependency.getArtifact());
            if (dependentBundle == null) {
                logger.warn("Fail to resolve the bundle corresponding to the artifact " + dependency.getArtifact() + ", its export packages are ignored");
                continue;
            }
            Set<ExportPackage> exportPackages = context.getEffectExportPackages(dependentBundle.getBundleId());
            if (exportPackages.size() > 0) {
                bundleIdExportPackages.put(dependentBundle.getBundleId(), exportPackages);
                recordHighestPackageVersion(packageNameVersionMap, exportPackages);
            }
            for (Long parentDependentBundleId : dependencyManager.getFullDependentBundleIds(dependentBundle)) {
                if (!bundleIdExportPackages.containsKey(parentDependentBundleId)) {
                    Set<ExportPackage> parentExportPackages = context.getEffectExportPackages(parentDependentBundleId);
                    if (parentExportPackages.size() > 0) {
                        bundleIdExportPackages.put(parentDependentBundleId, parentExportPackages);
                        recordHighestPackageVersion(packageNameVersionMap, parentExportPackages);
                    }
                }
            }
        }

        //Add framework bundle export packages
        Set<ExportPackage> systemExportPackages = context.getEffectExportPackages(0L);
        bundleIdExportPackages.put(0L, systemExportPackages);
        recordHighestPackageVersion(packageNameVersionMap, systemExportPackages);

        for (Iterator<Entry<Long, Set<ExportPackage>>> entryIt = bundleIdExportPackages.entrySet().iterator(); entryIt.hasNext();) {
            Entry<Long, Set<ExportPackage>> entry = entryIt.next();
            for (Iterator<ExportPackage> it = entry.getValue().iterator(); it.hasNext();) {
                ExportPackage exportPackage = it.next();
                Version highestVersion = packageNameVersionMap.get(exportPackage.getName());
                //Use != operator should be enough
                if (highestVersion != exportPackage.getVersion()) {
                    it.remove();
                }
            }
            if (entry.getValue().size() == 0) {
                entryIt.remove();
            }
        }
        packageNameVersionMap.clear();
        return bundleIdExportPackages;
    }

    private void recordHighestPackageVersion(Map<String, Version> packageNameVersionMap, Set<ExportPackage> exportPackages) {
        for (ExportPackage exportPackage : exportPackages) {
            Version existedVersion = packageNameVersionMap.get(exportPackage.getName());
            if (existedVersion == null || exportPackage.getVersion().compareTo(existedVersion) > 0) {
                packageNameVersionMap.put(exportPackage.getName(), exportPackage.getVersion());
            }
        }
    }
}
