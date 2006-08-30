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
package org.apache.geronimo.tomcat.cluster;

import java.util.Map;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.tomcat.ManagerGBean;
import org.apache.geronimo.tomcat.ObjectRetriever;
import org.codehaus.wadi.tomcat55.TomcatManager;

public class WADIGBean extends ManagerGBean implements GBeanLifecycle, ObjectRetriever{
    
    public WADIGBean() throws Exception{
        
        //super("org.codehaus.wadi.tomcat55.TomcatManager", null);
        super("org.codehaus.wadi.tomcat55.TomcatManager");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("TomcatManager", WADIGBean.class, ManagerGBean.GBEAN_INFO);
        //infoFactory.addOperation("getInternalObject");
        infoFactory.setConstructor(new String[0]);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
