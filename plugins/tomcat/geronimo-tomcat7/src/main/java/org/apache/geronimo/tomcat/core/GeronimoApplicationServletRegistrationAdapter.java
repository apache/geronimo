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

package org.apache.geronimo.tomcat.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;

import org.apache.catalina.LifecycleState;
import org.apache.catalina.Wrapper;
import org.apache.geronimo.tomcat.GeronimoStandardContext;
import org.apache.geronimo.web.security.SpecSecurityBuilder;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoApplicationServletRegistrationAdapter implements ServletRegistration.Dynamic {

    private ServletRegistration.Dynamic applicationServletRegistration;

    private Wrapper wrapper;

    private GeronimoStandardContext standardContext;

    private GeronimoApplicationContext applicationContext;

    public GeronimoApplicationServletRegistrationAdapter(GeronimoStandardContext standardContext, GeronimoApplicationContext applicationContext, Wrapper wrapper,
            ServletRegistration.Dynamic applicationServletRegistration) {
        this.applicationServletRegistration = applicationServletRegistration;
        this.standardContext = standardContext;
        this.wrapper = wrapper;
        this.applicationContext = applicationContext;
    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {
        applicationServletRegistration.setLoadOnStartup(loadOnStartup);
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        applicationServletRegistration.setMultipartConfig(multipartConfig);
    }

    @Override
    public void setRunAsRole(String roleName) {
        applicationServletRegistration.setRunAsRole(roleName);
        SpecSecurityBuilder specSecurityBuilder = applicationContext.getSpecSecurityBuilder();
        specSecurityBuilder.declareRoles(roleName);
    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        if (constraint == null) {
            throw new IllegalArgumentException("ServletSecurityElement configured by setServletSecurity should not be null");
        }
        if (standardContext.getState() != LifecycleState.STARTING_PREP) {
            throw new IllegalStateException("setServletSecurity action is not allowed after the context " + standardContext.getPath() + " is initialized");
        }
        SpecSecurityBuilder specSecurityBuilder = applicationContext.getSpecSecurityBuilder();
        if (specSecurityBuilder == null) {
            //Should Never Happen ?
            throw new IllegalStateException(
                    "Web security builder is null, setServletSecurity action is not supported, you must make sure enable the security configuration while deploying the web application");
        }
        return specSecurityBuilder.setServletSecurity(constraint, getMappings());
    }

    @Override
    public Set<String> addMapping(String... urlPatterns) {
        return applicationServletRegistration.addMapping(urlPatterns);
    }

    @Override
    public Collection<String> getMappings() {
        return applicationServletRegistration.getMappings();
    }

    @Override
    public String getRunAsRole() {
        return applicationServletRegistration.getRunAsRole();
    }

    @Override
    public void setAsyncSupported(boolean asyncSupported) {
        applicationServletRegistration.setAsyncSupported(asyncSupported);
    }

    @Override
    public String getClassName() {
        return applicationServletRegistration.getClassName();
    }

    @Override
    public String getInitParameter(String name) {
        return applicationServletRegistration.getInitParameter(name);
    }

    @Override
    public Map<String, String> getInitParameters() {
        return applicationServletRegistration.getInitParameters();
    }

    @Override
    public String getName() {
        return applicationServletRegistration.getName();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return applicationServletRegistration.setInitParameter(name, value);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        return applicationServletRegistration.setInitParameters(initParameters);
    }
}
