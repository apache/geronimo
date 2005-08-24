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
package org.apache.geronimo.xml.ns.j2ee.application.impl;

import org.apache.geronimo.xml.ns.j2ee.application.ApplicationFactory;
import org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage;
import org.apache.geronimo.xml.ns.j2ee.application.ApplicationType;
import org.apache.geronimo.xml.ns.j2ee.application.DocumentRoot;
import org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType;
import org.apache.geronimo.xml.ns.j2ee.application.ModuleType;
import org.apache.geronimo.xml.ns.j2ee.application.PathType;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ApplicationFactoryImpl extends EFactoryImpl implements ApplicationFactory {
    /**
     * Creates an instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ApplicationFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case ApplicationPackage.APPLICATION_TYPE: return createApplicationType();
            case ApplicationPackage.DOCUMENT_ROOT: return createDocumentRoot();
            case ApplicationPackage.EXT_MODULE_TYPE: return createExtModuleType();
            case ApplicationPackage.MODULE_TYPE: return createModuleType();
            case ApplicationPackage.PATH_TYPE: return createPathType();
            case ApplicationPackage.STRING: return createString();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ApplicationType createApplicationType() {
        ApplicationTypeImpl applicationType = new ApplicationTypeImpl();
        return applicationType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DocumentRoot createDocumentRoot() {
        DocumentRootImpl documentRoot = new DocumentRootImpl();
        return documentRoot;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ExtModuleType createExtModuleType() {
        ExtModuleTypeImpl extModuleType = new ExtModuleTypeImpl();
        return extModuleType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ModuleType createModuleType() {
        ModuleTypeImpl moduleType = new ModuleTypeImpl();
        return moduleType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PathType createPathType() {
        PathTypeImpl pathType = new PathTypeImpl();
        return pathType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public org.apache.geronimo.xml.ns.j2ee.application.String createString() {
        StringImpl string = new StringImpl();
        return string;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ApplicationPackage getApplicationPackage() {
        return (ApplicationPackage)getEPackage();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @deprecated
     * @generated
     */
    public static ApplicationPackage getPackage() {
        return ApplicationPackage.eINSTANCE;
    }

} //ApplicationFactoryImpl
