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
package org.apache.geronimo.tomcat.cluster;

import java.util.Map;

import org.apache.catalina.tribes.transport.MultiPointSender;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.tomcat.BaseGBean;
import org.apache.geronimo.tomcat.ObjectRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class TransportGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {
    
    private static final Logger log = LoggerFactory.getLogger(TransportGBean.class);
    
    public static final String J2EE_TYPE = "Transport";
    
    private final MultiPointSender transport;

    public TransportGBean() {
        this.transport = null;
    }
    public TransportGBean(String className, Map initParams) throws Exception {
        super();
        
        if (className == null) {
            throw new IllegalArgumentException("Must have a 'className' attribute.");
        }
        
        transport = (MultiPointSender) Class.forName(className).newInstance();
        
        setParameters(transport, initParams);
    }

    public void doFail() {
        log.debug("Failed");
    }

    public void doStart() throws Exception {
        log.debug("Started Transport GBean");
    }

    public void doStop() throws Exception {
        log.debug("Stopped Transport GBean");
    }

    public Object getInternalObject() {
        return transport;
    }

    
    public final static GBeanInfo GBEAN_INFO;
    
    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Transport", TransportGBean.class, J2EE_TYPE);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addOperation("getInternalObject", "Object");
        infoFactory.setConstructor(new String[] { "className", "initParams" });
        GBEAN_INFO = infoFactory.getBeanInfo();        
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}



