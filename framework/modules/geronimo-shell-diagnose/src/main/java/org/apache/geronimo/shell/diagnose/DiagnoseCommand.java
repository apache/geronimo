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

import java.util.Iterator;
import java.util.List;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
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

@Command(scope = "osgi", name = "diagnose", description = "Diagnose common OSGi resolver problems")
public class DiagnoseCommand extends OsgiCommandSupport {

    @Argument(index = 0, name = "ids", description = "The list of bundle IDs separated by whitespaces", required = true, multiValued = true)
    List<Long> ids;
    
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
        System.out.println(bundle.getLocation() + " [" + bundle.getBundleId() + "]");

        StateHelper stateHelper = platformAdmin.getStateHelper();
        VersionConstraint[] unsatisfied = stateHelper.getUnsatisfiedConstraints(bundle);
        ResolverError[] resolverErrors = platformAdmin.getState(false).getResolverErrors(bundle);
        for (int i = 0; i < resolverErrors.length; i++) {
            if ((resolverErrors[i].getType() & (ResolverError.MISSING_FRAGMENT_HOST
                                                | ResolverError.MISSING_GENERIC_CAPABILITY
                                                | ResolverError.MISSING_IMPORT_PACKAGE 
                                                | ResolverError.MISSING_REQUIRE_BUNDLE)) != 0) {
                continue;
            }
            System.out.print("  ");
            System.out.println(resolverErrors[i].toString());
        }

        if (unsatisfied.length == 0 && resolverErrors.length == 0) {
            System.out.print("  ");
            System.out.println("No unresolved constraints.");
        }
        if (unsatisfied.length > 0) {
            System.out.print("  ");
            System.out.println("Unresolved direct constraints:");        
            for (int i = 0; i < unsatisfied.length; i++) {
                System.out.print("    ");
                System.out.println(getResolutionFailureMessage(unsatisfied[i]));
            }
        }

        VersionConstraint[] unsatisfiedLeaves = stateHelper.getUnsatisfiedLeaves(new BundleDescription[] { bundle });
        boolean foundLeaf = false;
        for (int i = 0; i < unsatisfiedLeaves.length; i++) {
            BundleDescription leafBundle = unsatisfiedLeaves[i].getBundle();
            if (leafBundle == bundle) {
                continue;
            }
            if (!foundLeaf) {
                foundLeaf = true;
                System.out.print("  ");
                System.out.println("Unresolved constraints in dependency chain:");
            }
            System.out.print("    ");
            System.out.println(leafBundle.getLocation() + " [" + leafBundle.getBundleId() + "]");
            System.out.print("      ");
            System.out.println(getResolutionFailureMessage(unsatisfiedLeaves[i]));
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
                return warning("Missing optionally required bundle" + toString(unsatisfied));
            } else {
                return error("Missing required bundle" + toString(unsatisfied));
            }
        } else if (unsatisfied instanceof HostSpecification) {
            return error("Missing host bundle " + toString(unsatisfied));
        } else {
            return error("Unknown problem");
        }
    }
        
    private static String toString(VersionConstraint constraint) {
        VersionRange versionRange = constraint.getVersionRange();
        if (versionRange == null) {
            return constraint.getName();
        } else {
            return constraint.getName() + '_' + versionRange;
        }
    }
        
    private static String error(String msg) {
        return Ansi.ansi().fg(Color.RED).a(msg).reset().toString();
    }
    
    private static String warning(String msg) {
        return Ansi.ansi().fg(Color.YELLOW).a(msg).reset().toString();
    }
}
