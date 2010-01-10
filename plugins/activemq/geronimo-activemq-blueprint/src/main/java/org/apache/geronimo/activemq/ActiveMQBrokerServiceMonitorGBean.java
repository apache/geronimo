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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.geronimo.activemq.management.ActiveMQTransportConnector;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.AnnotationGBeanInfoFactory;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.osgi.framework.BundleContext;

public class ActiveMQBrokerServiceMonitorGBean implements GBeanLifecycle, LifecycleListener {

    private Kernel kernel;

    private Map<AbstractName, List<AbstractName>> brokerNameConnectorNamesMap = new ConcurrentHashMap<AbstractName, List<AbstractName>>();

    private BundleContext bundleContext;

    public ActiveMQBrokerServiceMonitorGBean(@ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel, @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) {
        this.kernel = kernel;
        this.bundleContext = bundleContext;
    }

    public void doFail() {
        kernel.getLifecycleMonitor().removeLifecycleListener(this);
    }

    public void doStart() throws Exception {
        AbstractNameQuery brokerServiceQuery = new AbstractNameQuery(new URI("?#org.apache.geronimo.activemq.BrokerServiceGBean"));
        kernel.getLifecycleMonitor().addLifecycleListener(this, brokerServiceQuery);
        Set<AbstractName> brokerServiceNames = kernel.listGBeans(brokerServiceQuery);
        for (AbstractName brokerServiceName : brokerServiceNames) {
            if (kernel.isRunning(brokerServiceName)) {
                startConnectorWrapperGBeans(brokerServiceName);
            }
        }
    }

    public void doStop() throws Exception {
        kernel.getLifecycleMonitor().removeLifecycleListener(this);
    }

    public void failed(AbstractName abstractName) {
        stopConnectorWrapperGBeans(abstractName);
    }

    public void loaded(AbstractName abstractName) {
    }

    public void running(AbstractName abstractName) {
        startConnectorWrapperGBeans(abstractName);
    }

    public void starting(AbstractName abstractName) {
    }

    public void stopped(AbstractName abstractName) {
        stopConnectorWrapperGBeans(abstractName);
    }

    public void stopping(AbstractName abstractName) {
    }

    public void unloaded(AbstractName abstractName) {
    }

    protected void startConnectorWrapperGBeans(AbstractName brokerAbstractName) {
        try {
            BrokerService brokerService = ((BrokerServiceGBean) kernel.getGBean(brokerAbstractName)).getBrokerContainer();
            List<AbstractName> connectorNames = new ArrayList<AbstractName>();
            GBeanInfo gBeanInfo = new AnnotationGBeanInfoFactory().getGBeanInfo(ActiveMQTransportConnector.class);
            for (TransportConnector transportConnector : brokerService.getTransportConnectors()) {
                AbstractName connectorAbName = kernel.getNaming().createSiblingName(brokerAbstractName, transportConnector.getUri().toString().replace(':', '_'), GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
                GBeanData gbeanData = new GBeanData(connectorAbName, gBeanInfo);
                gbeanData.setAttribute("transportConnector", transportConnector);
                kernel.loadGBean(gbeanData, bundleContext);
                kernel.startGBean(connectorAbName);
                connectorNames.add(connectorAbName);
            }
            brokerNameConnectorNamesMap.put(brokerAbstractName, connectorNames);
        } catch (Exception e) {
        }
    }

    protected void stopConnectorWrapperGBeans(AbstractName brokerAbstractName) {
        List<AbstractName> connectorNames = brokerNameConnectorNamesMap.remove(brokerAbstractName);
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
}
