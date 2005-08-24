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
package org.apache.geronimo.xml.ns.naming;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Resource Locator Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getDomain <em>Domain</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getServer <em>Server</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getApplication <em>Application</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getModule <em>Module</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getType <em>Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getName <em>Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getResourceLink <em>Resource Link</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getTargetName <em>Target Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getUrl <em>Url</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getResourceLocatorType()
 * @model extendedMetaData="name='resource-locatorType' kind='elementOnly'"
 * @generated
 */
public interface ResourceLocatorType extends EObject{
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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getResourceLocatorType_Domain()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='domain' namespace='##targetNamespace'"
     * @generated
     */
    String getDomain();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getDomain <em>Domain</em>}' attribute.
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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getResourceLocatorType_Server()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='server' namespace='##targetNamespace'"
     * @generated
     */
    String getServer();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getServer <em>Server</em>}' attribute.
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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getResourceLocatorType_Application()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='application' namespace='##targetNamespace'"
     * @generated
     */
    String getApplication();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getApplication <em>Application</em>}' attribute.
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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getResourceLocatorType_Module()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='module' namespace='##targetNamespace'"
     * @generated
     */
    String getModule();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getModule <em>Module</em>}' attribute.
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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getResourceLocatorType_Type()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='type' namespace='##targetNamespace'"
     * @generated
     */
    String getType();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getType <em>Type</em>}' attribute.
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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getResourceLocatorType_Name()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='name' namespace='##targetNamespace'"
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Resource Link</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Resource Link</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Resource Link</em>' attribute.
     * @see #setResourceLink(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getResourceLocatorType_ResourceLink()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='resource-link' namespace='##targetNamespace'"
     * @generated
     */
    String getResourceLink();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getResourceLink <em>Resource Link</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Resource Link</em>' attribute.
     * @see #getResourceLink()
     * @generated
     */
    void setResourceLink(String value);

    /**
     * Returns the value of the '<em><b>Target Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Target Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Target Name</em>' attribute.
     * @see #setTargetName(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getResourceLocatorType_TargetName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='target-name' namespace='##targetNamespace'"
     * @generated
     */
    String getTargetName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getTargetName <em>Target Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Target Name</em>' attribute.
     * @see #getTargetName()
     * @generated
     */
    void setTargetName(String value);

    /**
     * Returns the value of the '<em><b>Url</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Url</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Url</em>' attribute.
     * @see #setUrl(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getResourceLocatorType_Url()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='url' namespace='##targetNamespace'"
     * @generated
     */
    String getUrl();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getUrl <em>Url</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Url</em>' attribute.
     * @see #getUrl()
     * @generated
     */
    void setUrl(String value);

} // ResourceLocatorType
