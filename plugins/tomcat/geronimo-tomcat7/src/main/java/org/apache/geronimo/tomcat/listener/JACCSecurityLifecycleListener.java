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

package org.apache.geronimo.tomcat.listener;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;
import javax.security.jacc.PolicyContextException;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.tomcat.GeronimoStandardContext;
import org.apache.geronimo.tomcat.core.GeronimoApplicationContext;
import org.apache.geronimo.web.security.SpecSecurityBuilder;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This LifecycleListener is used to calculate the JACC Security Permissions after the StandardContext is started
 * @version $Rev$ $Date$
 */
public class JACCSecurityLifecycleListener implements LifecycleListener {

    private static final Logger logger = LoggerFactory.getLogger(JACCSecurityLifecycleListener.class);

    private Bundle bundle;

    private boolean annotationScanRequired;

    private String contextId;

    private ApplicationPolicyConfigurationManager applicationPolicyConfigurationManager;

    private String deploymentDescriptor;

    public JACCSecurityLifecycleListener(Bundle bundle, String deploymentDescriptor, boolean annotationScanRequired, ApplicationPolicyConfigurationManager applicationPolicyConfigurationManager,
            String contextId) {
        this.bundle = bundle;
        this.contextId = contextId;
        this.annotationScanRequired = annotationScanRequired;
        this.applicationPolicyConfigurationManager = applicationPolicyConfigurationManager;
        this.deploymentDescriptor = deploymentDescriptor;
    }

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        String lifecycleEventType = lifecycleEvent.getType();
        if (lifecycleEventType.equals(Lifecycle.BEFORE_START_EVENT)) {
            //Initialize SpecSecurityBuilder
            SpecSecurityBuilder specSecurityBuilder = new SpecSecurityBuilder(bundle, deploymentDescriptor, annotationScanRequired);
            GeronimoStandardContext standardContext = (GeronimoStandardContext) lifecycleEvent.getSource();
            GeronimoApplicationContext applicationContext = (GeronimoApplicationContext) standardContext.getInternalServletContext();
            applicationContext.setSpecSecurityBuilder(specSecurityBuilder);
        } else if (lifecycleEventType.equals(Lifecycle.START_EVENT)) {
            GeronimoStandardContext standardContext = (GeronimoStandardContext) lifecycleEvent.getSource();
            GeronimoApplicationContext applicationContext = (GeronimoApplicationContext) standardContext.getInternalServletContext();
            //Calculate the final Security Permissions
            SpecSecurityBuilder specSecurityBuilder = applicationContext.getSpecSecurityBuilder();
            Map<String, ComponentPermissions> contextIdPermissionsMap = new HashMap<String, ComponentPermissions>();
            contextIdPermissionsMap.put(contextId, specSecurityBuilder.buildSpecSecurityConfig());
            //Update ApplicationPolicyConfigurationManager
            try {
                applicationPolicyConfigurationManager.updateApplicationPolicyConfiguration(contextIdPermissionsMap);
            } catch (LoginException e) {
                logger.error("Fail to set application policy configurations", e);
                throw new RuntimeException("Fail to set application policy configurations", e);
            } catch (PolicyContextException e) {
                logger.error("Fail to set application policy configurations", e);
                throw new RuntimeException("Fail to set application policy configurations", e);
            } catch (ClassNotFoundException e) {
                logger.error("Fail to set application policy configurations", e);
                throw new RuntimeException("Fail to set application policy configurations", e);
            } finally {
                //Clear SpecSecurityBuilder
                specSecurityBuilder.clear();
                applicationContext.setSpecSecurityBuilder(null);
            }
        }
    }
}
