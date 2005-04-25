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

import org.apache.catalina.Engine;
import org.apache.catalina.Realm;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

public class EngineGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {
    
    private final Engine engine;

    public EngineGBean(String className, Map initParams, ObjectRetriever realmGBean) throws Exception {
        super(); // TODO: make it an attribute
        
        if (className == null){
            className = "org.apache.geronimo.tomcat.TomcatEngine";
        }
        
        engine = (Engine)Class.forName(className).newInstance();

        //Set the parameters
        setParameters(engine, initParams);
        
        if (realmGBean != null){
            engine.setRealm((Realm)realmGBean.getInternalObject());
        }
    }

    public Object getInternalObject() {
        return engine;
    }

    public void doFail() {
    }

    public void doStart() throws Exception {
    }

    public void doStop() throws Exception {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("TomcatEngine", EngineGBean.class);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addReference("realmGBean", ObjectRetriever.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addOperation("getInternalObject");
        infoFactory.setConstructor(new String[] { "className", "initParams", "realmGBean" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
