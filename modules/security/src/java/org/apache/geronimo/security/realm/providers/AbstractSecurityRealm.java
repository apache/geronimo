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

package org.apache.geronimo.security.realm.providers;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.security.realm.SecurityRealm;


/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractSecurityRealm implements SecurityRealm, GBeanLifecycle {
    private final String realmName;
    private long maxLoginModuleAge;

    //default constructor for use as endpoint
    //TODO we probably always use the SecurityRealm interface and don't need this
    public AbstractSecurityRealm() {
        this.realmName = null;
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

    public void doStart() {
    }

    public void doStop() {
    }

    public void doFail() {
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(AbstractSecurityRealm.class);

        infoFactory.addInterface(SecurityRealm.class);
        infoFactory.addAttribute("realmName", String.class, true);
        infoFactory.addAttribute("maxLoginModuleAge", Long.TYPE, true);

        infoFactory.setConstructor(new String[]{"realmName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
