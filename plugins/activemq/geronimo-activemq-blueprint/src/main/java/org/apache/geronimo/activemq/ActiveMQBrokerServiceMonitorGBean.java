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

package org.apache.geronimo.activemq;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.geronimo.activemq.management.ActiveMQTransportConnector;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.AnnotationGBeanInfoFactory;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.osgi.service.blueprint.container.BlueprintListener;

public class ActiveMQBrokerServiceMonitorGBean implements GBeanLifecycle{

    private Kernel kernel;

    private Map<String, List<AbstractName>> brokerNameConnectorNamesMap = new ConcurrentHashMap<String, List<AbstractName>>();

    private BundleContext bundleContext;
    
    private AbstractName abstractName; 
    
    private ServiceRegistration registration;

    public ActiveMQBrokerServiceMonitorGBean(@ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
            @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext,
            @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abstractName) {
        this.kernel = kernel;
        this.bundleContext = bundleContext;
        this.abstractName = abstractName;
    }

    public void doStart() throws Exception {     
        BlueprintListener listener = new BlueprintListener() {
            @Override
            public void blueprintEvent(BlueprintEvent event) {
                if (event.getType() == BlueprintEvent.CREATED) {
                    startConnectorGBeans(event.getBundle());
                } else if (event.getType() == BlueprintEvent.DESTROYED) {
                    stopConnectorGBeans(event.getBundle());
                }
            }
        };        
        registration = bundleContext.registerService(BlueprintListener.class.getName(), listener, new Hashtable());
        
        startConnectorGBeans(null);
   }
    
    protected void startConnectorGBeans(Bundle bundle) {
        Set<BrokerService> brokerServices = getBrokerService(bundle);
        if (brokerServices == null) {
            return;
        }
        for (BrokerService brokerService : brokerServices) {
            if (brokerService.isStarted()) {
                startConnectorWrapperGBeans(brokerService);
            }
        }
    }
    
    protected void stopConnectorGBeans(Bundle bundle) {
        Set<BrokerService> brokerServices = getBrokerService(bundle);
        if (brokerServices == null) {
            return;
        }
        for (BrokerService brokerService : brokerServices) {
            stopConnectorWrapperGBeans(brokerService.getBrokerName());
        }
    }

    public void doStop() throws Exception {
        doFail();
    }
    
    public void doFail() {
        if (registration != null) {
            registration.unregister();            
        }        
        for (String brokerName: brokerNameConnectorNamesMap.keySet()) {
            stopConnectorWrapperGBeans(brokerName);
        }
        brokerNameConnectorNamesMap.clear();
    }
    
    protected void startConnectorWrapperGBeans(BrokerService brokerService) {
        try {
            // in case this brokerService has been processed
            List<AbstractName> oldConnectorNames = brokerNameConnectorNamesMap.get(brokerService.getBrokerName());
            if (oldConnectorNames!=null) {
                return;
            }
            
            List<AbstractName> connectorNames = new ArrayList<AbstractName>();
            GBeanInfo gBeanInfo = new AnnotationGBeanInfoFactory().getGBeanInfo(ActiveMQTransportConnector.class);
            for (TransportConnector transportConnector : brokerService.getTransportConnectors()) {
                AbstractName connectorAbName = kernel.getNaming().createSiblingName(this.abstractName, transportConnector.getUri().toString().replace(':', '_'), GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
                GBeanData gbeanData = new GBeanData(connectorAbName, gBeanInfo);
                gbeanData.setAttribute("transportConnector", transportConnector);
                kernel.loadGBean(gbeanData, bundleContext);
                kernel.startGBean(connectorAbName);
                connectorNames.add(connectorAbName);
            }
            brokerNameConnectorNamesMap.put(brokerService.getBrokerName(), connectorNames);
        } catch (Exception e) {
        }
    }
 
    protected void stopConnectorWrapperGBeans(String brokerName) {
        List<AbstractName> connectorNames = brokerNameConnectorNamesMap.get(brokerName);
        if (connectorNames == null) {
            return;
        }
        for (AbstractName connectorName : connectorNames) {
            try {
                kernel.stopGBean(connectorName);
                kernel.unloadGBean(connectorName);
            } catch (Exception e) {
            }
        }
    }    
    
    private Set<BrokerService> getBrokerService(Bundle bundle) {
        Set<BrokerService> brokerServices = new HashSet<BrokerService>();
        String targetBundleSymbolicName = "org.apache.geronimo.configs.activemq-broker-blueprint";
 
        try {           
            if (bundle!=null && !targetBundleSymbolicName.equals(bundle.getSymbolicName())) {
                return brokerServices;
            }
            String filter = "(osgi.blueprint.container.symbolicname=" + targetBundleSymbolicName + ")";            
            String clazz = "org.osgi.service.blueprint.container.BlueprintContainer";            
            ServiceReference[] references = bundleContext.getServiceReferences(clazz, filter);
            if (references != null) {
                for (ServiceReference reference: references) {
                    BlueprintContainer container = (BlueprintContainer) bundleContext.getService(reference);
                    Set<String> ids = (Set<String>) container.getComponentIds();
                    for (Object id: ids) {
                        Object object = container.getComponentInstance((String)id);
                        if (object instanceof BrokerService) {
                            brokerServices.add((BrokerService)object);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return brokerServices;
    }
}
