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

package org.apache.geronimo.naming.deployment;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;

/**
 * @version $Rev$ $Date$
 */
public class ENCHelper {


    public static final String[][] ENC_XPATHS = {
        {"ejb-ref"},
        {"ejb-local-ref"},
        {"resource-env-ref"},
        {"resource-ref"},
    };

    public static final String[][] NAME_XPATHS = {
        {"ejb-ref-name"},
        {"ejb-ref-name"},
        {"resource-env-ref-name"},
        {"res-ref-name"}
    };


    private final String[] xpaths;
    private final String[] namePaths;

    private RemoteRefDConfigBean[] ejbRefs;

    private LocalRefDConfigBean[] ejbLocalRefs;
    private LocalRefDConfigBean[] resourceEnvRefs;
    private LocalRefDConfigBean[] resourceRefs;

    public ENCHelper(DDBean ddBean, XmlEnvRefs envRefs, String[] xpaths, String[] namePaths) {
        this.xpaths = xpaths;
        this.namePaths = namePaths;
        DDBean[] ddEjbRefs = ddBean.getChildBean(xpaths[0]);
        if (ddEjbRefs == null) {
            ddEjbRefs = new DDBean[0];
        }
        ejbRefs = new RemoteRefDConfigBean[ddEjbRefs.length];
        GerEjbRefType[] xmlEjbRefs = envRefs.getEjbRefs();
        Map ejbRefMap = new HashMap();
        for (int i = 0; i < xmlEjbRefs.length; i++) {
            GerEjbRefType refAdapter = xmlEjbRefs[i];
            ejbRefMap.put(refAdapter.getRefName(), refAdapter);
//            envRefs.removeEjbRef(0);
        }
//        for (int i = 0; i < ddEjbRefs.length; i++) {
//            DDBean ddRef = ddEjbRefs[i];
//            String name = ddRef.getText(namePaths[0])[0];
//            GerRemoteRefType refAdapter;
//            if (ejbRefMap.get(name) == null) {
//                refAdapter = envRefs.addNewEjbRef();
//                refAdapter.setRefName(name);
//            } else {
//                refAdapter = (GerRemoteRefType) ejbRefMap.get(name);
//                envRefs.setEjbRef(i, refAdapter);
//            }
//            ejbRefs[i] = new RemoteRefDConfigBean(ddRef, refAdapter, namePaths[0]);
//        }
//
//        DDBean[] ddEjbLocalRefs = ddBean.getChildBean(xpaths[1]);
//        if (ddEjbLocalRefs == null) {
//            ddEjbLocalRefs = new DDBean[0];
//        }
//        ejbLocalRefs = new LocalRefDConfigBean[ddEjbLocalRefs.length];
//        GerLocalRefType[] xmlEjbLocalRefs = envRefs.getEjbLocalRefs();
//        Map ejbLocalRefMap = new HashMap();
//        for (int i = 0; i < xmlEjbLocalRefs.length; i++) {
//            GerLocalRefType refAdapter = xmlEjbLocalRefs[i];
//            ejbLocalRefMap.put(refAdapter.getRefName(), refAdapter);
//            envRefs.removeEjbLocalRef(0);
//        }
//        for (int i = 0; i < ddEjbLocalRefs.length; i++) {
//            DDBean ddRef = ddEjbLocalRefs[i];
//            String name = ddRef.getText(namePaths[1])[0];
//            GerLocalRefType refAdapter;
//            if (ejbLocalRefMap.get(name) == null) {
//                refAdapter = envRefs.addNewEjbLocalRef();
//                refAdapter.setRefName(name);
//            } else {
//                refAdapter = (GerLocalRefType) ejbLocalRefMap.get(name);
//                envRefs.setEjbLocalRef(i, refAdapter);
//            }
//            ejbLocalRefs[i] = new LocalRefDConfigBean(ddRef, refAdapter, namePaths[1]);
//        }
//
//
//        DDBean[] ddResourceEnvRefs = ddBean.getChildBean(xpaths[2]);
//        if (ddResourceEnvRefs == null) {
//            ddResourceEnvRefs = new DDBean[0];
//        }
//        resourceEnvRefs = new LocalRefDConfigBean[ddResourceEnvRefs.length];
//        GerLocalRefType[] xmlResourceEnvRefs = envRefs.getResourceEnvRefs();
//        Map resourceEnvRefMap = new HashMap();
//        for (int i = 0; i < xmlResourceEnvRefs.length; i++) {
//            GerLocalRefType refAdapter = xmlResourceEnvRefs[i];
//            resourceEnvRefMap.put(refAdapter.getRefName(), refAdapter);
//            envRefs.removeResourceEnvRef(0);
//        }
//        for (int i = 0; i < ddResourceEnvRefs.length; i++) {
//            DDBean ddRef = ddResourceEnvRefs[i];
//            String name = ddRef.getText(namePaths[2])[0];
//            GerLocalRefType refAdapter;
//            if (resourceEnvRefMap.get(name) == null) {
//                refAdapter = envRefs.addNewResourceEnvRef();
//                refAdapter.setRefName(name);
//            } else {
//                refAdapter = (GerLocalRefType) resourceEnvRefMap.get(name);
//                envRefs.setResourceEnvRef(i, refAdapter);
//            }
//            resourceEnvRefs[i] = new LocalRefDConfigBean(ddRef, refAdapter, namePaths[2]);
//        }
//
//        DDBean[] ddResourceRefs = ddBean.getChildBean(xpaths[3]);
//        if (ddResourceRefs == null) {
//            ddResourceRefs = new DDBean[0];
//        }
//        resourceRefs = new LocalRefDConfigBean[ddResourceRefs.length];
//        GerLocalRefType[] xmlResourceRefs = envRefs.getResourceRefs();
//        Map resourceRefMap = new HashMap();
//        for (int i = 0; i < xmlResourceRefs.length; i++) {
//            GerLocalRefType refAdapter = xmlResourceRefs[i];
//            resourceRefMap.put(refAdapter.getRefName(), refAdapter);
//            envRefs.removeResourceRef(0);
//        }
//        for (int i = 0; i < ddResourceRefs.length; i++) {
//            DDBean ddRef = ddResourceRefs[i];
//            String name = ddRef.getText(namePaths[3])[0];
//            GerLocalRefType refAdapter;
//            if (resourceRefMap.get(name) == null) {
//                refAdapter = envRefs.addNewResourceRef();
//                refAdapter.setRefName(name);
//            } else {
//                refAdapter = (GerLocalRefType) resourceRefMap.get(name);
//                envRefs.setResourceRef(i, refAdapter);
//            }
//            resourceRefs[i] = new LocalRefDConfigBean(ddRef, refAdapter, namePaths[3]);
//        }

    }

    public DConfigBean getDConfigBean(DDBean ddBean) throws ConfigurationException {
        String xpath = ddBean.getXpath();
        if (xpath.equals(xpaths[0])) {
            String name = ddBean.getText(namePaths[0])[0];
            for (int i = 0; i < ejbRefs.length; i++) {
                RemoteRefDConfigBean ejbRef = ejbRefs[i];
                if (ejbRef.getRefName().equals(name)) {
                    return ejbRef;
                }
            }
            throw new ConfigurationException("no such ejb-ref-name" + name);
        } else if (xpath.equals(xpaths[1])) {
            String name = ddBean.getText(namePaths[1])[0];
            for (int i = 0; i < ejbLocalRefs.length; i++) {
                LocalRefDConfigBean ejbLocalRef = ejbLocalRefs[i];
                if (ejbLocalRef.getRefName().equals(name)) {
                    return ejbLocalRef;
                }
            }
            throw new ConfigurationException("no such ejb-ref-name" + name);
        } else if (xpath.equals(xpaths[2])) {
            String name = ddBean.getText(namePaths[2])[0];
            for (int i = 0; i < resourceEnvRefs.length; i++) {
                LocalRefDConfigBean resourceEnvRef = resourceEnvRefs[i];
                if (resourceEnvRef.getRefName().equals(name)) {
                    return resourceEnvRef;
                }
            }
            throw new ConfigurationException("no such ejb-ref-name" + name);
        } else if (xpath.equals(xpaths[3])) {
            String name = ddBean.getText(namePaths[3])[0];
            for (int i = 0; i < resourceRefs.length; i++) {
                LocalRefDConfigBean resourceRef = resourceRefs[i];
                if (resourceRef.getRefName().equals(name)) {
                    return resourceRef;
                }
            }
            throw new ConfigurationException("no such res-ref-name" + name);
        } else {
            throw new ConfigurationException("Unrecognized XPath: " + ddBean.getXpath());
        }
    }

    public void removeDConfigBean(DConfigBean dcBean) throws BeanNotFoundException {
//          DDBean ddBean = dcBean.getDDBean();
//          String xpath = ddBean.getXpath();
//          String name = ddBean.getText();
        /*
              if (xpath.endsWith("ejb-ref/ejb-ref-name")) {
                  if (ejbRefs.remove(name) == null) {
                      throw new BeanNotFoundException("No DConfigBean found with name: " + name);
                  }
              } else if (xpath.endsWith("ejb-local-ref/ejb-ref-name")) {
                  if (ejbLocalRefs.remove(name) == null) {
                      throw new BeanNotFoundException("No DConfigBean found with name: " + name);
                  }
              } else if (xpath.endsWith("service-ref/service-ref-name")) {
                  if (serviceRefs.remove(name) == null) {
                      throw new BeanNotFoundException("No DConfigBean found with name: " + name);
                  }
              } else if (xpath.endsWith("resource-ref/res-ref-name")) {
                  if (resourceRefs.remove(name) == null) {
                      throw new BeanNotFoundException("No DConfigBean found with name: " + name);
                  }
              } else {
                  throw new BeanNotFoundException("Unrecognized XPath: " + xpath);
              }
              */
    }


    public static class XmlEnvRefs {
        private final GerEjbRefType[] ejbRefs;
        private final GerEjbLocalRefType[] ejbLocalRefs;
        private final GerResourceRefType[] resourceRefs;
        private final GerResourceEnvRefType[] resourceEnvRefs;

        public XmlEnvRefs(GerEjbRefType[] ejbRefs, GerEjbLocalRefType[] ejbLocalRefs, GerResourceRefType[] resourceRefs, GerResourceEnvRefType[] resourceEnvRefs) {
            this.ejbRefs = ejbRefs;
            this.ejbLocalRefs = ejbLocalRefs;
            this.resourceRefs = resourceRefs;
            this.resourceEnvRefs = resourceEnvRefs;
        }

        public GerEjbRefType[] getEjbRefs() {
            return ejbRefs;
        }

        public GerEjbLocalRefType[] getEjbLocalRefs() {
            return ejbLocalRefs;
        }

        public GerResourceRefType[] getResourceRefs() {
            return resourceRefs;
        }

        public GerResourceEnvRefType[] getResourceEnvRefs() {
            return resourceEnvRefs;
        }

    }
}
