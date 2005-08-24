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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Enterprise Beans Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.EnterpriseBeansType#getGroup <em>Group</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.EnterpriseBeansType#getSession <em>Session</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.EnterpriseBeansType#getEntity <em>Entity</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.EnterpriseBeansType#getMessageDriven <em>Message Driven</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEnterpriseBeansType()
 * @model extendedMetaData="name='enterprise-beans_._type' kind='elementOnly'"
 * @generated
 */
public interface EnterpriseBeansType extends EObject {
    /**
     * Returns the value of the '<em><b>Group</b></em>' attribute list.
     * The list contents are of type {@link org.eclipse.emf.ecore.util.FeatureMap.Entry}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Group</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Group</em>' attribute list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEnterpriseBeansType_Group()
     * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry" many="true"
     *        extendedMetaData="kind='group' name='group:0'"
     * @generated
     */
    FeatureMap getGroup();

    /**
     * Returns the value of the '<em><b>Session</b></em>' containment reference list.
     * The list contents are of type {@link org.openejb.xml.ns.openejb.jar.SessionBeanType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Session</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Session</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEnterpriseBeansType_Session()
     * @model type="org.openejb.xml.ns.openejb.jar.SessionBeanType" containment="true" resolveProxies="false" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='session' namespace='##targetNamespace' group='#group:0'"
     * @generated
     */
    EList getSession();

    /**
     * Returns the value of the '<em><b>Entity</b></em>' containment reference list.
     * The list contents are of type {@link org.openejb.xml.ns.openejb.jar.EntityBeanType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Entity</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Entity</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEnterpriseBeansType_Entity()
     * @model type="org.openejb.xml.ns.openejb.jar.EntityBeanType" containment="true" resolveProxies="false" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='entity' namespace='##targetNamespace' group='#group:0'"
     * @generated
     */
    EList getEntity();

    /**
     * Returns the value of the '<em><b>Message Driven</b></em>' containment reference list.
     * The list contents are of type {@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Message Driven</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Message Driven</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getEnterpriseBeansType_MessageDriven()
     * @model type="org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType" containment="true" resolveProxies="false" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='message-driven' namespace='##targetNamespace' group='#group:0'"
     * @generated
     */
    EList getMessageDriven();

} // EnterpriseBeansType
