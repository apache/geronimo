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

package org.apache.geronimo.jetty8.security;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;
import javax.security.jacc.PolicyContextException;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.jetty8.handler.GeronimoWebAppContext;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.web.security.SpecSecurityBuilder;
import org.apache.geronimo.web.security.WebSecurityConstraintStore;
import org.eclipse.jetty.util.component.LifeCycle;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class JACCSecurityEventListener implements LifeCycle.Listener {

    private static final Logger logger = LoggerFactory.getLogger(JACCSecurityEventListener.class);

    private Bundle bundle;

    private boolean annotationScanRequired;

    private String contextId;

    private ApplicationPolicyConfigurationManager applicationPolicyConfigurationManager;

    private WebAppInfo webXmlAppInfo;

    private GeronimoWebAppContext.SecurityContext applicationContext;

    private WebSecurityConstraintStore webSecurityConstraintStore;

    public JACCSecurityEventListener(Bundle bundle, WebAppInfo webXmlAppInfo, boolean annotationScanRequired, ApplicationPolicyConfigurationManager applicationPolicyConfigurationManager,
            String contextId, GeronimoWebAppContext.SecurityContext applicationContext) {
        this.bundle = bundle;
        this.contextId = contextId;
        this.annotationScanRequired = annotationScanRequired;
        this.applicationPolicyConfigurationManager = applicationPolicyConfigurationManager;
        this.webXmlAppInfo = webXmlAppInfo == null ? new WebAppInfo() : webXmlAppInfo;
        this.applicationContext = applicationContext;
    }

    @Override
    public void lifeCycleStarting(LifeCycle event) {
        webSecurityConstraintStore = new WebSecurityConstraintStore(webXmlAppInfo, bundle, annotationScanRequired, applicationContext);
        applicationContext.setWebSecurityConstraintStore(webSecurityConstraintStore);
    }

    @Override
    public void lifeCycleStarted(LifeCycle event) {
        //Calculate the final Security Permissions
        SpecSecurityBuilder specSecurityBuilder = new SpecSecurityBuilder(webSecurityConstraintStore.exportMergedWebAppInfo());
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
            applicationContext.setWebSecurityConstraintStore(null);
        }
    }

    @Override
    public void lifeCycleFailure(LifeCycle event, Throwable cause) {
    }

    @Override
    public void lifeCycleStopping(LifeCycle event) {
    }

    @Override
    public void lifeCycleStopped(LifeCycle event) {
    }

}
