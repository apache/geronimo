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

import java.util.Iterator;
import java.util.Map;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.naming.reference.KernelAwareReference;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev$ $Date$
 */
public class StaticJndiContextPlugin implements AppClientPlugin {
    private final Context context;

    public StaticJndiContextPlugin(Map context, Kernel kernel, ClassLoader classLoader) throws NamingException {
        // create ReadOnlyContext
        for (Iterator iterator = context.values().iterator(); iterator.hasNext();) {
            Object value = iterator.next();
            if (value instanceof KernelAwareReference) {
                ((KernelAwareReference) value).setKernel(kernel);
            }
            if (value instanceof ClassLoaderAwareReference) {
                ((ClassLoaderAwareReference) value).setClassLoader(classLoader);
            }
        }
        this.context = EnterpriseNamingContext.createEnterpriseNamingContext(context);
    }

    public void startClient(ObjectName appClientModuleName, Kernel kernel, ClassLoader classLoader) throws Exception {
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
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(StaticJndiContextPlugin.class);

        infoFactory.addAttribute("context", Map.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addInterface(AppClientPlugin.class);

        infoFactory.setConstructor(new String[]{"context", "kernel", "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
