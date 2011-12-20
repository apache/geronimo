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

package org.apache.geronimo.shell.diagnose;

import static org.eclipse.osgi.service.resolver.ResolverError.IMPORT_PACKAGE_USES_CONFLICT;
import static org.eclipse.osgi.service.resolver.ResolverError.MISSING_FRAGMENT_HOST;
import static org.eclipse.osgi.service.resolver.ResolverError.MISSING_IMPORT_PACKAGE;
import static org.eclipse.osgi.service.resolver.ResolverError.MISSING_REQUIRE_BUNDLE;
import static org.eclipse.osgi.service.resolver.ResolverError.REQUIRE_BUNDLE_USES_CONFLICT;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.StateHelper;
import org.eclipse.osgi.service.resolver.VersionConstraint;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

@Command(scope = "equinox", name = "diagnose", description = "Diagnose common OSGi resolver problems")
public class DiagnoseCommand extends OsgiCommandSupport {

    @Argument(index = 0, name = "ids", description = "The list of bundle IDs separated by whitespaces", required = true, multiValued = true)
    List<Long> ids;
    
    @Option(name = "-s", aliases = { "--simple" }, description = "Do not perform deeper analysis of resolver problems")
    boolean simple = false;
    
    private boolean hasPlatformAdmin() {
        try {
            bundleContext.getBundle().loadClass("org.eclipse.osgi.service.resolver.PlatformAdmin");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    protected Object doExecute() throws Exception {
        if (!hasPlatformAdmin()) {
            System.err.println("This command is only supported on Equinox.");
            return null;
        }
        ServiceReference ref = bundleContext.getServiceReference(PlatformAdmin.class.getName());
        PlatformAdmin platformAdmin = (PlatformAdmin) getService(PlatformAdmin.class, ref);

        try {
            State systemState = platformAdmin.getState(false);
            Iterator<Long> iterator = ids.iterator();
            while (iterator.hasNext()) {
                Long id = iterator.next();
                BundleDescription bundle = systemState.getBundle(id);
                if (bundle == null) {
                    System.err.println("Bundle ID" + id + " is invalid");
                    continue;
                }
                diagnose(bundle, platformAdmin);
                if (iterator.hasNext()) {
                    System.out.println();
                }
            }

        } finally {
            bundleContext.ungetService(ref);
        }

        return null;
    }
    
    private void diagnose(BundleDescription bundle, PlatformAdmin platformAdmin) {
        System.out.println(Utils.bundleToString(bundle));

        StateHelper stateHelper = platformAdmin.getStateHelper();
        VersionConstraint[] unsatisfied = stateHelper.getUnsatisfiedConstraints(bundle);        
        ResolverError[] resolverErrors = analyzeErrors(bundle, platformAdmin.getState(false));
        
        if (unsatisfied.length == 0 && resolverErrors.length == 0) {
            System.out.println(Utils.formatMessage(2, "No unresolved constraints."));
        }
        if (unsatisfied.length > 0) {
            System.out.println(Utils.formatMessage(2, "Unresolved constraints:"));        
            for (int i = 0; i < unsatisfied.length; i++) {
                System.out.println(Utils.formatMessage(3, getResolutionFailureMessage(unsatisfied[i])));
            }
        }
    }

    public static String getResolutionFailureMessage(VersionConstraint unsatisfied) {
        if (unsatisfied.isResolved())
            throw new IllegalArgumentException();
        if (unsatisfied instanceof ImportPackageSpecification) {
            String resolution = (String) ((ImportPackageSpecification) unsatisfied).getDirective(Constants.RESOLUTION_DIRECTIVE);
            if (ImportPackageSpecification.RESOLUTION_OPTIONAL.equals(resolution)) {
                return Utils.warning("Missing optionally imported package " + Utils.versionToString(unsatisfied));
            } else if (ImportPackageSpecification.RESOLUTION_DYNAMIC.equals(resolution)) {
                return Utils.warning("Missing dynamically imported package " + Utils.versionToString(unsatisfied));
            } else {
                return Utils.error("Missing imported package " + Utils.versionToString(unsatisfied));
            }
        } else if (unsatisfied instanceof BundleSpecification) {
            if (((BundleSpecification) unsatisfied).isOptional()) { 
                return Utils.warning("Missing optionally required bundle " + Utils.versionToString(unsatisfied));
            } else {
                return Utils.error("Missing required bundle " + Utils.versionToString(unsatisfied));
            }
        } else if (unsatisfied instanceof HostSpecification) {
            return Utils.error("Missing host bundle " + Utils.versionToString(unsatisfied));
        } else {
            return Utils.error("Unknown problem");
        }
    }
    
    public ResolverError[] analyzeErrors(BundleDescription bundle, State state) {
        return analyzeErrors(bundle, state, new HashSet<BundleDescription>(), 2);
    }

    private ResolverError[] analyzeErrors(BundleDescription bundle, State state, Set<BundleDescription> bundles, int level) {
        if (bundles.contains(bundle)) {
            return null;
        }
        
        bundles.add(bundle);
        ResolverError[] errors = state.getResolverErrors(bundle);
        if (level == 2 && errors.length > 0) {
            System.out.println(Utils.formatMessage(level, "Resolver errors:"));
        }
        PackageUsesHelper helper = null;
        for (ResolverError error : errors) {
            Utils.displayError(bundle, level, error.toString());
            VersionConstraint constraint = error.getUnsatisfiedConstraint();
            switch (error.getType()) {
                case MISSING_IMPORT_PACKAGE:
                    ImportPackageSpecification pkgSpec = (ImportPackageSpecification)constraint;
                    for (BundleDescription b : state.getBundles()) {
                        for (ExportPackageDescription pkg : b.getExportPackages()) {
                            if (pkg.getName().equals(pkgSpec.getName())) {
                                if (pkgSpec.getVersionRange().isIncluded(pkg.getVersion())) {
                                    if (!pkg.getExporter().isResolved()) {
                                        Utils.displayError(b, level + 1, "Bundle unresolved: " + pkg);
                                        analyzeErrors(pkg.getExporter(), state, bundles, level + 1);
                                    }
                                } else {
                                    Utils.displayError(b, level + 1, "Version mismatch: " + pkgSpec + " " + pkg);
                                }
                            }
                        }
                    }
                    break;
                case MISSING_REQUIRE_BUNDLE:
                case MISSING_FRAGMENT_HOST:
                    for (BundleDescription b : state.getBundles()) {
                        if (b == bundle) {
                            continue;
                        }
                        if (b.getSymbolicName() == null) {
                            Utils.displayError(b, level, "No SymbolicName for " + b.getLocation());
                            continue;
                        }
                        if (constraint.getName() == null) {
                            Utils.displayError(bundle, level, "No constraint name: " + constraint);
                        }
                        if (b.getSymbolicName().equals(constraint.getName())) {
                            if (constraint.getVersionRange().isIncluded(b.getVersion())) {
                                // There must be something wrong in the bundle
                                analyzeErrors(b, state, bundles, level + 1);
                            } else {
                                Utils.displayError(bundle, level, "Version mismatch: " + constraint + " " + b);
                            }
                        }
                    }
                    break;
                case IMPORT_PACKAGE_USES_CONFLICT:
                    if (!simple) {
                        // multiple conflicts can be reported on the same bundle
                        // so ensure helper only runs once.
                        if (helper == null) {
                            helper = new PackageUsesHelper(state);
                            helper.analyzeConflict(bundle, level);
                        }
                    }
                    break;
                case REQUIRE_BUNDLE_USES_CONFLICT:
                default:   
                    // error is already logged
                    break;
            }
        }
        return errors;
    }
   
}
