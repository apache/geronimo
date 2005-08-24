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
package org.openejb.xml.ns.openejb.jar;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Tss Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.TssType#getDomain <em>Domain</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.TssType#getServer <em>Server</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.TssType#getApplication <em>Application</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.TssType#getModule <em>Module</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.TssType#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getTssType()
 * @model extendedMetaData="name='tssType' kind='elementOnly'"
 * @generated
 */
public interface TssType extends EObject {
    /**
     * Returns the value of the '<em><b>Domain</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Domain</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Domain</em>' attribute.
     * @see #setDomain(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getTssType_Domain()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='domain' namespace='##targetNamespace'"
     * @generated
     */
    String getDomain();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.TssType#getDomain <em>Domain</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Domain</em>' attribute.
     * @see #getDomain()
     * @generated
     */
    void setDomain(String value);

    /**
     * Returns the value of the '<em><b>Server</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Server</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Server</em>' attribute.
     * @see #setServer(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getTssType_Server()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='server' namespace='##targetNamespace'"
     * @generated
     */
    String getServer();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.TssType#getServer <em>Server</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Server</em>' attribute.
     * @see #getServer()
     * @generated
     */
    void setServer(String value);

    /**
     * Returns the value of the '<em><b>Application</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Application</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Application</em>' attribute.
     * @see #setApplication(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getTssType_Application()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='application' namespace='##targetNamespace'"
     * @generated
     */
    String getApplication();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.TssType#getApplication <em>Application</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Application</em>' attribute.
     * @see #getApplication()
     * @generated
     */
    void setApplication(String value);

    /**
     * Returns the value of the '<em><b>Module</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Module</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Module</em>' attribute.
     * @see #setModule(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getTssType_Module()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='module' namespace='##targetNamespace'"
     * @generated
     */
    String getModule();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.TssType#getModule <em>Module</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Module</em>' attribute.
     * @see #getModule()
     * @generated
     */
    void setModule(String value);

    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Name</em>' attribute.
     * @see #setName(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getTssType_Name()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='name' namespace='##targetNamespace'"
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.TssType#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

} // TssType
