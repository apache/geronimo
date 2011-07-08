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
 * "AS IS" BASIS, WITHOUT WARRANTIESOR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.aries.jpa;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.aries.util.FragmentBuilder;
import org.apache.geronimo.hook.BundleHelper;
import org.apache.xbean.osgi.bundle.util.BundleDescription;
import org.apache.xbean.osgi.bundle.util.BundleDescription.ExportPackage;
import org.apache.xbean.osgi.bundle.util.BundleDescription.HeaderEntry;
import org.apache.xbean.osgi.bundle.util.BundleDescription.ImportPackage;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceBundleHelper {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceBundleHelper.class);
    
    private Map<Bundle, Bundle> fragmentMap = Collections.synchronizedMap(new HashMap<Bundle, Bundle>());

    public void addProviderImports(BundleContext containerContext,
                                   Bundle persistenceBundle,
                                   ServiceReference providerReference) {
        if (persistenceBundle.getState() == Bundle.INSTALLED && providerReference != null) {
            Bundle providerBundle = providerReference.getBundle();
            BundleDescription providerDescription = new BundleDescription(providerBundle.getHeaders());            
            if (needsProviderImports(persistenceBundle, providerDescription)) {
                
                StringBuilder providerConstraint = new StringBuilder();  
                providerConstraint.append(";");
                providerConstraint.append(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE);
                providerConstraint.append("=\"").append(providerBundle.getSymbolicName()).append("\"");
                providerConstraint.append(";");
                providerConstraint.append(Constants.BUNDLE_VERSION_ATTRIBUTE);
                String exportVersion = providerBundle.getVersion().toString();
                providerConstraint.append("=\"[").append(exportVersion).append(",").append(exportVersion).append("]\"");
                
                if (BundleHelper.isBundleExtenderSet()) {
                    LOG.debug("Adding DynamicImport-Package for persistence bundle {}", persistenceBundle.getSymbolicName());
                    
                    StringBuilder packageList = new StringBuilder();
                    Iterator<ExportPackage> iterator = providerDescription.getExportPackage().iterator();
                    while (iterator.hasNext()) {
                        ExportPackage exportPackage = iterator.next();
                        packageList.append(exportPackage.getName()).append(providerConstraint);
                        if (iterator.hasNext()) {
                            packageList.append(",");
                        }
                    }
                                        
                    BundleHelper.addDynamicImportPackage(persistenceBundle.getBundleId(), packageList.toString());
                } else {
                    LOG.debug("Generating JPA fragment for persistence bundle {}", persistenceBundle.getSymbolicName());
                    
                    FragmentBuilder builder = new FragmentBuilder(persistenceBundle, ".jpa.fragment", "JPA Fragment");                    
                    for (ExportPackage exportPackage : providerDescription.getExportPackage()) {
                        builder.addImports(exportPackage.getName() + providerConstraint);
                    }
                    
                    Bundle fragment;
                    try {
                        fragment = builder.install(containerContext);
                    } catch (IOException e) {
                        throw new RuntimeException("Error installing JPA fragment bundle", e);
                    } catch (BundleException e) {
                        throw new RuntimeException("Error installing JPA fragment bundle", e);
                    }
                    
                    fragmentMap.put(persistenceBundle, fragment);
                }
            }
        }
    }
    

    private boolean needsProviderImports(Bundle persistenceBundle, BundleDescription providerDescription) {
        BundleDescription description = new BundleDescription(persistenceBundle.getHeaders());
        
        for (HeaderEntry importPackage : description.getDynamicImportPackage()) {
            if ("*".equals(importPackage.getName())) {
                LOG.debug("Persistence bundle {} can load any class.", 
                          persistenceBundle.getSymbolicName());
                return false;
            }
        }
        
        for (ImportPackage importPackage : description.getImportPackage()) {
            for (ExportPackage exportPackage : providerDescription.getExportPackage()) {
                if (importPackage.getName().equals(exportPackage.getName())
                    && importPackage.getVersionRange().isInRange(exportPackage.getVersion())) {
                    LOG.debug("Persistence bundle {} already imports at least one package from JPA provider bundle.",
                              persistenceBundle.getSymbolicName());
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public void removeProviderImports(BundleContext containerContext, Bundle persistenceBundle) {
        if (persistenceBundle.getState() == Bundle.UNINSTALLED) {
            if (BundleHelper.isBundleExtenderSet()) {
                LOG.debug("Persistence bundle {} was uninstalled. Removing DynamicImport-Package from persistence bundle", 
                          persistenceBundle.getSymbolicName());
                
                BundleHelper.removeDynamicImportPackage(persistenceBundle.getBundleId());
            } else {
                LOG.debug("Persistence bundle {} was uninstalled. Uninstalling the corresponding JPA fragment bundle", 
                          persistenceBundle.getSymbolicName());
                
                Bundle fragment = fragmentMap.remove(persistenceBundle);
                if (fragment != null) {
                    try {
                        fragment.uninstall();
                    } catch (BundleException e) {
                        // ignore
                    }
                }
            }
        }
    }

}
