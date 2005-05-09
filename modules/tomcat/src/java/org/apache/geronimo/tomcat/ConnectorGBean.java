/**
*
* Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.tomcat;

import java.util.Map;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

public class ConnectorGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {
    
    private final Connector connector;
    private final TomcatContainer container;

    public ConnectorGBean(String protocol, Map initParams, TomcatContainer container) throws Exception {
        super(); // TODO: make it an attribute
        
        if (container == null){
            throw new IllegalArgumentException("container cannot be null.");
        }
        
        this.container = container;
                
        //Create the Connector object
        connector = new Connector(protocol);
        
        //Set the parameters
        setParameters(connector, initParams);
        
    }

    public Object getInternalObject() {
        return connector;
    }

    public void doStart() throws LifecycleException {
        container.addConnector(connector);
        connector.start();
    }

    public void doStop() {
        container.removeConnector(connector);
    }

    public void doFail() {
        doStop();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("TomcatConnector", ConnectorGBean.class);
        infoFactory.addAttribute("protocol", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addReference("TomcatContainer", TomcatContainer.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addOperation("getInternalObject");
        infoFactory.setConstructor(new String[] { "protocol", "initParams", "TomcatContainer"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
