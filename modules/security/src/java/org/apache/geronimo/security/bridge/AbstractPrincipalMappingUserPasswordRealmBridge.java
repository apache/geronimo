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

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;


/**
 * @version $Revision: 1.6 $ $Date: 2004/07/12 06:07:50 $
 */
public abstract class AbstractPrincipalMappingUserPasswordRealmBridge extends AbstractRealmBridge {
    protected final Map principalMap = new HashMap();
    private Class principalSourceType;
    private String principalTargetCallbackName;
    protected final Map userNameMap = new HashMap();
    private Class userNameSourceType;
    private String userNameTargetCallbackName;
    protected final Map passwordMap = new HashMap();
    private Class passwordSourceType;


    public AbstractPrincipalMappingUserPasswordRealmBridge() {
    }

    public AbstractPrincipalMappingUserPasswordRealmBridge(String targetRealm,
            Class principalSourceType,
            String principalTargetCallbackName,
            Class userNameSourceType,
            String userNameTargetCallbackName,
            Class passwordSourceType) {

        super(targetRealm);
        this.principalSourceType = principalSourceType;
        this.principalTargetCallbackName = principalTargetCallbackName;
        this.userNameSourceType = userNameSourceType;
        this.userNameTargetCallbackName = userNameTargetCallbackName;
        this.passwordSourceType = passwordSourceType;
    }

    public Class getPrincipalSourceType() {
        return principalSourceType;
    }

    public void setPrincipalSourceType(Class principalSourceType) {
        this.principalSourceType = principalSourceType;
    }

    public String getPrincipalTargetCallbackName() {
        return principalTargetCallbackName;
    }

    public void setPrincipalTargetCallbackName(String principalTargetCallbackName) {
        this.principalTargetCallbackName = principalTargetCallbackName;
    }

    public Class getUserNameSourceType() {
        return userNameSourceType;
    }

    public void setUserNameSourceType(Class userNameSourceType) {
        this.userNameSourceType = userNameSourceType;
    }

    public String getUserNameTargetCallbackName() {
        return userNameTargetCallbackName;
    }

    public void setUserNameTargetCallbackName(String userNameTargetCallbackName) {
        this.userNameTargetCallbackName = userNameTargetCallbackName;
    }

    public Class getPasswordSourceType() {
        return passwordSourceType;
    }

    public void setPasswordSourceType(Class passwordSourceType) {
        this.passwordSourceType = passwordSourceType;
    }

    protected CallbackHandler getCallbackHandler(final Subject sourceSubject) {
        return new CallbackHandler() {
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                Principal principalSourcePrincipal = findPrincipalOfType(sourceSubject, principalSourceType);
                Principal userNameSourcePrincipal;
                if (userNameSourceType == principalSourceType) {
                    userNameSourcePrincipal = principalSourcePrincipal;
                } else {
                    userNameSourcePrincipal = findPrincipalOfType(sourceSubject, userNameSourceType);
                }
                Principal passwordSourcePrincipal;
                if (passwordSourceType == principalSourceType) {
                    passwordSourcePrincipal = principalSourcePrincipal;
                } else {
                    passwordSourcePrincipal = findPrincipalOfType(sourceSubject, passwordSourceType);
                }
                for (int i = 0; i < callbacks.length; i++) {
                    Callback callback = callbacks[i];
                    if (callback instanceof NameCallback) {
                        NameCallback nameCallback = (NameCallback) callback;
                        if (nameCallback.getPrompt().equals(principalTargetCallbackName)) {
                            nameCallback.setName((String) principalMap.get(principalSourcePrincipal.getName()));
                        } else if (nameCallback.getPrompt().equals(userNameTargetCallbackName)) {
                            nameCallback.setName((String) userNameMap.get(userNameSourcePrincipal.getName()));
                        } else {
                            throw new UnsupportedCallbackException(callback, "Only name callbacks with prompts " + principalTargetCallbackName + " or " + userNameTargetCallbackName + " are supported");
                        }
                    } else if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword((char[]) passwordMap.get(passwordSourcePrincipal.getName()));
                    } else {
                        throw new UnsupportedCallbackException(callback, "Only name and password callbacks supported");
                    }

                }
            }

            private Principal findPrincipalOfType(final Subject sourceSubject, Class principalClass) throws UnsupportedCallbackException {
                Set principalPrincipals = sourceSubject.getPrincipals(principalClass);
                if (principalPrincipals == null || principalPrincipals.size() != 1) {
                    throw new UnsupportedCallbackException(null, "No principals of type " + principalClass + " to read");
                }
                Principal principal = (Principal) principalPrincipals.iterator().next();
                return principal;
            }

        };
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(CallerIdentityUserPasswordRealmBridge.class, AbstractRealmBridge.GBEAN_INFO);

        infoFactory.addAttribute("principalSourceType", Class.class, true);
        infoFactory.addAttribute("principalTargetCallbackName", String.class, true);
        infoFactory.addAttribute("userNameSourceType", Class.class, true);
        infoFactory.addAttribute("userNameTargetCallbackName", String.class, true);
        infoFactory.addAttribute("passwordSourceType", Class.class, true);

        infoFactory.setConstructor(new String[]{
            "userNameSourceType",
            "userNameTargetCallbackName",
            "passwordSourceType"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
