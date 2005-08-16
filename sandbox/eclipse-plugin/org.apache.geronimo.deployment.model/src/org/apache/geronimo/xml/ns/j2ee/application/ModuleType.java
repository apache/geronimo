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
package org.apache.geronimo.xml.ns.j2ee.application;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Module Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 * 	Mirrors the moduleType defined by application_1_4.xsd and adds an
 * 	optional alt-dd element defining a Geronimo specific deployment descriptor.
 *             
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getConnector <em>Connector</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getEjb <em>Ejb</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getJava <em>Java</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getWeb <em>Web</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getAltDd <em>Alt Dd</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getAny <em>Any</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getModuleType()
 * @model extendedMetaData="name='moduleType' kind='elementOnly'"
 * @generated
 */
public interface ModuleType extends EObject {
    /**
     * Returns the value of the '<em><b>Connector</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Connector</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Connector</em>' containment reference.
     * @see #setConnector(PathType)
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getModuleType_Connector()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='connector' namespace='##targetNamespace'"
     * @generated
     */
    PathType getConnector();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getConnector <em>Connector</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Connector</em>' containment reference.
     * @see #getConnector()
     * @generated
     */
    void setConnector(PathType value);

    /**
     * Returns the value of the '<em><b>Ejb</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb</em>' containment reference.
     * @see #setEjb(PathType)
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getModuleType_Ejb()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='ejb' namespace='##targetNamespace'"
     * @generated
     */
    PathType getEjb();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getEjb <em>Ejb</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ejb</em>' containment reference.
     * @see #getEjb()
     * @generated
     */
    void setEjb(PathType value);

    /**
     * Returns the value of the '<em><b>Java</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Java</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Java</em>' containment reference.
     * @see #setJava(PathType)
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getModuleType_Java()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='java' namespace='##targetNamespace'"
     * @generated
     */
    PathType getJava();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getJava <em>Java</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Java</em>' containment reference.
     * @see #getJava()
     * @generated
     */
    void setJava(PathType value);

    /**
     * Returns the value of the '<em><b>Web</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Web</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Web</em>' containment reference.
     * @see #setWeb(PathType)
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getModuleType_Web()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='web' namespace='##targetNamespace'"
     * @generated
     */
    PathType getWeb();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getWeb <em>Web</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Web</em>' containment reference.
     * @see #getWeb()
     * @generated
     */
    void setWeb(PathType value);

    /**
     * Returns the value of the '<em><b>Alt Dd</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * 
     *   Specifies an optional URI to the post-assembly version of the Geronimo
     *   specific deployment descriptor file for a particular J2EE module.
     *                         
     * <!-- end-model-doc -->
     * @return the value of the '<em>Alt Dd</em>' containment reference.
     * @see #setAltDd(PathType)
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getModuleType_AltDd()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='alt-dd' namespace='##targetNamespace'"
     * @generated
     */
    PathType getAltDd();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getAltDd <em>Alt Dd</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Alt Dd</em>' containment reference.
     * @see #getAltDd()
     * @generated
     */
    void setAltDd(PathType value);

    /**
     * Returns the value of the '<em><b>Any</b></em>' attribute list.
     * The list contents are of type {@link org.eclipse.emf.ecore.util.FeatureMap.Entry}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Any</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Any</em>' attribute list.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getModuleType_Any()
     * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry"
     *        extendedMetaData="kind='elementWildcard' wildcards='##other' name=':5' processing='lax'"
     * @generated
     */
    FeatureMap getAny();

} // ModuleType
