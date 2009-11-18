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
package org.apache.geronimo.concurrent.impl.thread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.concurrent.impl.GBeanBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.ManagedConstants;
import org.osgi.framework.Bundle;

/**
 * Deploys ManagedThreadFactoryGBean under the right JMX object name.
 */
public class ManagedThreadFactoryWrapperGBean
    extends GBeanBuilder
    implements GBeanLifecycle,
               GeronimoManagedThreadFactorySource {

    private final static Log LOG = LogFactory.getLog(ManagedThreadFactoryWrapperGBean.class);

    public static final GBeanInfo GBEAN_INFO;

    private AbstractName name;

    private String groupName;
    private boolean daemonThread;
    private int threadPriority;
    private long hungTaskThreshold;
    private long hungTaskMonitorFrequency;
    private String[] contextHandlerClasses;

    private AbstractName threadFactoryName;
    private ManagedThreadFactoryGBean threadFactoryGBean;

    public ManagedThreadFactoryWrapperGBean(Kernel kernel,
                                            Bundle bundle,
                                            AbstractName name,
                                            String[] contextHandlerClasses,
                                            String groupName,
                                            int threadPriority,
                                            boolean daemonThread,
                                            long hungTaskThreshold,
                                            long hungTaskMonitorFrequency) {
        super(kernel, bundle);
        this.name = name;

        this.contextHandlerClasses = contextHandlerClasses;
        this.groupName = groupName;
        this.daemonThread = daemonThread;
        this.threadPriority = threadPriority;
        this.hungTaskThreshold = hungTaskThreshold;
        this.hungTaskMonitorFrequency = hungTaskMonitorFrequency;
    }

    public GeronimoManagedThreadFactory getManagedThreadFactory() {
        return (this.threadFactoryGBean == null) ? null : this.threadFactoryGBean.getManagedThreadFactory();
    }

    public void doStart() throws Exception {
        String threadName =  (String)this.name.getName().get("name");
        this.threadFactoryName = kernel.getNaming().createRootName(name.getArtifact(), threadName, ManagedConstants.MANAGED_THREAD_FACTORY);
        GBeanData threadFactoryData = new GBeanData(this.threadFactoryName, ManagedThreadFactoryGBean.getGBeanInfo());
        threadFactoryData.setAttribute("contextHandlers", this.contextHandlerClasses);
        threadFactoryData.setAttribute("groupName", this.groupName);
        threadFactoryData.setAttribute("priority", this.threadPriority);
        threadFactoryData.setAttribute("daemon", this.daemonThread);
        threadFactoryData.setAttribute("hungTaskThreshold", this.hungTaskThreshold);
        threadFactoryData.setAttribute("hungTaskMonitorFrequency", this.hungTaskMonitorFrequency);

        addGBeanKernel(this.threadFactoryName, threadFactoryData);

        this.threadFactoryGBean = (ManagedThreadFactoryGBean)kernel.getGBean(this.threadFactoryName);
        this.threadFactoryGBean.verifyObjectName();
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            // ignore
        }
    }

    public void doStop() throws Exception {
        this.threadFactoryGBean = null;
        if (this.threadFactoryName != null) {
            removeGBeanKernel(this.threadFactoryName);
        }
    }

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ManagedThreadFactoryWrapperGBean.class, ManagedConstants.MANAGED_THREAD_FACTORY + "Builder");

        infoFactory.addAttribute("bundle", Bundle.class, false, false);
        infoFactory.addAttribute("abstractName", AbstractName.class, false, false);
        infoFactory.addAttribute("kernel", Kernel.class, false, false);

        infoFactory.addAttribute("contextHandlers", String[].class, true, false);

        infoFactory.addAttribute("groupName", String.class, true);
        infoFactory.addAttribute("priority", int.class, true);
        infoFactory.addAttribute("daemon", boolean.class, true);
        infoFactory.addAttribute("hungTaskThreshold", long.class, true);
        infoFactory.addAttribute("hungTaskMonitorFrequency", long.class, true);

        infoFactory.addInterface(GeronimoManagedThreadFactorySource.class);

        infoFactory.setConstructor(new String[]{"kernel",
                                                "bundle",
                                                "abstractName",
                                                "contextHandlers",
                                                "groupName",
                                                "priority",
                                                "daemon",
                                                "hungTaskThreshold",
                                                "hungTaskMonitorFrequency"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
