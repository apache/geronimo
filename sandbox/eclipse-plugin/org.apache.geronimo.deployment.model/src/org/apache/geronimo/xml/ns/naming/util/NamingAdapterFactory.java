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
package org.apache.geronimo.xml.ns.naming.util;

import org.apache.geronimo.xml.ns.naming.*;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.apache.geronimo.xml.ns.naming.NamingPackage
 * @generated
 */
public class NamingAdapterFactory extends AdapterFactoryImpl {
    /**
     * The cached model package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static NamingPackage modelPackage;

    /**
     * Creates an instance of the adapter factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NamingAdapterFactory() {
        if (modelPackage == null) {
            modelPackage = NamingPackage.eINSTANCE;
        }
    }

    /**
     * Returns whether this factory is applicable for the type of the object.
     * <!-- begin-user-doc -->
     * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
     * <!-- end-user-doc -->
     * @return whether this factory is applicable for the type of the object.
     * @generated
     */
    public boolean isFactoryForType(Object object) {
        if (object == modelPackage) {
            return true;
        }
        if (object instanceof EObject) {
            return ((EObject)object).eClass().getEPackage() == modelPackage;
        }
        return false;
    }

    /**
     * The switch the delegates to the <code>createXXX</code> methods.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected NamingSwitch modelSwitch =
        new NamingSwitch() {
            public Object caseCssType(CssType object) {
                return createCssTypeAdapter();
            }
            public Object caseDocumentRoot(DocumentRoot object) {
                return createDocumentRootAdapter();
            }
            public Object caseEjbLocalRefType(EjbLocalRefType object) {
                return createEjbLocalRefTypeAdapter();
            }
            public Object caseEjbRefType(EjbRefType object) {
                return createEjbRefTypeAdapter();
            }
            public Object caseGbeanLocatorType(GbeanLocatorType object) {
                return createGbeanLocatorTypeAdapter();
            }
            public Object caseGbeanRefType(GbeanRefType object) {
                return createGbeanRefTypeAdapter();
            }
            public Object casePortCompletionType(PortCompletionType object) {
                return createPortCompletionTypeAdapter();
            }
            public Object casePortType(PortType object) {
                return createPortTypeAdapter();
            }
            public Object caseResourceEnvRefType(ResourceEnvRefType object) {
                return createResourceEnvRefTypeAdapter();
            }
            public Object caseResourceLocatorType(ResourceLocatorType object) {
                return createResourceLocatorTypeAdapter();
            }
            public Object caseResourceRefType(ResourceRefType object) {
                return createResourceRefTypeAdapter();
            }
            public Object caseServiceCompletionType(ServiceCompletionType object) {
                return createServiceCompletionTypeAdapter();
            }
            public Object caseServiceRefType(ServiceRefType object) {
                return createServiceRefTypeAdapter();
            }
            public Object defaultCase(EObject object) {
                return createEObjectAdapter();
            }
        };

    /**
     * Creates an adapter for the <code>target</code>.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param target the object to adapt.
     * @return the adapter for the <code>target</code>.
     * @generated
     */
    public Adapter createAdapter(Notifier target) {
        return (Adapter)modelSwitch.doSwitch((EObject)target);
    }


    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.naming.CssType <em>Css Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.naming.CssType
     * @generated
     */
    public Adapter createCssTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot <em>Document Root</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.naming.DocumentRoot
     * @generated
     */
    public Adapter createDocumentRootAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.naming.EjbLocalRefType <em>Ejb Local Ref Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.naming.EjbLocalRefType
     * @generated
     */
    public Adapter createEjbLocalRefTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.naming.EjbRefType <em>Ejb Ref Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType
     * @generated
     */
    public Adapter createEjbRefTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.naming.GbeanLocatorType <em>Gbean Locator Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.naming.GbeanLocatorType
     * @generated
     */
    public Adapter createGbeanLocatorTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType <em>Gbean Ref Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.naming.GbeanRefType
     * @generated
     */
    public Adapter createGbeanRefTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.naming.PortCompletionType <em>Port Completion Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.naming.PortCompletionType
     * @generated
     */
    public Adapter createPortCompletionTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.naming.PortType <em>Port Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.naming.PortType
     * @generated
     */
    public Adapter createPortTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.naming.ResourceEnvRefType <em>Resource Env Ref Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.naming.ResourceEnvRefType
     * @generated
     */
    public Adapter createResourceEnvRefTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType <em>Resource Locator Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.naming.ResourceLocatorType
     * @generated
     */
    public Adapter createResourceLocatorTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.naming.ResourceRefType <em>Resource Ref Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.naming.ResourceRefType
     * @generated
     */
    public Adapter createResourceRefTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.naming.ServiceCompletionType <em>Service Completion Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.naming.ServiceCompletionType
     * @generated
     */
    public Adapter createServiceCompletionTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.naming.ServiceRefType <em>Service Ref Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.naming.ServiceRefType
     * @generated
     */
    public Adapter createServiceRefTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for the default case.
     * <!-- begin-user-doc -->
     * This default implementation returns null.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @generated
     */
    public Adapter createEObjectAdapter() {
        return null;
    }

} //NamingAdapterFactory
