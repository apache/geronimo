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

import org.apache.catalina.tribes.ChannelSender;
import org.apache.catalina.tribes.transport.MultiPointSender;
import org.apache.catalina.tribes.transport.ReplicationTransmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.tomcat.BaseGBean;
import org.apache.geronimo.tomcat.ObjectRetriever;

public class SenderGBean extends BaseGBean implements
        GBeanLifecycle, ObjectRetriever {

    private static final Logger log = LoggerFactory.getLogger(SenderGBean.class);

    public static final String J2EE_TYPE = "Sender";

    private final ChannelSender sender;
    
    public SenderGBean(){
        sender = null;
    }

    public SenderGBean(String className, Map initParams, TransportGBean transport) throws Exception {

        super(); // TODO: make it an attribute

        // Validate
        if (className == null) {
            throw new IllegalArgumentException("Must have a 'className' attribute.");
        }

        // Create the Sender object
        sender = (ChannelSender) Class.forName(className).newInstance();
        
        if (sender instanceof ReplicationTransmitter) {
            
            ReplicationTransmitter replicationTransmitter = (ReplicationTransmitter) sender;
            
            if (transport != null) {
                replicationTransmitter.setTransport((MultiPointSender)transport.getInternalObject());
            }
            
        } else {
            log.warn("Sender is not of type ReplicationTransmitter, no transport object will be set");
        }

        // Set the parameters
        setParameters(sender, initParams);

    }

    public Object getInternalObject() {
        return sender;
    }

    public void doFail() {
        log.warn("Failed");
    }

    public void doStart() throws Exception {
        log.debug("Started Sender service gbean.");
    }

    public void doStop() throws Exception {
        log.debug("Stopped Sender gbean.");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Sender", SenderGBean.class, J2EE_TYPE);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addReference("Transport", TransportGBean.class, TransportGBean.J2EE_TYPE);
        infoFactory.addOperation("getInternalObject", "Object");
        infoFactory.setConstructor(new String[] { "className", "initParams", "Transport" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
