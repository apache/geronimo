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

import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.concurrent.ManagedContextHandler;
import org.apache.geronimo.concurrent.ManagedContextHandlerChain;
import org.apache.geronimo.concurrent.impl.ContextHandlerUtils;
import org.apache.geronimo.concurrent.impl.GBeanBuilder;
import org.apache.geronimo.concurrent.impl.NotificationHelper;
import org.apache.geronimo.concurrent.naming.ModuleAwareResourceSource;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.EventProvider;
import org.apache.geronimo.management.ManagedConstants;
import org.osgi.framework.Bundle;

public class ManagedThreadFactoryGBean
    extends GBeanBuilder
    implements GBeanLifecycle,
               GeronimoManagedThreadFactorySource,
               ModuleAwareResourceSource,
               EventProvider,
               org.apache.geronimo.management.ManagedThreadFactory {

    private final static Log LOG = LogFactory.getLog(ManagedThreadFactoryGBean.class);

    public static final GBeanInfo GBEAN_INFO;

    private AbstractName name;

    private GeronimoManagedThreadFactory threadFactory;

    private ManagedContextHandlerChain mainContextHandler;

    private String groupName;
    private boolean daemonThread;
    private int threadPriority;
    private long hungTaskThreshold;

    private long hungTaskMonitorFrequency;

    private NotificationHelper notificationHelper;

    public ManagedThreadFactoryGBean(Kernel kernel,
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

        this.notificationHelper = new NotificationHelper(kernel, name);

        List<ManagedContextHandler> handlers =
            ContextHandlerUtils.loadHandlers(bundle, contextHandlerClasses);

        this.mainContextHandler = new ManagedContextHandlerChain(handlers);

        this.groupName = groupName;
        this.daemonThread = daemonThread;
        this.threadPriority = getThreadPriority(threadPriority);
        this.hungTaskThreshold = hungTaskThreshold;
        this.hungTaskMonitorFrequency = getHungTaskMonitorFrequency(hungTaskMonitorFrequency);
    }

    private static int getThreadPriority(int threadPriority) {
        return (threadPriority <= 0) ? Thread.NORM_PRIORITY : threadPriority;
    }

    private static long getHungTaskMonitorFrequency(long hungTaskMonitorFrequency) {
        return (hungTaskMonitorFrequency <= 0) ? 1000 * 60 : hungTaskMonitorFrequency;
    }

    private void sendNotification(ManagedThreadGBean threadGBean) {
        if (this.notificationHelper.isNotificationSupported()) {
            Properties userData = new Properties();
            userData.setProperty("managedthread", threadGBean.getObjectName());

            this.notificationHelper.sendNotification(ManagedConstants.NEW_THREAD_EVENT, userData);
        }
    }

    protected void addThreadGBean(GeronimoManagedThread thread) {
        AbstractName aName = kernel.getNaming().createRootName(name.getArtifact(), thread.getName(), ManagedConstants.MANAGED_THREAD);
        GBeanData threadData = new GBeanData(aName, ManagedThreadGBean.getGBeanInfo());

        try {
            // use either addGBeanKernel() or addGBeanConfiguration()
            addGBeanKernel(aName, threadData);

            ManagedThreadGBean threadGBean = (ManagedThreadGBean)kernel.getGBean(aName);
            threadGBean.verifyObjectName();

            // let gbean know about the thread
            threadGBean.setManagedThread(thread);
            // let thread know about the gbean
            thread.setGbean(threadGBean);

            // send JMX notification
            sendNotification(threadGBean);

        } catch (Exception e) {
            LOG.warn("Failed to add thread gbean", e);
        }
    }

    protected void removeThreadGBean(GeronimoManagedThread thread) {
        AbstractName gbeanName = thread.getGbean().getName();

        removeGBeanKernel(gbeanName);
    }

    public synchronized GeronimoManagedThreadFactory getManagedThreadFactory() {
        if (this.threadFactory == null) {
            // create the factory
            this.threadFactory = new GeronimoManagedThreadFactory(this);

            this.threadFactory.setThreadGroup(groupName);
            this.threadFactory.setDaemonThread(daemonThread);
            this.threadFactory.setThreadPriority(getThreadPriority(threadPriority));
            this.threadFactory.setHungTaskThreshold(hungTaskThreshold);
            this.threadFactory.setHungTaskMonitorFrequency(getHungTaskMonitorFrequency(hungTaskMonitorFrequency));
        }

        return this.threadFactory;
    }

    public Object $getResource(AbstractName moduleID) {
        GeronimoManagedThreadFactory threadFactory = getManagedThreadFactory();
        return new StandaloneManagedThreadFactory(threadFactory, this.mainContextHandler, moduleID);
    }

    public void doStart() throws Exception {
    }

    public void doFail() {
        if (this.threadFactory != null) {
            this.threadFactory.shutdown();
        }
    }

    public void doStop() throws Exception {
        doFail();
    }

    public String[] getThreads() {
        if (this.threadFactory != null) {
            return this.threadFactory.getThreads();
        } else {
            return new String [] {};
        }
    }

    public String[] getHungTaskThreads() {
        if (this.threadFactory != null) {
            return this.threadFactory.getHungTaskThreads();
        } else {
            return new String [] {};
        }
    }

    public int getPriority() {
        return this.threadPriority;
    }

    public boolean getDaemon() {
        return this.daemonThread;
    }

    public long getHungTaskThreshold() {
        return this.hungTaskThreshold;
    }

    public long getHungTaskMonitorFrequency() {
        return this.hungTaskMonitorFrequency;
    }

    public AbstractName getName() {
        return this.name;
    }

    public String getObjectName() {
        return this.name.getObjectName().getCanonicalName();
    }

    public String[] getEventTypes() {
        return new String[] { ManagedConstants.NEW_THREAD_EVENT };
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
                                      ManagedConstants.MANAGED_THREAD_FACTORY,
                                      ManagedConstants.MANAGED_THREAD_FACTORY);
    }

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ManagedThreadFactoryGBean.class, ManagedConstants.MANAGED_THREAD_FACTORY);

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
        infoFactory.addInterface(org.apache.geronimo.management.ManagedThreadFactory.class);

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
