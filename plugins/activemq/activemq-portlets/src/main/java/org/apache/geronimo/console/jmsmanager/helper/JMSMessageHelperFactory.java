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

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.portlet.PortletRequest;

import org.apache.geronimo.console.jmsmanager.DestinationStatistics;
import org.apache.geronimo.console.jmsmanager.JMSDestinationInfo;
import org.apache.geronimo.console.jmsmanager.JMSMessageInfo;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
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
                public DestinationStatistics getDestinationStatistics(PortletRequest portletRequest, JMSDestinationInfo destinationInfo) throws JMSException {
                    return new DestinationStatistics();
                }

                @Override
                protected JMSMessageInfo[] getMessagesFromTopic(PortletRequest portletRequest, JMSDestinationInfo destinationInfo, String selector) throws JMSException {
                    return new JMSMessageInfo[0];
                }

                @Override
                public void purge(PortletRequest portletRequest, JMSDestinationInfo destinationInfo) throws JMSException {
                    throw new UnsupportedOperationException("purge action is not supported for current vendor");
                }
            });
        } catch (Exception e) {
            log.warn("Failed to init JMS message helpers");
        }
    }

    public static JMSMessageHelper getMessageHelper(PortletRequest renderRequest, String resourceAdapterModuleName) {
        String vendorName = null;
        ResourceAdapterModule module = (ResourceAdapterModule)PortletManager.getManagementHelper(renderRequest).getObject(new AbstractName(URI.create(resourceAdapterModuleName)));
        if(module != null) {
            vendorName = module.getVendorName();
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
