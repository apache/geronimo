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

package org.apache.geronimo.security.bridge;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;


/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractRealmBridge implements RealmBridge {
    private String targetRealm;

    public AbstractRealmBridge() {
    }

    public AbstractRealmBridge(String targetRealm) {
        this.targetRealm = targetRealm;
    }

    public Subject mapSubject(Subject sourceSubject) throws LoginException {
        Subject targetSubject = new Subject();
        LoginContext loginContext = new LoginContext(targetRealm, targetSubject, getCallbackHandler(sourceSubject));
        loginContext.login();
        return targetSubject;
    }

    protected abstract CallbackHandler getCallbackHandler(Subject sourceSubject);

    public String getTargetRealm() {
        return targetRealm;
    }

    public void setTargetRealm(String targetRealm) {
        this.targetRealm = targetRealm;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AbstractRealmBridge.class);
        infoFactory.addAttribute("targetRealm", String.class, true);
        infoFactory.setConstructor(new String[]{"targetRealm"});
        infoFactory.addOperation("mapSubject", new Class[] {Subject.class});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
