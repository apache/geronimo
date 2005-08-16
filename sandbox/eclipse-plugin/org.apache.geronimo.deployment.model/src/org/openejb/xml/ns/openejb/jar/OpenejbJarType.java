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

import org.apache.geronimo.xml.ns.naming.ResourceLocatorType;

import org.apache.geronimo.xml.ns.security.SecurityType;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Openejb Jar Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getDependency <em>Dependency</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getCmpConnectionFactory <em>Cmp Connection Factory</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getEjbQlCompilerFactory <em>Ejb Ql Compiler Factory</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getDbSyntaxFactory <em>Db Syntax Factory</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getEnforceForeignKeyConstraints <em>Enforce Foreign Key Constraints</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getEnterpriseBeans <em>Enterprise Beans</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getRelationships <em>Relationships</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getSecurity <em>Security</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getGbean <em>Gbean</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getConfigId <em>Config Id</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getParentId <em>Parent Id</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getOpenejbJarType()
 * @model extendedMetaData="name='openejb-jarType' kind='elementOnly'"
 * @generated
 */
public interface OpenejbJarType extends EObject {
    /**
     * Returns the value of the '<em><b>Dependency</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.deployment.DependencyType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Dependency</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Dependency</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getOpenejbJarType_Dependency()
     * @model type="org.apache.geronimo.xml.ns.deployment.DependencyType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='dependency' namespace='http://geronimo.apache.org/xml/ns/deployment'"
     * @generated
     */
    EList getDependency();

    /**
     * Returns the value of the '<em><b>Cmp Connection Factory</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Cmp Connection Factory</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Cmp Connection Factory</em>' containment reference.
     * @see #setCmpConnectionFactory(ResourceLocatorType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getOpenejbJarType_CmpConnectionFactory()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='cmp-connection-factory' namespace='http://geronimo.apache.org/xml/ns/naming'"
     * @generated
     */
    ResourceLocatorType getCmpConnectionFactory();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getCmpConnectionFactory <em>Cmp Connection Factory</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Cmp Connection Factory</em>' containment reference.
     * @see #getCmpConnectionFactory()
     * @generated
     */
    void setCmpConnectionFactory(ResourceLocatorType value);

    /**
     * Returns the value of the '<em><b>Ejb Ql Compiler Factory</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ejb Ql Compiler Factory</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ejb Ql Compiler Factory</em>' containment reference.
     * @see #setEjbQlCompilerFactory(EObject)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getOpenejbJarType_EjbQlCompilerFactory()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='ejb-ql-compiler-factory' namespace='##targetNamespace'"
     * @generated
     */
    EObject getEjbQlCompilerFactory();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getEjbQlCompilerFactory <em>Ejb Ql Compiler Factory</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Ejb Ql Compiler Factory</em>' containment reference.
     * @see #getEjbQlCompilerFactory()
     * @generated
     */
    void setEjbQlCompilerFactory(EObject value);

    /**
     * Returns the value of the '<em><b>Db Syntax Factory</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Db Syntax Factory</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Db Syntax Factory</em>' containment reference.
     * @see #setDbSyntaxFactory(EObject)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getOpenejbJarType_DbSyntaxFactory()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='db-syntax-factory' namespace='##targetNamespace'"
     * @generated
     */
    EObject getDbSyntaxFactory();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getDbSyntaxFactory <em>Db Syntax Factory</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Db Syntax Factory</em>' containment reference.
     * @see #getDbSyntaxFactory()
     * @generated
     */
    void setDbSyntaxFactory(EObject value);

    /**
     * Returns the value of the '<em><b>Enforce Foreign Key Constraints</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Enforce Foreign Key Constraints</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Enforce Foreign Key Constraints</em>' containment reference.
     * @see #setEnforceForeignKeyConstraints(EObject)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getOpenejbJarType_EnforceForeignKeyConstraints()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='enforce-foreign-key-constraints' namespace='##targetNamespace'"
     * @generated
     */
    EObject getEnforceForeignKeyConstraints();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getEnforceForeignKeyConstraints <em>Enforce Foreign Key Constraints</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Enforce Foreign Key Constraints</em>' containment reference.
     * @see #getEnforceForeignKeyConstraints()
     * @generated
     */
    void setEnforceForeignKeyConstraints(EObject value);

    /**
     * Returns the value of the '<em><b>Enterprise Beans</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Enterprise Beans</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Enterprise Beans</em>' containment reference.
     * @see #setEnterpriseBeans(EnterpriseBeansType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getOpenejbJarType_EnterpriseBeans()
     * @model containment="true" resolveProxies="false" required="true"
     *        extendedMetaData="kind='element' name='enterprise-beans' namespace='##targetNamespace'"
     * @generated
     */
    EnterpriseBeansType getEnterpriseBeans();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getEnterpriseBeans <em>Enterprise Beans</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Enterprise Beans</em>' containment reference.
     * @see #getEnterpriseBeans()
     * @generated
     */
    void setEnterpriseBeans(EnterpriseBeansType value);

    /**
     * Returns the value of the '<em><b>Relationships</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Relationships</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Relationships</em>' containment reference.
     * @see #setRelationships(RelationshipsType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getOpenejbJarType_Relationships()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='relationships' namespace='##targetNamespace'"
     * @generated
     */
    RelationshipsType getRelationships();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getRelationships <em>Relationships</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Relationships</em>' containment reference.
     * @see #getRelationships()
     * @generated
     */
    void setRelationships(RelationshipsType value);

    /**
     * Returns the value of the '<em><b>Security</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Security</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Security</em>' containment reference.
     * @see #setSecurity(SecurityType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getOpenejbJarType_Security()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='security' namespace='http://geronimo.apache.org/xml/ns/security'"
     * @generated
     */
    SecurityType getSecurity();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getSecurity <em>Security</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Security</em>' containment reference.
     * @see #getSecurity()
     * @generated
     */
    void setSecurity(SecurityType value);

    /**
     * Returns the value of the '<em><b>Gbean</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.deployment.GbeanType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Gbean</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Gbean</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getOpenejbJarType_Gbean()
     * @model type="org.apache.geronimo.xml.ns.deployment.GbeanType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='gbean' namespace='http://geronimo.apache.org/xml/ns/deployment'"
     * @generated
     */
    EList getGbean();

    /**
     * Returns the value of the '<em><b>Config Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Config Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Config Id</em>' attribute.
     * @see #setConfigId(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getOpenejbJarType_ConfigId()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='attribute' name='configId'"
     * @generated
     */
    String getConfigId();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getConfigId <em>Config Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Config Id</em>' attribute.
     * @see #getConfigId()
     * @generated
     */
    void setConfigId(String value);

    /**
     * Returns the value of the '<em><b>Parent Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Parent Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Parent Id</em>' attribute.
     * @see #setParentId(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getOpenejbJarType_ParentId()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='parentId'"
     * @generated
     */
    String getParentId();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getParentId <em>Parent Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Parent Id</em>' attribute.
     * @see #getParentId()
     * @generated
     */
    void setParentId(String value);

} // OpenejbJarType
