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

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Pattern Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.PatternType#getDomain <em>Domain</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.PatternType#getServer <em>Server</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.PatternType#getApplication <em>Application</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.PatternType#getModuleType <em>Module Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.PatternType#getModule <em>Module</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.PatternType#getType <em>Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.PatternType#getName <em>Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.PatternType#getGbeanName <em>Gbean Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getPatternType()
 * @model extendedMetaData="name='patternType' kind='elementOnly'"
 * @generated
 */
public interface PatternType extends EObject{
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
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getPatternType_Domain()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='domain' namespace='##targetNamespace'"
     * @generated
     */
    String getDomain();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getDomain <em>Domain</em>}' attribute.
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
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getPatternType_Server()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='server' namespace='##targetNamespace'"
     * @generated
     */
    String getServer();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getServer <em>Server</em>}' attribute.
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
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getPatternType_Application()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='application' namespace='##targetNamespace'"
     * @generated
     */
    String getApplication();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getApplication <em>Application</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Application</em>' attribute.
     * @see #getApplication()
     * @generated
     */
    void setApplication(String value);

    /**
     * Returns the value of the '<em><b>Module Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Module Type</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Module Type</em>' attribute.
     * @see #setModuleType(String)
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getPatternType_ModuleType()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='moduleType' namespace='##targetNamespace'"
     * @generated
     */
    String getModuleType();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getModuleType <em>Module Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Module Type</em>' attribute.
     * @see #getModuleType()
     * @generated
     */
    void setModuleType(String value);

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
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getPatternType_Module()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='module' namespace='##targetNamespace'"
     * @generated
     */
    String getModule();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getModule <em>Module</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Module</em>' attribute.
     * @see #getModule()
     * @generated
     */
    void setModule(String value);

    /**
     * Returns the value of the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Type</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Type</em>' attribute.
     * @see #setType(String)
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getPatternType_Type()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='type' namespace='##targetNamespace'"
     * @generated
     */
    String getType();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getType <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Type</em>' attribute.
     * @see #getType()
     * @generated
     */
    void setType(String value);

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
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getPatternType_Name()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='name' namespace='##targetNamespace'"
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Gbean Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Gbean Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Gbean Name</em>' attribute.
     * @see #setGbeanName(String)
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getPatternType_GbeanName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='gbean-name' namespace='##targetNamespace'"
     * @generated
     */
    String getGbeanName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getGbeanName <em>Gbean Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Gbean Name</em>' attribute.
     * @see #getGbeanName()
     * @generated
     */
    void setGbeanName(String value);

} // PatternType
