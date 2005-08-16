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
package org.openejb.xml.ns.pkgen;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Database Generated Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *                 Indicates that the database automatically populates a primary key
 *                 ID in the listed column(s).  Typically this is used for columns
 *                 with an AUTO_INCREMENT flag or the equivalent.  This only makes
 *                 sense if this key generator is used for an EJB or something else
 *                 with a corresponding database table (not if it's meant to generate
 *                 unique web session IDs or something like that -- see
 *                 auto-increment-tableType for that case).
 *             
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.pkgen.DatabaseGeneratedType#getIdentityColumn <em>Identity Column</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.pkgen.PkgenPackage#getDatabaseGeneratedType()
 * @model extendedMetaData="name='database-generatedType' kind='elementOnly'"
 * @generated
 */
public interface DatabaseGeneratedType extends EObject {
    /**
     * Returns the value of the '<em><b>Identity Column</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Identity Column</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Identity Column</em>' attribute list.
     * @see org.openejb.xml.ns.pkgen.PkgenPackage#getDatabaseGeneratedType_IdentityColumn()
     * @model type="java.lang.String" unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='identity-column' namespace='##targetNamespace'"
     * @generated
     */
    EList getIdentityColumn();

} // DatabaseGeneratedType
