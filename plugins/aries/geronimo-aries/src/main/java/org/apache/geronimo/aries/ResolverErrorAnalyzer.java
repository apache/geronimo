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

package org.apache.geronimo.aries;

import static org.eclipse.osgi.service.resolver.ResolverError.IMPORT_PACKAGE_USES_CONFLICT;
import static org.eclipse.osgi.service.resolver.ResolverError.MISSING_FRAGMENT_HOST;
import static org.eclipse.osgi.service.resolver.ResolverError.MISSING_IMPORT_PACKAGE;
import static org.eclipse.osgi.service.resolver.ResolverError.MISSING_REQUIRE_BUNDLE;
import static org.eclipse.osgi.service.resolver.ResolverError.REQUIRE_BUNDLE_USES_CONFLICT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.VersionConstraint;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

/*
 * Collects resolver errors for a given set of bundles and trims out any errors caused by
 * dependent errors and so generates a list of "root" errors.
 */
public class ResolverErrorAnalyzer {

    private BundleContext bundleContext;
    
    public ResolverErrorAnalyzer(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
    
    private boolean hasPlatformAdmin() {
        try {
            bundleContext.getBundle().loadClass("org.eclipse.osgi.service.resolver.PlatformAdmin");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
        
    public String getErrorsAsString(Collection<Bundle> bundles) {
        Collection<String> errors = getErrors(bundles);
        if (errors.isEmpty()) {
            return null;
        } else {
            String LF = System.getProperty("line.separator");
            StringBuilder builder = new StringBuilder();
            builder.append("The following problems were detected:").append(LF);
            Iterator<String> iterator = errors.iterator();
            while (iterator.hasNext()) {
                builder.append("    ");
                builder.append(iterator.next());
                if (iterator.hasNext()) {
                    builder.append(LF);
                }
            }
            return builder.toString();
        }
    }
    
    public Collection<String> getErrors(Collection<Bundle> bundles) {
        if (!hasPlatformAdmin()) {
            return Collections.emptyList();
        }

        List<String> errors = new ArrayList<String>();
        
        ServiceReference ref = bundleContext.getServiceReference(PlatformAdmin.class.getName());
        try {
            PlatformAdmin platformAdmin = (PlatformAdmin) bundleContext.getService(ref);
            State systemState = platformAdmin.getState(false);
            List<BundleDescription> bundleDescriptions = new ArrayList<BundleDescription>(bundles.size());
            for (Bundle bundle : bundles) {
                BundleDescription bundleDescription = systemState.getBundle(bundle.getBundleId());
                if (bundleDescription != null) {
                    bundleDescriptions.add(bundleDescription);
                }
            }
            for (BundleDescription bundleDescription : bundleDescriptions) {
                collectErrors(bundleDescription, systemState, bundleDescriptions, errors);                
            }            
        } finally {
            bundleContext.ungetService(ref);
        }
        
        return errors;
    }
        
    private void collectErrors(BundleDescription bundle, State state, List<BundleDescription> bundleDescriptions, Collection<String> errorList) {
        ResolverError[] errors = state.getResolverErrors(bundle);
        for (ResolverError error : errors) {
            VersionConstraint constraint = error.getUnsatisfiedConstraint();
            switch (error.getType()) {
                case MISSING_IMPORT_PACKAGE:
                    ImportPackageSpecification importPackageSpecification = (ImportPackageSpecification) constraint;
                    String resolution = (String) importPackageSpecification.getDirective(Constants.RESOLUTION_DIRECTIVE);
                    if (ImportPackageSpecification.RESOLUTION_OPTIONAL.equals(resolution) || ImportPackageSpecification.RESOLUTION_DYNAMIC.equals(resolution)) {
                        // don't care about unsatisfied optional or dynamic imports
                        continue;
                    }
                    if (isSatisfied(importPackageSpecification, bundleDescriptions) == null) {
                        errorList.add("The package dependency " + versionToString(importPackageSpecification) + " required by bundle " + bundleToString(bundle) + " cannot be satisfied.");
                    }
                    break;
                case MISSING_REQUIRE_BUNDLE:
                    BundleSpecification bundleSpecification = (BundleSpecification) constraint;
                    if (bundleSpecification.isOptional()) {
                        // don't care about unsatisfied optional require-bundle
                        continue;
                    }
                    if (isSatisfied(bundleSpecification, bundleDescriptions) == null) {
                        errorList.add("The bundle dependency " + versionToString(bundleSpecification) + " required by bundle " + bundleToString(bundle) + " cannot be satisfied.");
                    }
                    break;
                case MISSING_FRAGMENT_HOST:
                    HostSpecification hostSpecification = (HostSpecification) constraint;
                    if (isSatisfied(hostSpecification, bundleDescriptions) == null) {
                        errorList.add("The host bundle dependency " + versionToString(hostSpecification) + " required by bundle " + bundleToString(bundle) + " cannot be satisfied.");
                    }
                    break;
                case IMPORT_PACKAGE_USES_CONFLICT:
                case REQUIRE_BUNDLE_USES_CONFLICT:
                default:   
                    errorList.add(error.toString());
                    break;
            }
        }
    }
    
    private static ExportPackageDescription isSatisfied(ImportPackageSpecification importPackageSpecification, Collection<BundleDescription> bundleDescriptions) {
        for (BundleDescription b : bundleDescriptions) {
            ExportPackageDescription[] exportedPackages = b.getExportPackages();
            if (exportedPackages != null) {
                for (ExportPackageDescription exportedPackage : exportedPackages) {
                    if (importPackageSpecification.isSatisfiedBy(exportedPackage)) {
                        return exportedPackage;
                    }
                }
            }
        }
        return null;
    }    
    
    private static BundleDescription isSatisfied(VersionConstraint constraint, Collection<BundleDescription> bundleDescriptions) {
        for (BundleDescription b : bundleDescriptions) {
            if (constraint.isSatisfiedBy(b)) {
                return b;
            }
        }
        return null;
    }

    private static String bundleToString(BundleDescription bundle) {
        return bundle.getSymbolicName() + " [" + bundle.getBundleId() + "]";            
    }
    
    private static String versionToString(VersionConstraint constraint) {
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
