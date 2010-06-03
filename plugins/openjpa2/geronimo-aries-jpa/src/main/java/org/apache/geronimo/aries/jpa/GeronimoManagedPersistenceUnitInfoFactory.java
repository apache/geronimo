/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.aries.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.aries.jpa.container.ManagedPersistenceUnitInfo;
import org.apache.aries.jpa.container.parsing.ParsedPersistenceUnit;
import org.apache.aries.jpa.container.unit.impl.ManagedPersistenceUnitInfoFactoryImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class GeronimoManagedPersistenceUnitInfoFactory extends ManagedPersistenceUnitInfoFactoryImpl {
    
    private PersistenceBundleHelper helper = new PersistenceBundleHelper();
    
    private Map<Bundle, Collection<ManagedPersistenceUnitInfo>> map = 
        Collections.synchronizedMap(new HashMap<Bundle, Collection<ManagedPersistenceUnitInfo>>());
    
    @Override
    public Collection<ManagedPersistenceUnitInfo> createManagedPersistenceUnitMetadata(
            BundleContext containerContext, 
            Bundle persistenceBundle,
            ServiceReference providerReference,
            Collection<ParsedPersistenceUnit> persistenceMetadata) {
        
        Collection<ManagedPersistenceUnitInfo> managedUnits = new ArrayList<ManagedPersistenceUnitInfo>();    
        for (ParsedPersistenceUnit unit : persistenceMetadata) {
            managedUnits.add(new GeronimoManagedPersistenceUnitInfo(persistenceBundle, unit, providerReference));
        }
     
        helper.addProviderImports(containerContext, persistenceBundle, providerReference);
        
        map.put(persistenceBundle, managedUnits);
        
        return managedUnits;
    }
    
    @Override
    public void destroyPersistenceBundle(BundleContext containerContext, Bundle persistenceBundle) {
        Collection<ManagedPersistenceUnitInfo> managedUnits = map.remove(persistenceBundle);
        if (managedUnits != null) {
            for (ManagedPersistenceUnitInfo unit : managedUnits) {
                ((GeronimoManagedPersistenceUnitInfo) unit).destroy();
            }
            helper.removeProviderImports(containerContext, persistenceBundle);
        }
    }
    
}
