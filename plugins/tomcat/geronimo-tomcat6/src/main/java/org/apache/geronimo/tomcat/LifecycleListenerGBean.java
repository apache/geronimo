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
package org.apache.geronimo.tomcat;

import java.util.Map;

import org.apache.catalina.LifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

/**
 * @version $Rev: 486195 $ $Date: 2006-12-12 08:42:02 -0700 (Tue, 12 Dec 2006) $
 */
public class LifecycleListenerGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {

    private static final Logger log = LoggerFactory.getLogger(LifecycleListenerGBean.class);

    public static final String J2EE_TYPE = "TomcatLifecycleListener";
        
    private final LifecycleListener listener;
    private final LifecycleListenerGBean nextListener;
    private final String className;
 
    
    public LifecycleListenerGBean(){      
        listener = null;
        nextListener = null;
        className = null;
    }
    
    public LifecycleListenerGBean(String className, Map initParams, LifecycleListenerGBean nextListener) throws Exception{

        //Validate
        if (className == null){
            throw new IllegalArgumentException("className cannot be null.");
        }
        
        if (nextListener != null){
            if (!(nextListener.getInternalObject() instanceof LifecycleListener)){
                throw new IllegalArgumentException("The class given as the NextListener attribute does not wrap an object of org.apache.catalina.LifecycleListener type.");                
            }
            this.nextListener = nextListener;
        } else {
            this.nextListener = null;
        }
        
        this.className = className;
        
        //Create the Listener object
        listener = (LifecycleListener)Class.forName(className).newInstance();

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

    public LifecycleListenerGBean getNextListener() {
        return nextListener;
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(LifecycleListenerGBean.class, J2EE_TYPE);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addReference("NextListener", LifecycleListenerGBean.class, J2EE_TYPE);
        infoFactory.addOperation("getInternalObject");
        infoFactory.addOperation("getNextListener");
        infoFactory.setConstructor(new String[] { "className", "initParams", "NextListener" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
