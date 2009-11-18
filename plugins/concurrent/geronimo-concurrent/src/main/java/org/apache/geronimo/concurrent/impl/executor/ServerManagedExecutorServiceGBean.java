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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.util.concurrent.ManagedExecutorService;

import org.apache.geronimo.concurrent.executor.ServerManagedExecutorService;
import org.apache.geronimo.concurrent.impl.GBeanBuilder;
import org.apache.geronimo.concurrent.impl.NotificationHelper;
import org.apache.geronimo.concurrent.impl.thread.GeronimoManagedThreadFactorySource;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.EventProvider;
import org.apache.geronimo.management.ManagedConstants;
import org.osgi.framework.Bundle;

public class ServerManagedExecutorServiceGBean extends ComponentManagedExecutorServiceGBean implements EventProvider, org.apache.geronimo.management.ManagedExecutorService {

    public static final GBeanInfo GBEAN_INFO;

    private AbstractName name;

    private ServerManagedThreadFactory threadFactory;

    public ServerManagedExecutorServiceGBean(Kernel kernel,
                                             AbstractName name,
                                             Bundle bundle,
                                             int minPoolSize,
                                             int maxPoolSize,
                                             long keepAliveTime,
                                             int queueCapacity,
                                             GeronimoManagedThreadFactorySource threadFactorySource,
                                             String[] contextHandlerClasses) {
        super(bundle, minPoolSize, maxPoolSize, keepAliveTime, queueCapacity, threadFactorySource, contextHandlerClasses);
        this.name = name;

        NotificationHelper notificationHelper = new NotificationHelper(kernel, name);
        this.threadFactory = new ServerManagedThreadFactory(threadFactorySource.getManagedThreadFactory(), notificationHelper);
    }

    protected synchronized ManagedExecutorService getManagedExecutorService() {
        if (this.executor == null) {
            BlockingQueue<Runnable> queue = null;
            if (this.queueCapacity <= 0) {
                queue = new LinkedBlockingQueue<Runnable>();
            } else {
                queue = new ArrayBlockingQueue<Runnable>(this.queueCapacity);
            }
            this.executor = new ServerManagedExecutorService(this.minPoolSize,
                                                             this.maxPoolSize,
                                                             this.keepAliveTime,
                                                             TimeUnit.MILLISECONDS,
                                                             queue,
                                                             this.threadFactory,
                                                             this.contextHandler);
        }
        return this.executor;
    }

    @Override
    public Object $getResource(AbstractName moduleID) {
        return new ManagedExecutorServiceModuleFacade(getManagedExecutorService(), moduleID);
    }

    public AbstractName getName() {
        return this.name;
    }

    public String getObjectName() {
        return this.name.getObjectName().getCanonicalName();
    }

    public String[] getEventTypes() {
        return this.threadFactory.getEventTypes();
    }

    public String[] getHungTaskThreads() {
        return this.threadFactory.getHungTaskThreads();
    }

    public String[] getThreads() {
        return this.threadFactory.getThreads();
    }

    public boolean isEventProvider() {
        return true;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    protected void verifyObjectName() {
        GBeanBuilder.verifyObjectName(getObjectName(),
                                      ManagedConstants.MANAGED_EXECUTOR_SERVICE,
                                      ManagedConstants.MANAGED_EXECUTOR_SERVICE);
    }

    static {
        GBeanInfoBuilder infoFactory =
            GBeanInfoBuilder.createStatic(ServerManagedExecutorServiceGBean.class,
                                          ManagedConstants.MANAGED_EXECUTOR_SERVICE);

        infoFactory.addAttribute("bundle", Bundle.class, false);
        infoFactory.addAttribute("abstractName", AbstractName.class, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.addAttribute("minPoolSize", int.class, true);
        infoFactory.addAttribute("maxPoolSize", int.class, true);
        infoFactory.addAttribute("keepAliveTime", long.class, true);
        infoFactory.addAttribute("queueCapacity", int.class, true);
        infoFactory.addAttribute("contextHandlers", String[].class, true);

        infoFactory.addReference("threadFactory", GeronimoManagedThreadFactorySource.class);

        infoFactory.addInterface(org.apache.geronimo.management.ManagedExecutorService.class);

        infoFactory.setConstructor(new String[] { "kernel",
                                                  "abstractName",
                                                  "bundle",
                                                  "minPoolSize",
                                                  "maxPoolSize",
                                                  "keepAliveTime",
                                                  "queueCapacity",
                                                  "threadFactory",
                                                  "contextHandlers" });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
