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

package org.apache.geronimo.tomcat.connector;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.tomcat.TomcatContainer;

/**
 * Apache Tomcat HTTP 1.1 connector
 * 
 * @version $Rev: 56022 $ $Date: 2004-10-30 07:16:18 +0200 (Sat, 30 Oct 2004) $
 */
public class HTTPConnector extends Connector implements GBeanLifecycle {
    private final TomcatContainer container;

    public HTTPConnector(TomcatContainer container) throws Exception {
        super("HTTP/1.1"); // TODO: make it an attribute
        this.container = container;
    }

    public void doStart() throws LifecycleException {
        container.addConnector(this);
        start();
    }

    public void doStop() {
        container.removeConnector(this);
    }

    public void doFail() {
        doStop();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Tomcat HTTP Connector", HTTPConnector.class);
        infoFactory.addAttribute("port", int.class, true);
        infoFactory.addReference("TomcatContainer", TomcatContainer.class);
        infoFactory.setConstructor(new String[] { "TomcatContainer" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
