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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Method Params Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.MethodParamsType#getMethodParam <em>Method Param</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getMethodParamsType()
 * @model extendedMetaData="name='method-params_._type' kind='elementOnly'"
 * @generated
 */
public interface MethodParamsType extends EObject {
    /**
     * Returns the value of the '<em><b>Method Param</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Method Param</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Method Param</em>' attribute list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getMethodParamsType_MethodParam()
     * @model type="java.lang.String" unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='method-param' namespace='##targetNamespace'"
     * @generated
     */
    EList getMethodParam();

} // MethodParamsType
