/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.j2ee.deployment.annotation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.xbeans.javaee6.EjbLocalRefType;
import org.apache.geronimo.xbeans.javaee6.EjbRefType;
import org.apache.geronimo.xbeans.javaee6.EnvEntryType;
import org.apache.geronimo.xbeans.javaee6.LifecycleCallbackType;
import org.apache.geronimo.xbeans.javaee6.MessageDestinationRefType;
import org.apache.geronimo.xbeans.javaee6.PersistenceContextRefType;
import org.apache.geronimo.xbeans.javaee6.PersistenceUnitRefType;
import org.apache.geronimo.xbeans.javaee6.ResourceEnvRefType;
import org.apache.geronimo.xbeans.javaee6.ResourceRefType;
import org.apache.geronimo.xbeans.javaee6.ServiceRefType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;

/**
 * Wrapper class to encapsulate the WebAppType class with an interface that the various
 * AnnotationHelpers can use
 * <p/>
 * <p><strong>Remaining ToDo(s):</strong>
 * <ul>
 * <li>None
 * </ul>
 *
 * @version $Rev $Date
 * @since Geronimo 2.0
 */
public class AnnotatedWebApp implements AnnotatedApp {
    private static final Logger log = LoggerFactory.getLogger(AnnotatedWebApp.class);
    private WebAppType webApp;

    // Protected instance variables
    protected List<EjbRefType> ambiguousEjbRefs;

    /**
     * WebAppType-qualified constructor
     *
     * @param webApp WebAppType
     */
    public AnnotatedWebApp(WebAppType webApp) {
        this.webApp = webApp;
    }


    /**
     * WebAppType methods used for the @EJB, @EJBs annotations
     */
    public EjbLocalRefType[] getEjbLocalRefArray() {
        return webApp.getEjbLocalRefArray();
    }

    public EjbLocalRefType addNewEjbLocalRef() {
        return webApp.addNewEjbLocalRef();
    }

    public EjbRefType[] getEjbRefArray() {
        return webApp.getEjbRefArray();
    }

    public EjbRefType addNewEjbRef() {
        return webApp.addNewEjbRef();
    }


    /**
     * WebAppType methods used for the @Resource, @Resources annotations
     */
    public EnvEntryType[] getEnvEntryArray() {
        return webApp.getEnvEntryArray();
    }

    public EnvEntryType addNewEnvEntry() {
        return webApp.addNewEnvEntry();
    }

    public ServiceRefType[] getServiceRefArray() {
        return webApp.getServiceRefArray();
    }

    public ServiceRefType addNewServiceRef() {
        return webApp.addNewServiceRef();
    }

    public ResourceRefType[] getResourceRefArray() {
        return webApp.getResourceRefArray();
    }

    public ResourceRefType addNewResourceRef() {
        return webApp.addNewResourceRef();
    }

    public MessageDestinationRefType[] getMessageDestinationRefArray() {
        return webApp.getMessageDestinationRefArray();
    }

    public MessageDestinationRefType addNewMessageDestinationRef() {
        return webApp.addNewMessageDestinationRef();
    }

    public ResourceEnvRefType[] getResourceEnvRefArray() {
        return webApp.getResourceEnvRefArray();
    }

    public ResourceEnvRefType addNewResourceEnvRef() {
        return webApp.addNewResourceEnvRef();
    }


    /**
     * webApp getter
     *
     * @return String representation of webApp
     */
    public String toString() {
        return webApp.toString();
    }


    /**
     * webApp getter
     *
     * @return webApp WebAppType
     */
    public WebAppType getWebApp() {
        return webApp;
    }


    /**
     * ambiguousRefs getter
     * <p/>
     * <p>There is no corresponding setter method. To add a new item to the list do:
     * <pre>
     *    getAmbiguousEjbRefs().add(ejbRef);
     * </pre>
     *
     * @return ambiguousRefs list
     */
    public List<EjbRefType> getAmbiguousEjbRefs() {
        if (ambiguousEjbRefs == null) {
            ambiguousEjbRefs = new ArrayList<EjbRefType>();
        }
        return this.ambiguousEjbRefs;
    }

    public LifecycleCallbackType[] getPostConstructArray() {
        return webApp.getPostConstructArray();
    }

    public LifecycleCallbackType addPostConstruct() {
        return webApp.addNewPostConstruct();
    }

    public LifecycleCallbackType[] getPreDestroyArray() {
        return webApp.getPreDestroyArray();
    }

    public LifecycleCallbackType addPreDestroy() {
        return webApp.addNewPreDestroy();
    }

    public PersistenceContextRefType[] getPersistenceContextRefArray() {
        return webApp.getPersistenceContextRefArray();
    }

    public PersistenceContextRefType addNewPersistenceContextRef() {
        return webApp.addNewPersistenceContextRef();
    }

    public PersistenceUnitRefType[] getPersistenceUnitRefArray() {
        return webApp.getPersistenceUnitRefArray();
    }

    public PersistenceUnitRefType addNewPersistenceUnitRef() {
        return webApp.addNewPersistenceUnitRef();
    }

    public String getComponentType() {
        return null;
    }
}
