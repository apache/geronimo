/**
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.xml.ns.web;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Container Config Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *                 Holds any configuration settings appropriate to the
 *                 specific web container this application is targeting.
 *                 Generally speaking this configuration file reflects
 *                 the commonalities between web containers, and it's
 *                 possible for a single container to have additional
 *                 configuration options.  We recommend these be avoided
 *                 wherever possible for the sake of portability.
 * 
 *                 Known containers are:
 *                   Tomcat
 *                   Jetty
 * 
 *                 Some known parameters are:
 *                   Tomcat:  VirtualServer
 *                   Tomcat:  TomcatRealm
 *                   Tomcat:  TomcatValveChain
 *                 An authoritative list is left to the individual container
 *                 documentation.
 *             
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.web.ContainerConfigType#getConfigParam <em>Config Param</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.ContainerConfigType#getContainer <em>Container</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.web.WebPackage#getContainerConfigType()
 * @model extendedMetaData="name='container-configType' kind='elementOnly'"
 * @generated
 */
public interface ContainerConfigType extends EObject {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

    /**
     * Returns the value of the '<em><b>Config Param</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.web.ConfigParamType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Config Param</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Config Param</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getContainerConfigType_ConfigParam()
     * @model type="org.apache.geronimo.xml.ns.web.ConfigParamType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='config-param' namespace='##targetNamespace'"
     * @generated
     */
    EList getConfigParam();

    /**
     * Returns the value of the '<em><b>Container</b></em>' attribute.
     * The default value is <code>"Tomcat"</code>.
     * The literals are from the enumeration {@link org.apache.geronimo.xml.ns.web.WebContainerType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Container</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Container</em>' attribute.
     * @see org.apache.geronimo.xml.ns.web.WebContainerType
     * @see #isSetContainer()
     * @see #unsetContainer()
     * @see #setContainer(WebContainerType)
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getContainerConfigType_Container()
     * @model default="Tomcat" unique="false" unsettable="true" required="true"
     *        extendedMetaData="kind='attribute' name='container'"
     * @generated
     */
    WebContainerType getContainer();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.web.ContainerConfigType#getContainer <em>Container</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Container</em>' attribute.
     * @see org.apache.geronimo.xml.ns.web.WebContainerType
     * @see #isSetContainer()
     * @see #unsetContainer()
     * @see #getContainer()
     * @generated
     */
    void setContainer(WebContainerType value);

    /**
     * Unsets the value of the '{@link org.apache.geronimo.xml.ns.web.ContainerConfigType#getContainer <em>Container</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSetContainer()
     * @see #getContainer()
     * @see #setContainer(WebContainerType)
     * @generated
     */
    void unsetContainer();

    /**
     * Returns whether the value of the '{@link org.apache.geronimo.xml.ns.web.ContainerConfigType#getContainer <em>Container</em>}' attribute is set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return whether the value of the '<em>Container</em>' attribute is set.
     * @see #unsetContainer()
     * @see #getContainer()
     * @see #setContainer(WebContainerType)
     * @generated
     */
    boolean isSetContainer();

} // ContainerConfigType
