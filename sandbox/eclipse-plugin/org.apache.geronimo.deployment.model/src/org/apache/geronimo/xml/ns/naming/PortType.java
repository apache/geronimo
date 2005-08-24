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
 * A representation of the model object '<em><b>Port Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.PortType#getPortName <em>Port Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.PortType#getProtocol <em>Protocol</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.PortType#getHost <em>Host</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.PortType#getPort <em>Port</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.PortType#getUri <em>Uri</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.PortType#getCredentialsName <em>Credentials Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getPortType()
 * @model extendedMetaData="name='portType' kind='elementOnly'"
 * @generated
 */
public interface PortType extends EObject{
    /**
     * Returns the value of the '<em><b>Port Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Port Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Port Name</em>' attribute.
     * @see #setPortName(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getPortType_PortName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='port-name' namespace='##targetNamespace'"
     * @generated
     */
    String getPortName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.PortType#getPortName <em>Port Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Port Name</em>' attribute.
     * @see #getPortName()
     * @generated
     */
    void setPortName(String value);

    /**
     * Returns the value of the '<em><b>Protocol</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Protocol</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Protocol</em>' attribute.
     * @see #setProtocol(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getPortType_Protocol()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='protocol' namespace='##targetNamespace'"
     * @generated
     */
    String getProtocol();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.PortType#getProtocol <em>Protocol</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Protocol</em>' attribute.
     * @see #getProtocol()
     * @generated
     */
    void setProtocol(String value);

    /**
     * Returns the value of the '<em><b>Host</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Host</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Host</em>' attribute.
     * @see #setHost(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getPortType_Host()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='host' namespace='##targetNamespace'"
     * @generated
     */
    String getHost();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.PortType#getHost <em>Host</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Host</em>' attribute.
     * @see #getHost()
     * @generated
     */
    void setHost(String value);

    /**
     * Returns the value of the '<em><b>Port</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Port</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Port</em>' attribute.
     * @see #isSetPort()
     * @see #unsetPort()
     * @see #setPort(int)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getPortType_Port()
     * @model unique="false" unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Int"
     *        extendedMetaData="kind='element' name='port' namespace='##targetNamespace'"
     * @generated
     */
    int getPort();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.PortType#getPort <em>Port</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Port</em>' attribute.
     * @see #isSetPort()
     * @see #unsetPort()
     * @see #getPort()
     * @generated
     */
    void setPort(int value);

    /**
     * Unsets the value of the '{@link org.apache.geronimo.xml.ns.naming.PortType#getPort <em>Port</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSetPort()
     * @see #getPort()
     * @see #setPort(int)
     * @generated
     */
    void unsetPort();

    /**
     * Returns whether the value of the '{@link org.apache.geronimo.xml.ns.naming.PortType#getPort <em>Port</em>}' attribute is set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return whether the value of the '<em>Port</em>' attribute is set.
     * @see #unsetPort()
     * @see #getPort()
     * @see #setPort(int)
     * @generated
     */
    boolean isSetPort();

    /**
     * Returns the value of the '<em><b>Uri</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Uri</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Uri</em>' attribute.
     * @see #setUri(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getPortType_Uri()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='uri' namespace='##targetNamespace'"
     * @generated
     */
    String getUri();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.PortType#getUri <em>Uri</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Uri</em>' attribute.
     * @see #getUri()
     * @generated
     */
    void setUri(String value);

    /**
     * Returns the value of the '<em><b>Credentials Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Credentials Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Credentials Name</em>' attribute.
     * @see #setCredentialsName(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getPortType_CredentialsName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='credentials-name' namespace='##targetNamespace'"
     * @generated
     */
    String getCredentialsName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.PortType#getCredentialsName <em>Credentials Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Credentials Name</em>' attribute.
     * @see #getCredentialsName()
     * @generated
     */
    void setCredentialsName(String value);

} // PortType
