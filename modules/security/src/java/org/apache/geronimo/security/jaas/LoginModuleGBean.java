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
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class LoginModuleGBean {
    private String loginDomainName;
    private String loginModuleClass;
    private Properties options;
    private String objectName;
    private boolean serverSide;

    public LoginModuleGBean() {
    }
    
    public LoginModuleGBean(String loginModuleClass, String objectName, boolean serverSide) {
        this.loginModuleClass = loginModuleClass;
        this.objectName = objectName;
        this.serverSide = serverSide;
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

    public String getObjectName() {
        return objectName;
    }

    public boolean isServerSide() {
        return serverSide;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(LoginModuleGBean.class, NameFactory.LOGIN_MODULE);
        infoFactory.addAttribute("options", Properties.class, true);
        infoFactory.addAttribute("loginModuleClass", String.class, true);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("serverSide", boolean.class, true);
        infoFactory.addAttribute("loginDomainName", String.class, true);
        infoFactory.setConstructor(new String[]{"loginModuleClass","objectName","serverSide"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
