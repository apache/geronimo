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
package org.apache.geronimo.console.jmsmanager.wizard;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jms.JMSException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.jmsmanager.DestinationStatistics;
import org.apache.geronimo.console.jmsmanager.JMSDestinationInfo;
import org.apache.geronimo.console.jmsmanager.helper.JMSMessageHelper;
import org.apache.geronimo.console.jmsmanager.helper.JMSMessageHelperFactory;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.management.geronimo.JCAAdminObject;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAResource;
import org.apache.geronimo.management.geronimo.ResourceAdapter;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A handles for the front page that lists available resources.
 *
 * @version $Rev$ $Date$
 */
public class ListScreenHandler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(ListScreenHandler.class);

    public ListScreenHandler() {
        super(LIST_MODE, "/WEB-INF/view/jmswizard/list.jsp");
    }
    
    public ListScreenHandler(BasePortlet basePortlet) {
        super(LIST_MODE, "/WEB-INF/view/jmswizard/list.jsp", basePortlet);
    }
    
    public String actionBeforeView(ActionRequest actionRequest, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String purgeStr = actionRequest.getParameter(PURGE);
        if (purgeStr != null) {
            String physicalName = actionRequest.getParameter(PHYSICAL_NAME);
            String resourceAdapterModuleName = actionRequest.getParameter(RESOURCE_ADAPTER_MODULE_NAME);
            JMSMessageHelper helper = JMSMessageHelperFactory.getMessageHelper(actionRequest, resourceAdapterModuleName);
            try {
                helper.purge(actionRequest, JMSDestinationInfo.create(actionRequest));
            } catch (Exception e) {
                portlet.addErrorMessage(actionRequest, portlet.getLocalizedString(actionRequest, "activemq.errorMsg04", physicalName), e.getMessage());
                logger.error("Fail to purge message destination " + physicalName, e);
            }
        }
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        populateExistingList(request);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        JMSResourceData data = (JMSResourceData) model;
        String provider = request.getParameter(PROVIDER_PARAMETER);
        if (isEmpty(provider)) {
            return SELECT_PROVIDER_MODE + BEFORE_ACTION;
        }
        JMSProviderData providerData = JMSProviderData.getProviderByName(provider);
        data.setRarURI(providerData.getRaURI());
        data.setDependency(providerData.getDependency());
        return CONFIGURE_RA_MODE + BEFORE_ACTION;
    }


    private void populateExistingList(PortletRequest renderRequest) throws PortletException {       
        // Prepare a list of JMS configurations
        List<JMSResourceSummary> resources = new ArrayList<JMSResourceSummary>();

        // Get the list of connection factories
        ResourceAdapterModule[] modules = PortletManager.getOutboundRAModules(renderRequest, new String[]{
                "javax.jms.ConnectionFactory", "javax.jms.QueueConnectionFactory", "javax.jms.TopicConnectionFactory",});
        try {
            for (int i = 0; i < modules.length; i++) {
                ResourceAdapterModule module = modules[i];                 
                JMSResourceSummary target = null;
                ResourceAdapter[] adapters = PortletManager.getResourceAdapters(renderRequest, module);
                String name = null;
                if (adapters.length == 1 && adapters[0].getJCAResources().length == 1) {
                    JCAResource[] resource = PortletManager.getJCAResources(renderRequest, adapters[0]);
                    if (resource.length == 1 && resource[0].getResourceAdapterInstances().length == 1) {
                        name = ObjectName.getInstance(resource[0].getResourceAdapterInstanceNames()[0]).getKeyProperty(NameFactory.J2EE_NAME);
                    }
                }
                if (name == null) {
                    name = ObjectName.getInstance(module.getObjectName()).getKeyProperty(NameFactory.J2EE_NAME);
                }
                String configurationName = PortletManager.getConfigurationFor(renderRequest, PortletManager.getNameFor(renderRequest, module)).toString();
                String resourceAdapterModuleName = PortletManager.getNameFor(renderRequest, module).toString();
                target = new JMSResourceSummary(configurationName, module.getObjectName(), resourceAdapterModuleName, name);
                resources.add(target);
                JCAManagedConnectionFactory[] factories = PortletManager.getOutboundFactoriesForRA(renderRequest, module, new String[]{
                        "javax.jms.ConnectionFactory", "javax.jms.QueueConnectionFactory", "javax.jms.TopicConnectionFactory",});
                for (int j = 0; j < factories.length; j++) {
                    JCAManagedConnectionFactory factory = factories[j];
                    ObjectName objectName = ObjectName.getInstance(factory.getObjectName());
                    Kernel kernel = PortletManager.getKernel();
                    AbstractName abstractName = kernel.getAbstractNameFor(factory);
                    int state = kernel.getGBeanState(abstractName);
                    target.getConnectionFactories().add(new ConnectionFactorySummary(factory.getObjectName(), objectName.getKeyProperty(NameFactory.J2EE_NAME),
                            state));
                    String factoryInterface = factory.getConnectionInterface();
                    if (!target.isQueueConnectionFactoryContained()) {                        
                        target.setQueueConnectionFactoryContained(factoryInterface.equals("javax.jms.ConnectionFactory") || factoryInterface.equals("javax.jms.QueueConnectionFactory"));
                    }
                    if (!target.isTopicConnectionFactoryContained()) {                       
                        target.setQueueConnectionFactoryContained(factoryInterface.equals("javax.jms.ConnectionFactory") || factoryInterface.equals("javax.jms.TopicConnectionFactory"));
                    }
                }
            }

            // Get the list of admin objects
            modules = PortletManager.getAdminObjectModules(renderRequest, new String[]{"javax.jms.Queue", "javax.jms.Topic"});
            for (int i = 0; i < modules.length; i++) {
                ResourceAdapterModule module = modules[i];

                JMSResourceSummary target = null;
                for (int j = 0; j < resources.size(); j++) {
                    JMSResourceSummary data = (JMSResourceSummary) resources.get(j);
                    if (data.adapterObjectName.equals(module.getObjectName())) {
                        target = data;
                        break;
                    }
                }
                if (target == null) {
                    ResourceAdapter[] adapters = PortletManager.getResourceAdapters(renderRequest, module);
                    String name = null;                    
                    if (adapters.length == 1 && adapters[0].getJCAResources().length == 1) {
                        JCAResource[] resource = PortletManager.getJCAResources(renderRequest, adapters[0]);
                        if (resource.length == 1 && resource[0].getResourceAdapterInstances().length == 1) {
                            name = ObjectName.getInstance(resource[0].getResourceAdapterInstanceNames()[0]).getKeyProperty(NameFactory.J2EE_NAME);
                        }
                    }
                    if (name == null) {
                        name = ObjectName.getInstance(module.getObjectName()).getKeyProperty(NameFactory.J2EE_NAME);
                    }
                    String configurationName = PortletManager.getConfigurationFor(renderRequest, PortletManager.getNameFor(renderRequest, module)).toString();
                    String resourceAdapterModuleName = PortletManager.getNameFor(renderRequest, module).toString();
                    target = new JMSResourceSummary(configurationName, module.getObjectName(), resourceAdapterModuleName, name);
                    resources.add(target);
                }
                
                boolean activeMQAdapter = module.getVendorName().equals("activemq.org");                

                JCAAdminObject[] admins = PortletManager.getAdminObjectsForRA(renderRequest, module, new String[]{"javax.jms.Queue", "javax.jms.Topic"});
                for (int j = 0; j < admins.length; j++) {
                    Kernel kernel = PortletManager.getKernel();
                    AbstractName abstractName = kernel.getAbstractNameFor(admins[j]);
                    ObjectName name = ObjectName.getInstance(abstractName.getObjectName());
                    String queueName = name.getKeyProperty(NameFactory.J2EE_NAME);
                    String physicalName = null;
                    try {
                        physicalName = (String) admins[j].getConfigProperty("PhysicalName");
                    } catch (Exception e) {
                        logger.warn("PhysicalName undefined, using queueName as PhysicalName");
                        physicalName = queueName;
                    }
                    String destType = admins[j].getAdminObjectInterface().indexOf("Queue") > -1 ? "Queue" : "Topic";
                    String vendorName = module.getVendorName();
                    DestinationStatistics destinationStat = null;
                    if (physicalName != null) {                        
                        try {
                            JMSDestinationInfo jmsDestinationInfo = new JMSDestinationInfo();
                            jmsDestinationInfo.setPhysicalName(physicalName);
                            jmsDestinationInfo.setType(destType);
                            jmsDestinationInfo.setResourceAdapterModuleAbName(PortletManager.getNameFor(renderRequest, module));
                            destinationStat = JMSMessageHelperFactory.getJMSMessageHelper(vendorName).getDestinationStatistics(renderRequest, jmsDestinationInfo);
                        } catch (JMSException e) {
                            destinationStat = new DestinationStatistics();
                        }
                    }
                    AdminObjectSummary adminObjectSummary = new AdminObjectSummary(abstractName.getObjectName().toString(), queueName, physicalName, destType, kernel.getGBeanState(abstractName), destinationStat);                                        
                    adminObjectSummary.setQueueBrowserSupported(true);
                    adminObjectSummary.setTopicHistoryBrowserSupported(false);
                    if(activeMQAdapter) {                        
                        adminObjectSummary.setSendMessageSupported(true);
                        if(destType.equals("Queue")) {
                            adminObjectSummary.setPurgeSupported(true);
                        }
                    } else {
                        if(destType.equals("Queue") && target.isQueueConnectionFactoryContained()) {
                            adminObjectSummary.setSendMessageSupported(true);                            
                        } else if(destType.equals("Topic") && target.isTopicConnectionFactoryContained()) {
                            adminObjectSummary.setSendMessageSupported(true);                            
                        }
                    }
                    target.getAdminObjects().add(adminObjectSummary);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new PortletException(e);
        }

        Collections.sort(resources);
        renderRequest.setAttribute("resources", resources);
        // Get the list of JMS providers
        renderRequest.setAttribute("providers", JMSProviderData.getAllProviders());
    }
    
    
    
    public static class JMSResourceSummary implements Serializable, Comparable<JMSResourceSummary> {
        private static final long serialVersionUID = -2788803234448047035L;
        private final String configurationName;
        private final String adapterObjectName;
        private final String resourceAdapterModuleName;
        private final String name;
        private final String parentName;
        //private final int state;
        private final List<ConnectionFactorySummary> connectionFactories = new ArrayList<ConnectionFactorySummary>();
        private final List<AdminObjectSummary> adminObjects = new ArrayList<AdminObjectSummary>();
        private boolean topicConnectionFactoryContained = false;
        private boolean queueConnectionFactoryContained = false;

        public JMSResourceSummary(String configurationName, String adapterObjectName, String resourceAdapterModuleName, String name) {
            this.configurationName = configurationName;
            this.adapterObjectName = adapterObjectName;
            this.resourceAdapterModuleName = resourceAdapterModuleName;
            //this.state = state;            
            try {
                ObjectName objectName = ObjectName.getInstance(adapterObjectName);
                String parent = objectName.getKeyProperty(NameFactory.J2EE_APPLICATION);
                if (parent != null && parent.equals("null")) {
                    parent = null;
                }
                parentName = parent;
                this.name = name;
            } catch (MalformedObjectNameException e) {
                throw new RuntimeException("Can't parse ObjectName", e);
            }
        }

        public String getConfigurationName() {
            return configurationName;
        }

        public String getAdapterObjectName() {
            return adapterObjectName;
        }

        public String getResourceAdapterModuleName() {
            return resourceAdapterModuleName;
        }

        public String getName() {
            return name;
        }

        public String getParentName() {
            return parentName;
        }
        
        /*
        public int getState() {
            return state;
        }
        */
        
        public List<ConnectionFactorySummary> getConnectionFactories() {
            return connectionFactories;
        }

        public List<AdminObjectSummary> getAdminObjects() {
            return adminObjects;
        }
        
        /*
        public String getStateName() {
            return State.toString(state);
        }
        */
        
        public int compareTo(JMSResourceSummary o) {            
            int names = name.toLowerCase().compareTo(o.name.toLowerCase());
            if (parentName == null) {
                if (o.parentName == null) {
                    return names;
                } else {
                    return -1;
                }
            } else {
                if (o.parentName == null) {
                    return 1;
                } else {
                    int test = parentName.compareTo(o.parentName);
                    if (test != 0) {
                        return test;
                    } else {
                        return names;
                    }
                }
            }
        }

        public boolean isTopicConnectionFactoryContained() {
            return topicConnectionFactoryContained;
        }

        public void setTopicConnectionFactoryContained(boolean topicConnectionFactoryContained) {
            this.topicConnectionFactoryContained = topicConnectionFactoryContained;
        }

        public boolean isQueueConnectionFactoryContained() {
            return queueConnectionFactoryContained;
        }

        public void setQueueConnectionFactoryContained(boolean queueConnectionFactoryContained) {
            this.queueConnectionFactoryContained = queueConnectionFactoryContained;
        }
        
        
    }

    public static class ConnectionFactorySummary implements Serializable, Comparable<ConnectionFactorySummary> {
        private static final long serialVersionUID = 5777507920880039759L;
        private final String factoryObjectName;
        private final String name;
        private final int state;

        public ConnectionFactorySummary(String factoryObjectName, String name, int state) {
            this.factoryObjectName = factoryObjectName;
            this.name = name;
            this.state = state;
        }

        public String getFactoryObjectName() {
            return factoryObjectName;
        }

        public String getName() {
            return name;
        }

        public int getState() {
            return state;
        }

        public String getStateName() {
            return State.toString(state);
        }

        public int compareTo(ConnectionFactorySummary o) {            
            return name.compareTo(o.name);
        }
    }


    public static class AdminObjectSummary implements Serializable, Comparable<AdminObjectSummary> {
        private static final long serialVersionUID = 3941332145785485903L;
        private final String adminObjectName;
        private final String name;
        private final String type;
        private final int state;
        private final String physicalName;
        private final DestinationStatistics destinationStat;
        private boolean queueBrowserSupported = false;
        private boolean topicHistoryBrowserSupported = false;
        private boolean sendMessageSupported = false;
        private boolean purgeSupported = false;
        
        public AdminObjectSummary(String adminObjectName, String name, String physicalName, String type, int state,DestinationStatistics destinationStat) {
            this.adminObjectName = adminObjectName;
            this.name = name;
            this.physicalName = physicalName;
            this.type = type;
            this.state = state;
            this.destinationStat = destinationStat;
        }        
        
        public boolean isTopicHistoryBrowserSupported() {
            return topicHistoryBrowserSupported;
        }

        
        public void setTopicHistoryBrowserSupported(boolean topicHistoryBrowserSupported) {
            this.topicHistoryBrowserSupported = topicHistoryBrowserSupported;
        }

        public DestinationStatistics getdestinationStat(){
            return this.destinationStat;
        }

        public String getAdminObjectName() {
            return adminObjectName;
        }

        public String getName() {
            return name;
        }

        public String getPhysicalName() {
            return physicalName;
        }

        public String getType() {
            return type;
        }

        public int getState() {
            return state;
        }

        public String getStateName() {
            return State.toString(state);
        }

        public int compareTo(AdminObjectSummary o) {           
            int result = name.compareTo(o.name);
            return result == 0 ? type.compareTo(o.type) : result;
        }

        public boolean isQueueBrowserSupported() {
            return queueBrowserSupported;
        }

        public boolean isSendMessageSupported() {
            return sendMessageSupported;
        }

        public boolean isPurgeSupported() {
            return purgeSupported;
        }

        public void setQueueBrowserSupported(boolean queueBrowserSupported) {
            this.queueBrowserSupported = queueBrowserSupported;
        }

        public void setSendMessageSupported(boolean sendMessageSupported) {
            this.sendMessageSupported = sendMessageSupported;
        }

        public void setPurgeSupported(boolean purgeSupported) {
            this.purgeSupported = purgeSupported;
        }
    }
}
