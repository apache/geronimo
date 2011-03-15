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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
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
import org.eclipse.osgi.service.resolver.VersionRange;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
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
        System.out.println(bundleInfo(bundle));

        StateHelper stateHelper = platformAdmin.getStateHelper();
        VersionConstraint[] unsatisfied = stateHelper.getUnsatisfiedConstraints(bundle);        
        ResolverError[] resolverErrors = analyzeErrors(bundle, platformAdmin.getState(false));
        
        if (unsatisfied.length == 0 && resolverErrors.length == 0) {
            System.out.println("  No unresolved constraints.");
        }
        if (unsatisfied.length > 0) {
            System.out.println("  Unresolved constraints:");        
            for (int i = 0; i < unsatisfied.length; i++) {
                System.out.print("    ");
                System.out.println(getResolutionFailureMessage(unsatisfied[i]));
            }
        }
    }

    public static String getResolutionFailureMessage(VersionConstraint unsatisfied) {
        if (unsatisfied.isResolved())
            throw new IllegalArgumentException();
        if (unsatisfied instanceof ImportPackageSpecification) {
            String resolution = (String) ((ImportPackageSpecification) unsatisfied).getDirective(Constants.RESOLUTION_DIRECTIVE);
            if (ImportPackageSpecification.RESOLUTION_OPTIONAL.equals(resolution)) {
                return warning("Missing optionally imported package " + toString(unsatisfied));
            } else if (ImportPackageSpecification.RESOLUTION_DYNAMIC.equals(resolution)) {
                return warning("Missing dynamically imported package " + toString(unsatisfied));
            } else {
                return error("Missing imported package " + toString(unsatisfied));
            }
        } else if (unsatisfied instanceof BundleSpecification) {
            if (((BundleSpecification) unsatisfied).isOptional()) { 
                return warning("Missing optionally required bundle " + toString(unsatisfied));
            } else {
                return error("Missing required bundle " + toString(unsatisfied));
            }
        } else if (unsatisfied instanceof HostSpecification) {
            return error("Missing host bundle " + toString(unsatisfied));
        } else {
            return error("Unknown problem");
        }
    }
        
    private static String error(String msg) {
        return Ansi.ansi().fg(Color.RED).a(msg).reset().toString();
    }
    
    private static String warning(String msg) {
        return Ansi.ansi().fg(Color.YELLOW).a(msg).reset().toString();
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
            System.out.println("  Resolver errors:");
        }
        for (ResolverError error : errors) {
            displayError(bundle, level, error.toString());
            VersionConstraint constraint = error.getUnsatisfiedConstraint();
            switch (error.getType()) {
                case MISSING_IMPORT_PACKAGE:
                    ImportPackageSpecification pkgSpec = (ImportPackageSpecification)constraint;
                    for (BundleDescription b : state.getBundles()) {
                        for (ExportPackageDescription pkg : b.getExportPackages()) {
                            if (pkg.getName().equals(pkgSpec.getName())) {
                                if (pkgSpec.getVersionRange().isIncluded(pkg.getVersion())) {
                                    if (!pkg.getExporter().isResolved()) {
                                        displayError(b, level + 1, "Bundle unresolved: " + pkg);
                                        analyzeErrors(pkg.getExporter(), state, bundles, level + 1);
                                    }
                                } else {
                                    displayError(b, level + 1, "Version mismatch: " + pkgSpec + " " + pkg);
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
                            displayError(b, level, "No SymbolicName for " + b.getLocation());
                            continue;
                        }
                        if (constraint.getName() == null) {
                            displayError(bundle, level, "No constraint name: " + constraint);
                        }
                        if (b.getSymbolicName().equals(constraint.getName())) {
                            if (constraint.getVersionRange().isIncluded(b.getVersion())) {
                                // There must be something wrong in the bundle
                                analyzeErrors(b, state, bundles, level + 1);
                            } else {
                                displayError(bundle, level, "Version mismatch: " + constraint + " " + b);
                            }
                        }
                    }
                    break;
                case IMPORT_PACKAGE_USES_CONFLICT:
                    ImportPackageSpecification importPackage = (ImportPackageSpecification)constraint;
                    ExportPackageDescription pkg = findExportPackage(importPackage, state.getExportedPackages());
                    if (pkg != null) {
                        if (simple) {
                            displayError(pkg.getExporter(), level + 1, pkg.toString());
                        } else {
                            String[] uses = (String[]) pkg.getDirective("uses");
                            if (uses != null) {
                                for (String usePackageName : uses) {
                                    checkPackageConflict(importPackage, pkg, usePackageName, state, level + 1);
                                }
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
        return errors;
    }
    
    private void checkPackageConflict(ImportPackageSpecification importPackage, ExportPackageDescription wiredExportPackage, String usePackageName, State state, int level) {
        
        BundleDescription importing = importPackage.getBundle();
        BundleDescription exporting = wiredExportPackage.getExporter();
        
        // find import package the importing bundle has
        ImportPackageSpecification useImportPackage = findImportPackage(usePackageName, importing.getImportPackages());
        
        if (useImportPackage != null) {
            
            /* 
             * Find exported package the exporting bundle is wired to.
             * A package in "uses" clause can refer to an imported package or exported package so 
             * need to check both places.
             */
            ExportPackageDescription exportPackage = findExportPackage(usePackageName, exporting.getResolvedImports());
            if (exportPackage == null) {
                exportPackage = findExportPackage(usePackageName, exporting.getExportPackages());
            }
            
            ExportPackageDescription highestExportPackage = findExportPackage(useImportPackage, state.getExportedPackages());
            
            if (exportPackage.getExporter().getBundleId() != highestExportPackage.getExporter().getBundleId()) {
                
                displayError(null, level, "Found possible conflict for " + usePackageName + " package which is used by " + importPackage.getName() + " package:");
                
                displayError(importing, level + 1, useImportPackage + " is wiring to " + bundleInfo(highestExportPackage.getExporter()));
                displayError(importing, level + 1, importPackage + " is wiring to " + bundleInfo(wiredExportPackage.getExporter()));
                
                ImportPackageSpecification eImportPackage = findImportPackage(usePackageName, exporting.getImportPackages());
                if (eImportPackage == null) {
                    displayError(exporting, level + 2, exportPackage.toString());
                } else {
                    displayError(exporting, level + 2, eImportPackage + " is wiring to " + bundleInfo(exportPackage.getExporter()));
                }

            }
        }
        
    }
    
    private static ExportPackageDescription findExportPackage(String packageName, ExportPackageDescription[] exports) {
        for (ExportPackageDescription pkg : exports) {
            if (pkg.getName().equals(packageName)) {
                return pkg;
            }
        }
        return null;
    }
    
    private static ImportPackageSpecification findImportPackage(String packageName, ImportPackageSpecification[] imports) {
        for (ImportPackageSpecification pkg : imports) {
            if (pkg.getName().equals(packageName)) {
                return pkg;
            }
        }
        return null;
    }
    
    private static ExportPackageDescription findExportPackage(ImportPackageSpecification packageName, ExportPackageDescription[] exports) {
        List<ExportPackageDescription> matches = new ArrayList<ExportPackageDescription>(2);
        for (ExportPackageDescription pkg : exports) {
            if (packageName.getName().equals(pkg.getName()) && packageName.getVersionRange().isIncluded(pkg.getVersion())) {
                matches.add(pkg);
            }
        }
        int size = matches.size();
        if (size == 0) {
            return null;
        } else if (size == 1) {
            return matches.get(0);
        } else {
            Collections.sort(matches, ExportPackageComparator.INSTANCE);
            return matches.get(0);
        }      
    }
    
    private static class ExportPackageComparator implements Comparator<ExportPackageDescription> {
        
        static final ExportPackageComparator INSTANCE = new ExportPackageComparator();
        
        public int compare(ExportPackageDescription object1, ExportPackageDescription object2) {
            return object2.getVersion().compareTo(object1.getVersion());
        }            
    }
    
    private static void displayError(BundleDescription bundle, int level, String object) {
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < level; i++) {
            msg.append("  ");
        }
        if (bundle != null) {
            msg.append(error(bundleInfo(bundle)));
        }
        if (object != null) {
            if (bundle != null) {
                msg.append(" ");
            }
            msg.append(error(object));
        }
        System.out.println(msg.toString());
    }
    
    private static String bundleInfo(BundleDescription bundle) {
        return bundle.getSymbolicName() + " [" + bundle.getBundleId() + "]";
    }
            
    private static String toString(VersionConstraint constraint) {
        VersionRange versionRange = constraint.getVersionRange();
        if (versionRange == null) {
            return constraint.getName();
        } else {
            String versionAttribute;            
            if (constraint instanceof ImportPackageSpecification) {
                versionAttribute = "version=\"" + versionRange + "\"";
            } else {
                versionAttribute = "bundle-version=\"" + versionRange + "\"";
            }
            return constraint.getName() + "; " + versionAttribute;
        }
    }
    
}
