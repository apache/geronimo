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

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Dependency Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getGroupId <em>Group Id</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getType <em>Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getArtifactId <em>Artifact Id</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getVersion <em>Version</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getUri <em>Uri</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getDependencyType()
 * @model extendedMetaData="name='dependencyType' kind='elementOnly'"
 * @generated
 */
public interface DependencyType extends EObject{
    /**
     * Returns the value of the '<em><b>Group Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Group Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Group Id</em>' attribute.
     * @see #setGroupId(String)
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getDependencyType_GroupId()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='groupId' namespace='##targetNamespace'"
     * @generated
     */
    String getGroupId();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getGroupId <em>Group Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Group Id</em>' attribute.
     * @see #getGroupId()
     * @generated
     */
    void setGroupId(String value);

    /**
     * Returns the value of the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Type</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Type</em>' attribute.
     * @see #setType(String)
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getDependencyType_Type()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='type' namespace='##targetNamespace'"
     * @generated
     */
    String getType();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getType <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Type</em>' attribute.
     * @see #getType()
     * @generated
     */
    void setType(String value);

    /**
     * Returns the value of the '<em><b>Artifact Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Artifact Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Artifact Id</em>' attribute.
     * @see #setArtifactId(String)
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getDependencyType_ArtifactId()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='artifactId' namespace='##targetNamespace'"
     * @generated
     */
    String getArtifactId();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getArtifactId <em>Artifact Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Artifact Id</em>' attribute.
     * @see #getArtifactId()
     * @generated
     */
    void setArtifactId(String value);

    /**
     * Returns the value of the '<em><b>Version</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Version</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Version</em>' attribute.
     * @see #setVersion(String)
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getDependencyType_Version()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='version' namespace='##targetNamespace'"
     * @generated
     */
    String getVersion();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getVersion <em>Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Version</em>' attribute.
     * @see #getVersion()
     * @generated
     */
    void setVersion(String value);

    /**
     * Returns the value of the '<em><b>Uri</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Uri</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Uri</em>' attribute.
     * @see #setUri(String)
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getDependencyType_Uri()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='uri' namespace='##targetNamespace'"
     * @generated
     */
    String getUri();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getUri <em>Uri</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Uri</em>' attribute.
     * @see #getUri()
     * @generated
     */
    void setUri(String value);

} // DependencyType
