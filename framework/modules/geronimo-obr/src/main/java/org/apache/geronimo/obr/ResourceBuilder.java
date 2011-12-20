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
package org.apache.geronimo.obr;

import java.util.List;

import org.apache.xbean.osgi.bundle.util.BundleDescription;
import org.apache.xbean.osgi.bundle.util.VersionRange;
import org.apache.geronimo.obr.model.Capability;
import org.apache.geronimo.obr.model.P;
import org.apache.geronimo.obr.model.Require;
import org.apache.geronimo.obr.model.Resource;
import org.osgi.framework.Constants;

public class ResourceBuilder {
    
    private static String CAPABILITY_BUNDLE = "bundle";
    private static String CAPABILITY_PACKAGE = "package";
    private static String CAPABILITY_FRAGMENT = "fragment";
    
    private BundleDescription bundleDescription;

    public ResourceBuilder(BundleDescription bundleDescription) {
        this.bundleDescription = bundleDescription;
    }
        
    public Resource createResource()  {
        
        BundleDescription.SymbolicName symbolicName = bundleDescription.getSymbolicName();
        if (symbolicName == null) {
            // not a bundle
            return null;            
        }
        
        Resource resource = new Resource();
        resource.setId(String.valueOf(System.currentTimeMillis()));
        resource.setSymbolicname(symbolicName.getName());
               
        // Convert bundle manifest header attributes to resource properties.
        convertAttributesToProperties(resource);
            
        // Convert Import-Package declarations into requirements.
        convertImportPackageToRequirement(resource);
        
        // Convert Require-Bundle to requirements.
        convertRequireBundleToRequirement(resource);
        
        // Convert Bundle-RequireExecutionEnvironment into requirements
        convertRequireExecutionEnvironmentToRequirement(resource);
        
        // Convert Fragment-Host to requirement/extend.
        convertFragmentHostToExtends(resource);

        // Convert bundle to capability
        convertBundleToCapability(resource, symbolicName);
        
        // Convert Export-Package declarations into capabilities.
        convertExportPackageToCapability(resource);
             
        return resource;
    }
    
    private String getProperty(String name) {
        return (String) bundleDescription.getHeaders().get(name);
    }
    
    private void convertAttributesToProperties(Resource resource) {
        String bundleName = getProperty(Constants.BUNDLE_NAME);
        if (bundleName == null) {
            bundleName = resource.getSymbolicname();
        }
        resource.setPresentationname(bundleName);
        
        resource.setVersion(getProperty(Constants.BUNDLE_VERSION));
        resource.setDescription(getProperty(Constants.BUNDLE_DESCRIPTION));
        resource.setDocumentation(getProperty(Constants.BUNDLE_DOCURL));
        resource.setSource(getProperty("Bundle-Source"));
        resource.setLicense(getProperty("Bundle-License"));

        // resource.setCopyright(getProperty(Constants.BUNDLE_COPYRIGHT));
    }
       
    private void convertImportPackageToRequirement(Resource resource) {
        List<BundleDescription.ImportPackage> imports = bundleDescription.getImportPackage();
        for (BundleDescription.ImportPackage importPackage : imports) {
            Require require = new Require();
            require.setMultiple(false);
            require.setOptional(importPackage.isOptional());
            require.setName(CAPABILITY_PACKAGE);
            require.setContent("Import-Package: " + importPackage.getName());

            VersionRange range = importPackage.getVersionRange();
            String versionFilter = getVersionFilter(range);
            require.setFilter("(&(package=" + importPackage.getName() + ")" + versionFilter + ")");            

            resource.getRequire().add(require);
        }
    }
    
    private void convertRequireBundleToRequirement(Resource resource) {
        List<BundleDescription.RequireBundle> requireBundles = bundleDescription.getRequireBundle();
        for (BundleDescription.RequireBundle requireBundle : requireBundles) {
            Require require = new Require();
            require.setMultiple(false);
            require.setOptional(requireBundle.isOptional());
            require.setName(CAPABILITY_BUNDLE);
            require.setContent("Require-Bundle: " + requireBundle.getName());
            
            VersionRange range = requireBundle.getVersionRange();
            String versionExpression = getVersionFilter(range);
            require.setFilter("(&(symbolicname=" + requireBundle.getName() + ")" + versionExpression + ")");  
            
            resource.getRequire().add(require);
        }
    }
    
    private void convertRequireExecutionEnvironmentToRequirement(Resource resource) {
        String requiredEnvironments = getProperty(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT);
        if (requiredEnvironments != null) {
            String[] envs = requiredEnvironments.split(",");
            if (envs.length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("(|");
                for (String env : envs) {
                    sb.append("(ee=");
                    sb.append(env);
                    sb.append(")");
                }                
                sb.append(")");
                
                Require require = new Require();
                require.setName("ee");
                require.setContent("Execution Environment " + sb);
                require.setFilter(sb.toString());
                
                resource.getRequire().add(require);
            }
                        
        }
    }
    
    private void convertFragmentHostToExtends(Resource resource) {
        BundleDescription.FragmentHost fragment = bundleDescription.getFragmentHost();
        if (fragment != null) {
            /*
             * In RFC 112 the Fragment-Host is represented as a "<extend/>" element
             * not a "<require extend="true"/>" element. 
             */
            Require require = new Require(); 
            require.setExtend(true);
            require.setMultiple(false);
            require.setOptional(false);
            require.setName(CAPABILITY_BUNDLE);
            require.setContent("Required Host: " + fragment.getName());
            
            VersionRange range = fragment.getVersionRange();
            String versionExpression = getVersionFilter(range);
            require.setFilter("(&(symbolicname=" + fragment.getName() + ")" + versionExpression + ")");  
            
            resource.getRequire().add(require);
            
            // Add "fragment" capability
            Capability cap = new Capability();
            cap.setName(CAPABILITY_FRAGMENT);
            cap.getP().add(createP("host", null, fragment.getName()));
            // XXX: capability can't express a version range so always set to 0.0.0.
            cap.getP().add(createP("version", "version", "0.0.0"));
            resource.getCapability().add(cap);
        }
    }
    
    private void convertExportPackageToCapability(Resource resource) {
        List<BundleDescription.ExportPackage> exports = bundleDescription.getExportPackage();
        for (BundleDescription.ExportPackage exportPackage : exports) {
            Capability cap = new Capability();
            cap.setName(CAPABILITY_PACKAGE);
            cap.getP().add(createP("package", null, exportPackage.getName()));
            cap.getP().add(createP("version", "version", exportPackage.getVersion().toString()));
            resource.getCapability().add(cap);
        }
    }

    private void convertBundleToCapability(Resource resource, BundleDescription.SymbolicName symbolicName) {
        Capability cap = new Capability();
        cap.setName(CAPABILITY_BUNDLE);
        
        cap.getP().add(createP("symbolicname", null, symbolicName.getName()));
        cap.getP().add(createP("version", "version", bundleDescription.getVersion().toString()));
        cap.getP().add(createP("manifestversion", "version", (String) bundleDescription.getHeaders().get(Constants.BUNDLE_MANIFESTVERSION)));
        
        String attachment = symbolicName.getDirectives().get("fragment-attachment");
        if (attachment != null) {
            cap.getP().add(createP("fragment-attachment", null, attachment));
        }
        
        String singleton = symbolicName.getDirectives().get("singleton");
        if (singleton != null) {
            cap.getP().add(createP("singleton", null, singleton));
        }
        
        resource.getCapability().add(cap);
    }
    
    private P createP(String name, String type, String value) {
        P p = new P();
        p.setN(name);
        p.setV(value);
        p.setT(type);
        return p;
    }
    
    private String getVersionFilter(VersionRange range) {
        String low = range.isLowInclusive() ? "(version>=" + range.getLow() + ")"
                : "(!(version<=" + range.getLow() + "))";

        if (range.getHigh() != null) {
            String high = range.isHighInclusive() ? "(version<=" + range.getHigh() + ")"
                    : "(!(version>=" + range.getHigh() + "))";            
            return low + high;
        }
        
        return low; 
    }
}
