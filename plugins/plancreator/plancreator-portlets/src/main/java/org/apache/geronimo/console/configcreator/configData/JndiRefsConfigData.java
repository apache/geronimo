/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.configcreator.configData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Collection;

import javax.portlet.PortletRequest;

import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.ServiceRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.ResourceEnvRef;

import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPortType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;

/**
 * 
 * @version $Rev$ $Date$
 */
public class JndiRefsConfigData {
    private List<ReferenceData> jdbcPoolRefs = new ArrayList<ReferenceData>();

    private List<ReferenceData> jmsConnectionFactoryRefs = new ArrayList<ReferenceData>();

    private List<ReferenceData> javaMailSessionRefs = new ArrayList<ReferenceData>();

    private HashSet<String> dependenciesSet = new HashSet<String>();

    private boolean referenceNotResolved = false;

    public final static String REF_NAME = "refName";

    public final static String REF_LINK = "refLink";

    public void parseWebDD(JndiConsumer annotatedWebAppDD, GerWebAppType webApp) {
        Collection<EjbRef> ejbRefs = annotatedWebAppDD.getEjbRef();
        for (EjbRef ejbRef: ejbRefs) {
            String refName = ejbRef.getEjbRefName();
            webApp.addNewEjbRef().setRefName(refName);
        }

        Collection<EjbLocalRef> ejbLocalRefs = annotatedWebAppDD.getEjbLocalRef();
        for (EjbLocalRef ejbLocalRef: ejbLocalRefs) {
            String refName = ejbLocalRef.getEjbRefName();
            webApp.addNewEjbLocalRef().setRefName(refName);
        }

        Collection<ServiceRef> serviceRefs = annotatedWebAppDD.getServiceRef();
        for (ServiceRef serviceRef: serviceRefs) {
            String refName = serviceRef.getServiceRefName();
            webApp.addNewServiceRef().setServiceRefName(refName);
        }

        Collection<ResourceRef> resourceRefs = annotatedWebAppDD.getResourceRef();
        for (ResourceRef resourceRef: resourceRefs) {
            String refName = resourceRef.getResRefName();
            String refType = resourceRef.getResType();
            if ("javax.sql.DataSource".equalsIgnoreCase(refType)) {
                jdbcPoolRefs.add(new ReferenceData(refName));
            } else if ("javax.jms.ConnectionFactory".equalsIgnoreCase(refType)
                    || "javax.jms.QueueConnectionFactory".equalsIgnoreCase(refType)
                    || "javax.jms.TopicConnectionFactory".equalsIgnoreCase(refType)) {
                jmsConnectionFactoryRefs.add(new ReferenceData(refName));
            } else if ("javax.mail.Session".equalsIgnoreCase(refType)) {
                javaMailSessionRefs.add(new ReferenceData(refName));
            }
        }

        Collection<ResourceEnvRef> resourceEnvRefs = annotatedWebAppDD.getResourceEnvRef();
        for (ResourceEnvRef resourceEnvRef: resourceEnvRefs) {
            String refName = resourceEnvRef.getResourceEnvRefName();
            GerResourceEnvRefType gerResourceEnvRef = webApp.addNewResourceEnvRef();
            gerResourceEnvRef.setRefName(refName);
            // resourceEnvRef.setMessageDestinationLink(refName);
        }
    }

    public void readReferencesData(PortletRequest request, GerWebAppType webApp) {
        dependenciesSet.clear();
        Map map = request.getParameterMap();
        int index = 0;
        while (true) {
            String prefix = "ejbRef" + "." + (index) + ".";
            if (!map.containsKey(prefix + REF_NAME)) {
                break;
            }
            String referenceLink = request.getParameter(prefix + REF_LINK);
            if (isEmpty(referenceLink)) {
                referenceNotResolved = true;
            }
            dependenciesSet.add(getDependencyString(referenceLink));
            GerPatternType pattern = createPattern(referenceLink);
            // GERONIMO-5981
            // OpenEJB requires the type of the ejb for jndi binding
            pattern.setArtifactId(pattern.getArtifactId() + ".jar");
            webApp.getEjbRefArray(index).setPattern(pattern);
            index++;
        }
        index = 0;
        while (true) {
            String prefix = "ejbLocalRef" + "." + (index) + ".";
            if (!map.containsKey(prefix + REF_NAME)) {
                break;
            }
            String referenceLink = request.getParameter(prefix + REF_LINK);
            if (isEmpty(referenceLink)) {
                referenceNotResolved = true;
            }
            dependenciesSet.add(getDependencyString(referenceLink));
            webApp.getEjbLocalRefArray(index).setPattern(createPattern(referenceLink));
            index++;
        }
        index = 0;
        while (true) {
            String prefix = "jmsDestinationRef" + "." + (index) + ".";
            if (!map.containsKey(prefix + REF_NAME)) {
                break;
            }
            String referenceLink = request.getParameter(prefix + REF_LINK);
            if (isEmpty(referenceLink)) {
                referenceNotResolved = true;
            }
            dependenciesSet.add(getDependencyString(referenceLink));
            webApp.getResourceEnvRefArray(index).setPattern(createPattern(referenceLink));
            index++;
        }
        readWebServiceRefsData(request, webApp);
        readParameters("jdbcPoolRef", jdbcPoolRefs, request);
        readParameters("jmsConnectionFactoryRef", jmsConnectionFactoryRefs, request);
        readParameters("javaMailSessionRef", javaMailSessionRefs, request);
    }

    public static String getDependencyString(String patternString) {
        String[] elements = patternString.split("/", 6);
        return elements[0] + "/" + elements[1] + "/" + elements[2] + "/" + elements[3];
    }

    public static GerPatternType createPattern(String patternString) {
        GerPatternType pattern = GerPatternType.Factory.newInstance();
        String[] elements = patternString.split("/", 6);
        if (!isEmpty(elements[0])) {
            pattern.setGroupId(elements[0]);
        }
        if (!isEmpty(elements[1])) {
            pattern.setArtifactId(elements[1]);
        }
        if (!isEmpty(elements[2])) {
            pattern.setVersion(elements[2]);
        }
        if (!isEmpty(elements[3])) {
            // pattern.setType(elements[3]);
        }
        if (!isEmpty(elements[4])) {
            pattern.setModule(elements[4]);
        }
        if (!isEmpty(elements[5])) {
            pattern.setName(elements[5]);
        }
        return pattern;
    }

    private void readParameters(String prefix1, List<ReferenceData> list, PortletRequest request) {
        Map map = request.getParameterMap();
        list.clear();
        int index = 0;
        while (true) {
            String prefix2 = prefix1 + "." + (index++) + ".";
            if (!map.containsKey(prefix2 + REF_NAME)) {
                break;
            }
            ReferenceData referenceData = new ReferenceData();
            referenceData.load(request, prefix2);
            String referenceLink = referenceData.getRefLink();
            if (isEmpty(referenceLink)) {
                referenceNotResolved = true;
            }
            dependenciesSet.add(getDependencyString(referenceLink));
            list.add(referenceData);
        }
    }

    public void readWebServiceRefsData(PortletRequest request, GerWebAppType webApp) {
        Map map = request.getParameterMap();
        for (int i = 0; i < webApp.getServiceRefArray().length; i++) {
            GerServiceRefType serviceRef = webApp.getServiceRefArray(i);
            for (int j = serviceRef.getPortArray().length - 1; j >= 0; j--) {
                serviceRef.removePort(j);
            }
            String prefix1 = "serviceRef" + "." + i + "." + "port" + ".";
            int lastIndex = Integer.parseInt(request.getParameter(prefix1 + "lastIndex"));
            for (int j = 0; j < lastIndex; j++) {
                String prefix2 = prefix1 + j + ".";
                if (!map.containsKey(prefix2 + "portName")) {
                    continue;
                }
                GerPortType port = serviceRef.addNewPort();
                String value = request.getParameter(prefix2 + "portName");
                if (!isEmpty(value)) {
                    port.setPortName(value);
                }
                value = request.getParameter(prefix2 + "protocol");
                if (!isEmpty(value)) {
                    port.setProtocol(value);
                }
                value = request.getParameter(prefix2 + "host");
                if (!isEmpty(value)) {
                    port.setHost(value);
                }
                value = request.getParameter(prefix2 + "port");
                if (!isEmpty(value)) {
                    int portValue = Integer.parseInt(value);
                    port.setPort(portValue);
                }
                value = request.getParameter(prefix2 + "uri");
                if (!isEmpty(value)) {
                    port.setUri(value);
                }
                value = request.getParameter(prefix2 + "credentialsName");
                if (!isEmpty(value)) {
                    port.setCredentialsName(value);
                }
            }
        }
    }

    public void storeResourceRefs(GerWebAppType webApp) {
        for (int i = webApp.getResourceRefArray().length - 1; i >= 0; i--) {
            webApp.removeResourceRef(i);
        }
        for (int i = 0; i < jdbcPoolRefs.size(); i++) {
            ReferenceData referenceData = (ReferenceData) jdbcPoolRefs.get(i);
            GerResourceRefType resourceRef = webApp.addNewResourceRef();
            resourceRef.setRefName(referenceData.getRefName());
            resourceRef.setPattern(createPattern(referenceData.getRefLink()));
        }
        for (int i = 0; i < jmsConnectionFactoryRefs.size(); i++) {
            ReferenceData referenceData = (ReferenceData) jmsConnectionFactoryRefs.get(i);
            GerResourceRefType resourceRef = webApp.addNewResourceRef();
            resourceRef.setRefName(referenceData.getRefName());
            resourceRef.setPattern(createPattern(referenceData.getRefLink()));
        }
        for (int i = 0; i < javaMailSessionRefs.size(); i++) {
            ReferenceData referenceData = (ReferenceData) javaMailSessionRefs.get(i);
            GerResourceRefType resourceRef = webApp.addNewResourceRef();
            resourceRef.setRefName(referenceData.getRefName());
            resourceRef.setPattern(createPattern(referenceData.getRefLink()));
        }
    }

    public List<ReferenceData> getJdbcPoolRefs() {
        return jdbcPoolRefs;
    }

    public List<ReferenceData> getJmsConnectionFactoryRefs() {
        return jmsConnectionFactoryRefs;
    }

    public List<ReferenceData> getJavaMailSessionRefs() {
        return javaMailSessionRefs;
    }

    public HashSet<String> getDependenciesSet() {
        return dependenciesSet;
    }

    public boolean isReferenceNotResolved() {
        return referenceNotResolved;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().equals("");
    }

    public static class ReferenceData {
        private String refName;

        private String refLink;

        public ReferenceData() {
        }

        public ReferenceData(String refName) {
            this.refName = refName;
        }

        public void load(PortletRequest request, String prefix) {
            refName = request.getParameter(prefix + REF_NAME);
            refLink = request.getParameter(prefix + REF_LINK);
        }

        public String getRefName() {
            return refName;
        }

        public void setRefName(String refName) {
            this.refName = refName;
        }

        public String getRefLink() {
            return refLink;
        }

        public void setRefLink(String refLink) {
            this.refLink = refLink;
        }
    }
}
