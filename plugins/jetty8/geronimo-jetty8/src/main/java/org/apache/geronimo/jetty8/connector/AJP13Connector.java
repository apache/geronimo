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

package org.apache.geronimo.jetty8.connector;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.jetty8.JettyContainer;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.threads.ThreadPool;
import org.eclipse.jetty.ajp.Ajp13SocketConnector;

/**
 * @version $Rev$ $Date$
 */
public class AJP13Connector extends JettyConnector {
    public AJP13Connector(JettyContainer container, ThreadPool threadPool) {
        super(container, new Ajp13SocketConnector(), threadPool, "AJP13Connector");
    }

    public String getProtocol() {
        return WebManager.PROTOCOL_AJP;
    }

    public int getDefaultPort() {
        return -1;
    }

    public void setRedirectPort(int port) {
        Ajp13SocketConnector ajpListener = (Ajp13SocketConnector) listener;
        ajpListener.setConfidentialPort(port);
        ajpListener.setIntegralPort(port);
        ajpListener.setIntegralScheme("https");
        ajpListener.setConfidentialScheme("https");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Jetty Connector AJP13", AJP13Connector.class, JettyConnector.GBEAN_INFO);
        infoFactory.setConstructor(new String[]{"JettyContainer", "ThreadPool"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
