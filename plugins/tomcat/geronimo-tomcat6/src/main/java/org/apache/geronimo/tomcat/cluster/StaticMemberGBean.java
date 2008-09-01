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

import org.apache.catalina.tribes.membership.StaticMember;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.tomcat.BaseGBean;
import org.apache.geronimo.tomcat.ObjectRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class StaticMemberGBean extends BaseGBean implements
        GBeanLifecycle, ObjectRetriever {

    private static final Logger log = LoggerFactory.getLogger(StaticMemberGBean.class);

    public static final String J2EE_TYPE = "StaticMember";

    private final StaticMember staticMember;
    private final StaticMemberGBean nextStaticMember;

    public StaticMemberGBean() {
        staticMember=null;
        nextStaticMember=null;
    }
    
    public StaticMemberGBean(String className, Map initParams, StaticMemberGBean nextStaticMember) throws Exception {

        super(); // TODO: make it an attribute

        // Validate
        if (className == null) {
            throw new IllegalArgumentException("Must have a 'className' attribute.");
        }
        
        if (nextStaticMember != null) {
            if (!(nextStaticMember.getInternalObject() instanceof StaticMember)){
                throw new IllegalArgumentException("nextStaticMember is not of type StaticMember.");                
            }
            this.nextStaticMember = nextStaticMember;
        } else {
            this.nextStaticMember = null;
        }

        // Create the StaticMember object
        staticMember = (StaticMember) Class.forName(className).newInstance();

        // Set the parameters
        setParameters(staticMember, initParams);

    }

    public Object getInternalObject() {
        return staticMember;
    }
    
    public Object getNextStaticMember() {
        return nextStaticMember;
    }

    public void doFail() {
        log.warn("Failed");
    }

    public void doStart() throws Exception {
        log.debug("Started StaticMember gbean.");
    }

    public void doStop() throws Exception {
        log.debug("Stopped StaticMember gbean.");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("StaticMember", StaticMemberGBean.class, J2EE_TYPE);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addReference("NextStaticMember", StaticMemberGBean.class, J2EE_TYPE);
        infoFactory.addOperation("getInternalObject", "Object");
        infoFactory.addOperation("getNextStaticMember", "Object");
        infoFactory.setConstructor(new String[] { "className", "initParams", "NextStaticMember"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
