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

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.apache.geronimo.xml.ns.naming.NamingPackage
 * @generated
 */
public interface NamingFactory extends EFactory{
    /**
     * The singleton instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    NamingFactory eINSTANCE = new org.apache.geronimo.xml.ns.naming.impl.NamingFactoryImpl();

    /**
     * Returns a new object of class '<em>Css Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Css Type</em>'.
     * @generated
     */
    CssType createCssType();

    /**
     * Returns a new object of class '<em>Document Root</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Document Root</em>'.
     * @generated
     */
    DocumentRoot createDocumentRoot();

    /**
     * Returns a new object of class '<em>Ejb Local Ref Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Ejb Local Ref Type</em>'.
     * @generated
     */
    EjbLocalRefType createEjbLocalRefType();

    /**
     * Returns a new object of class '<em>Ejb Ref Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Ejb Ref Type</em>'.
     * @generated
     */
    EjbRefType createEjbRefType();

    /**
     * Returns a new object of class '<em>Gbean Locator Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Gbean Locator Type</em>'.
     * @generated
     */
    GbeanLocatorType createGbeanLocatorType();

    /**
     * Returns a new object of class '<em>Gbean Ref Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Gbean Ref Type</em>'.
     * @generated
     */
    GbeanRefType createGbeanRefType();

    /**
     * Returns a new object of class '<em>Port Completion Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Port Completion Type</em>'.
     * @generated
     */
    PortCompletionType createPortCompletionType();

    /**
     * Returns a new object of class '<em>Port Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Port Type</em>'.
     * @generated
     */
    PortType createPortType();

    /**
     * Returns a new object of class '<em>Resource Env Ref Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Resource Env Ref Type</em>'.
     * @generated
     */
    ResourceEnvRefType createResourceEnvRefType();

    /**
     * Returns a new object of class '<em>Resource Locator Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Resource Locator Type</em>'.
     * @generated
     */
    ResourceLocatorType createResourceLocatorType();

    /**
     * Returns a new object of class '<em>Resource Ref Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Resource Ref Type</em>'.
     * @generated
     */
    ResourceRefType createResourceRefType();

    /**
     * Returns a new object of class '<em>Service Completion Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Service Completion Type</em>'.
     * @generated
     */
    ServiceCompletionType createServiceCompletionType();

    /**
     * Returns a new object of class '<em>Service Ref Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Service Ref Type</em>'.
     * @generated
     */
    ServiceRefType createServiceRefType();

    /**
     * Returns the package supported by this factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the package supported by this factory.
     * @generated
     */
    NamingPackage getNamingPackage();

} //NamingFactory
