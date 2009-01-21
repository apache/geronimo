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
package org.apache.geronimo.management.geronimo;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.KernelException;
import org.apache.geronimo.kernel.config.InvalidConfigException;

/**
 * Main entry point for managing a particular JMS implementation.  The manager
 * has features to access JMS brokers (aka servers or containers) as well as
 * JMS connectors (aka network listeners).  Each manager should handle all
 * the brokers and connectors for a single JMS implementation; if the Geronimo
 * server has multiple JMS implementations available then there should be one
 * JMSManager instance for each.
 *
 * @version $Rev$ $Date$
 */
public interface JMSManager extends NetworkManager {
    /**
     * Creates a new connector, and returns the ObjectName for it.  Note that
     * the connector may well require further customization before being fully
     * functional (e.g. SSL settings for a secure connector).
     *
     * @param broker      The broker to add the connector for
     * @param uniqueName  A name fragment that's unique to this connector
     * @param protocol    The protocol the connector should be configured for
     * @param host        The listen host/IP for the connector
     * @param port        The listen port for the connector
     *
     * @return The newly added connector.  It will be valid (loaded) but not
     *         started.
     */
    public JMSConnector addConnector(JMSBroker broker, String uniqueName, String protocol, String host, int port);
    
    public JMSBroker addBroker(String brokerName, GBeanData gBeanData) throws KernelException, InvalidConfigException;
    
    public void removeBroker(AbstractName brokerName) throws KernelException, InvalidConfigException;
}
