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
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.jmsmanager.DestinationStatistics;
import org.apache.geronimo.console.jmsmanager.helper.JMSMessageHelper;
import org.apache.geronimo.console.jmsmanager.helper.JMSMessageHelperFactory;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
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
    private static final Logger log = LoggerFactory.getLogger(ListScreenHandler.class);

    public ListScreenHandler() {
        super(LIST_MODE, "/WEB-INF/view/jmswizard/list.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String purgeStr = request.getParameter(PURGE);
        if (purgeStr != null) {
            String physicalName = request.getParameter(PHYSICAL_NAME);
            String adminObjType = request.getParameter(ADMIN_OBJ_TYPE);
            String adapterObjectName = request.getParameter(RA_ADAPTER_OBJ_NAME);
            response.setRenderParameter(ADMIN_OBJ_TYPE, adminObjType);
            response.setRenderParameter(PHYSICAL_NAME, physicalName);
            response.setRenderParameter(RA_ADAPTER_OBJ_NAME, adapterObjectName);
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
            JMSMessageHelper helper = JMSMessageHelperFactory.getMessageHelper(renderRequest, adapterObjectName);
            helper.purge(renderRequest, adminObjType, physicalName);
        }

        // Prepare a list of JMS configurations
        List resources = new ArrayList();

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
                    target = new JMSResourceSummary(PortletManager.getConfigurationFor(renderRequest, PortletManager.getNameFor(renderRequest, module)).toString(),
                            module.getObjectName(), name, ((GeronimoManagedBean) module).getState());
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
                    target = new JMSResourceSummary(PortletManager.getConfigurationFor(renderRequest, PortletManager.getNameFor(renderRequest, module)).toString(),
                            module.getObjectName(), name, ((GeronimoManagedBean) module).getState());
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
                        log.warn("PhysicalName undefined, using queueName as PhysicalName", e);
                        physicalName = queueName;
                    }
                    String destType = admins[j].getAdminObjectInterface().indexOf("Queue") > -1 ? "Queue" : "Topic";
                    String vendorName = module.getVendorName();
                    DestinationStatistics destinationStat = JMSMessageHelperFactory.getJMSMessageHelper(vendorName).getDestinationStatistics(destType, physicalName);
                    target.getAdminObjects().add(
                            new AdminObjectSummary(bean.getObjectName(), queueName, physicalName,destType , bean
                                    .getState(),destinationStat));
                }
            }
        } catch (MalformedObjectNameException e) {
            log.error(e.getMessage(), e);
        }

        Collections.sort(resources);
        renderRequest.setAttribute("resources", resources);
        // Get the list of JMS providers
        renderRequest.setAttribute("providers", JMSProviderData.getAllProviders());
    }


    public static class JMSResourceSummary implements Serializable, Comparable {
        private static final long serialVersionUID = -2788803234448047035L;
        private final String configurationName;
        private final String adapterObjectName;
        private final String name;
        private final String parentName;
        private final int state;
        private final List connectionFactories = new ArrayList();
        private final List adminObjects = new ArrayList();

        public JMSResourceSummary(String configurationName, String adapterObjectName, String name, int state) {
            this.configurationName = configurationName;
            this.adapterObjectName = adapterObjectName;
            this.state = state;
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

        public List getConnectionFactories() {
            return connectionFactories;
        }

        public List getAdminObjects() {
            return adminObjects;
        }

        public String getStateName() {
            return State.toString(state);
        }

        public int compareTo(Object o) {
            final JMSResourceSummary pool = (JMSResourceSummary) o;
            int names = name.toLowerCase().compareTo(pool.name.toLowerCase());
            if (parentName == null) {
                if (pool.parentName == null) {
                    return names;
                } else {
                    return -1;
                }
            } else {
                if (pool.parentName == null) {
                    return 1;
                } else {
                    int test = parentName.compareTo(pool.parentName);
                    if (test != 0) {
                        return test;
                    } else {
                        return names;
                    }
                }
            }
        }
    }

    public static class ConnectionFactorySummary implements Serializable, Comparable {
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

        public int compareTo(Object o) {
            final ConnectionFactorySummary pool = (ConnectionFactorySummary) o;
            return name.compareTo(pool.name);
        }
    }


    public static class AdminObjectSummary implements Serializable, Comparable {
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

        public int compareTo(Object o) {
            final AdminObjectSummary pool = (AdminObjectSummary) o;
            int result = name.compareTo(pool.name);
            return result == 0 ? type.compareTo(pool.type) : result;
        }
    }
}
