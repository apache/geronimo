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
package org.apache.geronimo.xml.ns.deployment.util;

import org.apache.geronimo.xml.ns.deployment.*;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage
 * @generated
 */
public class DeploymentAdapterFactory extends AdapterFactoryImpl {
    /**
     * The cached model package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static DeploymentPackage modelPackage;

    /**
     * Creates an instance of the adapter factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DeploymentAdapterFactory() {
        if (modelPackage == null) {
            modelPackage = DeploymentPackage.eINSTANCE;
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
    protected DeploymentSwitch modelSwitch =
        new DeploymentSwitch() {
            public Object caseAttributeType(AttributeType object) {
                return createAttributeTypeAdapter();
            }
            public Object caseConfigurationType(ConfigurationType object) {
                return createConfigurationTypeAdapter();
            }
            public Object caseDependencyType(DependencyType object) {
                return createDependencyTypeAdapter();
            }
            public Object caseDocumentRoot(DocumentRoot object) {
                return createDocumentRootAdapter();
            }
            public Object caseGbeanType(GbeanType object) {
                return createGbeanTypeAdapter();
            }
            public Object casePatternType(PatternType object) {
                return createPatternTypeAdapter();
            }
            public Object caseReferencesType(ReferencesType object) {
                return createReferencesTypeAdapter();
            }
            public Object caseReferenceType(ReferenceType object) {
                return createReferenceTypeAdapter();
            }
            public Object caseXmlAttributeType(XmlAttributeType object) {
                return createXmlAttributeTypeAdapter();
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
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.deployment.AttributeType <em>Attribute Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.deployment.AttributeType
     * @generated
     */
    public Adapter createAttributeTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType <em>Configuration Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.deployment.ConfigurationType
     * @generated
     */
    public Adapter createConfigurationTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.deployment.DependencyType <em>Dependency Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.deployment.DependencyType
     * @generated
     */
    public Adapter createDependencyTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.deployment.DocumentRoot <em>Document Root</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.deployment.DocumentRoot
     * @generated
     */
    public Adapter createDocumentRootAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.deployment.GbeanType <em>Gbean Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.deployment.GbeanType
     * @generated
     */
    public Adapter createGbeanTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.deployment.PatternType <em>Pattern Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.deployment.PatternType
     * @generated
     */
    public Adapter createPatternTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.deployment.ReferencesType <em>References Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.deployment.ReferencesType
     * @generated
     */
    public Adapter createReferencesTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.deployment.ReferenceType <em>Reference Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.deployment.ReferenceType
     * @generated
     */
    public Adapter createReferenceTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.apache.geronimo.xml.ns.deployment.XmlAttributeType <em>Xml Attribute Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.apache.geronimo.xml.ns.deployment.XmlAttributeType
     * @generated
     */
    public Adapter createXmlAttributeTypeAdapter() {
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

} //DeploymentAdapterFactory
