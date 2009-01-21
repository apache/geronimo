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
package org.apache.geronimo.console.jmsmanager.helper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Destination;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;

import org.apache.geronimo.console.jmsmanager.DestinationStatistics;
import org.apache.geronimo.console.jmsmanager.JMSMessageInfo;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class JMSMessageHelperFactory {
    private static final Logger log = LoggerFactory.getLogger(JMSMessageHelperFactory.class);

    private static final Map<String,JMSMessageHelper> vendorNameJMSMessageHelperMap = new ConcurrentHashMap<String,JMSMessageHelper>();

    public static final String DEFAULT_JMS_MESSAGEHELPER = "default_jms_messagehelper";

    public static final String ACTIVEMQ_JMS_MESSAGEHELPER = "activemq.org";

    static {
        try {
            vendorNameJMSMessageHelperMap.put(ACTIVEMQ_JMS_MESSAGEHELPER, new AmqJMSMessageHelper());
            vendorNameJMSMessageHelperMap.put(DEFAULT_JMS_MESSAGEHELPER, new JMSMessageHelper() {

                @Override
                public DestinationStatistics getDestinationStatistics(String brokerName, String destType,
                        String physicalName) {
                    return null;
                }

                @Override
                protected List<JMSMessageInfo> getMessagesFromTopic(RenderRequest request, Destination destination,
                        String adapterObjectName, String adminObjName, String physicalName) throws Exception {
                    return null;
                }

                @Override
                public void purge(PortletRequest request, String adapterObjectName, String adminObjName,
                        String physicalName) throws Exception {
                }
            });
        } catch (Exception e) {
            log.warn("Failed to init JMS message helpers");
        }
    }

    public static JMSMessageHelper getMessageHelper(PortletRequest renderRequest, String raName) {
        ResourceAdapterModule[] modules = PortletManager.getOutboundRAModules(renderRequest, new String[] {
                "javax.jms.ConnectionFactory",
                "javax.jms.QueueConnectionFactory",
                "javax.jms.TopicConnectionFactory" });
        String vendorName = null;
        for (int i = 0; i < modules.length; i++) {
            ResourceAdapterModule module = modules[i];
            String objectNameTemp = module.getObjectName();
            if (raName != null && raName.equals(objectNameTemp)) {
                vendorName = module.getVendorName();
            }
        }
        return getJMSMessageHelper(vendorName);
    }

    public static JMSMessageHelper getJMSMessageHelper(String vendorName) {
        JMSMessageHelper messageHelper = vendorNameJMSMessageHelperMap.get(vendorName);
        return messageHelper == null ? vendorNameJMSMessageHelperMap.get(DEFAULT_JMS_MESSAGEHELPER) : messageHelper;
    }
    
    public static void registerJMSMessageHelper(String vendorName, JMSMessageHelper jmsMessageHelper) {
        vendorNameJMSMessageHelperMap.put(vendorName, jmsMessageHelper);
    }
    
    public static JMSMessageHelper unregisterJMSMessageHelper(String vendorName){
        return vendorNameJMSMessageHelperMap.remove(vendorName);
    }
}
