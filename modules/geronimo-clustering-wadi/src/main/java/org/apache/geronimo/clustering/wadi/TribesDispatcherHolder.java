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
package org.apache.geronimo.clustering.wadi;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.clustering.Node;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.codehaus.wadi.group.Dispatcher;
import org.codehaus.wadi.group.MessageExchangeException;
import org.codehaus.wadi.tribes.TribesDispatcher;
import org.codehaus.wadi.web.impl.URIEndPoint;

/**
 *
 * @version $Rev$ $Date$
 */
public class TribesDispatcherHolder implements GBeanLifecycle, DispatcherHolder {
    private static final Log log = LogFactory.getLog(TribesDispatcherHolder.class); 
    
    private final URI endPointURI;
    private final String clusterName;
    private final Node node;

    private TribesDispatcher dispatcher;


    public TribesDispatcherHolder(URI endPointURI, String clusterName, Node node) {
        if (null == endPointURI) {
            throw new IllegalArgumentException("endPointURI is required");
        } else if (null == clusterName) {
            throw new IllegalArgumentException("clusterName is required");
        } else if (null == node) {
            throw new IllegalArgumentException("node is required");
        }
        this.endPointURI = endPointURI;
        this.clusterName = clusterName;
        this.node = node;
    }

    public void doStart() throws Exception {
        dispatcher = new TribesDispatcher(clusterName, node.getName(), new URIEndPoint(endPointURI));
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
    
    public static final String GBEAN_ATTR_END_POINT_URI = "endPointURI";
    public static final String GBEAN_ATTR_CLUSTER_NAME = "clusterName";
    public static final String GBEAN_ATTR_CLUSTER_URI = "clusterUri";

    public static final String GBEAN_REF_NODE = "Node";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(TribesDispatcherHolder.class, 
                NameFactory.GERONIMO_SERVICE);
        
        infoBuilder.addAttribute(GBEAN_ATTR_END_POINT_URI, URI.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_CLUSTER_NAME, String.class, true);
        
        infoBuilder.addReference(GBEAN_REF_NODE, Node.class, NameFactory.GERONIMO_SERVICE);

        infoBuilder.addInterface(DispatcherHolder.class);
        
        infoBuilder.setConstructor(new String[] {
                GBEAN_ATTR_END_POINT_URI,
                GBEAN_ATTR_CLUSTER_NAME,
                GBEAN_REF_NODE });
        
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
