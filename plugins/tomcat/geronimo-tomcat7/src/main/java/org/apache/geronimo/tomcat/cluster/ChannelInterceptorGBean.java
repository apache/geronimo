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

import org.apache.geronimo.tomcat.BaseGBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.catalina.tribes.ChannelInterceptor;
import org.apache.catalina.tribes.group.interceptors.StaticMembershipInterceptor;
import org.apache.catalina.tribes.membership.StaticMember;

public class ChannelInterceptorGBean extends BaseGBean {

    private static final Logger log = LoggerFactory.getLogger(ChannelInterceptorGBean.class);

    public static final String J2EE_TYPE = "ChannelInterceptor";

    private final ChannelInterceptor interceptor;
    private final ChannelInterceptorGBean nextInterceptor;

    public ChannelInterceptorGBean() {
        interceptor = null;
        nextInterceptor = null;
    }

    public ChannelInterceptorGBean(String className, Map initParams,
       StaticMemberGBean staticMember, ChannelInterceptorGBean nextInterceptor) throws Exception {

        super(); // TODO: make it an attribute

        // Validate
        if (className == null) {
            throw new IllegalArgumentException("Must have a 'className' attribute.");
        }

        if (nextInterceptor != null){
            if (!(nextInterceptor.getInternalObject() instanceof ChannelInterceptor)){
                throw new IllegalArgumentException("nextInterceptor is not of type ChannelInterceptor.");                
            }
            
            this.nextInterceptor = nextInterceptor;
        } else {
            this.nextInterceptor = null;
        }

        // Create the ChannelInterceptor object
        interceptor = (ChannelInterceptor) Class.forName(className).newInstance();

        // Set the parameters
        setParameters(interceptor, initParams);

        //Add the static member
        boolean addNextStaticMember = true;
        
        while (addNextStaticMember) {
            if (staticMember != null && interceptor instanceof StaticMembershipInterceptor){
                StaticMembershipInterceptor staticMembershipInterceptor= (StaticMembershipInterceptor) interceptor;
                staticMembershipInterceptor.addStaticMember((StaticMember)staticMember.getInternalObject());
                if ( addNextStaticMember = (staticMember.getNextStaticMember() != null) ? true : false ) {
                    staticMember = (StaticMemberGBean) staticMember.getNextStaticMember();
                }
            } else {
                addNextStaticMember = false;
            }
        }

    }

    public Object getInternalObject() {
        return interceptor;
    }

    public void doFail() {
        log.warn("Failed");
    }

    public void doStart() throws Exception {
        log.debug("Started channel interceptor gbean.");
    }

    public void doStop() throws Exception {
        log.debug("Stopped channel interceptor gbean.");
    }
    
    public ChannelInterceptorGBean getNextInterceptor() {
        return nextInterceptor;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("ChannelInterceptor", ChannelInterceptorGBean.class, J2EE_TYPE);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addReference("StaticMember", StaticMemberGBean.class, StaticMemberGBean.J2EE_TYPE);
        infoFactory.addReference("NextInterceptor", ChannelInterceptorGBean.class, J2EE_TYPE);
        infoFactory.addOperation("getInternalObject", "Object");
        infoFactory.addOperation("getNextInterceptor","ChannelInterceptorGBean");
        infoFactory.setConstructor(new String[] { 
                "className", 
                "initParams", 
                "StaticMember", 
                "NextInterceptor" });
        
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
