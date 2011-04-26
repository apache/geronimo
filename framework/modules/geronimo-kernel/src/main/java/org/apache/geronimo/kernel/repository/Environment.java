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

package org.apache.geronimo.kernel.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.geronimo.kernel.config.Manifest;
import org.apache.geronimo.kernel.config.ManifestException;
import org.osgi.framework.Constants;

/**
 * holds the data from the EnvironmentType xml while it is being resolved, transitively closed, etc.
 *
 * @version $Rev$ $Date$
 */
public class Environment implements Serializable {
    private static final long serialVersionUID = 7075760873629376317L;

    private Artifact configId;
    private final LinkedHashSet<Dependency> dependencies = new LinkedHashSet<Dependency>();
    private final LinkedHashSet<String> bundleClassPath = new LinkedHashSet<String>();
    private final LinkedHashSet<String> imports = new LinkedHashSet<String>();
    private final LinkedHashSet<String> exports = new LinkedHashSet<String>();
    private final LinkedHashSet<String> requireBundles = new LinkedHashSet<String>();
    private final LinkedHashSet<String> dynamicImports = new LinkedHashSet<String>();
    private String bundleActivator;
    private final ClassLoadingRules classLoadingRules;
    private boolean suppressDefaultEnvironment;

    public Environment() {
        classLoadingRules = new ClassLoadingRules();
    }

    public Environment(Artifact configId) {
        this.configId = configId;

        classLoadingRules = new ClassLoadingRules();
    }

    public Environment(Environment environment) {
        configId = environment.getConfigId();
        dependencies.addAll(environment.dependencies);
        bundleClassPath.addAll(environment.bundleClassPath);
        imports.addAll(environment.imports);
        exports.addAll(environment.exports);
        requireBundles.addAll(environment.requireBundles);
        dynamicImports.addAll(environment.dynamicImports);
        bundleActivator = environment.bundleActivator;
        suppressDefaultEnvironment = environment.isSuppressDefaultEnvironment();
        classLoadingRules = environment.classLoadingRules;
    }

    public Artifact getConfigId() {
        return configId;
    }

    public void setConfigId(Artifact configId) {
        this.configId = configId;
    }

    /**
     * Gets a List (with elements of type Dependency) of the configuration and
     * JAR dependencies of this configuration.
     *
     * @return immutable copy of the current dependencies
     * @see Dependency
     */
    public List<Dependency> getDependencies() {
        return Collections.unmodifiableList(new ArrayList<Dependency>(dependencies));
    }

    public void addDependency(Artifact artifact, ImportType importType) {
        this.dependencies.add(new Dependency(artifact, importType));
    }

    public void addDependency(Dependency dependency) {
        this.dependencies.add(dependency);
    }

    public void addDependencies(Collection<Dependency> dependencies) {
        this.dependencies.addAll(dependencies);
    }

    public void setDependencies(Collection<Dependency> dependencies) {
        this.dependencies.clear();
        addDependencies(dependencies);
    }

    public ClassLoadingRules getClassLoadingRules() {
        return classLoadingRules;
    }

    public boolean isSuppressDefaultEnvironment() {
        return suppressDefaultEnvironment;
    }

    public void setSuppressDefaultEnvironment(boolean suppressDefaultEnvironment) {
        this.suppressDefaultEnvironment = suppressDefaultEnvironment;
    }

    public void addToBundleClassPath(Collection<String> bundleClassPath) {
        this.bundleClassPath.addAll(bundleClassPath);
    }

    public void addToBundleClassPath(String bundleClassPath) {
        this.bundleClassPath.add(bundleClassPath);
    }

    public void removeBundleClassPath(Collection<String> bundleClassPath) {
        this.bundleClassPath.removeAll(bundleClassPath);
    }

    public void removeBundleClassPath(String bundleClassPath) {
        this.bundleClassPath.remove(bundleClassPath);
    }

    public List<String> getBundleClassPath() {
        return Collections.unmodifiableList(new ArrayList<String>(bundleClassPath));
    }

    public void addImportPackages(Collection<String> imports) {
        this.imports.addAll(imports);
    }

    public void addImportPackage(String imports) {
        this.imports.add(imports);
    }

    public List<String> getImportPackages() {
        return Collections.unmodifiableList(new ArrayList<String>(imports));
    }

    public void addExportPackages(Collection<String> exports) {
        this.exports.addAll(exports);
    }

    public void addExportPackage(String exports) {
        this.exports.add(exports);
    }

    public void removeExportPackages(Collection<String> exports) {
        this.exports.removeAll(exports);
    }

    public void removeExportPackage(String exports) {
        this.exports.remove(exports);
    }

    public List<String> getExportPackages() {
        return Collections.unmodifiableList(new ArrayList<String>(exports));
    }

    public String getBundleActivator() {
        return bundleActivator;
    }

    public void setBundleActivator(String bundleActivator) {
        this.bundleActivator = bundleActivator;
    }

    public void addRequireBundles(Collection<String> symbolicNames) {
        this.requireBundles.addAll(symbolicNames);
    }

    public void addRequireBundle(String symbolicName) {
        this.requireBundles.add(symbolicName);
    }

    public void removeRequireBundles(Collection<String> symbolicNames) {
        this.requireBundles.removeAll(symbolicNames);
    }

    public void removeRequireBundle(String symbolicName) {
        this.requireBundles.remove(symbolicName);
    }

    public List<String> getRequireBundles() {
        return Collections.unmodifiableList(new ArrayList<String>(requireBundles));
    }

    public void addDynamicImportPackages(Collection<String> imports) {
        this.dynamicImports.addAll(imports);
    }

    public void addDynamicImportPackage(String imports) {
        this.dynamicImports.add(imports);
    }

    public void removeDynamicImportPackages(Collection<String> imports) {
        this.dynamicImports.removeAll(imports);
    }

    public void removeDynamicImportPackage(String imports) {
        this.dynamicImports.remove(imports);
    }

    public List<String> getDynamicImportPackages() {
        return Collections.unmodifiableList(new ArrayList<String>(dynamicImports));
    }

    public void removeImportPackage(String importPackage) {
        this.imports.remove(importPackage);
    }

    public void removeImportPackages(Collection<String> importPackages) {
        this.imports.removeAll(importPackages);
    }

    public Manifest getManifest() throws ManifestException {
        Manifest manifest = new Manifest();
        manifest.addConfiguredAttribute(new Manifest.Attribute(Constants.BUNDLE_MANIFESTVERSION, "2"));
        manifest.addConfiguredAttribute(new Manifest.Attribute(Constants.BUNDLE_SYMBOLICNAME, configId.getGroupId() + "." + configId.getArtifactId()));
        String versionString = "" + configId.getVersion().getMajorVersion() + "." + configId.getVersion().getMinorVersion() + "." + configId.getVersion().getIncrementalVersion();
        if (configId.getVersion().getQualifier() != null) {
            versionString += "." + configId.getVersion().getQualifier().replaceAll("[^-_\\w]{1}", "_");
        }

        manifest.addConfiguredAttribute(new Manifest.Attribute(Constants.BUNDLE_VERSION, versionString));

        if (bundleActivator != null) {
            manifest.addConfiguredAttribute(new Manifest.Attribute(Constants.BUNDLE_ACTIVATOR, bundleActivator));
        }

        if (!imports.isEmpty()) {
            manifest.addConfiguredAttribute(new Manifest.Attribute(Manifest.Attribute.Separator.COMMA, Constants.IMPORT_PACKAGE, imports));
        }

        if (!exports.isEmpty()) {
            manifest.addConfiguredAttribute(new Manifest.Attribute(Manifest.Attribute.Separator.COMMA, Constants.EXPORT_PACKAGE, exports));
        }

        if (!dynamicImports.isEmpty()) {
            manifest.addConfiguredAttribute(new Manifest.Attribute(Manifest.Attribute.Separator.COMMA, Constants.DYNAMICIMPORT_PACKAGE, dynamicImports));
        }

        if (!bundleClassPath.isEmpty()) {
            Manifest.Attribute bundleClassPath = new Manifest.Attribute(Manifest.Attribute.Separator.COMMA, Constants.BUNDLE_CLASSPATH, this.bundleClassPath);
            manifest.addConfiguredAttribute(bundleClassPath);
        }

        if (!requireBundles.isEmpty()) {
            Manifest.Attribute requireBundle = new Manifest.Attribute(Manifest.Attribute.Separator.COMMA, Constants.REQUIRE_BUNDLE, this.requireBundles);
            manifest.addConfiguredAttribute(requireBundle);
        }
        return manifest;
    }


    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("Environment [");
        buf.append("\n  Id: ").append(getConfigId());
        buf.append("]\n");
        return buf.toString();
    }
}
