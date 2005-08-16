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

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Service Ref Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ServiceRefType#getServiceRefName <em>Service Ref Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ServiceRefType#getServiceCompletion <em>Service Completion</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ServiceRefType#getPort <em>Port</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getServiceRefType()
 * @model extendedMetaData="name='service-refType' kind='elementOnly'"
 * @generated
 */
public interface ServiceRefType extends EObject{
    /**
     * Returns the value of the '<em><b>Service Ref Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Service Ref Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Service Ref Name</em>' attribute.
     * @see #setServiceRefName(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getServiceRefType_ServiceRefName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='service-ref-name' namespace='##targetNamespace'"
     * @generated
     */
    String getServiceRefName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.ServiceRefType#getServiceRefName <em>Service Ref Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Service Ref Name</em>' attribute.
     * @see #getServiceRefName()
     * @generated
     */
    void setServiceRefName(String value);

    /**
     * Returns the value of the '<em><b>Service Completion</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Service Completion</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Service Completion</em>' containment reference.
     * @see #setServiceCompletion(ServiceCompletionType)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getServiceRefType_ServiceCompletion()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='service-completion' namespace='##targetNamespace'"
     * @generated
     */
    ServiceCompletionType getServiceCompletion();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.ServiceRefType#getServiceCompletion <em>Service Completion</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Service Completion</em>' containment reference.
     * @see #getServiceCompletion()
     * @generated
     */
    void setServiceCompletion(ServiceCompletionType value);

    /**
     * Returns the value of the '<em><b>Port</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.naming.PortType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Port</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Port</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getServiceRefType_Port()
     * @model type="org.apache.geronimo.xml.ns.naming.PortType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='port' namespace='##targetNamespace'"
     * @generated
     */
    EList getPort();

} // ServiceRefType
