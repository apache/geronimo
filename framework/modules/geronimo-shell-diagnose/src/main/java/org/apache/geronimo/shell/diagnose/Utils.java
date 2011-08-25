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

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.VersionConstraint;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;

public class Utils {

    public static final String PADDING = "  ";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    public static String error(String msg) {
        return Ansi.ansi().fg(Color.RED).a(msg).reset().toString();
    }
    
    public static String warning(String msg) {
        return Ansi.ansi().fg(Color.YELLOW).a(msg).reset().toString();
    }
    
    public static String formatMessage(int level, String message) {
        return formatMessage(level, message, null);
    }
    
    public static String formatErrorMessage(int level, String message) {
        return formatMessage(level, message, Color.RED);
    }
    
    public static String formatWarningMessage(int level, String message) {
        return formatMessage(level, message, Color.YELLOW);
    }
       
    private static String formatMessage(int level, String message, Color color) {
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < level; i++) {
            msg.append(PADDING);
        }
        if (color != null) {
            msg.append(Ansi.ansi().fg(color).a(message).reset().toString());
        } else {
            msg.append(message);
        }
        return msg.toString();
    }
    
    public static void displayError(BundleDescription bundle, int level, String object) {
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < level; i++) {
            msg.append(PADDING);
        }
        if (bundle != null) {
            msg.append(error(bundleToString(bundle)));
        }
        if (object != null) {
            if (bundle != null) {
                msg.append(" ");
            }
            msg.append(error(object));
        }
        System.out.println(msg.toString());
    }
    
    public static String bundleToString(BundleDescription bundle) {
        return bundle.getSymbolicName() + " [" + bundle.getBundleId() + "]";            
    }
    
    public static String importPackageToString(ImportPackageSpecification importPackage) {
        return importPackage.getName() + "; version=\"" + importPackage.getVersionRange() + "\"";
    }
    
    public static String exportPackageToString(ExportPackageDescription exportPackage) {
        return exportPackage.getName() + "; version=\"" + exportPackage.getVersion() + "\"";
    }
            
    public static String versionToString(VersionConstraint constraint) {
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
