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

import org.apache.catalina.Container;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Service;
import org.apache.catalina.Valve;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType="TomcatValve")
public class ValveGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {

    private static final Logger log = LoggerFactory.getLogger(ValveGBean.class);

    public static final String J2EE_TYPE = "TomcatValve";
        
    private Valve valve;

    private ValveGBean nextValve;

    private String className;
    
    public ValveGBean(){      
        valve = null;
        nextValve = null;
        className = null;
    }

    public ValveGBean(
            //fish engine out of server configured with server.xml
            @ParamReference(name="Server")TomcatServerGBean server,
            @ParamAttribute(name="serviceName")String serviceName,
            @ParamAttribute(name="containerName") String containerName,
            @ParamAttribute(name="seq") int seq,
            
            //Legacy configuration
            @ParamAttribute(name="className") String className, 
            @ParamAttribute(name = "initParams") Map<String,String> initParams, 
            @ParamReference(name="NextValve",namingType="TomcatValve") ValveGBean nextValve, 
            @ParamSpecial(type=SpecialAttributeType.classLoader) ClassLoader classLoader) throws Exception{
        
        if (server != null) {
            Service service = server.getService(serviceName);
            Container targetContainer = null;
            Container engine = service.getContainer();
            if (engine.getName().equals(containerName)) {
                targetContainer = engine;
            } else {
                targetContainer = engine.findChild(containerName);
            }
            if (targetContainer == null) {
                log.warn("Could not find the container named [" + containerName + "] in the service [" + serviceName + "]");
                return;
            }
            Valve targetValve = findValve(targetContainer, seq);
            if (targetValve == null) {
                log.warn("Could not find the container named [" + containerName + "] with sequence [" + seq + "] in the service [" + serviceName + "]");
                return;
            }
            valve = targetValve;
        } else {
            //Validate
            if (className == null) {
                throw new IllegalArgumentException("className cannot be null.");
            }
            if (nextValve != null) {
                if (!(nextValve.getInternalObject() instanceof Valve)) {
                    throw new IllegalArgumentException("The class given as the NextValve attribute does not wrap an object of org.apache.catalina.Valve type.");
                }
                this.nextValve = nextValve;
            } else {
                this.nextValve = null;
            }
            this.className = className;
            //Create the Valve object
            valve = (Valve) classLoader.loadClass(className).newInstance();
            //Set the parameters
            setParameters(valve, initParams);
        }
        
    }

    
    private Valve findValve(Container container, int seq) {        
        Pipeline pipeline = container.getPipeline();
        if (pipeline == null) {
            return null;
        }
        Valve[] valves = pipeline.getValves();
        if (valves == null || seq >= valves.length) {
            return null;
        } else {
            return valves[seq];
        }
    }

    public void doStart() throws Exception {
        if (className != null) {
            log.debug(className + " started.");
        }
    }

    public void doStop() throws Exception {
        if (className != null) {
            log.debug(className + " stopped.");
        }
    }

    public void doFail() {
        if (className != null) {
            log.debug(className + " failed.");
        }
    }

    public Object getInternalObject() {
        return valve;
    }

    public ValveGBean getNextValve() {
        return nextValve;
    }       
}
