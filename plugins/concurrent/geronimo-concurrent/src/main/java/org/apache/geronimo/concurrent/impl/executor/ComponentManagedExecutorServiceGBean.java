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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.util.concurrent.ManagedExecutorService;
import javax.util.concurrent.ManagedThreadFactory;

import org.apache.geronimo.concurrent.ManagedContextHandler;
import org.apache.geronimo.concurrent.ManagedContextHandlerChain;
import org.apache.geronimo.concurrent.executor.ComponentManagedExecutorService;
import org.apache.geronimo.concurrent.executor.ManagedExecutorServiceFacade;
import org.apache.geronimo.concurrent.impl.ContextHandlerUtils;
import org.apache.geronimo.concurrent.impl.thread.GeronimoManagedThreadFactorySource;
import org.apache.geronimo.concurrent.naming.ModuleAwareResourceSource;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.management.ManagedConstants;

public class ComponentManagedExecutorServiceGBean implements GBeanLifecycle, ModuleAwareResourceSource {

    public static final GBeanInfo GBEAN_INFO;

    protected ManagedExecutorService executor;

    protected int minPoolSize;
    protected int maxPoolSize;
    protected long keepAliveTime;
    protected int queueCapacity;
    protected ManagedThreadFactory threadFactory;    
    protected ManagedContextHandlerChain contextHandler;
   
    public ComponentManagedExecutorServiceGBean(ClassLoader classLoader,
                                                int minPoolSize,
                                                int maxPoolSize,
                                                long keepAliveTime,
                                                int queueCapacity,
                                                GeronimoManagedThreadFactorySource threadFactorySource,
                                                String[] contextHandlerClasses) {
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.queueCapacity = queueCapacity;
        this.threadFactory = threadFactorySource.getManagedThreadFactory();
        List<ManagedContextHandler> handlers = 
            ContextHandlerUtils.loadHandlers(classLoader, contextHandlerClasses);
        this.contextHandler = new ManagedContextHandlerChain(handlers);
    }

    protected synchronized ManagedExecutorService getManagedExecutorService() {
        if (this.executor == null) {
            BlockingQueue<Runnable> queue = null;
            if (this.queueCapacity <= 0) {
                queue = new LinkedBlockingQueue<Runnable>();
            } else {
                queue = new ArrayBlockingQueue<Runnable>(this.queueCapacity); 
            }
            this.executor = new ComponentManagedExecutorService(this.minPoolSize,
                                                                this.maxPoolSize,
                                                                this.keepAliveTime,
                                                                TimeUnit.MILLISECONDS,
                                                                queue, 
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
        return new ManagedExecutorServiceFacade(getManagedExecutorService(), false);
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
            GBeanInfoBuilder.createStatic(ComponentManagedExecutorServiceGBean.class, 
                                          ManagedConstants.MANAGED_EXECUTOR_SERVICE);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        
        infoFactory.addAttribute("minPoolSize", int.class, true);
        infoFactory.addAttribute("maxPoolSize", int.class, true);
        infoFactory.addAttribute("keepAliveTime", long.class, true);
        infoFactory.addAttribute("queueCapacity", int.class, true);
        infoFactory.addAttribute("contextHandlers", String[].class, true);

        infoFactory.addReference("threadFactory", GeronimoManagedThreadFactorySource.class);

        infoFactory.setConstructor(new String[] { "classLoader",
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
