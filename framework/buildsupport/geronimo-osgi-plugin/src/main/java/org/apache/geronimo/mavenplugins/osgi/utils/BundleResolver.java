/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.geronimo.mavenplugins.osgi.utils;

import static org.eclipse.osgi.service.resolver.ResolverError.IMPORT_PACKAGE_USES_CONFLICT;
import static org.eclipse.osgi.service.resolver.ResolverError.MISSING_FRAGMENT_HOST;
import static org.eclipse.osgi.service.resolver.ResolverError.MISSING_IMPORT_PACKAGE;
import static org.eclipse.osgi.service.resolver.ResolverError.MISSING_REQUIRE_BUNDLE;
import static org.eclipse.osgi.service.resolver.ResolverError.REQUIRE_BUNDLE_USES_CONFLICT;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.StateObjectFactory;
import org.eclipse.osgi.service.resolver.VersionConstraint;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

public class BundleResolver {
    private static final String PROP_MAVEN_PROJECT = "MavenProject";
    private static final String PROP_MANIFEST = "BundleManifest";

    private StateObjectFactory factory = StateObjectFactory.defaultFactory;
    private State state;
    private long id = 0;
    private Logger logger;

    public static BundleDescription[] getDependentBundles(BundleDescription root) {
        if (root == null)
            return new BundleDescription[0];
        BundleDescription[] imported = getImportedBundles(root);
        BundleDescription[] required = getRequiredBundles(root);
        BundleDescription[] dependents = new BundleDescription[imported.length + required.length];
        System.arraycopy(imported, 0, dependents, 0, imported.length);
        System.arraycopy(required, 0, dependents, imported.length, required.length);
        return dependents;
    }

    public static BundleDescription[] getImportedBundles(BundleDescription root) {
        if (root == null)
            return new BundleDescription[0];
        ExportPackageDescription[] packages = root.getResolvedImports();
        List<BundleDescription> resolvedImports = new ArrayList<BundleDescription>(packages.length);
        for (int i = 0; i < packages.length; i++)
            if (!root.getLocation().equals(packages[i].getExporter().getLocation()) && !resolvedImports
                .contains(packages[i].getExporter()))
                resolvedImports.add(packages[i].getExporter());
        return (BundleDescription[])resolvedImports.toArray(new BundleDescription[resolvedImports.size()]);
    }

    public static BundleDescription[] getRequiredBundles(BundleDescription root) {
        if (root == null)
            return new BundleDescription[0];
        return root.getResolvedRequires();
    }

    public BundleResolver(Logger logger) {
        this.logger = logger;
        this.state = factory.createState(true);
        Properties props = new Properties();
        props.putAll(System.getProperties());
        BundleUtil.loadVMProfile(props);
        state.setPlatformProperties(props);
        URL url = Bundle.class.getProtectionDomain().getCodeSource().getLocation();

        File osgi = toFile(url);
        try {
            addBundle(osgi);
        } catch (BundleException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private long getNextId() {
        return ++id;
    }

    public BundleDescription addBundle(File bundleLocation) throws BundleException {
        return addBundle(bundleLocation, false);
    }

    public BundleDescription addBundle(File bundleLocation, boolean override) throws BundleException {
        if (bundleLocation == null || !bundleLocation.exists())
            throw new IllegalArgumentException("bundleLocation not found: " + bundleLocation);
        Dictionary manifest = loadManifestAttributes(bundleLocation);
        if (manifest == null) {
            throw new BundleException("Manifest not found in " + bundleLocation);
        }
        return addBundle(manifest, bundleLocation, override);
    }

    public BundleDescription addBundle(File manifestLocation, File bundleLocation, boolean override)
        throws BundleException {
        if (bundleLocation == null || !bundleLocation.exists())
            throw new IllegalArgumentException("bundleLocation not found: " + bundleLocation);
        Dictionary manifest = loadManifestAttributes(manifestLocation);
        if (manifest == null)
            throw new IllegalArgumentException("Manifest not found in " + manifestLocation);
        return addBundle(manifest, bundleLocation, override);
    }

    private Dictionary loadManifestAttributes(File bundleLocation) {
        Manifest m = loadManifest(bundleLocation);
        if (m == null) {
            return null;
        }

        return manifestToProperties(m.getMainAttributes());
    }

    public Manifest loadManifest(File bundleLocation) {
        try {
            return BundleUtil.getManifest(bundleLocation);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Properties manifestToProperties(Attributes d) {
        Iterator iter = d.keySet().iterator();
        Properties result = new Properties();
        while (iter.hasNext()) {
            Attributes.Name key = (Attributes.Name)iter.next();
            result.put(key.toString(), d.get(key));
        }
        return result;
    }

    private BundleDescription addBundle(Dictionary enhancedManifest, File bundleLocation, boolean override)
        throws BundleException {

        BundleDescription descriptor =
            factory.createBundleDescription(state, enhancedManifest, bundleLocation.getAbsolutePath(), getNextId());

        setUserProperty(descriptor, PROP_MANIFEST, enhancedManifest);
        if (override) {
            BundleDescription[] conflicts = state.getBundles(descriptor.getSymbolicName());
            if (conflicts != null) {
                for (BundleDescription conflict : conflicts) {
                    state.removeBundle(conflict);
                    logger
                        .warn(conflict.toString() + " has been replaced by another bundle with the same symbolic name "
                            + descriptor.toString());
                }
            }
        }

        state.addBundle(descriptor);
        return descriptor;
    }

    public BundleDescription getResolvedBundle(String bundleId) {
        BundleDescription[] description = state.getBundles(bundleId);
        if (description == null)
            return null;
        for (int i = 0; i < description.length; i++) {
            if (description[i].isResolved())
                return description[i];
        }
        return null;
    }

    public void resolveState() {
        state.resolve(false);

        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Resolved OSGi state\n");
            for (BundleDescription bundle : state.getBundles()) {
                if (!bundle.isResolved()) {
                    sb.append("[X] ");
                } else {
                    sb.append("[V] ");
                }
                sb.append(bundle).append(": (").append(bundle.getLocation());
                sb.append(")\n");
                for (ResolverError error : state.getResolverErrors(bundle)) {
                    sb.append("    ").append(error.toString()).append('\n');
                }
            }
            logger.debug(sb.toString());
        }
    }

    public State getState() {
        return state;
    }

    public BundleDescription[] getBundles() {
        return state.getBundles();
    }

    public ResolverError[] getResolverErrors(BundleDescription bundle) {
        Set<ResolverError> errors = new LinkedHashSet<ResolverError>();
        getRelevantErrors(errors, bundle);
        return (ResolverError[])errors.toArray(new ResolverError[errors.size()]);
    }

    private void getRelevantErrors(Set<ResolverError> errors, BundleDescription bundle) {
        ResolverError[] bundleErrors = state.getResolverErrors(bundle);
        for (int j = 0; j < bundleErrors.length; j++) {
            ResolverError error = bundleErrors[j];
            errors.add(error);

            VersionConstraint constraint = error.getUnsatisfiedConstraint();
            if (constraint instanceof BundleSpecification || constraint instanceof HostSpecification) {
                BundleDescription[] requiredBundles = state.getBundles(constraint.getName());
                for (int i = 0; i < requiredBundles.length; i++) {
                    getRelevantErrors(errors, requiredBundles[i]);
                }
            }
        }
    }

    private void logError(BundleDescription bundle, int level, Object object) {
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < level; i++) {
            msg.append("--");
        }
        msg.append("> [").append(bundle.getSymbolicName()).append("] ");
        msg.append(object);
        logger.error(msg.toString());
    }

    public void analyzeErrors(BundleDescription bundle) {
        analyzeErrors(bundle, new HashSet<BundleDescription>(), 1);
    }

    private void analyzeErrors(BundleDescription bundle, Set<BundleDescription> bundles, int level) {
        if (bundles.contains(bundle)) {
            return;
        }
        bundles.add(bundle);
        ResolverError[] errors = state.getResolverErrors(bundle);
        for (ResolverError error : errors) {
            logError(bundle, level, error);
            VersionConstraint constraint = error.getUnsatisfiedConstraint();
            switch (error.getType()) {
                case MISSING_IMPORT_PACKAGE:
                    ImportPackageSpecification pkgSpec = (ImportPackageSpecification)constraint;
                    for (BundleDescription b : getBundles()) {
                        for (ExportPackageDescription pkg : b.getExportPackages()) {
                            if (pkg.getName().equals(pkgSpec.getName())) {
                                if (pkgSpec.getVersionRange().isIncluded(pkg.getVersion())) {
                                    if (!pkg.getExporter().isResolved()) {
                                        logError(b, level, "Bundle unresolved: " + pkg);
                                        analyzeErrors(pkg.getExporter(), bundles, level + 1);
                                    }
                                } else {
                                    logError(b, level, "Version mismatch: " + pkgSpec + " " + pkg);
                                }
                            }
                        }
                    }
                    break;
                case MISSING_REQUIRE_BUNDLE:
                case MISSING_FRAGMENT_HOST:
                    // BundleSpecification bundleSpec = (BundleSpecification)constraint;
                    for (BundleDescription b : getBundles()) {
                        if (b == bundle) {
                            continue;
                        }
                        if (b.getSymbolicName() == null) {
                            logError(b, level, "No SymbolicName for " + b.getLocation());
                            continue;
                        }
                        if (constraint.getName() == null) {
                            logError(bundle, level, "no constraint name: " + constraint);
                        }
                        if (b.getSymbolicName().equals(constraint.getName())) {
                            if (constraint.getVersionRange().isIncluded(b.getVersion())) {
                                // There must be something wrong in the bundle
                                analyzeErrors(b, bundles, level);
                            } else {
                                logError(bundle, level, "Version mismatch: " + constraint + " " + b);
                            }
                        }
                    }
                    break;
                case IMPORT_PACKAGE_USES_CONFLICT:
                    ImportPackageSpecification importPackage = (ImportPackageSpecification)constraint;
                    for (BundleDescription b : getBundles()) {
                        for (ExportPackageDescription pkg : b.getExportPackages()) {
                            if (pkg.getName().equals(importPackage.getName())) {
                                logError(pkg.getExporter(), level + 1, pkg.toString());
                            }
                        }
                    }
                    break;
                case REQUIRE_BUNDLE_USES_CONFLICT:
                default:   
                    // error is already logged
                    break;
            }
        }

    }

    public Set<ResolverError> getAllErrors() {
        BundleDescription[] bundles = state.getBundles();
        Set<ResolverError> errors = new LinkedHashSet<ResolverError>();
        for (int i = 0; i < bundles.length; i++) {
            BundleDescription bundle = bundles[i];
            ResolverError[] bundleErrors = state.getResolverErrors(bundle);
            if (bundleErrors != null) {
                errors.addAll(Arrays.asList(bundleErrors));
            }
        }
        return errors;
    }

    public List<BundleDescription> getDependencies(BundleDescription desc) {
        Set<Long> bundleIds = new LinkedHashSet<Long>();
        addBundleAndDependencies(desc, bundleIds, true);
        List<BundleDescription> dependencies = new ArrayList<BundleDescription>();
        for (long bundleId : bundleIds) {
            if (desc.getBundleId() != bundleId) {
                BundleDescription dependency = state.getBundle(bundleId);
                BundleDescription supplier = dependency.getSupplier().getSupplier();
                HostSpecification host = supplier.getHost();
                if (host == null || !desc.equals(host.getSupplier())) {
                    dependencies.add(dependency);
                }
            }
        }
        return dependencies;
    }

    /**
     * Code below is copy&paste from org.eclipse.pde.internal.core.DependencyManager
     * which seems to calculate runtime dependencies. In particular, it adds
     * fragments' dependencies to the host bundle (see TychoTest#testFragment unit test).
     * This may or may not cause problems...
     * 
     * RequiredPluginsClasspathContainer#computePluginEntries has the logic to
     * calculate compile-time dependencies in IDE.
     * 
     * TODO find the code used by PDE/Build  
     */
    private static void addBundleAndDependencies(BundleDescription desc, Set<Long> bundleIds, boolean includeOptional) {
        if (desc != null && bundleIds.add(new Long(desc.getBundleId()))) {
            BundleSpecification[] required = desc.getRequiredBundles();
            for (int i = 0; i < required.length; i++) {
                if (includeOptional || !required[i].isOptional())
                    addBundleAndDependencies((BundleDescription)required[i].getSupplier(), bundleIds, includeOptional);
            }
            ImportPackageSpecification[] importedPkgs = desc.getImportPackages();
            for (int i = 0; i < importedPkgs.length; i++) {
                ExportPackageDescription exporter = (ExportPackageDescription)importedPkgs[i].getSupplier();
                // Continue if the Imported Package is unresolved of the package is optional and don't want optional packages
                if (exporter == null || (!includeOptional && Constants.RESOLUTION_OPTIONAL.equals(importedPkgs[i]
                    .getDirective(Constants.RESOLUTION_DIRECTIVE))))
                    continue;
                addBundleAndDependencies(exporter.getExporter(), bundleIds, includeOptional);
            }
            BundleDescription[] fragments = desc.getFragments();
            for (int i = 0; i < fragments.length; i++) {
                if (!fragments[i].isResolved())
                    continue;
                String id = fragments[i].getSymbolicName();
                if (!"org.eclipse.ui.workbench.compatibility".equals(id)) //$NON-NLS-1$
                    addBundleAndDependencies(fragments[i], bundleIds, includeOptional);
            }
            HostSpecification host = desc.getHost();
            if (host != null)
                addBundleAndDependencies((BundleDescription)host.getSupplier(), bundleIds, includeOptional);
        }
    }

    public BundleDescription getBundleDescription(MavenProject project) {
        String location = project.getFile().getParentFile().getAbsolutePath();
        return state.getBundleByLocation(location);
    }

    public BundleDescription getBundleDescription(File location) {
        String absolutePath = location.getAbsolutePath();
        return state.getBundleByLocation(absolutePath);
    }

    private static void setUserProperty(BundleDescription desc, String name, Object value) {
        Object userObject = desc.getUserObject();

        if (userObject != null && !(userObject instanceof Map)) {
            throw new IllegalStateException("Unexpected user object " + desc.toString());
        }

        Map props = (Map)userObject;
        if (props == null) {
            props = new HashMap();
            desc.setUserObject(props);
        }

        props.put(name, value);
    }

    private static Object getUserProperty(BundleDescription desc, String name) {
        Object userObject = desc.getUserObject();
        if (userObject instanceof Map) {
            return ((Map)userObject).get(name);
        }
        return null;
    }

    public MavenProject getMavenProject(BundleDescription desc) {
        return (MavenProject)getUserProperty(desc, PROP_MAVEN_PROJECT);
    }

    public void assertResolved(BundleDescription desc) throws BundleException {
        if (!desc.isResolved()) {
            throw new BundleException("Bundle cannot be resolved: " + desc);
        }
    }

    public String reportErrors(BundleDescription desc) {
        StringBuilder msg = new StringBuilder();
        msg.append("Bundle ").append(desc.getSymbolicName()).append(" cannot be resolved: \n");
        BundleDescription[] bundles = state.getBundles();
        int index = 0;
        for (BundleDescription b : bundles) {
            if (b.isResolved()) {
                continue;
            }
            ResolverError[] errors = state.getResolverErrors(b);
            if (errors.length > 0) {
                msg.append("  ").append("[").append(index++).append("] ").append(b.getSymbolicName()).append("\n");
            }
            for (int i = 0; i < errors.length; i++) {
                ResolverError error = errors[i];
                msg.append("  -->").append(error).append("\n");
            }
        }
        return msg.toString();
    }
    
    public String getManifestAttribute(BundleDescription desc, String attr) {
        Dictionary mf = (Dictionary)getUserProperty(desc, PROP_MANIFEST);
        if (mf != null) {
            return (String)mf.get(attr);
        }
        return null;
    }

    private static File toFile(URL url) {
        if (url.getProtocol().equals("file") == false) {
            return null;
        } else {
            String filename = url.getFile().replace('/', File.separatorChar).replace("%20", " ");
            return new File(filename);
        }
    }
}
