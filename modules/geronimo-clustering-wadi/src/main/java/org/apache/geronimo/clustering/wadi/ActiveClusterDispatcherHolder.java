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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.clustering.Node;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.codehaus.wadi.activecluster.ActiveClusterDispatcher;
import org.codehaus.wadi.group.Dispatcher;
import org.codehaus.wadi.group.MessageExchangeException;

/**
 *
 * @version $Rev$ $Date$
 */
public class ActiveClusterDispatcherHolder implements GBeanLifecycle, DispatcherHolder {
    private static final Log log = LogFactory.getLog(ActiveClusterDispatcher.class); 
    
    private final String clusterName;
    private final String clusterUri;
    private final long inactiveTime;
    private final Node node;

    private ActiveClusterDispatcher dispatcher;

    public ActiveClusterDispatcherHolder(String clusterName, String clusterUri, long inactiveTime, Node node) {
        this.clusterName = clusterName;
        this.clusterUri = clusterUri;
        this.inactiveTime = inactiveTime;
        this.node = node;
    }

    public void doStart() throws Exception {
        dispatcher = new ActiveClusterDispatcher(clusterName, node.getName(), clusterUri, inactiveTime);
        dispatcher.start();
    }

    public void doStop() throws Exception {
        dispatcher.stop();
    }

    public void doFail() {
        try {
            dispatcher.stop();
        } catch (MessageExchangeException e) {
            log.error(e);
        }
    }
    
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public Node getNode() {
        return node;
    }
    
    
    public static final GBeanInfo GBEAN_INFO;
    
    public static final String GBEAN_ATTR_CLUSTER_NAME = "clusterName";
    public static final String GBEAN_ATTR_CLUSTER_URI = "clusterUri";
    public static final String GBEAN_ATTR_INACTIVE_TIME = "inactiveTime";

    public static final String GBEAN_REF_NODE = "Node";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ActiveClusterDispatcherHolder.class, 
                NameFactory.GERONIMO_SERVICE);
        
        infoBuilder.addAttribute(GBEAN_ATTR_CLUSTER_NAME, String.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_CLUSTER_URI, String.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_INACTIVE_TIME, long.class, true);
        
        infoBuilder.addReference(GBEAN_REF_NODE, Node.class, NameFactory.GERONIMO_SERVICE);

        infoBuilder.addInterface(DispatcherHolder.class);
        
        infoBuilder.setConstructor(new String[] { GBEAN_ATTR_CLUSTER_NAME, 
                GBEAN_ATTR_CLUSTER_URI,
                GBEAN_ATTR_INACTIVE_TIME, 
                GBEAN_REF_NODE });
        
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
