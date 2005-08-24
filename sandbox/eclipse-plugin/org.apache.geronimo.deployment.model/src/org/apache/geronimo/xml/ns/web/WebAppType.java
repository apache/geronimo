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

import org.apache.geronimo.xml.ns.security.SecurityType;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>App Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#getDependency <em>Dependency</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#getContextRoot <em>Context Root</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#isContextPriorityClassloader <em>Context Priority Classloader</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#getContainerConfig <em>Container Config</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#getSecurityRealmName <em>Security Realm Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#getSecurity <em>Security</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#getEjbRef <em>Ejb Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#getEjbLocalRef <em>Ejb Local Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#getServiceRef <em>Service Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#getResourceRef <em>Resource Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#getResourceEnvRef <em>Resource Env Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#getGbean <em>Gbean</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#getConfigId <em>Config Id</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.web.WebAppType#getParentId <em>Parent Id</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType()
 * @model extendedMetaData="name='web-appType' kind='elementOnly'"
 * @generated
 */
public interface WebAppType extends EObject{
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
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_Dependency()
     * @model type="org.apache.geronimo.xml.ns.deployment.DependencyType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='dependency' namespace='http://geronimo.apache.org/xml/ns/deployment'"
     * @generated
     */
    EList getDependency();

    /**
     * Returns the value of the '<em><b>Context Root</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Context Root</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Context Root</em>' attribute.
     * @see #setContextRoot(String)
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_ContextRoot()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='context-root' namespace='##targetNamespace'"
     * @generated
     */
    String getContextRoot();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.web.WebAppType#getContextRoot <em>Context Root</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Context Root</em>' attribute.
     * @see #getContextRoot()
     * @generated
     */
    void setContextRoot(String value);

    /**
     * Returns the value of the '<em><b>Context Priority Classloader</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Context Priority Classloader</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Context Priority Classloader</em>' attribute.
     * @see #isSetContextPriorityClassloader()
     * @see #unsetContextPriorityClassloader()
     * @see #setContextPriorityClassloader(boolean)
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_ContextPriorityClassloader()
     * @model unique="false" unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Boolean" required="true"
     *        extendedMetaData="kind='element' name='context-priority-classloader' namespace='##targetNamespace'"
     * @generated
     */
    boolean isContextPriorityClassloader();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.web.WebAppType#isContextPriorityClassloader <em>Context Priority Classloader</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Context Priority Classloader</em>' attribute.
     * @see #isSetContextPriorityClassloader()
     * @see #unsetContextPriorityClassloader()
     * @see #isContextPriorityClassloader()
     * @generated
     */
    void setContextPriorityClassloader(boolean value);

    /**
     * Unsets the value of the '{@link org.apache.geronimo.xml.ns.web.WebAppType#isContextPriorityClassloader <em>Context Priority Classloader</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSetContextPriorityClassloader()
     * @see #isContextPriorityClassloader()
     * @see #setContextPriorityClassloader(boolean)
     * @generated
     */
    void unsetContextPriorityClassloader();

    /**
     * Returns whether the value of the '{@link org.apache.geronimo.xml.ns.web.WebAppType#isContextPriorityClassloader <em>Context Priority Classloader</em>}' attribute is set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return whether the value of the '<em>Context Priority Classloader</em>' attribute is set.
     * @see #unsetContextPriorityClassloader()
     * @see #isContextPriorityClassloader()
     * @see #setContextPriorityClassloader(boolean)
     * @generated
     */
    boolean isSetContextPriorityClassloader();

    /**
     * Returns the value of the '<em><b>Container Config</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.web.ContainerConfigType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Container Config</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Container Config</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_ContainerConfig()
     * @model type="org.apache.geronimo.xml.ns.web.ContainerConfigType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='container-config' namespace='##targetNamespace'"
     * @generated
     */
    EList getContainerConfig();

    /**
     * Returns the value of the '<em><b>Security Realm Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Security Realm Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Security Realm Name</em>' attribute.
     * @see #setSecurityRealmName(String)
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_SecurityRealmName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='security-realm-name' namespace='##targetNamespace'"
     * @generated
     */
    String getSecurityRealmName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.web.WebAppType#getSecurityRealmName <em>Security Realm Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Security Realm Name</em>' attribute.
     * @see #getSecurityRealmName()
     * @generated
     */
    void setSecurityRealmName(String value);

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
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_Security()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='security' namespace='http://geronimo.apache.org/xml/ns/security'"
     * @generated
     */
    SecurityType getSecurity();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.web.WebAppType#getSecurity <em>Security</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Security</em>' containment reference.
     * @see #getSecurity()
     * @generated
     */
    void setSecurity(SecurityType value);

    /**
     * Returns the value of the '<em><b>Ejb Ref</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.naming.EjbRefType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb Ref</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb Ref</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_EjbRef()
     * @model type="org.apache.geronimo.xml.ns.naming.EjbRefType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='ejb-ref' namespace='http://geronimo.apache.org/xml/ns/naming'"
     * @generated
     */
    EList getEjbRef();

    /**
     * Returns the value of the '<em><b>Ejb Local Ref</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.naming.EjbLocalRefType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb Local Ref</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb Local Ref</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_EjbLocalRef()
     * @model type="org.apache.geronimo.xml.ns.naming.EjbLocalRefType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='ejb-local-ref' namespace='http://geronimo.apache.org/xml/ns/naming'"
     * @generated
     */
    EList getEjbLocalRef();

    /**
     * Returns the value of the '<em><b>Service Ref</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.naming.ServiceRefType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Service Ref</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Service Ref</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_ServiceRef()
     * @model type="org.apache.geronimo.xml.ns.naming.ServiceRefType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='service-ref' namespace='http://geronimo.apache.org/xml/ns/naming'"
     * @generated
     */
    EList getServiceRef();

    /**
     * Returns the value of the '<em><b>Resource Ref</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.naming.ResourceRefType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Resource Ref</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Resource Ref</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_ResourceRef()
     * @model type="org.apache.geronimo.xml.ns.naming.ResourceRefType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='resource-ref' namespace='http://geronimo.apache.org/xml/ns/naming'"
     * @generated
     */
    EList getResourceRef();

    /**
     * Returns the value of the '<em><b>Resource Env Ref</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.naming.ResourceEnvRefType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Resource Env Ref</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Resource Env Ref</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_ResourceEnvRef()
     * @model type="org.apache.geronimo.xml.ns.naming.ResourceEnvRefType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='resource-env-ref' namespace='http://geronimo.apache.org/xml/ns/naming'"
     * @generated
     */
    EList getResourceEnvRef();

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
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_Gbean()
     * @model type="org.apache.geronimo.xml.ns.deployment.GbeanType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='gbean' namespace='http://geronimo.apache.org/xml/ns/deployment'"
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
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_ConfigId()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='attribute' name='configId'"
     * @generated
     */
    String getConfigId();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.web.WebAppType#getConfigId <em>Config Id</em>}' attribute.
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
     * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebAppType_ParentId()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='parentId'"
     * @generated
     */
    String getParentId();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.web.WebAppType#getParentId <em>Parent Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Parent Id</em>' attribute.
     * @see #getParentId()
     * @generated
     */
    void setParentId(String value);

} // WebAppType
