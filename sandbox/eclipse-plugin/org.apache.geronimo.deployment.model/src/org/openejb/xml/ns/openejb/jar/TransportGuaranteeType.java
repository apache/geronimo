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
package org.openejb.xml.ns.openejb.jar;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.AbstractEnumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Transport Guarantee Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getTransportGuaranteeType()
 * @model
 * @generated
 */
public final class TransportGuaranteeType extends AbstractEnumerator {
    /**
     * The '<em><b>NONE</b></em>' literal value.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>NONE</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @see #NONE_LITERAL
     * @model
     * @generated
     * @ordered
     */
    public static final int NONE = 0;

    /**
     * The '<em><b>INTEGRAL</b></em>' literal value.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>INTEGRAL</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @see #INTEGRAL_LITERAL
     * @model
     * @generated
     * @ordered
     */
    public static final int INTEGRAL = 1;

    /**
     * The '<em><b>CONFIDENTIAL</b></em>' literal value.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>CONFIDENTIAL</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @see #CONFIDENTIAL_LITERAL
     * @model
     * @generated
     * @ordered
     */
    public static final int CONFIDENTIAL = 2;

    /**
     * The '<em><b>NONE</b></em>' literal object.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #NONE
     * @generated
     * @ordered
     */
    public static final TransportGuaranteeType NONE_LITERAL = new TransportGuaranteeType(NONE, "NONE");

    /**
     * The '<em><b>INTEGRAL</b></em>' literal object.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #INTEGRAL
     * @generated
     * @ordered
     */
    public static final TransportGuaranteeType INTEGRAL_LITERAL = new TransportGuaranteeType(INTEGRAL, "INTEGRAL");

    /**
     * The '<em><b>CONFIDENTIAL</b></em>' literal object.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #CONFIDENTIAL
     * @generated
     * @ordered
     */
    public static final TransportGuaranteeType CONFIDENTIAL_LITERAL = new TransportGuaranteeType(CONFIDENTIAL, "CONFIDENTIAL");

    /**
     * An array of all the '<em><b>Transport Guarantee Type</b></em>' enumerators.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private static final TransportGuaranteeType[] VALUES_ARRAY =
        new TransportGuaranteeType[] {
            NONE_LITERAL,
            INTEGRAL_LITERAL,
            CONFIDENTIAL_LITERAL,
        };

    /**
     * A public read-only list of all the '<em><b>Transport Guarantee Type</b></em>' enumerators.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static final List VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

    /**
     * Returns the '<em><b>Transport Guarantee Type</b></em>' literal with the specified name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static TransportGuaranteeType get(String name) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            TransportGuaranteeType result = VALUES_ARRAY[i];
            if (result.toString().equals(name)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the '<em><b>Transport Guarantee Type</b></em>' literal with the specified value.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static TransportGuaranteeType get(int value) {
        switch (value) {
            case NONE: return NONE_LITERAL;
            case INTEGRAL: return INTEGRAL_LITERAL;
            case CONFIDENTIAL: return CONFIDENTIAL_LITERAL;
        }
        return null;	
    }

    /**
     * Only this class can construct instances.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private TransportGuaranteeType(int value, String name) {
        super(value, name);
    }

} //TransportGuaranteeType
