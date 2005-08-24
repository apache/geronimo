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

import org.apache.geronimo.xml.ns.security.SecurityType;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getDependency <em>Dependency</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getModule <em>Module</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getExtModule <em>Ext Module</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getSecurity <em>Security</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getGbean <em>Gbean</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getApplicationName <em>Application Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getConfigId <em>Config Id</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getParentId <em>Parent Id</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getApplicationType()
 * @model extendedMetaData="name='applicationType' kind='elementOnly'"
 * @generated
 */
public interface ApplicationType extends EObject {
    /**
     * Returns the value of the '<em><b>Dependency</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.deployment.DependencyType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Dependency</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Dependency</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getApplicationType_Dependency()
     * @model type="org.apache.geronimo.xml.ns.deployment.DependencyType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='dependency' namespace='http://geronimo.apache.org/xml/ns/deployment'"
     * @generated
     */
    EList getDependency();

    /**
     * Returns the value of the '<em><b>Module</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Module</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Module</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getApplicationType_Module()
     * @model type="org.apache.geronimo.xml.ns.j2ee.application.ModuleType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='module' namespace='##targetNamespace'"
     * @generated
     */
    EList getModule();

    /**
     * Returns the value of the '<em><b>Ext Module</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ext Module</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ext Module</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getApplicationType_ExtModule()
     * @model type="org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='ext-module' namespace='##targetNamespace'"
     * @generated
     */
    EList getExtModule();

    /**
     * Returns the value of the '<em><b>Security</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Security</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Security</em>' containment reference.
     * @see #setSecurity(SecurityType)
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getApplicationType_Security()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='security' namespace='http://geronimo.apache.org/xml/ns/security'"
     * @generated
     */
    SecurityType getSecurity();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getSecurity <em>Security</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Security</em>' containment reference.
     * @see #getSecurity()
     * @generated
     */
    void setSecurity(SecurityType value);

    /**
     * Returns the value of the '<em><b>Gbean</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.deployment.GbeanType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Gbean</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Gbean</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getApplicationType_Gbean()
     * @model type="org.apache.geronimo.xml.ns.deployment.GbeanType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='gbean' namespace='http://geronimo.apache.org/xml/ns/deployment'"
     * @generated
     */
    EList getGbean();

    /**
     * Returns the value of the '<em><b>Application Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Application Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Application Name</em>' attribute.
     * @see #setApplicationName(String)
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getApplicationType_ApplicationName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='application-name'"
     * @generated
     */
    String getApplicationName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getApplicationName <em>Application Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Application Name</em>' attribute.
     * @see #getApplicationName()
     * @generated
     */
    void setApplicationName(String value);

    /**
     * Returns the value of the '<em><b>Config Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Config Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Config Id</em>' attribute.
     * @see #setConfigId(String)
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getApplicationType_ConfigId()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='attribute' name='configId'"
     * @generated
     */
    String getConfigId();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getConfigId <em>Config Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Config Id</em>' attribute.
     * @see #getConfigId()
     * @generated
     */
    void setConfigId(String value);

    /**
     * Returns the value of the '<em><b>Parent Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Parent Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Parent Id</em>' attribute.
     * @see #setParentId(String)
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getApplicationType_ParentId()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='parentId'"
     * @generated
     */
    String getParentId();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getParentId <em>Parent Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Parent Id</em>' attribute.
     * @see #getParentId()
     * @generated
     */
    void setParentId(String value);

} // ApplicationType
