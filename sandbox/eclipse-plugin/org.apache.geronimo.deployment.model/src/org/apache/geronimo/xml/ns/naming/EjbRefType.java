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
 * A representation of the model object '<em><b>Ejb Ref Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getRefName <em>Ref Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getDomain <em>Domain</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getServer <em>Server</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getApplication <em>Application</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getModule <em>Module</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getType <em>Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getName <em>Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getNsCorbaloc <em>Ns Corbaloc</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getName1 <em>Name1</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getCss <em>Css</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getCssLink <em>Css Link</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getCssName <em>Css Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getEjbLink <em>Ejb Link</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getTargetName <em>Target Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType()
 * @model extendedMetaData="name='ejb-refType' kind='elementOnly'"
 * @generated
 */
public interface EjbRefType extends EObject {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_RefName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='ref-name' namespace='##targetNamespace'"
     * @generated
     */
    String getRefName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getRefName <em>Ref Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ref Name</em>' attribute.
     * @see #getRefName()
     * @generated
     */
    void setRefName(String value);

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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_Domain()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='domain' namespace='##targetNamespace'"
     * @generated
     */
    String getDomain();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getDomain <em>Domain</em>}' attribute.
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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_Server()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='server' namespace='##targetNamespace'"
     * @generated
     */
    String getServer();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getServer <em>Server</em>}' attribute.
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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_Application()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='application' namespace='##targetNamespace'"
     * @generated
     */
    String getApplication();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getApplication <em>Application</em>}' attribute.
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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_Module()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='module' namespace='##targetNamespace'"
     * @generated
     */
    String getModule();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getModule <em>Module</em>}' attribute.
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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_Type()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='type' namespace='##targetNamespace'"
     * @generated
     */
    String getType();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getType <em>Type</em>}' attribute.
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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_Name()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='name' namespace='##targetNamespace'"
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Ns Corbaloc</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * 
     *                         The corbaloc used to access the CORBA name server.
     *                     
     * <!-- end-model-doc -->
     * @return the value of the '<em>Ns Corbaloc</em>' attribute.
     * @see #setNsCorbaloc(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_NsCorbaloc()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
     *        extendedMetaData="kind='element' name='ns-corbaloc' namespace='##targetNamespace'"
     * @generated
     */
    String getNsCorbaloc();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getNsCorbaloc <em>Ns Corbaloc</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ns Corbaloc</em>' attribute.
     * @see #getNsCorbaloc()
     * @generated
     */
    void setNsCorbaloc(String value);

    /**
     * Returns the value of the '<em><b>Name1</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * 
     *                         The name of the object in the name server.
     *                     
     * <!-- end-model-doc -->
     * @return the value of the '<em>Name1</em>' attribute.
     * @see #setName1(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_Name1()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='name' namespace='##targetNamespace'"
     * @generated
     */
    String getName1();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getName1 <em>Name1</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name1</em>' attribute.
     * @see #getName1()
     * @generated
     */
    void setName1(String value);

    /**
     * Returns the value of the '<em><b>Css</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Css</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Css</em>' containment reference.
     * @see #setCss(CssType)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_Css()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='css' namespace='##targetNamespace'"
     * @generated
     */
    CssType getCss();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getCss <em>Css</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Css</em>' containment reference.
     * @see #getCss()
     * @generated
     */
    void setCss(CssType value);

    /**
     * Returns the value of the '<em><b>Css Link</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Css Link</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Css Link</em>' attribute.
     * @see #setCssLink(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_CssLink()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='css-link' namespace='##targetNamespace'"
     * @generated
     */
    String getCssLink();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getCssLink <em>Css Link</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Css Link</em>' attribute.
     * @see #getCssLink()
     * @generated
     */
    void setCssLink(String value);

    /**
     * Returns the value of the '<em><b>Css Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * 
     *                         The GBean name of the client security server used to make interop calls.
     *                         
     * <!-- end-model-doc -->
     * @return the value of the '<em>Css Name</em>' attribute.
     * @see #setCssName(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_CssName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='css-name' namespace='##targetNamespace'"
     * @generated
     */
    String getCssName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getCssName <em>Css Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Css Name</em>' attribute.
     * @see #getCssName()
     * @generated
     */
    void setCssName(String value);

    /**
     * Returns the value of the '<em><b>Ejb Link</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb Link</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb Link</em>' attribute.
     * @see #setEjbLink(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_EjbLink()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='ejb-link' namespace='##targetNamespace'"
     * @generated
     */
    String getEjbLink();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getEjbLink <em>Ejb Link</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ejb Link</em>' attribute.
     * @see #getEjbLink()
     * @generated
     */
    void setEjbLink(String value);

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
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getEjbRefType_TargetName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='target-name' namespace='##targetNamespace'"
     * @generated
     */
    String getTargetName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getTargetName <em>Target Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Target Name</em>' attribute.
     * @see #getTargetName()
     * @generated
     */
    void setTargetName(String value);

} // EjbRefType
