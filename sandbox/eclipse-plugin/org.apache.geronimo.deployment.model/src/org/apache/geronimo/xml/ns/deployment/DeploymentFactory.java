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
package org.apache.geronimo.xml.ns.deployment;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage
 * @generated
 */
public interface DeploymentFactory extends EFactory {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

    /**
     * The singleton instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    DeploymentFactory eINSTANCE = new org.apache.geronimo.xml.ns.deployment.impl.DeploymentFactoryImpl();

    /**
     * Returns a new object of class '<em>Attribute Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Attribute Type</em>'.
     * @generated
     */
    AttributeType createAttributeType();

    /**
     * Returns a new object of class '<em>Configuration Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Configuration Type</em>'.
     * @generated
     */
    ConfigurationType createConfigurationType();

    /**
     * Returns a new object of class '<em>Dependency Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Dependency Type</em>'.
     * @generated
     */
    DependencyType createDependencyType();

    /**
     * Returns a new object of class '<em>Document Root</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Document Root</em>'.
     * @generated
     */
    DocumentRoot createDocumentRoot();

    /**
     * Returns a new object of class '<em>Gbean Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Gbean Type</em>'.
     * @generated
     */
    GbeanType createGbeanType();

    /**
     * Returns a new object of class '<em>Pattern Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Pattern Type</em>'.
     * @generated
     */
    PatternType createPatternType();

    /**
     * Returns a new object of class '<em>References Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>References Type</em>'.
     * @generated
     */
    ReferencesType createReferencesType();

    /**
     * Returns a new object of class '<em>Reference Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Reference Type</em>'.
     * @generated
     */
    ReferenceType createReferenceType();

    /**
     * Returns a new object of class '<em>Xml Attribute Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Xml Attribute Type</em>'.
     * @generated
     */
    XmlAttributeType createXmlAttributeType();

    /**
     * Returns the package supported by this factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the package supported by this factory.
     * @generated
     */
    DeploymentPackage getDeploymentPackage();

} //DeploymentFactory
