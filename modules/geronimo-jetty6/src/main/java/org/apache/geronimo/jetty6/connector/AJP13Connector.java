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

package org.apache.geronimo.jetty6.connector;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.jetty6.JettyContainer;
import org.apache.geronimo.management.geronimo.WebManager;

/**
 * @version $Rev$ $Date$
 */
public class AJP13Connector extends JettyConnector {
    public AJP13Connector(JettyContainer container) {
        //throw new UnsupportedOperationException("No AJP13Connector for jetty6
        //TODO:there is no ajp13 connector, but just ignore this for now
    }

    public String getProtocol() {
        return WebManager.PROTOCOL_AJP;
    }

    public int getDefaultPort() {
        return -1;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Jetty Connector AJP13", AJP13Connector.class, JettyConnector.GBEAN_INFO);
        infoFactory.setConstructor(new String[]{"JettyContainer"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
