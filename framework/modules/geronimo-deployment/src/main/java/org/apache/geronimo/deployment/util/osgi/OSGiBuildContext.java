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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.system.configuration.DependencyManager;
import org.apache.xbean.osgi.bundle.util.BundleDescription.ExportPackage;

/**
 * @version $Rev$ $Date$
 */
public class OSGiBuildContext {

    private List<String> hiddenImportPackageNamePrefixes;

    private Set<String> hiddenImportPackageNames;

    private DependencyManager dependencyManager;

    private Environment environment;

    private boolean inverseClassLoading;

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
}
