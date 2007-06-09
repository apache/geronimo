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
import java.lang.reflect.Constructor;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.security.ContextManager;

/**
 * @version $Rev:$ $Date:$
 */
public class SimpleCredentialStoreImpl implements CredentialStore {

    private final Map<String, Map<String, Map<String, SingleCallbackHandler>>> credentialStore = new HashMap<String, Map<String, Map<String, SingleCallbackHandler>>>();

    public SimpleCredentialStoreImpl(Map<String, Map<String, Map<String, String>>> credentials, ClassLoader cl) {
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
        Subject subject = new Subject();
        LoginContext loginContext = new LoginContext(realm, subject, new CallbackHandler() {

            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (Callback callback: callbacks) {
                    if (!callbackInfos.containsKey(callback.getClass().getName())) {
                        throw new UnsupportedCallbackException(callback);
                    }
                    SingleCallbackHandler singleCallbackHandler = callbackInfos.get(callback.getClass().getName());
                    singleCallbackHandler.handle(callback);
                }
            }
        });
        loginContext.login();
        return ContextManager.getServerSideSubject(subject);
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(SimpleCredentialStoreImpl.class);

        infoBuilder.addAttribute("credentialStore", Map.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.setConstructor(new String[]{"credentialStore", "classLoader"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
