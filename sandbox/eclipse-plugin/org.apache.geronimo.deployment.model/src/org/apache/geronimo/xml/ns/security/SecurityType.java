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
package org.apache.geronimo.xml.ns.security;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *                 Security entries
 * 
 *                 If this element is present, all web and EJB modules MUST make the
 *                 appropriate access checks as outlined in the JACC spec.
 *             
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.security.SecurityType#getDescription <em>Description</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.SecurityType#getDefaultPrincipal <em>Default Principal</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.SecurityType#getRoleMappings <em>Role Mappings</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.SecurityType#getDefaultRole <em>Default Role</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.SecurityType#isDoasCurrentCaller <em>Doas Current Caller</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.SecurityType#isUseContextHandler <em>Use Context Handler</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getSecurityType()
 * @model extendedMetaData="name='securityType' kind='elementOnly'"
 * @generated
 */
public interface SecurityType extends EObject {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

    /**
     * Returns the value of the '<em><b>Description</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.security.DescriptionType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Description</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Description</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getSecurityType_Description()
     * @model type="org.apache.geronimo.xml.ns.security.DescriptionType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='description' namespace='##targetNamespace'"
     * @generated
     */
    EList getDescription();

    /**
     * Returns the value of the '<em><b>Default Principal</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Default Principal</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Default Principal</em>' containment reference.
     * @see #setDefaultPrincipal(DefaultPrincipalType)
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getSecurityType_DefaultPrincipal()
     * @model containment="true" resolveProxies="false" required="true"
     *        extendedMetaData="kind='element' name='default-principal' namespace='##targetNamespace'"
     * @generated
     */
    DefaultPrincipalType getDefaultPrincipal();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.security.SecurityType#getDefaultPrincipal <em>Default Principal</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Default Principal</em>' containment reference.
     * @see #getDefaultPrincipal()
     * @generated
     */
    void setDefaultPrincipal(DefaultPrincipalType value);

    /**
     * Returns the value of the '<em><b>Role Mappings</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Role Mappings</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Role Mappings</em>' containment reference.
     * @see #setRoleMappings(RoleMappingsType)
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getSecurityType_RoleMappings()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='role-mappings' namespace='##targetNamespace'"
     * @generated
     */
    RoleMappingsType getRoleMappings();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.security.SecurityType#getRoleMappings <em>Role Mappings</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Role Mappings</em>' containment reference.
     * @see #getRoleMappings()
     * @generated
     */
    void setRoleMappings(RoleMappingsType value);

    /**
     * Returns the value of the '<em><b>Default Role</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * 
     *                     Used by the the Deployer to assign method permissions for
     *                     all of the unspecified methods, either by assigning them
     *                     to security roles, or by marking them as unchecked.  If
     *                     the value of default-role is empty, then the unspecified
     *                     methods are marked unchecked
     *                 
     * <!-- end-model-doc -->
     * @return the value of the '<em>Default Role</em>' attribute.
     * @see #setDefaultRole(String)
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getSecurityType_DefaultRole()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='default-role'"
     * @generated
     */
    String getDefaultRole();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.security.SecurityType#getDefaultRole <em>Default Role</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Default Role</em>' attribute.
     * @see #getDefaultRole()
     * @generated
     */
    void setDefaultRole(String value);

    /**
     * Returns the value of the '<em><b>Doas Current Caller</b></em>' attribute.
     * The default value is <code>"false"</code>.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * 
     *                     Set this attribute to "true" if the work is to be performed
     *                     as the calling Subject.
     *                 
     * <!-- end-model-doc -->
     * @return the value of the '<em>Doas Current Caller</em>' attribute.
     * @see #isSetDoasCurrentCaller()
     * @see #unsetDoasCurrentCaller()
     * @see #setDoasCurrentCaller(boolean)
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getSecurityType_DoasCurrentCaller()
     * @model default="false" unique="false" unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Boolean"
     *        extendedMetaData="kind='attribute' name='doas-current-caller'"
     * @generated
     */
    boolean isDoasCurrentCaller();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.security.SecurityType#isDoasCurrentCaller <em>Doas Current Caller</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Doas Current Caller</em>' attribute.
     * @see #isSetDoasCurrentCaller()
     * @see #unsetDoasCurrentCaller()
     * @see #isDoasCurrentCaller()
     * @generated
     */
    void setDoasCurrentCaller(boolean value);

    /**
     * Unsets the value of the '{@link org.apache.geronimo.xml.ns.security.SecurityType#isDoasCurrentCaller <em>Doas Current Caller</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSetDoasCurrentCaller()
     * @see #isDoasCurrentCaller()
     * @see #setDoasCurrentCaller(boolean)
     * @generated
     */
    void unsetDoasCurrentCaller();

    /**
     * Returns whether the value of the '{@link org.apache.geronimo.xml.ns.security.SecurityType#isDoasCurrentCaller <em>Doas Current Caller</em>}' attribute is set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return whether the value of the '<em>Doas Current Caller</em>' attribute is set.
     * @see #unsetDoasCurrentCaller()
     * @see #isDoasCurrentCaller()
     * @see #setDoasCurrentCaller(boolean)
     * @generated
     */
    boolean isSetDoasCurrentCaller();

    /**
     * Returns the value of the '<em><b>Use Context Handler</b></em>' attribute.
     * The default value is <code>"false"</code>.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * 
     *                     Set this attribute to "true" if the installed JACC policy
     *                     contexts will use PolicyContextHandlers.
     *                 
     * <!-- end-model-doc -->
     * @return the value of the '<em>Use Context Handler</em>' attribute.
     * @see #isSetUseContextHandler()
     * @see #unsetUseContextHandler()
     * @see #setUseContextHandler(boolean)
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getSecurityType_UseContextHandler()
     * @model default="false" unique="false" unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Boolean"
     *        extendedMetaData="kind='attribute' name='use-context-handler'"
     * @generated
     */
    boolean isUseContextHandler();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.security.SecurityType#isUseContextHandler <em>Use Context Handler</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Use Context Handler</em>' attribute.
     * @see #isSetUseContextHandler()
     * @see #unsetUseContextHandler()
     * @see #isUseContextHandler()
     * @generated
     */
    void setUseContextHandler(boolean value);

    /**
     * Unsets the value of the '{@link org.apache.geronimo.xml.ns.security.SecurityType#isUseContextHandler <em>Use Context Handler</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSetUseContextHandler()
     * @see #isUseContextHandler()
     * @see #setUseContextHandler(boolean)
     * @generated
     */
    void unsetUseContextHandler();

    /**
     * Returns whether the value of the '{@link org.apache.geronimo.xml.ns.security.SecurityType#isUseContextHandler <em>Use Context Handler</em>}' attribute is set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return whether the value of the '<em>Use Context Handler</em>' attribute is set.
     * @see #unsetUseContextHandler()
     * @see #isUseContextHandler()
     * @see #setUseContextHandler(boolean)
     * @generated
     */
    boolean isSetUseContextHandler();

} // SecurityType
