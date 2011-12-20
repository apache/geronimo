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


package org.apache.geronimo.security.deployment;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.XmlAttributeBuilder;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.xbeans.geronimo.credentialstore.CredentialStoreDocument;
import org.apache.geronimo.xbeans.geronimo.credentialstore.CredentialStoreType;
import org.apache.geronimo.xbeans.geronimo.credentialstore.CredentialType;
import org.apache.geronimo.xbeans.geronimo.credentialstore.RealmType;
import org.apache.geronimo.xbeans.geronimo.credentialstore.SubjectType;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
@Component(immediate = true)
@Service
public class CredentialStoreBuilder implements XmlAttributeBuilder {

    private static final String NAMESPACE = CredentialStoreDocument.type.getDocumentElementName().getNamespaceURI();

    public String getNamespace() {
        return NAMESPACE;
    }

    public Object getValue(XmlObject xmlObject, XmlObject enclosing, String type, Bundle bundle) throws DeploymentException {
        Map<String, Map<String, Map<String, String>>> credentialStore = new HashMap<String, Map<String, Map<String, String>>>();
        CredentialStoreType cst = (CredentialStoreType) xmlObject.copy().changeType(CredentialStoreType.type);
        for (RealmType realmType: cst.getRealmArray()) {
            String realmName = realmType.getName().trim();
            Map<String, Map<String, String>> realm = new HashMap<String, Map<String, String>>();
            credentialStore.put(realmName, realm);
            for (SubjectType subjectType: realmType.getSubjectArray()) {
                String id = subjectType.getId().trim();
                Map<String, String> subject = new HashMap<String, String>();
                realm.put(id, subject);
                for (CredentialType credentialType: subjectType.getCredentialArray()) {
                    String handlerType = credentialType.getType().trim();
                    String value = credentialType.getValue().trim();
                    subject.put(handlerType, value);
                }

            }
        }
        return credentialStore;
    }


//    public static final GBeanInfo GBEAN_INFO;
//
//    static {
//        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(CredentialStoreBuilder.class, "XmlAttributeBuilder");
//        GBEAN_INFO = infoBuilder.getBeanInfo();
//    }
//
//    public static GBeanInfo getGBeanInfo() {
//        return GBEAN_INFO;
//    }
}
