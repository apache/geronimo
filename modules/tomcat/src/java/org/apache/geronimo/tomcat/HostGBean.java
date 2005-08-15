/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

import org.apache.catalina.Host;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardHost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev$ $Date$
 */
public class HostGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {

    private static final Log log = LogFactory.getLog(HostGBean.class);
    
    public static final String J2EE_TYPE = "Host";
    
    private final Host host;

    public HostGBean(String className, 
            Map initParams, 
            ObjectRetriever realmGBean,            
            ValveGBean tomcatValveChain) throws Exception {
        super(); // TODO: make it an attribute
        
        //Validate
        if (className == null){
            className = "org.apache.catalina.core.StandardHost";
        }
        
        //Create the Host object
        host = (Host)Class.forName(className).newInstance();
        
        //Set the parameters
        setParameters(host, initParams);
        
        if (realmGBean != null)
            host.setRealm((Realm)realmGBean.getInternalObject());

        //Add the valve list
        if (host instanceof StandardHost){
            if (tomcatValveChain != null){
                ValveGBean valveGBean = tomcatValveChain;
                while(valveGBean != null){
                    ((StandardHost)host).addValve((Valve)valveGBean.getInternalObject());
                    valveGBean = valveGBean.getNextValve();
                }
            }
        }
        
    }

    public Object getInternalObject() {
        return host;
    }

    public void doFail() {
        log.info("Failed");
    }

    public void doStart() throws Exception {
        log.info("Started");
    }

    public void doStop() throws Exception {
        log.info("Stopped");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("TomcatHost", HostGBean.class, J2EE_TYPE);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addReference("RealmGBean", ObjectRetriever.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("TomcatValveChain", ValveGBean.class, ValveGBean.J2EE_TYPE);
        infoFactory.addOperation("getInternalObject");
        infoFactory.setConstructor(new String[] { "className", "initParams", "RealmGBean", "TomcatValveChain" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
