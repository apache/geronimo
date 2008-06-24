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

/**
 * Deploys ServerManagedExecutorServiceGBean under the right JMX object name. 
 */
public class ServerManagedExecutorServiceWrapperGBean 
    extends GBeanBuilder
    implements GBeanLifecycle {

    private final static Log LOG = LogFactory.getLog(ServerManagedExecutorServiceWrapperGBean.class);
    
    public static final GBeanInfo GBEAN_INFO;

    private AbstractName name;
      
    private String[] contextHandlerClasses;
    private int minPoolSize;
    private int maxPoolSize;
    private long keepAliveTime;
    private int queueCapacity;
    
    private AbstractName executorServiceName;
    private ServerManagedExecutorServiceGBean executorServiceGBean;
        
    public ServerManagedExecutorServiceWrapperGBean(Kernel kernel,                                              
                                                    AbstractName name, 
                                                    ClassLoader classLoader, 
                                                    int minPoolSize,
                                                    int maxPoolSize,
                                                    long keepAliveTime,
                                                    int queueCapacity,
                                                    GeronimoManagedThreadFactorySource threadFactorySource,
                                                    String[] contextHandlerClasses) {   
        super(kernel, classLoader);
        this.name = name;
                
        this.contextHandlerClasses = contextHandlerClasses;
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.queueCapacity = queueCapacity;        
    }
                    
    public void doStart() throws Exception {   
        String threadName =  (String)this.name.getName().get("name");
        this.executorServiceName = kernel.getNaming().createRootName(name.getArtifact(), threadName, ManagedConstants.MANAGED_EXECUTOR_SERVICE);
        GBeanData executorServiceData = new GBeanData(this.executorServiceName, ServerManagedExecutorServiceGBean.getGBeanInfo());
        executorServiceData.setAttribute("contextHandlers", this.contextHandlerClasses);     
        executorServiceData.setAttribute("minPoolSize", this.minPoolSize);
        executorServiceData.setAttribute("maxPoolSize", this.maxPoolSize);
        executorServiceData.setAttribute("keepAliveTime", this.keepAliveTime);
        executorServiceData.setAttribute("queueCapacity", this.queueCapacity);
        
        GBeanData wrapperData = kernel.getGBeanData(this.name);
        
        executorServiceData.setReferencePatterns("threadFactory", 
                                                 wrapperData.getReferencePatterns("threadFactory"));
        
        addGBeanKernel(this.executorServiceName, executorServiceData);

        this.executorServiceGBean = (ServerManagedExecutorServiceGBean)kernel.getGBean(this.executorServiceName);
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
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ServerManagedExecutorServiceWrapperGBean.class, ManagedConstants.MANAGED_THREAD_FACTORY + "Builder");

        infoFactory.addAttribute("classLoader", ClassLoader.class, false, false);
        infoFactory.addAttribute("abstractName", AbstractName.class, false, false);
        infoFactory.addAttribute("kernel", Kernel.class, false, false);
                
        infoFactory.addAttribute("minPoolSize", int.class, true);
        infoFactory.addAttribute("maxPoolSize", int.class, true);
        infoFactory.addAttribute("keepAliveTime", long.class, true);
        infoFactory.addAttribute("queueCapacity", int.class, true);
        infoFactory.addAttribute("contextHandlers", String[].class, true);
        
        infoFactory.addReference("threadFactory", GeronimoManagedThreadFactorySource.class);
        
        infoFactory.setConstructor(new String[] { "kernel",  
                                                  "abstractName", 
                                                  "classLoader",
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
