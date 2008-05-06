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

package org.apache.geronimo.console.core.jms;

import javax.jms.ConnectionFactory;
import javax.resource.ResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.naming.ResourceSource;

public class JMSConnectionFactoryBean implements GBeanLifecycle {

    private static final Logger log = LoggerFactory.getLogger(JMSConnectionFactoryBean.class);

    private final ResourceSource<ResourceException> managedConnectionFactoryWrapper;

    private String connectionFactoryName = "jms/DefaultActiveMQConnectionFactory";

    private ConnectionFactory connectionFactory;

    public JMSConnectionFactoryBean(ResourceSource managedConnectionFactoryWrapper) {
        this.managedConnectionFactoryWrapper = managedConnectionFactoryWrapper;

    }

    public ConnectionFactory getConnectionFactory() {

        return this.connectionFactory;
    }

    public synchronized void doStart() throws Exception {

        connectionFactory = (ConnectionFactory) managedConnectionFactoryWrapper
                .$getResource();

        log.debug("JMSConnection started");

    }

    public synchronized void doStop() {

    }

    public synchronized void doFail() {

    }

    public static final GBeanInfo GBEAN_INFO;

    static {

        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("JMS Connection Factory Bean", JMSConnectionFactoryBean.class);
        infoFactory.addAttribute("connectionFactory", ConnectionFactory.class, false);

        infoFactory.addReference("ManagedConnectionFactoryWrapper", ResourceSource.class);
        infoFactory.addOperation("getConnectionFactory");

        infoFactory.setConstructor(new String[] { "ManagedConnectionFactoryWrapper" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}