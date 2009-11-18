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
package org.apache.geronimo.concurrent.impl.executor;

import java.util.List;

import javax.util.concurrent.ManagedScheduledExecutorService;
import javax.util.concurrent.ManagedThreadFactory;

import org.apache.geronimo.concurrent.ManagedContextHandler;
import org.apache.geronimo.concurrent.ManagedContextHandlerChain;
import org.apache.geronimo.concurrent.executor.ComponentManagedScheduledExecutorService;
import org.apache.geronimo.concurrent.executor.ManagedScheduledExecutorServiceFacade;
import org.apache.geronimo.concurrent.impl.ContextHandlerUtils;
import org.apache.geronimo.concurrent.impl.thread.GeronimoManagedThreadFactorySource;
import org.apache.geronimo.concurrent.naming.ModuleAwareResourceSource;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.management.ManagedConstants;
import org.osgi.framework.Bundle;


public class ComponentManagedScheduledExecutorServiceGBean implements GBeanLifecycle, ModuleAwareResourceSource {

    public static final GBeanInfo GBEAN_INFO;

    protected ManagedScheduledExecutorService executor;

    protected int corePoolSize;
    protected ManagedThreadFactory threadFactory;
    protected ManagedContextHandlerChain contextHandler;

    public ComponentManagedScheduledExecutorServiceGBean(Bundle bundle,
                                                         int corePoolSize,
                                                         GeronimoManagedThreadFactorySource threadFactorySource,
                                                         String[] contextHandlerClasses) {
        this.corePoolSize = corePoolSize;
        this.threadFactory = threadFactorySource.getManagedThreadFactory();
        List<ManagedContextHandler> handlers =
            ContextHandlerUtils.loadHandlers(bundle, contextHandlerClasses);
        this.contextHandler = new ManagedContextHandlerChain(handlers);
    }

    protected synchronized ManagedScheduledExecutorService getManagedScheduledExecutorService() {
        if (this.executor == null) {
            this.executor = new ComponentManagedScheduledExecutorService(this.corePoolSize,
                                                                         this.threadFactory,
                                                                         this.contextHandler);
        }
        return this.executor;
    }

    protected synchronized void shutdownExecutor() {
        if (this.executor != null) {
            this.executor.shutdown();
        }
    }

    public Object $getResource(AbstractName moduleID) {
        return new ManagedScheduledExecutorServiceFacade(getManagedScheduledExecutorService(), false);
    }

    public void doStart() throws Exception {
    }

    public void doFail() {
        shutdownExecutor();
    }

    public void doStop() throws Exception {
        doFail();
    }

    static {
        GBeanInfoBuilder infoFactory =
            GBeanInfoBuilder.createStatic(ComponentManagedScheduledExecutorServiceGBean.class,
                                          ManagedConstants.MANAGED_EXECUTOR_SERVICE);

        infoFactory.addAttribute("bundle", Bundle.class, false);

        infoFactory.addAttribute("corePoolSize", int.class, true);
        infoFactory.addAttribute("contextHandlers", String[].class, true);

        infoFactory.addReference("threadFactory", GeronimoManagedThreadFactorySource.class);

        infoFactory.setConstructor(new String[] { "bundle",
                                                  "corePoolSize",
                                                  "threadFactory",
                                                  "contextHandlers" });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
