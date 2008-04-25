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
import org.apache.geronimo.xbeans.javaee.ApplicationClientType;
import org.apache.geronimo.xbeans.javaee.EjbLocalRefType;
import org.apache.geronimo.xbeans.javaee.EjbRefType;
import org.apache.geronimo.xbeans.javaee.EnvEntryType;
import org.apache.geronimo.xbeans.javaee.LifecycleCallbackType;
import org.apache.geronimo.xbeans.javaee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.javaee.PersistenceContextRefType;
import org.apache.geronimo.xbeans.javaee.PersistenceUnitRefType;
import org.apache.geronimo.xbeans.javaee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.javaee.ResourceRefType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;

/**
 * Wrapper class to encapsulate the ApplicationClientType class with an interface that the various
 * AnnotationHelpers can use
 * <p/>
 * <p/>
 * <p><strong>Remaining ToDo(s):</strong>
 * <ul>
 * <li>Can AppClients have unresolved EJB references ??
 * </ul>
 *
 * @version $Rev $Date
 * @since Geronimo 2.0
 */
public class AnnotatedApplicationClient implements AnnotatedApp {

    // Private instance variables
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ApplicationClientType applicationClient;
    private List<EjbRefType> ambiguousEjbRefs;
    private final String componentType;

    /**
     * ApplicationClientType-qualified constructor
     *
     * @param applicationClient ApplicationClientType
     * @param applicationClientClassName
     */
    public AnnotatedApplicationClient(ApplicationClientType applicationClient, String applicationClientClassName) {
        this.applicationClient = applicationClient;
        this.componentType = applicationClientClassName;
    }


    /**
     * ApplicationClientType methods used for the @EJB, @EJBs annotations
     */
    public EjbLocalRefType[] getEjbLocalRefArray() {
        return null;                                            // Not supported by app clients
    }

    public EjbLocalRefType addNewEjbLocalRef() {
        return null;                                            // Not supported by app clients
    }

    public EjbRefType[] getEjbRefArray() {
        return applicationClient.getEjbRefArray();
    }

    public EjbRefType addNewEjbRef() {
        return applicationClient.addNewEjbRef();
    }


    /**
     * ApplicationClientType methods used for the @Resource, @Resources annotations
     */
    public EnvEntryType[] getEnvEntryArray() {
        return applicationClient.getEnvEntryArray();
    }

    public EnvEntryType addNewEnvEntry() {
        return applicationClient.addNewEnvEntry();
    }

    public ServiceRefType[] getServiceRefArray() {
        return applicationClient.getServiceRefArray();
    }

    public ServiceRefType addNewServiceRef() {
        return applicationClient.addNewServiceRef();
    }

    public ResourceRefType[] getResourceRefArray() {
        return applicationClient.getResourceRefArray();
    }

    public ResourceRefType addNewResourceRef() {
        return applicationClient.addNewResourceRef();
    }

    public MessageDestinationRefType[] getMessageDestinationRefArray() {
        return applicationClient.getMessageDestinationRefArray();
    }

    public MessageDestinationRefType addNewMessageDestinationRef() {
        return applicationClient.addNewMessageDestinationRef();
    }

    public ResourceEnvRefType[] getResourceEnvRefArray() {
        return applicationClient.getResourceEnvRefArray();
    }

    public ResourceEnvRefType addNewResourceEnvRef() {
        return applicationClient.addNewResourceEnvRef();
    }


    /**
     * applicationClient getter
     *
     * @return String representation of applicationClient
     */
    public String toString() {
        return applicationClient.toString();
    }


    /**
     * applicationClient getter
     *
     * @return applicationClient ApplicationClientType
     */
    public ApplicationClientType getApplicationClient() {
        return applicationClient;
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
        return applicationClient.getPostConstructArray();
    }

    public LifecycleCallbackType addPostConstruct() {
        return applicationClient.addNewPostConstruct();
    }

    public LifecycleCallbackType[] getPreDestroyArray() {
        return applicationClient.getPreDestroyArray();
    }

    public LifecycleCallbackType addPreDestroy() {
        return applicationClient.addNewPreDestroy();
    }

    public PersistenceContextRefType[] getPersistenceContextRefArray() {
        return null;                                            // Not supported by app clients
    }

    public PersistenceContextRefType addNewPersistenceContextRef() {
        return null;                                            // Not supported by app clients
    }

    public PersistenceUnitRefType[] getPersistenceUnitRefArray() {
        return applicationClient.getPersistenceUnitRefArray();
    }

    public PersistenceUnitRefType addNewPersistenceUnitRef() {
        return applicationClient.addNewPersistenceUnitRef();
    }

    public String getComponentType() {
        return componentType;
    }

}
