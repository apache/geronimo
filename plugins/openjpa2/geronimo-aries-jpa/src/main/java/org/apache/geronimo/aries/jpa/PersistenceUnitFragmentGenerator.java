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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.aries.jpa.container.ManagedPersistenceUnitInfo;
import org.apache.aries.util.FragmentBuilder;
import org.apache.xbean.osgi.bundle.util.BundleDescription;
import org.apache.xbean.osgi.bundle.util.BundleDescription.ExportPackage;
import org.apache.xbean.osgi.bundle.util.BundleDescription.HeaderEntry;
import org.apache.xbean.osgi.bundle.util.BundleDescription.ImportPackage;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link ManagedPersistenceUnitInfoFactoryListener} that generates and installs
 * a fragment bundle that attaches to the persistence bundle. The generated fragment bundle imports 
 * every package exported by the JPA provider bundle.  
 */
public class PersistenceUnitFragmentGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceUnitFragmentGenerator.class);
    
    private Map<Bundle, Bundle> fragmentMap = Collections.synchronizedMap(new HashMap<Bundle, Bundle>());

    public void persistenceUnitMetadataCreated(BundleContext containerContext,
                                               Bundle persistenceBundle,
                                               ServiceReference providerReference,
                                               Collection<ManagedPersistenceUnitInfo> managedUnits) {
        if (persistenceBundle.getState() == Bundle.INSTALLED 
            && providerReference != null 
            && needsFragment(persistenceBundle, providerReference.getBundle())) {
            
            LOG.debug("Generating JPA fragment for persistence bundle {}", persistenceBundle.getSymbolicName());

            FragmentBuilder builder = new FragmentBuilder(persistenceBundle, ".jpa.fragment", "JPA Fragment");
            builder.addImportsFromExports(providerReference.getBundle());
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

    private boolean needsFragment(Bundle persistenceBundle, Bundle providerBundle) {
        BundleDescription description = new BundleDescription(persistenceBundle.getHeaders());
        
        for (HeaderEntry importPackage : description.getDynamicImportPackage()) {
            if ("*".equals(importPackage.getName())) {
                LOG.debug("JPA fragment will not be generated: Persistence bundle {} can load any class.", 
                          persistenceBundle.getSymbolicName());
                return false;
            }
        }
        
        BundleDescription providerDescription = new BundleDescription(providerBundle.getHeaders());
        for (ImportPackage importPackage : description.getImportPackage()) {
            for (ExportPackage exportPackage : providerDescription.getExportPackage()) {
                if (importPackage.getName().equals(exportPackage.getName())
                    && importPackage.getVersionRange().isInRange(exportPackage.getVersion())) {
                    LOG.debug("JPA fragment will not be generated: Persistence bundle {} already imports at least one package from JPA provider bundle.",
                              persistenceBundle.getSymbolicName());
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public void persistenceBundleDestroyed(BundleContext containerContext, Bundle persistenceBundle) {
        if (persistenceBundle.getState() == Bundle.UNINSTALLED) {
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
