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

import java.util.List;

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

/**
 * Many of the classes generated from the JEE schemas have methods with identical signatures (see
 * examples below). This interface can be used to encapsulate those methods so that the various
 * AnnotationHelper classes can use the same code for multiple application types (e.g. WebAppType,
 * ApplicationClientType, etc).
 * <p/>
 * <p><strong>Example(s):</strong>
 * <pre>
 *      public interface ApplicationClientType extends org.apache.xmlbeans.XmlObject {
 *          org.apache.geronimo.xbeans.javaee6.EnvEntryType[] getEnvEntryArray();
 *          org.apache.geronimo.xbeans.javaee6.ResourceRefType[] getResourceRefArray();
 *      }
 * <p/>
 *      public interface WebAppType extends org.apache.xmlbeans.XmlObject {
 *          org.apache.geronimo.xbeans.javaee6.EnvEntryType[] getEnvEntryArray();
 *          org.apache.geronimo.xbeans.javaee6.ResourceRefType[] getResourceRefArray();
 *      }
 * </pre>
 * <p/>
 * <p><strong>Remaining ToDo(s):</strong>
 * <ul>
 * <li>None
 * </ul>
 *
 * @version $Rev $Date
 * @since Geronimo 2.0
 */
public interface AnnotatedApp {

    /**
     * XmlBeans methods used for the @EJB, @EJBs annotations
     */
    EjbLocalRefType[] getEjbLocalRefArray();

    EjbLocalRefType addNewEjbLocalRef();

    EjbRefType[] getEjbRefArray();

    EjbRefType addNewEjbRef();


    /**
     * XmlBeans methods used for the @Resource, @Resources annotations
     */
    EnvEntryType[] getEnvEntryArray();

    EnvEntryType addNewEnvEntry();

    ServiceRefType[] getServiceRefArray();

    ServiceRefType addNewServiceRef();

    ResourceRefType[] getResourceRefArray();

    ResourceRefType addNewResourceRef();

    MessageDestinationRefType[] getMessageDestinationRefArray();

    MessageDestinationRefType addNewMessageDestinationRef();

    ResourceEnvRefType[] getResourceEnvRefArray();

    ResourceEnvRefType addNewResourceEnvRef();


    /**
     * ApplicationType getter in string format
     *
     * @return String representation of ApplicationType
     */
    String toString();


    /**
     * ambiguousRefs getter
     * <p/>
     * <p>There is no corresponding setter method. To add a new item to the list do this:
     * <p/>
     * <pre>
     *    getAmbiguousEjbRefs().add(ejbRef);
     * </pre>
     *
     * @return ambiguousRefs list
     */
    List<EjbRefType> getAmbiguousEjbRefs();

    LifecycleCallbackType[] getPostConstructArray();
    LifecycleCallbackType addPostConstruct();

    LifecycleCallbackType[] getPreDestroyArray();
    LifecycleCallbackType addPreDestroy();

    PersistenceContextRefType[] getPersistenceContextRefArray();
    PersistenceContextRefType addNewPersistenceContextRef();

    PersistenceUnitRefType[] getPersistenceUnitRefArray();
    PersistenceUnitRefType addNewPersistenceUnitRef();

    String getComponentType();
}
