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

import java.lang.ClassLoader;
import java.util.Map;

import org.apache.catalina.Valve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

/**
 * @version $Rev$ $Date$
 */
public class ValveGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {

    private static final Logger log = LoggerFactory.getLogger(ValveGBean.class);

    public static final String J2EE_TYPE = "TomcatValve";
        
    private final Valve valve;
    private final ValveGBean nextValve;
    private final String className;
    
    public ValveGBean(){      
        valve = null;
        nextValve = null;
        className = null;
    }
    
    public ValveGBean(String className, Map initParams, ValveGBean nextValve, ClassLoader classLoader) throws Exception{

        //Validate
        if (className == null){
            throw new IllegalArgumentException("className cannot be null.");
        }
        
        if (nextValve != null){
            if (!(nextValve.getInternalObject() instanceof Valve)){
                throw new IllegalArgumentException("The class given as the NextValve attribute does not wrap an object of org.apache.catalina.Valve type.");                
            }
            this.nextValve = nextValve;
        } else {
            this.nextValve = null;
        }
        
        this.className = className;

        //Create the Valve object
        valve = (Valve)classLoader.loadClass(className).newInstance();

        //Set the parameters
        setParameters(valve, initParams);
        
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
        return valve;
    }

    public ValveGBean getNextValve() {
        return nextValve;
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ValveGBean.class, J2EE_TYPE);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addReference("NextValve", ValveGBean.class, J2EE_TYPE);
        infoFactory.addOperation("getInternalObject");
        infoFactory.addOperation("getNextValve");
        infoFactory.setConstructor(new String[] { "className", "initParams", "NextValve", "classLoader" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
