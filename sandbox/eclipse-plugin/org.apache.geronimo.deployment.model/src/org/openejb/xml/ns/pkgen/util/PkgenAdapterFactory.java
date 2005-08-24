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
package org.openejb.xml.ns.pkgen.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.openejb.xml.ns.pkgen.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.openejb.xml.ns.pkgen.PkgenPackage
 * @generated
 */
public class PkgenAdapterFactory extends AdapterFactoryImpl {
    /**
     * The cached model package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static PkgenPackage modelPackage;

    /**
     * Creates an instance of the adapter factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PkgenAdapterFactory() {
        if (modelPackage == null) {
            modelPackage = PkgenPackage.eINSTANCE;
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
    protected PkgenSwitch modelSwitch =
        new PkgenSwitch() {
            public Object caseAutoIncrementTableType(AutoIncrementTableType object) {
                return createAutoIncrementTableTypeAdapter();
            }
            public Object caseCustomGeneratorType(CustomGeneratorType object) {
                return createCustomGeneratorTypeAdapter();
            }
            public Object caseDatabaseGeneratedType(DatabaseGeneratedType object) {
                return createDatabaseGeneratedTypeAdapter();
            }
            public Object caseDocumentRoot(DocumentRoot object) {
                return createDocumentRootAdapter();
            }
            public Object caseKeyGeneratorType(KeyGeneratorType object) {
                return createKeyGeneratorTypeAdapter();
            }
            public Object caseSequenceTableType(SequenceTableType object) {
                return createSequenceTableTypeAdapter();
            }
            public Object caseSqlGeneratorType(SqlGeneratorType object) {
                return createSqlGeneratorTypeAdapter();
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
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.pkgen.AutoIncrementTableType <em>Auto Increment Table Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.pkgen.AutoIncrementTableType
     * @generated
     */
    public Adapter createAutoIncrementTableTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.pkgen.CustomGeneratorType <em>Custom Generator Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.pkgen.CustomGeneratorType
     * @generated
     */
    public Adapter createCustomGeneratorTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.pkgen.DatabaseGeneratedType <em>Database Generated Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.pkgen.DatabaseGeneratedType
     * @generated
     */
    public Adapter createDatabaseGeneratedTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.pkgen.DocumentRoot <em>Document Root</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.pkgen.DocumentRoot
     * @generated
     */
    public Adapter createDocumentRootAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.pkgen.KeyGeneratorType <em>Key Generator Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.pkgen.KeyGeneratorType
     * @generated
     */
    public Adapter createKeyGeneratorTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.pkgen.SequenceTableType <em>Sequence Table Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.pkgen.SequenceTableType
     * @generated
     */
    public Adapter createSequenceTableTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.pkgen.SqlGeneratorType <em>Sql Generator Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.pkgen.SqlGeneratorType
     * @generated
     */
    public Adapter createSqlGeneratorTypeAdapter() {
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

} //PkgenAdapterFactory
