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
package org.apache.geronimo.tomcat.cluster;

import java.util.Map;

import org.apache.catalina.ha.ClusterListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.tomcat.BaseGBean;
import org.apache.geronimo.tomcat.ObjectRetriever;

public class ClusterListenerGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {

    private static final Logger log = LoggerFactory.getLogger(ClusterListenerGBean.class);

    public static final String J2EE_TYPE = "ClusterListener";
        
    private final ClusterListener listener;
    private final ClusterListenerGBean nextListener;
    private final String className;
 
    
    public ClusterListenerGBean(){      
        listener = null;
        nextListener = null;
        className = null;
    }
    
    public ClusterListenerGBean(String className, Map initParams, ClusterListenerGBean nextListener) throws Exception{

        //Validate
        if (className == null){
            throw new IllegalArgumentException("className cannot be null.");
        }
        
        if (nextListener != null){
            if (!(nextListener.getInternalObject() instanceof ClusterListener)){
                throw new IllegalArgumentException("nextListener is not of type ClusterListener.");                
            }
            
            this.nextListener = nextListener;
        } else {
            this.nextListener = null;
        }
        
        this.className = className;
        
        //Create the listener object
        listener = (ClusterListener)Class.forName(className).newInstance();

        //Set the parameters
        setParameters(listener, initParams);
        
    }
    
    public void doStart() throws Exception {
        log.debug(className + " started.");
    }

    public void doStop() throws Exception {
        log.debug(className + " stopped.");
    }

    public void doFail() {
        log.warn(className + " failed.");
    }

    public Object getInternalObject() {
        return listener;
    }

    public ClusterListenerGBean getNextValve() {
        return nextListener;
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("ClusterListener", ClusterListenerGBean.class, J2EE_TYPE);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addReference("NextListener", ClusterListenerGBean.class, J2EE_TYPE);
        infoFactory.addOperation("getInternalObject", "Object");
        infoFactory.addOperation("getNextValve", "ClusterListenerGBean");
        infoFactory.setConstructor(new String[] { 
                "className", 
                "initParams", 
                "NextListener" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
