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
package org.apache.geronimo.console.jmsmanager.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;

import org.apache.activemq.broker.BrokerService;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.management.activemq.ActiveMQBroker;
import org.apache.geronimo.management.activemq.ActiveMQManager;
import org.apache.geronimo.management.geronimo.JMSBroker;
import org.apache.geronimo.management.geronimo.JMSManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;

/**
 * Common methods for JMS portlets
 *
 * @version $Rev$ $Date$
 */
public class BaseJMSPortlet extends BasePortlet {
    
    /*
    protected List<BrokerWrapper> getBrokerList(PortletRequest renderRequest, JMSManager manager) throws PortletException {
        List<BrokerWrapper> beans = new ArrayList<BrokerWrapper>();
        //For we need list all the brokers including running and stop on the page,
        //While querying in the kernel, we could not get the full list.
        //Currently, all the brokers are in the activemq-broker configuration, we will list all the gbeans in it.
        //But we still could not load those GBeans which are marked with load="false"
        Artifact activeMQBrokersConfig = PortletManager.getNameFor(renderRequest, manager).getArtifact();
        Configuration configuration = PortletManager.getConfigurationManager().getConfiguration(activeMQBrokersConfig);
        AbstractNameQuery query = new AbstractNameQuery(ActiveMQBroker.class.getName());
        LinkedHashSet<GBeanData> brokerNameSet = configuration.findGBeanDatas(Collections.singleton(query));
        Kernel kernel = PortletManager.getKernel();
        try {
            for (GBeanData gBeanData : brokerNameSet) {
                AbstractName abstractName = gBeanData.getAbstractName();
                String brokerName = abstractName.getNameProperty("name");
                if (kernel.isRunning(abstractName)) {
                    beans.add(new BrokerWrapper(brokerName, abstractName.toString(), (JMSBroker) kernel.getGBean(abstractName), State.RUNNING));
                } else {
                    beans.add(new BrokerWrapper(brokerName, abstractName.toString(), null, State.STOPPED));
                }
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
        return beans;
    }
    */
    
    protected Map<String, BrokerServiceWrapper> getBrokerServices() {
        Map<String, BrokerServiceWrapper> brokerServices = new HashMap<String, BrokerServiceWrapper>();
        try {
            Kernel kernel = PortletManager.getKernel();
            BundleContext context = kernel.getBundleFor(kernel.getKernelName()).getBundleContext();
            String clazz = "org.osgi.service.blueprint.container.BlueprintContainer";
            String filter = "(osgi.blueprint.container.symbolicname=org.apache.geronimo.configs.activemq-broker-blueprint)";
            ServiceReference[] references = context.getServiceReferences(clazz, filter);
            for (ServiceReference reference: references) {
                BlueprintContainer container = (BlueprintContainer) context.getService(reference);
                @SuppressWarnings("unchecked")
                Set<String> ids = (Set<String>) container.getComponentIds();
                for (Object id: ids) {
                    Object object = container.getComponentInstance((String)id);
                    if (object instanceof BrokerService) {
                        BrokerService brokerService = (BrokerService) object;
                        String brokerName = brokerService.getBrokerName();
                        String brokerURI = brokerService.getMasterConnectorURI();
                        State state = brokerService.isStarted() ? State.RUNNING : State.STOPPED;
                        BrokerServiceWrapper wrapper = new BrokerServiceWrapper(brokerName, brokerURI, brokerService, state);
                        brokerServices.put(brokerName, wrapper);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return brokerServices;
    }
    
    /*
    protected BrokerWrapper getBrokerWrapper(PortletRequest portletRequest, AbstractName brokerAbstractName) throws PortletException {
        JMSBroker jmsBroker = PortletManager.getJMSBroker(portletRequest, brokerAbstractName);
        if (jmsBroker == null)
            return null;
        String displayName = brokerAbstractName.getName().get("name").toString();
        Kernel kernel = PortletManager.getKernel();
        try {
            return new BrokerWrapper(displayName, brokerAbstractName.toString(), jmsBroker, State.fromInt(kernel.getGBeanState(brokerAbstractName)));
        } catch (GBeanNotFoundException e) {
            throw new PortletException(e);
        }
    }
    */
    
    /*
    protected JMSManager getActiveMQManager(PortletRequest portletRequest) {
        for (JMSManager jmsManager : PortletManager.getCurrentServer(portletRequest).getJMSManagers()) {
            if (jmsManager instanceof ActiveMQManager)
                return jmsManager;
        }
        return null;
    }
    */
    
    public static class BrokerServiceWrapper {
        private String brokerName;
        private String brokerURI;
        private BrokerService brokerService;
        private State state;

        public BrokerServiceWrapper(String brokerName, String brokerURI, BrokerService brokerService, State state) {
            this.brokerName = brokerName;
            this.brokerURI = brokerURI;
            this.brokerService = brokerService;
            this.state = state;
        }

        public String getBrokerName() {
            return brokerName;
        }

        public BrokerService getBrokerService() {
            return brokerService;
        }

        public String getBrokerURI() {
            return brokerURI;
        }
        
        public State getState() {
            return state;
        }
    }
}
