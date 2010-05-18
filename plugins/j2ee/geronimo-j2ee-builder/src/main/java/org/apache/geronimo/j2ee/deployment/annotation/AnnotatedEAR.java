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

import org.apache.geronimo.xbeans.javaee6.ApplicationType;
import org.apache.geronimo.xbeans.javaee6.DataSourceType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
public class AnnotatedEAR implements AnnotatedApp {
    private static final Logger log = LoggerFactory.getLogger(AnnotatedEAR.class);
    private ApplicationType app;

    // Protected instance variables
    protected List<EjbRefType> ambiguousEjbRefs;

    /**
     * ApplicationType-qualified constructor
     *
     * @param app ApplicationType
     */
    public AnnotatedEAR(ApplicationType app) {
        this.app = app;
    }


    /**
     * WebAppType methods used for the @EJB, @EJBs annotations
     */
    public EjbLocalRefType[] getEjbLocalRefArray() {
        return app.getEjbLocalRefArray();
    }

    public EjbLocalRefType addNewEjbLocalRef() {
        return app.addNewEjbLocalRef();
    }

    public EjbRefType[] getEjbRefArray() {
        return app.getEjbRefArray();
    }

    public EjbRefType addNewEjbRef() {
        return app.addNewEjbRef();
    }


    /**
     * WebAppType methods used for the @Resource, @Resources annotations
     */
    public EnvEntryType[] getEnvEntryArray() {
        return app.getEnvEntryArray();
    }

    public EnvEntryType addNewEnvEntry() {
        return app.addNewEnvEntry();
    }

    public ServiceRefType[] getServiceRefArray() {
        return app.getServiceRefArray();
    }

    public ServiceRefType addNewServiceRef() {
        return app.addNewServiceRef();
    }

    public ResourceRefType[] getResourceRefArray() {
        return app.getResourceRefArray();
    }

    public ResourceRefType addNewResourceRef() {
        return app.addNewResourceRef();
    }

    public MessageDestinationRefType[] getMessageDestinationRefArray() {
        return app.getMessageDestinationRefArray();
    }

    public MessageDestinationRefType addNewMessageDestinationRef() {
        return app.addNewMessageDestinationRef();
    }

    public ResourceEnvRefType[] getResourceEnvRefArray() {
        return app.getResourceEnvRefArray();
    }

    public ResourceEnvRefType addNewResourceEnvRef() {
        return app.addNewResourceEnvRef();
    }


    /**
     * webApp getter
     *
     * @return String representation of webApp
     */
    public String toString() {
        return app.toString();
    }


    /**
     * webApp getter
     *
     * @return webApp ApplicationType
     */
    public ApplicationType getApp() {
        return app;
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
        return null;
//        return app.getPostConstructArray();
    }

    public LifecycleCallbackType addPostConstruct() {
        return null;
//        return app.addNewPostConstruct();
    }

    public LifecycleCallbackType[] getPreDestroyArray() {
        return null;
//        return app.getPreDestroyArray();
    }

    public LifecycleCallbackType addPreDestroy() {
        return null;
//        return app.addNewPreDestroy();
    }

    public PersistenceContextRefType[] getPersistenceContextRefArray() {
        return app.getPersistenceContextRefArray();
    }

    public PersistenceContextRefType addNewPersistenceContextRef() {
        return app.addNewPersistenceContextRef();
    }

    public PersistenceUnitRefType[] getPersistenceUnitRefArray() {
        return app.getPersistenceUnitRefArray();
    }

    public PersistenceUnitRefType addNewPersistenceUnitRef() {
        return app.addNewPersistenceUnitRef();
    }

    public DataSourceType[] getDataSourceArray() {
        return app.getDataSourceArray();
    }

    public DataSourceType addNewDataSource() {
        return app.addNewDataSource();
    }

    public String getComponentType() {
        return null;
    }
}