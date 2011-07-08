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

package org.apache.geronimo.mavenplugins.osgi;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.mavenplugins.osgi.utils.BundleResolver;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.HostSpecification;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.osgi.framework.BundleException;

/** 
 * @goal display-manifest
 */
public class DisplayManifestMojo extends AbstractLogEnabled implements Mojo {
    
    private final String TAB = "  ";

    private Log log;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;
        
    /**
     * Output directory.
     *
     * @parameter expression="${target}" default-value="${project.build.directory}/classes"
     * @required
     */
    protected File target = null;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        
        if (!target.exists()) {
            return;
        }
                
        BundleResolver stateController = new BundleResolver(getLogger());
                
        try {
            stateController.addBundle(target);
        } catch (BundleException e) {
            log.error(e.getMessage(), e);
        }        
        stateController.resolveState();
        BundleDescription b = stateController.getBundleDescription(target);                
        if (b != null) {
            displayManifest(b);
        }    
    }

    private void displayManifest(BundleDescription b) {
        System.out.println("Bundle: " + b.getSymbolicName());
        displayImports(b, ImportPackageSpecification.RESOLUTION_STATIC, "Imports:");
        displayImports(b, ImportPackageSpecification.RESOLUTION_OPTIONAL, "Optional Imports:");
        displayImports(b, ImportPackageSpecification.RESOLUTION_DYNAMIC, "Dynamic Imports:");
                            
        displayExports(b);
        
        displayRequiredBundles(b);
        
        displayFragments(b);
    }
    
    
    private void displayExports(BundleDescription b) {
        ExportPackageDescription[] exportPackages = b.getExportPackages();
        if (exportPackages != null && exportPackages.length > 0) {
            System.out.println("Exports:");
            for (ExportPackageDescription exportPackage : exportPackages) {
                StringBuilder buf = new StringBuilder();
                buf.append(TAB + exportPackage);
                
                Map attributes = exportPackage.getAttributes();
                if (attributes != null) {
                    attributes.remove("version");
                    if (!attributes.isEmpty()) {
                        buf.append(" Attributes: ").append(attributes);
                    }
                }
                Map directives = exportPackage.getDirectives();
                if (directives != null) {
                    directives.remove("uses");
                    directives.remove("x-equinox-ee");
                    directives.remove("x-internal");
                    if (!directives.isEmpty()) {
                        buf.append(" Directives: ").append(directives);
                    }
                }                
                
                System.out.println(buf);
                
                String [] list = (String[])exportPackage.getDirective("uses");
                if (list != null) {
                    System.out.println(TAB + TAB + "Uses: " + Arrays.asList(list));
                }
            }
            System.out.println();
        }
    }
    
    private void displayImports(BundleDescription b, String resolution, String header) {
        List<ImportPackageSpecification> imports = getImports(b, resolution);        
        if (!imports.isEmpty()) {
            System.out.println(header);
            for (ImportPackageSpecification importPackage : imports) {
                StringBuilder buf = new StringBuilder();
                buf.append(TAB + importPackage);
                
                Map attributes = importPackage.getAttributes();
                if (attributes != null) {
                    attributes.remove("version");
                    if (!attributes.isEmpty()) {
                        buf.append(" Attributes: ").append(attributes);
                    }
                }
                Map directives = importPackage.getDirectives();
                if (directives != null) {
                    directives.remove("resolution");
                    if (!directives.isEmpty()) {
                        buf.append(" Directives: ").append(directives);
                    }
                }                
                
                System.out.println(buf);
            }
            System.out.println();
        }
    }
    
    private List<ImportPackageSpecification> getImports(BundleDescription b, String resolution) {
        List<ImportPackageSpecification> imports = new ArrayList<ImportPackageSpecification>();       
        ImportPackageSpecification[] importPackages = b.getImportPackages();
        if (importPackages != null) {
            for (ImportPackageSpecification importPackage : importPackages) {
                String res = (String) importPackage.getDirective("resolution");
                if (resolution.equals(res)) {
                    imports.add(importPackage);
                }
            }
        }
        return imports;
    }
        
    private void displayRequiredBundles(BundleDescription b) {
        BundleSpecification[] bundles = b.getRequiredBundles();
        if (bundles != null && bundles.length > 0) {
            System.out.println("Requires Bundles:");
            for (BundleSpecification bundle : bundles) {
                System.out.println(TAB + bundle.getName()); 
            }
            System.out.println();
        }
    }
    
    private void displayFragments(BundleDescription b) {
        HostSpecification host = b.getHost();
        if (host != null) {
            System.out.println("Fragments:");
            System.out.println(TAB + host);
            System.out.println();
        }
    }
    
    public void setLog(Log log) {
        this.log = log;
    }

    public Log getLog() {
        if (log == null) {
            setLog(new SystemStreamLog());
        }
        return log;
    }
           
}
