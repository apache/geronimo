/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.client;

import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;

/**
 * @version $Revision$ $Date$
 */
public class AppClientJNDIProvider {
    private final String protocol;
    private final String host;
    private final int port;

    public AppClientJNDIProvider(String protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public void startClient(ObjectName appClientModuleName) throws Exception {
    }
    
    public void stopClient(ObjectName appClientModuleName) throws Exception {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AppClientJNDIProvider.class);

        infoFactory.addOperation("startClient", new Class[]{ObjectName.class});
        infoFactory.addOperation("stopClient", new Class[]{ObjectName.class});
        infoFactory.addAttribute("protocol", String.class, true);
        infoFactory.addAttribute("host", String.class, true);
        infoFactory.addAttribute("port", int.class, true);

        infoFactory.setConstructor(new String[]{"protocol", "host", "port"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
