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

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Custom Generator Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *                 Handles a user-provided generator.  You deploy any old generator
 *                 as a GBean, and then point to that GBean here.  The generator
 *                 should implement org.tranql.pkgenerator.PrimaryKeyGenerator.
 *             
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.pkgen.CustomGeneratorType#getGeneratorName <em>Generator Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.pkgen.CustomGeneratorType#getPrimaryKeyClass <em>Primary Key Class</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.pkgen.PkgenPackage#getCustomGeneratorType()
 * @model extendedMetaData="name='custom-generatorType' kind='elementOnly'"
 * @generated
 */
public interface CustomGeneratorType extends EObject {
    /**
     * Returns the value of the '<em><b>Generator Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Generator Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Generator Name</em>' attribute.
     * @see #setGeneratorName(String)
     * @see org.openejb.xml.ns.pkgen.PkgenPackage#getCustomGeneratorType_GeneratorName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='generator-name' namespace='##targetNamespace'"
     * @generated
     */
    String getGeneratorName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.pkgen.CustomGeneratorType#getGeneratorName <em>Generator Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Generator Name</em>' attribute.
     * @see #getGeneratorName()
     * @generated
     */
    void setGeneratorName(String value);

    /**
     * Returns the value of the '<em><b>Primary Key Class</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Primary Key Class</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Primary Key Class</em>' attribute.
     * @see #setPrimaryKeyClass(String)
     * @see org.openejb.xml.ns.pkgen.PkgenPackage#getCustomGeneratorType_PrimaryKeyClass()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='primary-key-class' namespace='##targetNamespace'"
     * @generated
     */
    String getPrimaryKeyClass();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.pkgen.CustomGeneratorType#getPrimaryKeyClass <em>Primary Key Class</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Primary Key Class</em>' attribute.
     * @see #getPrimaryKeyClass()
     * @generated
     */
    void setPrimaryKeyClass(String value);

} // CustomGeneratorType
