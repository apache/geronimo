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
package org.apache.geronimo.xml.ns.web.impl;

import org.apache.geronimo.xml.ns.web.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class WebFactoryImpl extends EFactoryImpl implements WebFactory {
    /**
     * Creates an instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public WebFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case WebPackage.CONFIG_PARAM_TYPE: return createConfigParamType();
            case WebPackage.CONTAINER_CONFIG_TYPE: return createContainerConfigType();
            case WebPackage.DOCUMENT_ROOT: return createDocumentRoot();
            case WebPackage.WEB_APP_TYPE: return createWebAppType();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object createFromString(EDataType eDataType, String initialValue) {
        switch (eDataType.getClassifierID()) {
            case WebPackage.WEB_CONTAINER_TYPE: {
                WebContainerType result = WebContainerType.get(initialValue);
                if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
                return result;
            }
            case WebPackage.WEB_CONTAINER_TYPE_OBJECT:
                return createWebContainerTypeObjectFromString(eDataType, initialValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertToString(EDataType eDataType, Object instanceValue) {
        switch (eDataType.getClassifierID()) {
            case WebPackage.WEB_CONTAINER_TYPE:
                return instanceValue == null ? null : instanceValue.toString();
            case WebPackage.WEB_CONTAINER_TYPE_OBJECT:
                return convertWebContainerTypeObjectToString(eDataType, instanceValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ConfigParamType createConfigParamType() {
        ConfigParamTypeImpl configParamType = new ConfigParamTypeImpl();
        return configParamType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ContainerConfigType createContainerConfigType() {
        ContainerConfigTypeImpl containerConfigType = new ContainerConfigTypeImpl();
        return containerConfigType;
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
    public WebAppType createWebAppType() {
        WebAppTypeImpl webAppType = new WebAppTypeImpl();
        return webAppType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public WebContainerType createWebContainerTypeObjectFromString(EDataType eDataType, String initialValue) {
        return (WebContainerType)WebFactory.eINSTANCE.createFromString(WebPackage.eINSTANCE.getWebContainerType(), initialValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertWebContainerTypeObjectToString(EDataType eDataType, Object instanceValue) {
        return WebFactory.eINSTANCE.convertToString(WebPackage.eINSTANCE.getWebContainerType(), instanceValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public WebPackage getWebPackage() {
        return (WebPackage)getEPackage();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @deprecated
     * @generated
     */
    public static WebPackage getPackage() {
        return WebPackage.eINSTANCE;
    }

} //WebFactoryImpl
