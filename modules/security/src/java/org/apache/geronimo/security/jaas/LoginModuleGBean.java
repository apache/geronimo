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
package org.apache.geronimo.security.jaas;

import java.util.Properties;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;


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
    private Properties options;
    private final String objectName;
    private boolean serverSide;
    private boolean wrapPrincipals;
    private final ClassLoader classLoader;

    public LoginModuleGBean() {
        classLoader = null;
        objectName = null;
    }

    public LoginModuleGBean(String loginModuleClass, String objectName, boolean serverSide, boolean wrapPrincipals, ClassLoader classLoader) {
        this.loginModuleClass = loginModuleClass;
        this.objectName = objectName;
        this.serverSide = serverSide;
        this.wrapPrincipals = wrapPrincipals;
        this.classLoader = classLoader;
    }

    public String getLoginDomainName() {
        return loginDomainName;
    }

    public void setLoginDomainName(String loginDomainName) {
        this.loginDomainName = loginDomainName;
    }

    public Properties getOptions() {
        return options;
    }

    public void setOptions(Properties options) {
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

    public boolean isServerSide() {
        return serverSide;
    }

    public void setServerSide(boolean serverSide) {
        this.serverSide = serverSide;
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
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(LoginModuleGBean.class, NameFactory.LOGIN_MODULE);
        infoFactory.addAttribute("options", Properties.class, true);
        infoFactory.addAttribute("loginModuleClass", String.class, true);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("serverSide", boolean.class, true);
        infoFactory.addAttribute("loginDomainName", String.class, true);
        infoFactory.addAttribute("wrapPrincipals", boolean.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addInterface(LoginModuleSettings.class);
        infoFactory.setConstructor(new String[]{"loginModuleClass", "objectName", "serverSide", "wrapPrincipals", "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
