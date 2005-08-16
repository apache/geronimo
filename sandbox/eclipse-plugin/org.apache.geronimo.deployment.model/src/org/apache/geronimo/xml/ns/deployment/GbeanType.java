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
package org.apache.geronimo.xml.ns.deployment;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Gbean Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getGroup <em>Group</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getAttribute <em>Attribute</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getXmlAttribute <em>Xml Attribute</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getReference <em>Reference</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getReferences <em>References</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getXmlReference <em>Xml Reference</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getClass_ <em>Class</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getGbeanName <em>Gbean Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getGbeanType()
 * @model extendedMetaData="name='gbeanType' kind='elementOnly'"
 * @generated
 */
public interface GbeanType extends EObject {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

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
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getGbeanType_Group()
     * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry" many="true"
     *        extendedMetaData="kind='group' name='group:0'"
     * @generated
     */
    FeatureMap getGroup();

    /**
     * Returns the value of the '<em><b>Attribute</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.deployment.AttributeType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Attribute</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Attribute</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getGbeanType_Attribute()
     * @model type="org.apache.geronimo.xml.ns.deployment.AttributeType" containment="true" resolveProxies="false" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='attribute' namespace='##targetNamespace' group='#group:0'"
     * @generated
     */
    EList getAttribute();

    /**
     * Returns the value of the '<em><b>Xml Attribute</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.deployment.XmlAttributeType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Xml Attribute</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Xml Attribute</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getGbeanType_XmlAttribute()
     * @model type="org.apache.geronimo.xml.ns.deployment.XmlAttributeType" containment="true" resolveProxies="false" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='xml-attribute' namespace='##targetNamespace' group='#group:0'"
     * @generated
     */
    EList getXmlAttribute();

    /**
     * Returns the value of the '<em><b>Reference</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.deployment.ReferenceType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Reference</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Reference</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getGbeanType_Reference()
     * @model type="org.apache.geronimo.xml.ns.deployment.ReferenceType" containment="true" resolveProxies="false" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='reference' namespace='##targetNamespace' group='#group:0'"
     * @generated
     */
    EList getReference();

    /**
     * Returns the value of the '<em><b>References</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.deployment.ReferencesType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>References</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>References</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getGbeanType_References()
     * @model type="org.apache.geronimo.xml.ns.deployment.ReferencesType" containment="true" resolveProxies="false" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='references' namespace='##targetNamespace' group='#group:0'"
     * @generated
     */
    EList getReferences();

    /**
     * Returns the value of the '<em><b>Xml Reference</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.deployment.XmlAttributeType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Xml Reference</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Xml Reference</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getGbeanType_XmlReference()
     * @model type="org.apache.geronimo.xml.ns.deployment.XmlAttributeType" containment="true" resolveProxies="false" transient="true" volatile="true" derived="true"
     *        extendedMetaData="kind='element' name='xml-reference' namespace='##targetNamespace' group='#group:0'"
     * @generated
     */
    EList getXmlReference();

    /**
     * Returns the value of the '<em><b>Class</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Class</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Class</em>' attribute.
     * @see #setClass(String)
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getGbeanType_Class()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='attribute' name='class'"
     * @generated
     */
    String getClass_();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getClass_ <em>Class</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Class</em>' attribute.
     * @see #getClass_()
     * @generated
     */
    void setClass(String value);

    /**
     * Returns the value of the '<em><b>Gbean Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Gbean Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Gbean Name</em>' attribute.
     * @see #setGbeanName(String)
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getGbeanType_GbeanName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='gbeanName'"
     * @generated
     */
    String getGbeanName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getGbeanName <em>Gbean Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Gbean Name</em>' attribute.
     * @see #getGbeanName()
     * @generated
     */
    void setGbeanName(String value);

    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Name</em>' attribute.
     * @see #setName(String)
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getGbeanType_Name()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='name'"
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

} // GbeanType
