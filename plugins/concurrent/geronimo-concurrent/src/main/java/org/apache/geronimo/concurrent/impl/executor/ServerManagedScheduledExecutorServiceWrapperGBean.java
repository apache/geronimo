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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.concurrent.impl.GBeanBuilder;
import org.apache.geronimo.concurrent.impl.thread.GeronimoManagedThreadFactorySource;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.ManagedConstants;

import org.osgi.framework.Bundle;

/**
 * Deploys ServerManagedScheduledExecutorServiceGBean under the right JMX object name.
 */
public class ServerManagedScheduledExecutorServiceWrapperGBean
    extends GBeanBuilder
    implements GBeanLifecycle {

    private final static Log LOG = LogFactory.getLog(ServerManagedScheduledExecutorServiceWrapperGBean.class);

    public static final GBeanInfo GBEAN_INFO;

    private AbstractName name;

    private int corePoolSize;
    private String[] contextHandlerClasses;

    private AbstractName executorServiceName;
    private ServerManagedScheduledExecutorServiceGBean executorServiceGBean;

    public ServerManagedScheduledExecutorServiceWrapperGBean(Kernel kernel,
                                                             AbstractName name,
                                                             Bundle bundle,
                                                             int corePoolSize,
                                                             GeronimoManagedThreadFactorySource threadFactorySource,
                                                             String[] contextHandlerClasses) {
        super(kernel, bundle);
        this.name = name;

        this.contextHandlerClasses = contextHandlerClasses;
        this.corePoolSize = corePoolSize;
    }

    public void doStart() throws Exception {
        String threadName =  (String)this.name.getName().get("name");
        this.executorServiceName = kernel.getNaming().createRootName(name.getArtifact(), threadName, ManagedConstants.MANAGED_EXECUTOR_SERVICE);
        GBeanData executorServiceData = new GBeanData(this.executorServiceName, ServerManagedScheduledExecutorServiceGBean.getGBeanInfo());
        executorServiceData.setAttribute("contextHandlers", this.contextHandlerClasses);
        executorServiceData.setAttribute("corePoolSize", this.corePoolSize);

        GBeanData wrapperData = kernel.getGBeanData(this.name);

        executorServiceData.setReferencePatterns("threadFactory",
                                                 wrapperData.getReferencePatterns("threadFactory"));

        addGBeanKernel(this.executorServiceName, executorServiceData);

        this.executorServiceGBean = (ServerManagedScheduledExecutorServiceGBean)kernel.getGBean(this.executorServiceName);
        this.executorServiceGBean.verifyObjectName();
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            // ignore
        }
    }

    public void doStop() throws Exception {
        this.executorServiceGBean = null;
        if (this.executorServiceName != null) {
            removeGBeanKernel(this.executorServiceName);
        }
    }

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ServerManagedScheduledExecutorServiceWrapperGBean.class, ManagedConstants.MANAGED_THREAD_FACTORY + "Builder");

        infoFactory.addAttribute("bundle", Bundle.class, false, false);
        infoFactory.addAttribute("abstractName", AbstractName.class, false, false);
        infoFactory.addAttribute("kernel", Kernel.class, false, false);

        infoFactory.addAttribute("corePoolSize", int.class, true);
        infoFactory.addAttribute("contextHandlers", String[].class, true);

        infoFactory.addReference("threadFactory", GeronimoManagedThreadFactorySource.class);

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
