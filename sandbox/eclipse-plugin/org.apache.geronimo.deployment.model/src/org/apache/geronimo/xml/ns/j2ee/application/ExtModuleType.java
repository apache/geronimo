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

import java.lang.String;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Ext Module Type</b></em>'.
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
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getConnector <em>Connector</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getEjb <em>Ejb</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getJava <em>Java</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getWeb <em>Web</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getInternalPath <em>Internal Path</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getExternalPath <em>External Path</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getAny <em>Any</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getExtModuleType()
 * @model extendedMetaData="name='ext-moduleType' kind='elementOnly'"
 * @generated
 */
public interface ExtModuleType extends EObject {
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
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getExtModuleType_Connector()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='connector' namespace='##targetNamespace'"
     * @generated
     */
    PathType getConnector();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getConnector <em>Connector</em>}' containment reference.
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
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getExtModuleType_Ejb()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='ejb' namespace='##targetNamespace'"
     * @generated
     */
    PathType getEjb();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getEjb <em>Ejb</em>}' containment reference.
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
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getExtModuleType_Java()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='java' namespace='##targetNamespace'"
     * @generated
     */
    PathType getJava();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getJava <em>Java</em>}' containment reference.
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
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getExtModuleType_Web()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='web' namespace='##targetNamespace'"
     * @generated
     */
    PathType getWeb();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getWeb <em>Web</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Web</em>' containment reference.
     * @see #getWeb()
     * @generated
     */
    void setWeb(PathType value);

    /**
     * Returns the value of the '<em><b>Internal Path</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Internal Path</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Internal Path</em>' attribute.
     * @see #setInternalPath(String)
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getExtModuleType_InternalPath()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.Token"
     *        extendedMetaData="kind='element' name='internal-path' namespace='##targetNamespace'"
     * @generated
     */
    String getInternalPath();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getInternalPath <em>Internal Path</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Internal Path</em>' attribute.
     * @see #getInternalPath()
     * @generated
     */
    void setInternalPath(String value);

    /**
     * Returns the value of the '<em><b>External Path</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>External Path</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>External Path</em>' attribute.
     * @see #setExternalPath(String)
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getExtModuleType_ExternalPath()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.Token"
     *        extendedMetaData="kind='element' name='external-path' namespace='##targetNamespace'"
     * @generated
     */
    String getExternalPath();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getExternalPath <em>External Path</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>External Path</em>' attribute.
     * @see #getExternalPath()
     * @generated
     */
    void setExternalPath(String value);

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
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getExtModuleType_Any()
     * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry" required="true"
     *        extendedMetaData="kind='elementWildcard' wildcards='##other' name=':6' processing='lax'"
     * @generated
     */
    FeatureMap getAny();

} // ExtModuleType
