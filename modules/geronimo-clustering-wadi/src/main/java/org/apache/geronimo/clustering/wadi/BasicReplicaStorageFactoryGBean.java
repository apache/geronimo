package org.apache.geronimo.clustering.wadi;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.codehaus.wadi.group.Dispatcher;
import org.codehaus.wadi.replication.storage.ReplicaStorage;
import org.codehaus.wadi.replication.storage.ReplicaStorageFactory;
import org.codehaus.wadi.replication.storage.basic.BasicReplicaStorageFactory;

public class BasicReplicaStorageFactoryGBean implements ReplicaStorageFactory, GBeanLifecycle {
    private BasicReplicaStorageFactory storageFactory;
    
    public ReplicaStorage factory(Dispatcher dispatcher) {
        return storageFactory.factory(dispatcher);
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
