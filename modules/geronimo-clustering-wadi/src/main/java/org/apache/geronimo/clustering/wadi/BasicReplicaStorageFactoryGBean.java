/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.clustering.wadi;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.codehaus.wadi.replication.storage.ReplicaStorage;
import org.codehaus.wadi.replication.storage.ReplicaStorageFactory;
import org.codehaus.wadi.replication.storage.basic.BasicReplicaStorageFactory;
import org.codehaus.wadi.servicespace.ServiceSpace;

/**
 *
 * @version $Rev$ $Date$
 */
public class BasicReplicaStorageFactoryGBean implements ReplicaStorageFactory, GBeanLifecycle {
    private BasicReplicaStorageFactory storageFactory;
    
    public ReplicaStorage factory(ServiceSpace serviceSpace) {
        return storageFactory.factory(serviceSpace);
    }

    public void doFail() {
        storageFactory = null;
    }

    public void doStart() throws Exception {
        storageFactory = new BasicReplicaStorageFactory();
    }

    public void doStop() throws Exception {
        storageFactory = null;
    }

    
    public static final GBeanInfo GBEAN_INFO;
    
    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(BasicReplicaStorageFactoryGBean.class, 
                NameFactory.GERONIMO_SERVICE);
        
        infoBuilder.addInterface(ReplicaStorageFactory.class);
        
        infoBuilder.setConstructor(new String[0]);
        
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
