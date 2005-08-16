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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Gbean Ref Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getRefName <em>Ref Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getRefType <em>Ref Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getProxyType <em>Proxy Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getGroup <em>Group</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getDomain <em>Domain</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getServer <em>Server</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getApplication <em>Application</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getModule <em>Module</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getType <em>Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getName <em>Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getTargetName <em>Target Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getGbeanRefType()
 * @model extendedMetaData="name='gbean-refType' kind='elementOnly'"
 * @generated
 */
public interface GbeanRefType extends EObject{
    /**
     * Returns the value of the '<em><b>Ref Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ref Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ref Name</em>' attribute.
     * @see #setRefName(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getGbeanRefType_RefName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='ref-name' namespace='##targetNamespace'"
     * @generated
     */
    String getRefName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getRefName <em>Ref Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ref Name</em>' attribute.
     * @see #getRefName()
     * @generated
     */
    void setRefName(String value);

    /**
     * Returns the value of the '<em><b>Ref Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ref Type</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ref Type</em>' attribute.
     * @see #setRefType(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getGbeanRefType_RefType()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='ref-type' namespace='##targetNamespace'"
     * @generated
     */
    String getRefType();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getRefType <em>Ref Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ref Type</em>' attribute.
     * @see #getRefType()
     * @generated
     */
    void setRefType(String value);

    /**
     * Returns the value of the '<em><b>Proxy Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Proxy Type</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Proxy Type</em>' attribute.
     * @see #setProxyType(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getGbeanRefType_ProxyType()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='proxy-type' namespace='##targetNamespace'"
     * @generated
     */
    String getProxyType();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getProxyType <em>Proxy Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Proxy Type</em>' attribute.
     * @see #getProxyType()
     * @generated
     */
    void setProxyType(String value);

    /**
     * Returns the value of the '<em><b>Group</b></em>' attribute list.
     * The list contents are of type {@link org.eclipse.emf.ecore.util.FeatureMap.Entry}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Group</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Group</em>' attribute list.
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getGbeanRefType_Group()
     * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry" many="true"
     *        extendedMetaData="kind='group' name='group:3'"
     * @generated
     */
    FeatureMap getGroup();

    /**
     * Returns the value of the '<em><b>Domain</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Domain</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Domain</em>' attribute list.
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getGbeanRefType_Domain()
     * @model type="java.lang.String" unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='domain' namespace='##targetNamespace' group='#group:3'"
     * @generated
     */
    EList getDomain();

    /**
     * Returns the value of the '<em><b>Server</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Server</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Server</em>' attribute list.
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getGbeanRefType_Server()
     * @model type="java.lang.String" unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='server' namespace='##targetNamespace' group='#group:3'"
     * @generated
     */
    EList getServer();

    /**
     * Returns the value of the '<em><b>Application</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Application</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Application</em>' attribute list.
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getGbeanRefType_Application()
     * @model type="java.lang.String" unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='application' namespace='##targetNamespace' group='#group:3'"
     * @generated
     */
    EList getApplication();

    /**
     * Returns the value of the '<em><b>Module</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Module</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Module</em>' attribute list.
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getGbeanRefType_Module()
     * @model type="java.lang.String" unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='module' namespace='##targetNamespace' group='#group:3'"
     * @generated
     */
    EList getModule();

    /**
     * Returns the value of the '<em><b>Type</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Type</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Type</em>' attribute list.
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getGbeanRefType_Type()
     * @model type="java.lang.String" unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='type' namespace='##targetNamespace' group='#group:3'"
     * @generated
     */
    EList getType();

    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Name</em>' attribute list.
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getGbeanRefType_Name()
     * @model type="java.lang.String" unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='name' namespace='##targetNamespace' group='#group:3'"
     * @generated
     */
    EList getName();

    /**
     * Returns the value of the '<em><b>Target Name</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Target Name</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Target Name</em>' attribute list.
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getGbeanRefType_TargetName()
     * @model type="java.lang.String" unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='target-name' namespace='##targetNamespace' group='#group:3'"
     * @generated
     */
    EList getTargetName();

} // GbeanRefType
