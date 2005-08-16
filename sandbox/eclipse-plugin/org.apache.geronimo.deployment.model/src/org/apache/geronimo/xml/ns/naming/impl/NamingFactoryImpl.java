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
package org.apache.geronimo.xml.ns.naming.impl;

import org.apache.geronimo.xml.ns.naming.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class NamingFactoryImpl extends EFactoryImpl implements NamingFactory {
    /**
     * Creates an instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NamingFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case NamingPackage.CSS_TYPE: return createCssType();
            case NamingPackage.DOCUMENT_ROOT: return createDocumentRoot();
            case NamingPackage.EJB_LOCAL_REF_TYPE: return createEjbLocalRefType();
            case NamingPackage.EJB_REF_TYPE: return createEjbRefType();
            case NamingPackage.GBEAN_LOCATOR_TYPE: return createGbeanLocatorType();
            case NamingPackage.GBEAN_REF_TYPE: return createGbeanRefType();
            case NamingPackage.PORT_COMPLETION_TYPE: return createPortCompletionType();
            case NamingPackage.PORT_TYPE: return createPortType();
            case NamingPackage.RESOURCE_ENV_REF_TYPE: return createResourceEnvRefType();
            case NamingPackage.RESOURCE_LOCATOR_TYPE: return createResourceLocatorType();
            case NamingPackage.RESOURCE_REF_TYPE: return createResourceRefType();
            case NamingPackage.SERVICE_COMPLETION_TYPE: return createServiceCompletionType();
            case NamingPackage.SERVICE_REF_TYPE: return createServiceRefType();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CssType createCssType() {
        CssTypeImpl cssType = new CssTypeImpl();
        return cssType;
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
    public EjbLocalRefType createEjbLocalRefType() {
        EjbLocalRefTypeImpl ejbLocalRefType = new EjbLocalRefTypeImpl();
        return ejbLocalRefType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EjbRefType createEjbRefType() {
        EjbRefTypeImpl ejbRefType = new EjbRefTypeImpl();
        return ejbRefType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public GbeanLocatorType createGbeanLocatorType() {
        GbeanLocatorTypeImpl gbeanLocatorType = new GbeanLocatorTypeImpl();
        return gbeanLocatorType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public GbeanRefType createGbeanRefType() {
        GbeanRefTypeImpl gbeanRefType = new GbeanRefTypeImpl();
        return gbeanRefType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PortCompletionType createPortCompletionType() {
        PortCompletionTypeImpl portCompletionType = new PortCompletionTypeImpl();
        return portCompletionType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PortType createPortType() {
        PortTypeImpl portType = new PortTypeImpl();
        return portType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ResourceEnvRefType createResourceEnvRefType() {
        ResourceEnvRefTypeImpl resourceEnvRefType = new ResourceEnvRefTypeImpl();
        return resourceEnvRefType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ResourceLocatorType createResourceLocatorType() {
        ResourceLocatorTypeImpl resourceLocatorType = new ResourceLocatorTypeImpl();
        return resourceLocatorType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ResourceRefType createResourceRefType() {
        ResourceRefTypeImpl resourceRefType = new ResourceRefTypeImpl();
        return resourceRefType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ServiceCompletionType createServiceCompletionType() {
        ServiceCompletionTypeImpl serviceCompletionType = new ServiceCompletionTypeImpl();
        return serviceCompletionType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ServiceRefType createServiceRefType() {
        ServiceRefTypeImpl serviceRefType = new ServiceRefTypeImpl();
        return serviceRefType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NamingPackage getNamingPackage() {
        return (NamingPackage)getEPackage();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @deprecated
     * @generated
     */
    public static NamingPackage getPackage() {
        return NamingPackage.eINSTANCE;
    }

} //NamingFactoryImpl
