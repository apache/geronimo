package org.apache.geronimo.clustering.wadi;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.codehaus.wadi.group.Dispatcher;
import org.codehaus.wadi.replication.manager.ReplicationManager;
import org.codehaus.wadi.replication.manager.ReplicationManagerFactory;
import org.codehaus.wadi.replication.manager.basic.BasicReplicationManagerFactory;
import org.codehaus.wadi.replication.storage.ReplicaStorageFactory;
import org.codehaus.wadi.replication.strategy.BackingStrategyFactory;

public class BasicReplicationManagerFactoryGBean implements ReplicationManagerFactory, GBeanLifecycle {
    private ReplicationManagerFactory factory;
    
    public ReplicationManager factory(Dispatcher dispatcher, ReplicaStorageFactory replicaStoragefactory,
            BackingStrategyFactory backingStrategyFactory) {
        return factory.factory(dispatcher, replicaStoragefactory, backingStrategyFactory);
    }
    
    public void doFail() {
        factory = null;
    }

    public void doStart() throws Exception {
        factory = new BasicReplicationManagerFactory();
    }

    public void doStop() throws Exception {
        factory = null;
    }
    
    public static final GBeanInfo GBEAN_INFO;
    
    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(BasicReplicationManagerFactoryGBean.class, 
                NameFactory.GERONIMO_SERVICE);

        infoBuilder.addInterface(ReplicationManagerFactory.class);
        
        infoBuilder.setConstructor(new String[0]);
        
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
