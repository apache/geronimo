/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.jmsmanager.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.AdminObjectWrapper;
import org.apache.geronimo.connector.AdminObjectWrapperGBean;
import org.apache.geronimo.console.core.jms.TopicBrowserGBean;
import org.apache.geronimo.console.jmsmanager.AbstractJMSManager;
import org.apache.geronimo.console.GeronimoVersion;
import org.apache.geronimo.gbean.DynamicGAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationUtil;

import javax.jms.Queue;
import javax.jms.Topic;
import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class CreateDestinationHandler extends AbstractJMSManager implements PortletResponseHandler {
    protected static Log log = LogFactory
            .getLog(CreateDestinationHandler.class);

    private static final List parentId = Arrays.asList(new URI[] {URI.create("geronimo/activemq-broker/" + GeronimoVersion.GERONIMO_VERSION + "/car")});

    static final GBeanInfo QUEUE_INFO;

    static final GBeanInfo TOPIC_INFO;

    static {
        GBeanInfoBuilder queueInfoBuilder = new GBeanInfoBuilder(
                AdminObjectWrapper.class, AdminObjectWrapperGBean.GBEAN_INFO);
        queueInfoBuilder.addAttribute(new DynamicGAttributeInfo("PhysicalName",
                String.class.getName(), true, true, true, true));
        QUEUE_INFO = queueInfoBuilder.getBeanInfo();
        GBeanInfoBuilder topicInfoBuilder = new GBeanInfoBuilder(
                AdminObjectWrapper.class, AdminObjectWrapperGBean.GBEAN_INFO);
        topicInfoBuilder.addAttribute(new DynamicGAttributeInfo("PhysicalName",
                String.class.getName(), true, true, true, true));
        TOPIC_INFO = topicInfoBuilder.getBeanInfo();
    }

    public void processAction(ActionRequest request, ActionResponse response)
            throws IOException, PortletException {
        String destinationName = request.getParameter(DESTINATION_NAME);
        String destinationPhysicalName = request
                .getParameter(DESTINATION_PHYSICAL_NAME);
        String destinationType = request.getParameter(DESTINATION_TYPE);
        String destinationApplicationName = request
                .getParameter(DESTINATION_APPLICATION_NAME);
        String destinationModuleName = request
                .getParameter(DESTINATION_MODULE_NAME);
        try {
            ObjectName adminObjectName = NameFactory.getComponentName(null,
                    null, destinationApplicationName, NameFactory.JCA_RESOURCE,
                    destinationModuleName, destinationName, null, baseContext);

            GBeanData adminObjectData;
            if (Topic.class.getName().equals(destinationType)) {
                adminObjectData = new GBeanData(adminObjectName, TOPIC_INFO);
                adminObjectData.setAttribute("adminObjectInterface", "javax.jms.Topic");
                adminObjectData.setAttribute("adminObjectClass", "org.activemq.message.ActiveMQTopic");
            } else if (Queue.class.getName().equals(destinationType)) {
                adminObjectData = new GBeanData(adminObjectName, QUEUE_INFO);
                adminObjectData.setAttribute("adminObjectInterface", "javax.jms.Queue");
                adminObjectData.setAttribute("adminObjectClass", "org.activemq.message.ActiveMQQueue.class");
            } else {
                throw new PortletException(
                        "Invalid choice destination, must be FQCL of Topic or Queue, not "
                                + destinationType);
            }
            adminObjectData.setAttribute("PhysicalName",
                    destinationPhysicalName);

            URI configId = new URI(BASE_CONFIG_URI + destinationName);

            ConfigurationManager configurationManager = ConfigurationUtil
                    .getConfigurationManager(kernel);
            List stores = configurationManager.listStores();
            assert stores.size() == 1 : "Piling one hack on another, this code only works with exactly one store";

            ObjectName storeName = (ObjectName) stores.iterator().next();
            File installDir = (File) kernel.invoke(storeName,
                    "createNewConfigurationDir");
            //DeploymentContext deploymentContext = new
            // DeploymentContext(installDir, configId,
            // ConfigurationModuleType.SERVICE, parentId, kernel);
            ConfigurationData configData = new ConfigurationData();
            configData.setId(configId);
            configData.setParentId(parentId);
            configData.setModuleType(ConfigurationModuleType.SERVICE);
            //deploymentContext.addGBean(adminObjectData);
            configData.addGBean(adminObjectData);
            // If we are adding a topic we have to add a browser so we can view
            // its messages later.
            if (Topic.class.getName().equals(destinationType)) {
                GBeanData tBrowserBeanData = new GBeanData(NameFactory
                        .getComponentName(null, null,
                                destinationApplicationName,
                                NameFactory.JCA_RESOURCE,
                                destinationModuleName, destinationName,
                                "TopicBrowser", baseContext),
                        TopicBrowserGBean.GBEAN_INFO);
                tBrowserBeanData
                        .setAttribute("subscriberName", destinationName);
                tBrowserBeanData.setReferencePattern(
                        "ConnectionFactoryWrapper", ObjectName
                                .getInstance(CONNECTION_FACTORY_NAME));
                tBrowserBeanData.setReferencePattern("TopicWrapper",
                        adminObjectName);

                configData.addGBean(tBrowserBeanData);
            }

            //saves it.
            //deploymentContext.close();
            kernel.invoke(storeName, "install", new Object[] {configData,
                    installDir}, new String[] {
                    ConfigurationData.class.getName(), File.class.getName() });

            configurationManager.load(configId);
            configurationManager.loadGBeans(configId);
            configurationManager.start(configId);

        } catch (Exception e) {
            log.error("problem", e);
        }
        response.setRenderParameter("processAction", "viewDestinations");
    }

}