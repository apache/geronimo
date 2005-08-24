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
package org.apache.geronimo.xml.ns.deployment;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Configuration Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getInclude <em>Include</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getDependency <em>Dependency</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getGbean <em>Gbean</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getConfigId <em>Config Id</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getDomain <em>Domain</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getParentId <em>Parent Id</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getServer <em>Server</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getConfigurationType()
 * @model extendedMetaData="name='configurationType' kind='elementOnly'"
 * @generated
 */
public interface ConfigurationType extends EObject{
    /**
     * Returns the value of the '<em><b>Include</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.deployment.DependencyType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Include</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Include</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getConfigurationType_Include()
     * @model type="org.apache.geronimo.xml.ns.deployment.DependencyType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='include' namespace='##targetNamespace'"
     * @generated
     */
    EList getInclude();

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
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getConfigurationType_Dependency()
     * @model type="org.apache.geronimo.xml.ns.deployment.DependencyType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='dependency' namespace='##targetNamespace'"
     * @generated
     */
    EList getDependency();

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
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getConfigurationType_Gbean()
     * @model type="org.apache.geronimo.xml.ns.deployment.GbeanType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='gbean' namespace='##targetNamespace'"
     * @generated
     */
    EList getGbean();

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
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getConfigurationType_ConfigId()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='attribute' name='configId'"
     * @generated
     */
    String getConfigId();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getConfigId <em>Config Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Config Id</em>' attribute.
     * @see #getConfigId()
     * @generated
     */
    void setConfigId(String value);

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
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getConfigurationType_Domain()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='domain'"
     * @generated
     */
    String getDomain();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getDomain <em>Domain</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Domain</em>' attribute.
     * @see #getDomain()
     * @generated
     */
    void setDomain(String value);

    /**
     * Returns the value of the '<em><b>Parent Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * 
     *                 You are required to specify either parentId or both domain and server.  domain and server form
     *                 the domain and J2EEServer key for gbeans in this configuration and any descendant configurations.
     *                 
     * <!-- end-model-doc -->
     * @return the value of the '<em>Parent Id</em>' attribute.
     * @see #setParentId(String)
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getConfigurationType_ParentId()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='parentId'"
     * @generated
     */
    String getParentId();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getParentId <em>Parent Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Parent Id</em>' attribute.
     * @see #getParentId()
     * @generated
     */
    void setParentId(String value);

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
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getConfigurationType_Server()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='server'"
     * @generated
     */
    String getServer();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getServer <em>Server</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Server</em>' attribute.
     * @see #getServer()
     * @generated
     */
    void setServer(String value);

} // ConfigurationType
