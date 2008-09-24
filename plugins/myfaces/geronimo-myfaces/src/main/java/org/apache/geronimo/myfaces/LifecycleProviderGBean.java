/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.myfaces;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.Context;

import org.apache.myfaces.config.annotation.LifecycleProvider;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.annotation.LifecycleMethod;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;

/**
 * @version $Rev$ $Date$
 */
public class LifecycleProviderGBean implements LifecycleProvider, GBeanLifecycle {
    
    private final Holder holder;
    private final Context context;
    private final ApplicationIndexedLifecycleProviderFactory factory;
    private final ClassLoader classLoader;


    public LifecycleProviderGBean(Holder holder, Map componentContext, LifecycleProviderFactorySource factory, Kernel kernel, ClassLoader classLoader) throws NamingException {
        this.holder = holder;
//        GeronimoUserTransaction userTransaction = new GeronimoUserTransaction(transactionManager);
        context = EnterpriseNamingContext.createEnterpriseNamingContext(componentContext, null, kernel, classLoader);
        this.factory = factory.getLifecycleProviderFactory();
        this.classLoader = classLoader;
    }

    public Object newInstance(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NamingException, InvocationTargetException {
        if (className == null) {
            throw new InstantiationException("no class name provided");
        }
        return holder.newInstance(className, classLoader, context);
    }

    public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException {
        Class clazz = o.getClass();
        if (holder != null) {
            Map<String, LifecycleMethod> preDestroy = holder.getPreDestroy();
            if (preDestroy != null) {
                Holder.apply(o, clazz, preDestroy);
            }
        }
    }

    public void doStart() {
        factory.registerLifecycleProvider(classLoader, this);
    }

    public void doStop() {
        factory.unregisterLifecycleProvider(classLoader);
    }

    public void doFail() {
        doStop();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(LifecycleProviderGBean.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoBuilder.addAttribute("holder", Holder.class, true);
        infoBuilder.addAttribute("componentContext", Map.class, true);

        infoBuilder.addReference("LifecycleProviderFactory", LifecycleProviderFactorySource.class);
//        infoBuilder.addReference("TransactionManager", TransactionManager.class);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.setConstructor(new String[] {"holder", "componentContext", "LifecycleProviderFactory", "kernel", "classLoader"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
