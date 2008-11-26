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

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * Hopefully this will only be used for tests where you need to set up a simple credential store
 * but don't want to set up a login configuration
 *
 * @version $Rev$ $Date$
 */
public class DirectConfigurationCredentialStoreImpl implements CredentialStore {

    private final Map<String, Map<String, Subject>> subjectStore = new HashMap<String, Map<String, Subject>>();

    public DirectConfigurationCredentialStoreImpl(Map<String, Map<String, Map<String, String>>> subjectInfo, ClassLoader cl) throws DeploymentException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (cl == null) {
            cl = getClass().getClassLoader();
        }
        for (Map.Entry<String, Map<String, Map<String, String>>> realmEntry: subjectInfo.entrySet()) {
            Map<String, Subject> realm = new HashMap<String, Subject>();
            for (Map.Entry<String, Map<String, String>> subjectEntry: realmEntry.getValue().entrySet()) {
                String id = subjectEntry.getKey();
                Map<String, String> principals = subjectEntry.getValue();
                Subject subject = new Subject();
                for (Map.Entry<String, String> principalInfo: principals.entrySet()) {
                    String className = principalInfo.getKey();
                    String principalName = principalInfo.getValue();
                    Class<? extends Principal> clazz = (Class<? extends Principal>) cl.loadClass(className);
                    Constructor<? extends Principal> c = clazz.getConstructor(new Class[] {String.class});
                    Principal p = c.newInstance(new Object[] {principalName});
                    subject.getPrincipals().add(p);
                }
                realm.put(id, subject);
            }
            subjectStore.put(realmEntry.getKey(), realm);
        }
    }

    public Subject getSubject(String realm, String id) throws LoginException {
        Map<String, Subject> realmMap = subjectStore.get(realm);
        if (realmMap == null) {
            throw new LoginException("Unknown realm : " + realm);
        }
        Subject subject = realmMap.get(id);
        if (subject == null) {
            throw new LoginException("Unknown id: " + id + " in realm: " + realm);
        }
        return subject;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(DirectConfigurationCredentialStoreImpl.class);

        infoBuilder.addAttribute("credentialStore", Map.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.setConstructor(new String[]{"credentialStore", "classLoader"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
