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

import javax.util.concurrent.ManagedScheduledExecutorService;

import org.apache.geronimo.concurrent.executor.ServerManagedScheduledExecutorService;
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

public class ServerManagedScheduledExecutorServiceGBean extends ComponentManagedScheduledExecutorServiceGBean implements EventProvider, org.apache.geronimo.management.ManagedExecutorService {

    public static final GBeanInfo GBEAN_INFO;

    private AbstractName name;

    private ServerManagedThreadFactory threadFactory;

    public ServerManagedScheduledExecutorServiceGBean(Kernel kernel,
                                                      AbstractName name,
                                                      Bundle bundle,
                                                      int corePoolSize,
                                                      GeronimoManagedThreadFactorySource threadFactorySource,
                                                      String[] contextHandlerClasses) {
        super(bundle, corePoolSize, threadFactorySource, contextHandlerClasses);
        this.name = name;

        NotificationHelper notificationHelper = new NotificationHelper(kernel, name);
        this.threadFactory = new ServerManagedThreadFactory(threadFactorySource.getManagedThreadFactory(), notificationHelper);
    }

    protected synchronized ManagedScheduledExecutorService getManagedScheduledExecutorService() {
        if (this.executor == null) {
            this.executor = new ServerManagedScheduledExecutorService(this.corePoolSize,
                                                                      this.threadFactory,
                                                                      this.contextHandler);
        }
        return this.executor;
    }

    @Override
    public Object $getResource(AbstractName moduleID) {
        return new ManagedScheduledExecutorServiceModuleFacade(getManagedScheduledExecutorService(), moduleID);
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
            GBeanInfoBuilder.createStatic(ServerManagedScheduledExecutorServiceGBean.class,
                                          ManagedConstants.MANAGED_EXECUTOR_SERVICE);

        infoFactory.addAttribute("bundle", Bundle.class, false);
        infoFactory.addAttribute("abstractName", AbstractName.class, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.addAttribute("corePoolSize", int.class, true);
        infoFactory.addAttribute("contextHandlers", String[].class, true);

        infoFactory.addReference("threadFactory", GeronimoManagedThreadFactorySource.class);

        infoFactory.addInterface(org.apache.geronimo.management.ManagedExecutorService.class);

        infoFactory.setConstructor(new String[] { "kernel",
                                                  "abstractName",
                                                  "bundle",
                                                  "corePoolSize",
                                                  "threadFactory",
                                                  "contextHandlers" });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
