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
import javax.naming.InitialContext;

import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;

/**
 * @version $Rev$ $Date$
 */
public class StaticJndiContextPlugin implements AppClientPlugin {
    private final ReadOnlyContext context;

    public StaticJndiContextPlugin(ReadOnlyContext context) {
        this.context = context;
    }

    public void startClient(ObjectName appClientModuleName) throws Exception {
        RootContext.setComponentContext(context);
        System.setProperty("java.naming.factory.initial", "com.sun.jndi.rmi.registry.RegistryContextFactory");
        System.setProperty("java.naming.factory.url.pkgs", "org.apache.geronimo.naming");
//        System.setProperty("java.naming.provider.url", "rmi://localhost:1099");
        new InitialContext().lookup("java:comp/env");
    }

    public void stopClient(ObjectName appClientModuleName) throws Exception {
        RootContext.setComponentContext(null);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(StaticJndiContextPlugin.class);

        infoFactory.addAttribute("context", ReadOnlyContext.class, true);
        infoFactory.addInterface(AppClientPlugin.class);

        infoFactory.setConstructor(new String[]{"context"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
