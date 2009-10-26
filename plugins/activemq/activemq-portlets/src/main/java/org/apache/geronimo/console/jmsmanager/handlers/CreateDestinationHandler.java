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

package org.apache.geronimo.console.jmsmanager.handlers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.jms.Queue;
import javax.jms.Topic;
import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;

import org.apache.geronimo.console.jmsmanager.AbstractJMSManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.ImportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateDestinationHandler extends AbstractJMSManager implements PortletResponseHandler {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final Artifact parentId = new Artifact("geronimo", "activemq-broker", org.apache.geronimo.system.serverinfo.ServerConstants.getVersion(), "car");

//    static final GBeanInfo QUEUE_INFO;
//
//    static final GBeanInfo TOPIC_INFO;
//
//    static {
//        GBeanInfoBuilder queueInfoBuilder = new GBeanInfoBuilder(
//                AdminObjectWrapper.class, AdminObjectWrapperGBean.GBEAN_INFO);
//        queueInfoBuilder.addAttribute(new DynamicGAttributeInfo("PhysicalName",
//                String.class.getName(), true, true, true, true));
//        QUEUE_INFO = queueInfoBuilder.getBeanInfo();
//        GBeanInfoBuilder topicInfoBuilder = new GBeanInfoBuilder(
//                AdminObjectWrapper.class, AdminObjectWrapperGBean.GBEAN_INFO);
//        topicInfoBuilder.addAttribute(new DynamicGAttributeInfo("PhysicalName",
//                String.class.getName(), true, true, true, true));
//        TOPIC_INFO = topicInfoBuilder.getBeanInfo();
//    }

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

            Artifact configId = new Artifact(Artifact.DEFAULT_GROUP_ID, BASE_CONFIG_URI + destinationName, "0", "car");
            ConfigurationData configurationData = new ConfigurationData(configId, kernel.getNaming());
            configurationData.getEnvironment().addDependency(new Dependency(ACTIVEMQ_ARTIFACT, ImportType.ALL));

            AbstractName adminObjectName = kernel.getNaming().createRootName(configId, destinationName, NameFactory.JCA_ADMIN_OBJECT);
//            ObjectName adminObjectName = NameFactory.getComponentName(null,
//                    null, destinationApplicationName, NameFactory.JCA_RESOURCE,
//                    destinationModuleName, destinationName, null, baseContext);

            GBeanData adminObjectData;
            if (Topic.class.getName().equals(destinationType)) {
                adminObjectData = getTopicGBeanData();
                // If we are adding a topic we have to add a browser so we can view
                // its messages later.
                /*
                AbstractName browserName = kernel.getNaming().createChildName(adminObjectName, destinationName, "TopicBrowser");
                GBeanData tBrowserBeanData = new GBeanData(browserName, TopicBrowserGBean.GBEAN_INFO);
                tBrowserBeanData.setAttribute("subscriberName", destinationName);
                tBrowserBeanData.setReferencePattern("ConnectionFactoryWrapper", JCA_MANAGED_CONNECTION_FACTORY_NAME);
                tBrowserBeanData.setReferencePattern("TopicWrapper",
                        adminObjectName);

                configurationData.addGBean(tBrowserBeanData);
                */
            } else if (Queue.class.getName().equals(destinationType)) {
                adminObjectData = getQueueGBeanData();
            } else {
                throw new PortletException(
                        "Invalid choice destination, must be FQCL of Topic or Queue, not "
                                + destinationType);
            }
            adminObjectData.setAbstractName(adminObjectName);
            adminObjectData.setAttribute("PhysicalName",
                    destinationPhysicalName);
            configurationData.addGBean(adminObjectData);


            ConfigurationManager configurationManager = ConfigurationUtil
                    .getConfigurationManager(kernel);
            List stores = configurationManager.listStores();
            if (stores.isEmpty()) {
                throw new PortletException("No configuration store");
            }
            ObjectName storeName = (ObjectName) stores.get(0);
            File installDir = (File) kernel.invoke(storeName, "createNewConfigurationDir");
//            Environment environment = new Environment();
//            environment.setConfigId(configId);
//            environment.addDependency(parentId, ImportType.ALL);
//            List gbeans = new ArrayList();
//            gbeans.add(adminObjectData);
            //TODO configid FIXME set configurationDir correctly
            File configurationDir = null;
//            ConfigurationData configData = new ConfigurationData(ConfigurationModuleType.SERVICE,
//                    new LinkedHashSet(),
//                    gbeans,
//                    Collections.EMPTY_LIST,
//                    environment, configurationDir,
//                    kernel.getNaming());

            //saves it.
            //deploymentContext.close();
            kernel.invoke(storeName, "install", new Object[] {configurationData, installDir},
                    new String[] {ConfigurationData.class.getName(), File.class.getName() });

            configurationManager.loadConfiguration(configId);
            configurationManager.startConfiguration(configId);

        } catch (Exception e) {
            log.error("problem", e);
        }
        response.setRenderParameter("processAction", "viewDestinations");
    }

}
