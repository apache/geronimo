/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.security.bridge;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GConstructorInfo;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/20 06:12:45 $
 *
 * */
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


    public AbstractPrincipalMappingUserPasswordRealmBridge() {}

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
                        NameCallback nameCallback = (NameCallback)callback;
                        if (nameCallback.getPrompt().equals(principalTargetCallbackName)) {
                            nameCallback.setName((String)principalMap.get(principalSourcePrincipal.getName()));
                        } else if (nameCallback.getPrompt().equals(userNameTargetCallbackName)) {
                            nameCallback.setName((String)userNameMap.get(userNameSourcePrincipal.getName()));
                        } else {
                            throw new UnsupportedCallbackException(callback, "Only name callbacks with prompts " + principalTargetCallbackName + " or " + userNameTargetCallbackName + " are supported");
                        }
                    } else if (callback instanceof PasswordCallback) {
                        ((PasswordCallback)callback).setPassword((char[])passwordMap.get(passwordSourcePrincipal.getName()));
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
                Principal principal = (Principal)principalPrincipals.iterator().next();
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
        infoFactory.setConstructor(new GConstructorInfo(
                new String[] {"TargetRealm", "PrincipalSourceType", "PrincipalTargetCallbackName", "UserNameSourceType", "UserNameTargetCallbackName", "PasswordSourceType"},
        new Class[] {String.class, Class.class, String.class, Class.class, String.class, Class.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() {
        GeronimoMBeanInfo mbeanInfo = AbstractRealmBridge.getGeronimoMBeanInfo();
        //set target class in concrete subclass
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("PrincipalSourceType", true, true, "Class of principal to use as source for target principal map key"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("PrincipalTargetCallbackName", true, true, "Pronpt of NameCallback used to query for target principal"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("UserNameSourceType", true, true, "Class of principal to use as source for target user name map key"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("UserNameTargetCallbackName", true, true, "Pronpt of NameCallback used to query for target user name"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("PasswordSourceType", true, true, "Class of principal to use as source for target password map key"));
        return mbeanInfo;
    }

}
