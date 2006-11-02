/**
 *
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
package org.apache.geronimo.client;

import java.util.Iterator;
import java.util.Map;
import java.util.Collections;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.naming.reference.KernelAwareReference;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.xbean.naming.context.ImmutableContext;

/**
 * @version $Rev$ $Date$
 */
public class StaticJndiContextPlugin implements AppClientPlugin {
    private final Context context;

    public StaticJndiContextPlugin(Map context, Kernel kernel, ClassLoader classLoader) throws NamingException {
        this.context = EnterpriseNamingContext.createEnterpriseNamingContext(context, null, kernel, classLoader);
    }

    public void startClient(AbstractName appClientModuleName, Kernel kernel, ClassLoader classLoader) throws Exception {
        RootContext.setComponentContext(context);
        new InitialContext().lookup("java:comp/env");
    }

    public void stopClient(AbstractName appClientModuleName) throws Exception {
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
