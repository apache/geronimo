/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.jetty.connector;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.jetty.JettyContainer;
import org.mortbay.http.HttpListener;
import org.mortbay.util.ThreadedServer;

/**
 * @version $Rev$ $Date$
 */
public abstract class JettyConnector implements GBeanLifecycle {
    private final JettyContainer container;
    protected final HttpListener listener;

    public JettyConnector(JettyContainer container) {
        this.container = container;
        this.listener = null;
    }

    public JettyConnector(JettyContainer container, HttpListener listener) {
        this.container = container;
        this.listener = listener;
    }

    public int getPort() {
        return listener.getPort();
    }

    public void setPort(int port) {
        listener.setPort(port);
    }

    public void doStart() throws WaitingException, Exception {
        container.addListener(listener);
        ((ThreadedServer) listener).open();
        listener.start();
    }

    public void doStop() throws WaitingException {
        while (true) {
            try {
                listener.stop();
                container.removeListener(listener);
                return;
            } catch (InterruptedException e) {
                continue;
            }
        }
    }

    public void doFail() {
        while (true) {
            try {
                listener.stop();
                container.removeListener(listener);
                return;
            } catch (InterruptedException e) {
                continue;
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Jetty HTTP Connector", JettyConnector.class);
        infoFactory.addAttribute("port", int.class, true);
        infoFactory.addReference("JettyContainer", JettyContainer.class);
        infoFactory.setConstructor(new String[] {"JettyContainer"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
