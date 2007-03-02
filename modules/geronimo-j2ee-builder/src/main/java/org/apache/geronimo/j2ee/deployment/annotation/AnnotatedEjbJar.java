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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.xbeans.javaee.EjbJarType;
import org.apache.geronimo.xbeans.javaee.EjbLocalRefType;
import org.apache.geronimo.xbeans.javaee.EjbRefType;
import org.apache.geronimo.xbeans.javaee.EnvEntryType;
import org.apache.geronimo.xbeans.javaee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.javaee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.javaee.ResourceRefType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;

/**
 * Wrapper class to encapsulate the EjbJarType class with an interface that the various
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
public class AnnotatedEjbJar implements AnnotatedApp {

    // Private instance variables
    private static final Log log = LogFactory.getLog(AnnotatedEjbJar.class);
    private EjbJarType ejbJar;
    private List<EjbRefType> ambiguousEjbRefs;

    /**
     * EjbJarType-qualified constructor
     *
     * @param ejbJar EjbJarType
     */
    public AnnotatedEjbJar(EjbJarType ejbJar) {
        this.ejbJar = ejbJar;
    }


    /**
     * EjbJarType methods used for the @EJB, @EJBs annotations
     */
    public EjbLocalRefType[] getEjbLocalRefArray() {
        return null;
    }

    public EjbLocalRefType addNewEjbLocalRef() {
        return null;
    }

    public EjbRefType[] getEjbRefArray() {
        return null;
    }

    public EjbRefType addNewEjbRef() {
        return null;
    }


    /**
     * EjbJarType methods used for the @Resource, @Resources annotations
     */
    public EnvEntryType[] getEnvEntryArray() {
        return null;
    }

    public EnvEntryType addNewEnvEntry() {
        return null;
    }

    public ServiceRefType[] getServiceRefArray() {
        return null;
    }

    public ServiceRefType addNewServiceRef() {
        return null;
    }

    public ResourceRefType[] getResourceRefArray() {
        return null;
    }

    public ResourceRefType addNewResourceRef() {
        return null;
    }

    public MessageDestinationRefType[] getMessageDestinationRefArray() {
        return null;
    }

    public MessageDestinationRefType addNewMessageDestinationRef() {
        return null;
    }

    public ResourceEnvRefType[] getResourceEnvRefArray() {
        return null;
    }

    public ResourceEnvRefType addNewResourceEnvRef() {
        return null;
    }


    /**
     * ejbJar getter
     *
     * @return String representation of ejbJar
     */
    public String toString() {
        return ejbJar.toString();
    }


    /**
     * ejbJar getter
     *
     * @return ejbJar EjbJarType
     */
    public EjbJarType getEjbJar() {
        return ejbJar;
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
}
