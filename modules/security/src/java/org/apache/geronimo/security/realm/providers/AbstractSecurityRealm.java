/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.security.realm.providers;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.security.realm.SecurityRealm;
import org.apache.regexp.RE;


/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:09 $
 */
public abstract class AbstractSecurityRealm implements SecurityRealm, GBean {

    private static final GBeanInfo GBEAN_INFO;

    private String realmName;
    private long maxLoginModuleAge;

    //default constructor for use as endpoint
    //TODO we probably always use the SecurityRealm interface and don't need this
    public AbstractSecurityRealm() {
    }


    public AbstractSecurityRealm(String realmName) {
        this.realmName = realmName;
    }

    public String getRealmName() {
        return realmName;
    }

    public long getMaxLoginModuleAge() {
        return maxLoginModuleAge;
    }

    public void setMaxLoginModuleAge(long maxLoginModuleAge) {
        this.maxLoginModuleAge = maxLoginModuleAge;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() {
    }

    public void doStop() {
    }

    public void doFail() {
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AbstractSecurityRealm.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("RealmName", true));
        infoFactory.addAttribute(new GAttributeInfo("MaxLoginModuleAge", true));
        infoFactory.addOperation(new GOperationInfo("getGroupPrincipals"));
        infoFactory.addOperation(new GOperationInfo("getGroupPrincipals", new String[]{RE.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("getUserPrincipals"));
        infoFactory.addOperation(new GOperationInfo("getUserPrincipals", new String[]{RE.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("refresh"));
        infoFactory.addOperation(new GOperationInfo("getAppConfigurationEntry"));
        infoFactory.setConstructor(new GConstructorInfo(new String[]{"realmName"}, new Class[]{String.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
