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

package org.apache.geronimo.kernel.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.osgi.framework.BundleContext;

import com.agical.rmock.extension.junit.RMockTestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class EditableKernelConfigurationManagerTest extends RMockTestCase {

    private BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), null, null, null);

    private ConfigurationStore storeA;
    private ConfigurationStore storeB;
    private KernelConfigurationManager manager;
    private Artifact artifact;
    private Collection<ConfigurationStore> stores;
    private AbstractName nameA;
    private AbstractName nameB;

    @Override
    protected void setUp() throws Exception {
        Kernel kernel = (Kernel) mock(Kernel.class);
        
        ManageableAttributeStore attributeStore = (ManageableAttributeStore) mock(ManageableAttributeStore.class);
        PersistentConfigurationList configurationList = (PersistentConfigurationList) mock(PersistentConfigurationList.class);
        ArtifactManager artifactManager = (ArtifactManager) mock(ArtifactManager.class);
        ArtifactResolver artifactResolver = (ArtifactResolver) mock(ArtifactResolver.class);
        
        storeA = (ConfigurationStore) mock(ConfigurationStore.class, "ConfigurationStoreA");
        storeB = (ConfigurationStore) mock(ConfigurationStore.class, "ConfigurationStoreB");
        
        stores = new ArrayList<ConfigurationStore>();
        
        artifact = new Artifact("groupId", "artifactId", "2.0", "car");

        storeA.getAbstractName();
        nameA = new AbstractName(artifact, Collections.singletonMap("name", "A"));
        modify().multiplicity(expect.from(0)).returnValue(nameA);
        
        storeB.getAbstractName();
        nameB = new AbstractName(artifact, Collections.singletonMap("name", "B"));
        modify().multiplicity(expect.from(0)).returnValue(nameB);

        manager = new KernelConfigurationManager(kernel,
            stores,
            attributeStore,
            configurationList,
            artifactManager,
            artifactResolver,
            Collections.EMPTY_LIST,
            Collections.EMPTY_LIST,
                bundleContext);
    }

    //TODO this test is not valid for KernelConfigurationManager
    public void testThatFirstStoreOfListStoresIsDefaultStore() throws Exception {
//        stores.add(storeA);
//        stores.add(storeB);
        manager.bindConfigurationStore(storeA);
        manager.bindConfigurationStore(storeB);

        startVerification();

        List listStores = manager.listStores();
        assertEquals(2, listStores.size());
//        assertEquals(nameB, listStores.get(0));
//        assertEquals(nameA, listStores.get(1));
    }
    
}
