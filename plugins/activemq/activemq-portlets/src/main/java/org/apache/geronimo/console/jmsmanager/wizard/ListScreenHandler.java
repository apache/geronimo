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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.activemq.BrokerServiceGBean;
import org.apache.geronimo.activemq.management.ActiveMQManagerGBean;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.jmsmanager.DestinationStatistics;
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
import org.apache.geronimo.management.geronimo.JMSBroker;
import org.apache.geronimo.management.geronimo.JMSManager;
import org.apache.geronimo.management.geronimo.NetworkConnector;
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
    
    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String purgeStr = request.getParameter(PURGE);
        if (purgeStr != null) {
            String physicalName = request.getParameter(PHYSICAL_NAME);
            String adminObjType = request.getParameter(ADMIN_OBJ_TYPE);
            String adapterObjectName = request.getParameter(RA_ADAPTER_OBJ_NAME);
            String brokerName = request.getParameter(BROKER_NAME);
            response.setRenderParameter(ADMIN_OBJ_TYPE, adminObjType);
            response.setRenderParameter(PHYSICAL_NAME, physicalName);
            response.setRenderParameter(RA_ADAPTER_OBJ_NAME, adapterObjectName);
            response.setRenderParameter(BROKER_NAME, brokerName);
            response.setRenderParameter(PURGE, purgeStr);
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
        String purgeStr = renderRequest.getParameter(PURGE);
        if (purgeStr != null) {
            String physicalName = renderRequest.getParameter(PHYSICAL_NAME);
            String adminObjType = renderRequest.getParameter(ADMIN_OBJ_TYPE);
            String adapterObjectName = renderRequest.getParameter(RA_ADAPTER_OBJ_NAME);
            String brokerName = renderRequest.getParameter(BROKER_NAME);
            JMSMessageHelper helper = JMSMessageHelperFactory.getMessageHelper(renderRequest, adapterObjectName);
            try {
                helper.purge(renderRequest, brokerName, adminObjType, physicalName);
            } catch (Exception e) {
                logger.error("Fail to purge the messages in [" + brokerName + "." + physicalName + "]", e);
                throw new PortletException("Fail to purge the messages in [" + brokerName + "." + physicalName + "]",e);
            }
        }                
        
        // Prepare a list of JMS configurations
        List<JMSResourceSummary> resources = new ArrayList<JMSResourceSummary>();

        // Get the list of connection factories
        ResourceAdapterModule[] modules = PortletManager.getOutboundRAModules(renderRequest, new String[]{
                "javax.jms.ConnectionFactory", "javax.jms.QueueConnectionFactory", "javax.jms.TopicConnectionFactory",});
        try {
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
                    String sServerUrl = adapters.length > 0 ? getServerUrl(renderRequest, module) : "";
                    target = new JMSResourceSummary(PortletManager.getConfigurationFor(renderRequest, PortletManager.getNameFor(renderRequest, module)).toString(), module.getObjectName(), name,
                            ((GeronimoManagedBean) module).getState(), getBrokerName(renderRequest, sServerUrl, name));
                    resources.add(target);
                }

                JCAManagedConnectionFactory[] factories = PortletManager.getOutboundFactoriesForRA(renderRequest, module, new String[]{
                        "javax.jms.ConnectionFactory", "javax.jms.QueueConnectionFactory", "javax.jms.TopicConnectionFactory",});
                for (int j = 0; j < factories.length; j++) {
                    JCAManagedConnectionFactory factory = factories[j];
                    ObjectName name = ObjectName.getInstance(factory.getObjectName());
                    target.getConnectionFactories().add(new ConnectionFactorySummary(factory.getObjectName(), name.getKeyProperty(NameFactory.J2EE_NAME),
                            ((GeronimoManagedBean) factory).getState()));
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
                    String sServerUrl = adapters.length > 0 ? getServerUrl(renderRequest,module) : "";
                    target = new JMSResourceSummary(PortletManager.getConfigurationFor(renderRequest, PortletManager.getNameFor(renderRequest, module)).toString(), module.getObjectName(), name,
                            ((GeronimoManagedBean) module).getState(), getBrokerName(renderRequest, sServerUrl, name));
                    resources.add(target);
                }

                JCAAdminObject[] admins = PortletManager.getAdminObjectsForRA(renderRequest, module, new String[]{"javax.jms.Queue", "javax.jms.Topic"});
                for (int j = 0; j < admins.length; j++) {
                    GeronimoManagedBean bean = (GeronimoManagedBean) admins[j];
                    ObjectName name = ObjectName.getInstance(bean.getObjectName());
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
                    if (target.getBrokerName() == null || physicalName == null) {
                        destinationStat = new DestinationStatistics();
                    } else {
                        destinationStat = JMSMessageHelperFactory.getJMSMessageHelper(vendorName).getDestinationStatistics(target.getBrokerName(), destType, physicalName);
                    }
                    target.getAdminObjects().add(
                            new AdminObjectSummary(bean.getObjectName(), queueName, physicalName,destType , bean
                                    .getState(),destinationStat));
                }
            }
        } catch (MalformedObjectNameException e) {
            logger.error(e.getMessage(), e);
        }

        Collections.sort(resources);
        renderRequest.setAttribute("resources", resources);
        // Get the list of JMS providers
        renderRequest.setAttribute("providers", JMSProviderData.getAllProviders());
    }
    
    private String getServerUrl(PortletRequest portletRequest, ResourceAdapterModule resourceAdapterModule) {
        try {
            Kernel kernel = PortletManager.getKernel();
            AbstractName resourceAdapterAbstractName = PortletManager.getNameFor(portletRequest, resourceAdapterModule.getResourceAdapterInstances()[0].getJCAResourceImplementations()[0].getResourceAdapterInstances()[0]);
            if (kernel.isRunning(resourceAdapterAbstractName)) {
                return (String) kernel.getAttribute(resourceAdapterAbstractName, "ServerUrl");
            }
            return "";
        } catch (Exception e) {
            logger.error("Fail to get server URL of the JMS resource",e);
            return "";
        }
    }
    
    private String getBrokerName(PortletRequest portletRequest, String serverURL, String resourceName) throws PortletException {
        try {
            GeronimoManagedBean[] geronimoManagedBeans = PortletManager.getManagedBeans(portletRequest,
                    ActiveMQManagerGBean.class);
            if (geronimoManagedBeans == null || geronimoManagedBeans.length == 0)
                throw new PortletException("Could not find the ActiveMQ Manager");
            if (geronimoManagedBeans.length > 1)
                logger.warn("More than one ActiveMQ Manager exist in kernel");            
            JMSManager activeMQManager = (JMSManager) geronimoManagedBeans[0];            
            JMSBroker targetJMSBroker = null;
            URI uri = new URI(serverURL);            
            String sResourceHostName = uri.getHost();
            if(!isLocalMachine(sResourceHostName)) {
                //The resource adapter may connect to the broker running on other servers, let's directly return null
                portlet.addWarningMessage(portletRequest, portlet.getLocalizedString(portletRequest, "activemq.warnMsg01", resourceName));
                return null;
            }               
            for (JMSBroker jmsBroker : (JMSBroker[]) activeMQManager.getContainers()) {
                NetworkConnector[] connectors = activeMQManager.getConnectorsForContainer(jmsBroker, uri.getScheme());
                for (NetworkConnector connector : connectors) {                                       
                    if(connector.getPort() != uri.getPort()) {
                        continue;
                    }
                    boolean findJMSBroker = false;
                    String sConnectorHostName = connector.getHost();
                    /*
                     * If the broker is binding on all the interfaces, it is the one we want.
                     * else if the broker is listening on loop back address, the host name of the resource adapter should also be loop back address. 
                     * else if the host name of resource adapter is local host name, it is the one we want.
                     * else let us compare whether those two host name are the same textual presentation 
                     * */
                    if (isBindOnAllInterfaces(sConnectorHostName)) {
                        findJMSBroker = true;
                    } else if (isLoopbackAddress(sConnectorHostName)) {
                        if (isLoopbackAddress(sResourceHostName)) {
                            findJMSBroker = true;
                        } else {
                            continue;
                        }
                    } else if (sResourceHostName.equals(getLocalHostName()) || sResourceHostName.equals(getFullLocalHostName())) {
                        findJMSBroker = true;
                    } else if (sResourceHostName.equals(sConnectorHostName)) {
                        findJMSBroker = true;
                    }
                    if (findJMSBroker) {
                        targetJMSBroker = jmsBroker;
                    }
                }
                if (targetJMSBroker != null)
                    return ((BrokerServiceGBean) targetJMSBroker).getBrokerName();
            }
            logger.warn("Could not find the broker according to the url [" + serverURL + "]");
            return null;
        } catch (URISyntaxException e) {
            logger.error("Unrecognized server URL [" + serverURL + "]", e);
            return null;
        }
    }
    
    private boolean isLoopbackAddress(String hostName) {
        try {
            return InetAddress.getByName(hostName).isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }    
    
    private boolean isBindOnAllInterfaces(String hostName) {
        if (hostName.equals("0.0.0.0") || hostName.equals(getLocalHostName())
                || hostName.equals(getFullLocalHostName())) {
            return true;
        }
        return false;
    }

    private String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }
    
    private String getFullLocalHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }
    
    private boolean isLocalMachine(String hostName) {
        try {
            InetAddress hostAddress = InetAddress.getByName(hostName);
            if (hostAddress.isAnyLocalAddress() || hostAddress.isLoopbackAddress() || hostName.equals(getLocalHostName()) || hostName.equals(getFullLocalHostName())) {
                return true;
            } else {
                Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                while (en.hasMoreElements()) {
                    NetworkInterface iface = en.nextElement();
                    Enumeration<InetAddress> ien = iface.getInetAddresses();
                    while (ien.hasMoreElements()) {
                        InetAddress address = ien.nextElement();
                        if (address.equals(hostAddress)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } catch (SocketException e) {
            return false;
        } catch (UnknownHostException e) {
            return false;
        }
    }
    
    public static class JMSResourceSummary implements Serializable, Comparable<JMSResourceSummary> {
        private static final long serialVersionUID = -2788803234448047035L;
        private final String configurationName;
        private final String adapterObjectName;
        private final String name;
        private final String parentName;
        private final int state;
        private final String brokerName;
        private final List<ConnectionFactorySummary> connectionFactories = new ArrayList<ConnectionFactorySummary>();
        private final List<AdminObjectSummary> adminObjects = new ArrayList<AdminObjectSummary>();

        public JMSResourceSummary(String configurationName, String adapterObjectName, String name, int state, String brokerName) {
            this.configurationName = configurationName;
            this.adapterObjectName = adapterObjectName;
            this.state = state;
            this.brokerName = brokerName;
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

        public String getName() {
            return name;
        }

        public String getParentName() {
            return parentName;
        }

        public int getState() {
            return state;
        }

        public List<ConnectionFactorySummary> getConnectionFactories() {
            return connectionFactories;
        }

        public List<AdminObjectSummary> getAdminObjects() {
            return adminObjects;
        }

        public String getStateName() {
            return State.toString(state);
        }
        
        public String getBrokerName() {
            return brokerName;
        }
        
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
        
        public AdminObjectSummary(String adminObjectName, String name, String physicalName, String type, int state,DestinationStatistics destinationStat) {
            this.adminObjectName = adminObjectName;
            this.name = name;
            this.physicalName = physicalName;
            this.type = type;
            this.state = state;
            this.destinationStat = destinationStat;
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
    }
}
