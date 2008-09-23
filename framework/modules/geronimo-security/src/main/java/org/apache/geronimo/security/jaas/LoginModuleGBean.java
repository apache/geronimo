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
package org.apache.geronimo.security.jaas;

import java.util.Collections;
import java.util.Map;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.security.SecurityNames;


/**
 * A GBean that wraps a LoginModule, plus options to configure the LoginModule.
 * If you want to deploy the same LoginModule with different options, you need
 * more than one of these GBeans.  But if you want two security realms to refer
 * to exactly the same login module configuration, you can have both realms
 * refer to a single login module GBean.
 *
 * @version $Rev$ $Date$
 */
public class LoginModuleGBean implements LoginModuleSettings {
    private String loginDomainName;
    private String loginModuleClass;
    private Map<String, Object> options;
    private final String objectName;
    private boolean wrapPrincipals;
    private final ClassLoader classLoader;

    public LoginModuleGBean(String loginModuleClass, String objectName, boolean wrapPrincipals, Map<String, Object> options, String loginDomainName, ClassLoader classLoader) {
        this.loginModuleClass = loginModuleClass;
        this.objectName = objectName;
        this.wrapPrincipals = wrapPrincipals;
        this.options = options == null? Collections.<String, Object>emptyMap(): options;
        this.loginDomainName = loginDomainName;
        this.classLoader = classLoader;
    }

    public String getLoginDomainName() {
        return loginDomainName;
    }

    public void setLoginDomainName(String loginDomainName) {
        this.loginDomainName = loginDomainName;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public String getLoginModuleClass() {
        return loginModuleClass;
    }

    public void setLoginModuleClass(String loginModuleClass) {
        this.loginModuleClass = loginModuleClass;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isWrapPrincipals() {
        return wrapPrincipals;
    }

    public void setWrapPrincipals(boolean wrapPrincipals) {
        this.wrapPrincipals = wrapPrincipals;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(LoginModuleGBean.class, SecurityNames.LOGIN_MODULE);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addInterface(LoginModuleSettings.class, new String[] {"options", "loginModuleClass", "loginDomainName", "wrapPrincipals"},
        		                 new String[] {"options", "loginModuleClass", "wrapPrincipals"} );
        infoFactory.setConstructor(new String[]{"loginModuleClass", "objectName", "wrapPrincipals", "options", "loginDomainName", "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
