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
package org.apache.geronimo.xml.ns.web;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.AbstractEnumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Container Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.apache.geronimo.xml.ns.web.WebPackage#getWebContainerType()
 * @model
 * @generated
 */
public final class WebContainerType extends AbstractEnumerator {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static final String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

    /**
     * The '<em><b>Tomcat</b></em>' literal value.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>Tomcat</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @see #TOMCAT_LITERAL
     * @model name="Tomcat"
     * @generated
     * @ordered
     */
    public static final int TOMCAT = 0;

    /**
     * The '<em><b>Jetty</b></em>' literal value.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>Jetty</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @see #JETTY_LITERAL
     * @model name="Jetty"
     * @generated
     * @ordered
     */
    public static final int JETTY = 1;

    /**
     * The '<em><b>Tomcat</b></em>' literal object.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #TOMCAT
     * @generated
     * @ordered
     */
    public static final WebContainerType TOMCAT_LITERAL = new WebContainerType(TOMCAT, "Tomcat");

    /**
     * The '<em><b>Jetty</b></em>' literal object.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #JETTY
     * @generated
     * @ordered
     */
    public static final WebContainerType JETTY_LITERAL = new WebContainerType(JETTY, "Jetty");

    /**
     * An array of all the '<em><b>Container Type</b></em>' enumerators.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private static final WebContainerType[] VALUES_ARRAY =
        new WebContainerType[] {
            TOMCAT_LITERAL,
            JETTY_LITERAL,
        };

    /**
     * A public read-only list of all the '<em><b>Container Type</b></em>' enumerators.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static final List VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

    /**
     * Returns the '<em><b>Container Type</b></em>' literal with the specified name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static WebContainerType get(String name) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            WebContainerType result = VALUES_ARRAY[i];
            if (result.toString().equals(name)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the '<em><b>Container Type</b></em>' literal with the specified value.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static WebContainerType get(int value) {
        switch (value) {
            case TOMCAT: return TOMCAT_LITERAL;
            case JETTY: return JETTY_LITERAL;
        }
        return null;	
    }

    /**
     * Only this class can construct instances.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private WebContainerType(int value, String name) {
        super(value, name);
    }

} //WebContainerType
