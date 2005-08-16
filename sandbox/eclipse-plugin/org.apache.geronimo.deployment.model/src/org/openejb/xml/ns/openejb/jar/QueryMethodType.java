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
package org.openejb.xml.ns.openejb.jar;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Query Method Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.QueryMethodType#getMethodName <em>Method Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.QueryMethodType#getMethodParams <em>Method Params</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getQueryMethodType()
 * @model extendedMetaData="name='query-method_._type' kind='elementOnly'"
 * @generated
 */
public interface QueryMethodType extends EObject {
    /**
     * Returns the value of the '<em><b>Method Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Method Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Method Name</em>' attribute.
     * @see #setMethodName(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getQueryMethodType_MethodName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='method-name' namespace='##targetNamespace'"
     * @generated
     */
    String getMethodName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.QueryMethodType#getMethodName <em>Method Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Method Name</em>' attribute.
     * @see #getMethodName()
     * @generated
     */
    void setMethodName(String value);

    /**
     * Returns the value of the '<em><b>Method Params</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Method Params</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Method Params</em>' containment reference.
     * @see #setMethodParams(MethodParamsType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getQueryMethodType_MethodParams()
     * @model containment="true" resolveProxies="false" required="true"
     *        extendedMetaData="kind='element' name='method-params' namespace='##targetNamespace'"
     * @generated
     */
    MethodParamsType getMethodParams();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.QueryMethodType#getMethodParams <em>Method Params</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Method Params</em>' containment reference.
     * @see #getMethodParams()
     * @generated
     */
    void setMethodParams(MethodParamsType value);

} // QueryMethodType
