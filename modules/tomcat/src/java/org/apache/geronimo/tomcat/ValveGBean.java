/**
*
* Copyright 2003-2004 The Apache Software Foundation
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
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

import org.apache.catalina.Valve;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

public class ValveGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {

    public static final String J2EE_TYPE = "TomcatValve";
        
    private final Valve valve;
    private final ValveGBean nextValve;
 
    
    public ValveGBean(){      
        valve = null;
        nextValve = null;
    }
    
    public ValveGBean(String className, Map initParams, ValveGBean nextValve) throws Exception{

        //Validate
        if (className == null){
            throw new IllegalArgumentException("className cannot be null.");
        }
        
        if (nextValve != null){
            if (!(nextValve.getInternalObject() instanceof Valve)){
                throw new IllegalArgumentException("className cannot be null.");                
            }
            
            this.nextValve = nextValve;
        } else {
            this.nextValve = null;
        }
        
        //Create the Valve object
        valve = (Valve)Class.forName(className).newInstance();

        //Set the parameters
        setParameters(valve, initParams);
        
    }
    
    public void doStart() throws Exception {
    }

    public void doStop() throws Exception {
    }

    public void doFail() {
    }

    public Object getInternalObject() {
        return valve;
    }

    public ValveGBean getNextValve() {
        return nextValve;
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ValveGBean.class, J2EE_TYPE);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addReference("NextValve", ValveGBean.class, J2EE_TYPE);
        infoFactory.addOperation("getInternalObject");
        infoFactory.addOperation("getNextValve");
        infoFactory.setConstructor(new String[] { "className", "initParams", "NextValve" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
