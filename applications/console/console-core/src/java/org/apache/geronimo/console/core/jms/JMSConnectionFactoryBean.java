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

package org.apache.geronimo.console.core.jms;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

public class JMSConnectionFactoryBean implements GBeanLifecycle {

    private static Log log = LogFactory.getLog(JMSConnectionFactoryBean.class);

    private final ManagedConnectionFactoryWrapper managedConnectionFactoryWrapper;

    private String connectionFactoryName = "jms/DefaultActiveMQConnectionFactory";

    private ConnectionFactory connectionFactory;

    public JMSConnectionFactoryBean(
            ManagedConnectionFactoryWrapper managedConnectionFactoryWrapper) {
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
        infoFactory.addAttribute("connectionFactory", ConnectionFactory.class,
                false);

        infoFactory.addReference("ManagedConnectionFactoryWrapper",
                ManagedConnectionFactoryWrapper.class);
        infoFactory.addOperation("getConnectionFactory");

        infoFactory
                .setConstructor(new String[] { "ManagedConnectionFactoryWrapper" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}