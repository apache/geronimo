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

package org.apache.geronimo.security.bridge;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;


/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:08 $
 */
public abstract class AbstractPrincipalMappingUserPasswordRealmBridge extends AbstractRealmBridge {

    private static final GBeanInfo GBEAN_INFO;

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
            public void handle(Callback[] callbacks)
                    throws IOException, UnsupportedCallbackException {
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

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(CallerIdentityUserPasswordRealmBridge.class.getName(), AbstractRealmBridge.getGBeanInfo());
        infoFactory.addAttribute(new GAttributeInfo("PrincipalSourceType", true));
        infoFactory.addAttribute(new GAttributeInfo("PrincipalTargetCallbackName", true));
        infoFactory.addAttribute(new GAttributeInfo("UserNameSourceType", true));
        infoFactory.addAttribute(new GAttributeInfo("UserNameTargetCallbackName", true));
        infoFactory.addAttribute(new GAttributeInfo("PasswordSourceType", true));
        infoFactory.setConstructor(new GConstructorInfo(new String[]{"TargetRealm", "PrincipalSourceType", "PrincipalTargetCallbackName", "UserNameSourceType", "UserNameTargetCallbackName", "PasswordSourceType"},
                                                        new Class[]{String.class, Class.class, String.class, Class.class, String.class, Class.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
