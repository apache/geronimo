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

import javax.portlet.PortletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.jmsmanager.DestinationStatistics;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;

/**
 * @version $Rev$ $Date$
 */
public class JMSMessageHelperFactory {
    private static final Logger log = LoggerFactory.getLogger(JMSMessageHelperFactory.class);

    public static JMSMessageHelper getMessageHelper(PortletRequest renderRequest, String raName) {
        JMSMessageHelper messageHelper = null;
        ResourceAdapterModule[] modules = PortletManager.getOutboundRAModules(renderRequest, new String[] {
                "javax.jms.ConnectionFactory", "javax.jms.QueueConnectionFactory",
                "javax.jms.TopicConnectionFactory", });
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

    public static JMSMessageHelper getJMSMessageHelper(String vendorName){
        JMSMessageHelper messageHelper = null;
        if ("activemq.org".equals(vendorName)) {
            try{
                Class class_ = Class
                    .forName("org.apache.geronimo.console.jmsmanager.helper.AmqJMSMessageHelper");
                messageHelper = (JMSMessageHelper) class_.newInstance();
            } catch (IllegalAccessException e) {
                log.error("Failed to load class", e);
            } catch (InstantiationException e) {
                log.error("Failed to load class", e);
            } catch (ClassNotFoundException e) {
                log.error("Failed to load class", e);
            }
        }
        if(messageHelper == null){
            messageHelper = new JMSMessageHelper() {
                public List getMessagesFromTopic(String type, String physicalQName) {
                    return null;
                }
 
                public void purge(PortletRequest renderRequest, String type, String physicalQName) {
                    return;
                }
                public  DestinationStatistics getDestinationStatistics(String destType,String physicalName){
                    return null;
                }
            };
         }
         return messageHelper;
     }
}
