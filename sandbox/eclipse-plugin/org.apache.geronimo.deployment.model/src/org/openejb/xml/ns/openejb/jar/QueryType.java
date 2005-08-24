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
 * A representation of the model object '<em><b>Query Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.QueryType#getQueryMethod <em>Query Method</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.QueryType#getResultTypeMapping <em>Result Type Mapping</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.QueryType#getEjbQl <em>Ejb Ql</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.QueryType#getNoCacheFlush <em>No Cache Flush</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.QueryType#getGroupName <em>Group Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getQueryType()
 * @model extendedMetaData="name='queryType' kind='elementOnly'"
 * @generated
 */
public interface QueryType extends EObject {
    /**
     * Returns the value of the '<em><b>Query Method</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Query Method</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Query Method</em>' containment reference.
     * @see #setQueryMethod(QueryMethodType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getQueryType_QueryMethod()
     * @model containment="true" resolveProxies="false" required="true"
     *        extendedMetaData="kind='element' name='query-method' namespace='##targetNamespace'"
     * @generated
     */
    QueryMethodType getQueryMethod();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.QueryType#getQueryMethod <em>Query Method</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Query Method</em>' containment reference.
     * @see #getQueryMethod()
     * @generated
     */
    void setQueryMethod(QueryMethodType value);

    /**
     * Returns the value of the '<em><b>Result Type Mapping</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Result Type Mapping</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Result Type Mapping</em>' attribute.
     * @see #setResultTypeMapping(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getQueryType_ResultTypeMapping()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='result-type-mapping' namespace='##targetNamespace'"
     * @generated
     */
    String getResultTypeMapping();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.QueryType#getResultTypeMapping <em>Result Type Mapping</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Result Type Mapping</em>' attribute.
     * @see #getResultTypeMapping()
     * @generated
     */
    void setResultTypeMapping(String value);

    /**
     * Returns the value of the '<em><b>Ejb Ql</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb Ql</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb Ql</em>' attribute.
     * @see #setEjbQl(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getQueryType_EjbQl()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='ejb-ql' namespace='##targetNamespace'"
     * @generated
     */
    String getEjbQl();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.QueryType#getEjbQl <em>Ejb Ql</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ejb Ql</em>' attribute.
     * @see #getEjbQl()
     * @generated
     */
    void setEjbQl(String value);

    /**
     * Returns the value of the '<em><b>No Cache Flush</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>No Cache Flush</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>No Cache Flush</em>' containment reference.
     * @see #setNoCacheFlush(EObject)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getQueryType_NoCacheFlush()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='no-cache-flush' namespace='##targetNamespace'"
     * @generated
     */
    EObject getNoCacheFlush();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.QueryType#getNoCacheFlush <em>No Cache Flush</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>No Cache Flush</em>' containment reference.
     * @see #getNoCacheFlush()
     * @generated
     */
    void setNoCacheFlush(EObject value);

    /**
     * Returns the value of the '<em><b>Group Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Group Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Group Name</em>' attribute.
     * @see #setGroupName(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getQueryType_GroupName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='group-name' namespace='##targetNamespace'"
     * @generated
     */
    String getGroupName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.QueryType#getGroupName <em>Group Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Group Name</em>' attribute.
     * @see #getGroupName()
     * @generated
     */
    void setGroupName(String value);

} // QueryType
