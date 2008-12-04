/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.security.credentialstore;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.lang.reflect.Constructor;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.Configuration;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.security.jaas.ConfigurationEntryFactory;
import org.apache.geronimo.security.jaas.GeronimoLoginConfiguration;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class SimpleCredentialStoreImpl implements CredentialStore {

    private final Map<String, Map<String, Map<String, SingleCallbackHandler>>> credentialStore = new HashMap<String, Map<String, Map<String, SingleCallbackHandler>>>();
    private final Configuration configuration;

    public SimpleCredentialStoreImpl(@ParamAttribute(name="credentialStore")Map<String, Map<String, Map<String, String>>> credentials,
                                     @ParamReference(name="Realms", namingType = SecurityNames.SECURITY_REALM)Collection<ConfigurationEntryFactory> realms,
                                     @ParamSpecial(type = SpecialAttributeType.classLoader)ClassLoader cl) {
        if (realms != null) {
            configuration = new GeronimoLoginConfiguration(realms, true);
        } else {
            configuration = null;
        }
        if (credentials != null) {
            for (Map.Entry<String, Map<String, Map<String, String>>> realmData: credentials.entrySet()) {
                String realmName = realmData.getKey();
                Map<String, Map<String, SingleCallbackHandler>> realm = getRealm(realmName);
                for  (Map.Entry<String, Map<String, String>> subjectData: realmData.getValue().entrySet()) {
                    String subjectId = subjectData.getKey();
                    Map<String, SingleCallbackHandler> subject = getSubject(realm, subjectId);
                    for (Map.Entry<String, String> credentialData: subjectData.getValue().entrySet()) {
                        String handlerType = credentialData.getKey();
                        String value = credentialData.getValue();
                        try {
                            Class<? extends SingleCallbackHandler> clazz = (Class<? extends SingleCallbackHandler>) cl.loadClass(handlerType);
                            Constructor<? extends SingleCallbackHandler> c = clazz.getConstructor(String.class);
                            SingleCallbackHandler handler = c.newInstance(value);
                            String callbackType = handler.getCallbackType();
                            subject.put(callbackType, handler);
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Could not construct SingleCallbackHandler of type: " + handlerType + " and value: " + value + " for subjectId: " + subjectId + " and realm: " + realmName, e);
                        }
                    }
                }

            }
        }
    }

    public Subject getSubject(String realm, String id) throws LoginException {
        Map<String, Map<String, SingleCallbackHandler>> idMap = credentialStore.get(realm);
        if (idMap == null) {
            throw new LoginException("Unknown realm: " + realm);
        }
        final Map<String, SingleCallbackHandler> callbackInfos = idMap.get(id);
        if (callbackInfos == null) {
            throw new LoginException("Unknown id: " + id + " in realm: " + realm);
        }
        LoginContext loginContext = ContextManager.login(realm, new CallbackHandler() {

            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (Callback callback: callbacks) {
                    if (!callbackInfos.containsKey(callback.getClass().getName())) {
                        throw new UnsupportedCallbackException(callback);
                    }
                    SingleCallbackHandler singleCallbackHandler = callbackInfos.get(callback.getClass().getName());
                    singleCallbackHandler.handle(callback);
                }
            }
        },
                configuration);
        return loginContext.getSubject();
    }

    public void addEntry(String realm, String id, Map<String, SingleCallbackHandler> callbackInfos) {
        Map<String, Map<String, SingleCallbackHandler>> idMap = getRealm(realm);
        idMap.put(id, callbackInfos);
    }

    private Map<String, Map<String, SingleCallbackHandler>> getRealm(String realm) {
        Map<String, Map<String, SingleCallbackHandler>> idMap = credentialStore.get(realm);
        if (idMap == null) {
            idMap = new HashMap<String, Map<String, SingleCallbackHandler>>();
            credentialStore.put(realm, idMap);
        }
        return idMap;
    }

    private Map<String, SingleCallbackHandler> getSubject(Map<String, Map<String, SingleCallbackHandler>> realm, String subjectId) {
        Map<String, SingleCallbackHandler> subject = realm.get(subjectId);
        if (subject == null) {
            subject = new HashMap<String, SingleCallbackHandler>();
            realm.put(subjectId, subject);
        }
        return subject;
    }

}
