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
package org.openejb.xml.ns.openejb.jar.impl;

import java.util.Collection;

import org.apache.geronimo.xml.ns.deployment.DependencyType;
import org.apache.geronimo.xml.ns.deployment.GbeanType;

import org.apache.geronimo.xml.ns.naming.ResourceLocatorType;

import org.apache.geronimo.xml.ns.security.SecurityType;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.openejb.xml.ns.openejb.jar.EnterpriseBeansType;
import org.openejb.xml.ns.openejb.jar.JarPackage;
import org.openejb.xml.ns.openejb.jar.OpenejbJarType;
import org.openejb.xml.ns.openejb.jar.RelationshipsType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Openejb Jar Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.OpenejbJarTypeImpl#getDependency <em>Dependency</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.OpenejbJarTypeImpl#getCmpConnectionFactory <em>Cmp Connection Factory</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.OpenejbJarTypeImpl#getEjbQlCompilerFactory <em>Ejb Ql Compiler Factory</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.OpenejbJarTypeImpl#getDbSyntaxFactory <em>Db Syntax Factory</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.OpenejbJarTypeImpl#getEnforceForeignKeyConstraints <em>Enforce Foreign Key Constraints</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.OpenejbJarTypeImpl#getEnterpriseBeans <em>Enterprise Beans</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.OpenejbJarTypeImpl#getRelationships <em>Relationships</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.OpenejbJarTypeImpl#getSecurity <em>Security</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.OpenejbJarTypeImpl#getGbean <em>Gbean</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.OpenejbJarTypeImpl#getConfigId <em>Config Id</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.OpenejbJarTypeImpl#getParentId <em>Parent Id</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class OpenejbJarTypeImpl extends EObjectImpl implements OpenejbJarType {
    /**
     * The cached value of the '{@link #getDependency() <em>Dependency</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDependency()
     * @generated
     * @ordered
     */
    protected EList dependency = null;

    /**
     * The cached value of the '{@link #getCmpConnectionFactory() <em>Cmp Connection Factory</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCmpConnectionFactory()
     * @generated
     * @ordered
     */
    protected ResourceLocatorType cmpConnectionFactory = null;

    /**
     * The cached value of the '{@link #getEjbQlCompilerFactory() <em>Ejb Ql Compiler Factory</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEjbQlCompilerFactory()
     * @generated
     * @ordered
     */
    protected EObject ejbQlCompilerFactory = null;

    /**
     * The cached value of the '{@link #getDbSyntaxFactory() <em>Db Syntax Factory</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDbSyntaxFactory()
     * @generated
     * @ordered
     */
    protected EObject dbSyntaxFactory = null;

    /**
     * The cached value of the '{@link #getEnforceForeignKeyConstraints() <em>Enforce Foreign Key Constraints</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEnforceForeignKeyConstraints()
     * @generated
     * @ordered
     */
    protected EObject enforceForeignKeyConstraints = null;

    /**
     * The cached value of the '{@link #getEnterpriseBeans() <em>Enterprise Beans</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEnterpriseBeans()
     * @generated
     * @ordered
     */
    protected EnterpriseBeansType enterpriseBeans = null;

    /**
     * The cached value of the '{@link #getRelationships() <em>Relationships</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRelationships()
     * @generated
     * @ordered
     */
    protected RelationshipsType relationships = null;

    /**
     * The cached value of the '{@link #getSecurity() <em>Security</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSecurity()
     * @generated
     * @ordered
     */
    protected SecurityType security = null;

    /**
     * The cached value of the '{@link #getGbean() <em>Gbean</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGbean()
     * @generated
     * @ordered
     */
    protected EList gbean = null;

    /**
     * The default value of the '{@link #getConfigId() <em>Config Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getConfigId()
     * @generated
     * @ordered
     */
    protected static final String CONFIG_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getConfigId() <em>Config Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getConfigId()
     * @generated
     * @ordered
     */
    protected String configId = CONFIG_ID_EDEFAULT;

    /**
     * The default value of the '{@link #getParentId() <em>Parent Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getParentId()
     * @generated
     * @ordered
     */
    protected static final String PARENT_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getParentId() <em>Parent Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getParentId()
     * @generated
     * @ordered
     */
    protected String parentId = PARENT_ID_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected OpenejbJarTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getOpenejbJarType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getDependency() {
        if (dependency == null) {
            dependency = new EObjectContainmentEList(DependencyType.class, this, JarPackage.OPENEJB_JAR_TYPE__DEPENDENCY);
        }
        return dependency;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ResourceLocatorType getCmpConnectionFactory() {
        return cmpConnectionFactory;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetCmpConnectionFactory(ResourceLocatorType newCmpConnectionFactory, NotificationChain msgs) {
        ResourceLocatorType oldCmpConnectionFactory = cmpConnectionFactory;
        cmpConnectionFactory = newCmpConnectionFactory;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__CMP_CONNECTION_FACTORY, oldCmpConnectionFactory, newCmpConnectionFactory);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setCmpConnectionFactory(ResourceLocatorType newCmpConnectionFactory) {
        if (newCmpConnectionFactory != cmpConnectionFactory) {
            NotificationChain msgs = null;
            if (cmpConnectionFactory != null)
                msgs = ((InternalEObject)cmpConnectionFactory).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__CMP_CONNECTION_FACTORY, null, msgs);
            if (newCmpConnectionFactory != null)
                msgs = ((InternalEObject)newCmpConnectionFactory).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__CMP_CONNECTION_FACTORY, null, msgs);
            msgs = basicSetCmpConnectionFactory(newCmpConnectionFactory, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__CMP_CONNECTION_FACTORY, newCmpConnectionFactory, newCmpConnectionFactory));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject getEjbQlCompilerFactory() {
        return ejbQlCompilerFactory;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetEjbQlCompilerFactory(EObject newEjbQlCompilerFactory, NotificationChain msgs) {
        EObject oldEjbQlCompilerFactory = ejbQlCompilerFactory;
        ejbQlCompilerFactory = newEjbQlCompilerFactory;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__EJB_QL_COMPILER_FACTORY, oldEjbQlCompilerFactory, newEjbQlCompilerFactory);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEjbQlCompilerFactory(EObject newEjbQlCompilerFactory) {
        if (newEjbQlCompilerFactory != ejbQlCompilerFactory) {
            NotificationChain msgs = null;
            if (ejbQlCompilerFactory != null)
                msgs = ((InternalEObject)ejbQlCompilerFactory).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__EJB_QL_COMPILER_FACTORY, null, msgs);
            if (newEjbQlCompilerFactory != null)
                msgs = ((InternalEObject)newEjbQlCompilerFactory).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__EJB_QL_COMPILER_FACTORY, null, msgs);
            msgs = basicSetEjbQlCompilerFactory(newEjbQlCompilerFactory, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__EJB_QL_COMPILER_FACTORY, newEjbQlCompilerFactory, newEjbQlCompilerFactory));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject getDbSyntaxFactory() {
        return dbSyntaxFactory;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetDbSyntaxFactory(EObject newDbSyntaxFactory, NotificationChain msgs) {
        EObject oldDbSyntaxFactory = dbSyntaxFactory;
        dbSyntaxFactory = newDbSyntaxFactory;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__DB_SYNTAX_FACTORY, oldDbSyntaxFactory, newDbSyntaxFactory);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setDbSyntaxFactory(EObject newDbSyntaxFactory) {
        if (newDbSyntaxFactory != dbSyntaxFactory) {
            NotificationChain msgs = null;
            if (dbSyntaxFactory != null)
                msgs = ((InternalEObject)dbSyntaxFactory).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__DB_SYNTAX_FACTORY, null, msgs);
            if (newDbSyntaxFactory != null)
                msgs = ((InternalEObject)newDbSyntaxFactory).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__DB_SYNTAX_FACTORY, null, msgs);
            msgs = basicSetDbSyntaxFactory(newDbSyntaxFactory, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__DB_SYNTAX_FACTORY, newDbSyntaxFactory, newDbSyntaxFactory));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject getEnforceForeignKeyConstraints() {
        return enforceForeignKeyConstraints;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetEnforceForeignKeyConstraints(EObject newEnforceForeignKeyConstraints, NotificationChain msgs) {
        EObject oldEnforceForeignKeyConstraints = enforceForeignKeyConstraints;
        enforceForeignKeyConstraints = newEnforceForeignKeyConstraints;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__ENFORCE_FOREIGN_KEY_CONSTRAINTS, oldEnforceForeignKeyConstraints, newEnforceForeignKeyConstraints);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEnforceForeignKeyConstraints(EObject newEnforceForeignKeyConstraints) {
        if (newEnforceForeignKeyConstraints != enforceForeignKeyConstraints) {
            NotificationChain msgs = null;
            if (enforceForeignKeyConstraints != null)
                msgs = ((InternalEObject)enforceForeignKeyConstraints).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__ENFORCE_FOREIGN_KEY_CONSTRAINTS, null, msgs);
            if (newEnforceForeignKeyConstraints != null)
                msgs = ((InternalEObject)newEnforceForeignKeyConstraints).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__ENFORCE_FOREIGN_KEY_CONSTRAINTS, null, msgs);
            msgs = basicSetEnforceForeignKeyConstraints(newEnforceForeignKeyConstraints, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__ENFORCE_FOREIGN_KEY_CONSTRAINTS, newEnforceForeignKeyConstraints, newEnforceForeignKeyConstraints));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EnterpriseBeansType getEnterpriseBeans() {
        return enterpriseBeans;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetEnterpriseBeans(EnterpriseBeansType newEnterpriseBeans, NotificationChain msgs) {
        EnterpriseBeansType oldEnterpriseBeans = enterpriseBeans;
        enterpriseBeans = newEnterpriseBeans;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__ENTERPRISE_BEANS, oldEnterpriseBeans, newEnterpriseBeans);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEnterpriseBeans(EnterpriseBeansType newEnterpriseBeans) {
        if (newEnterpriseBeans != enterpriseBeans) {
            NotificationChain msgs = null;
            if (enterpriseBeans != null)
                msgs = ((InternalEObject)enterpriseBeans).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__ENTERPRISE_BEANS, null, msgs);
            if (newEnterpriseBeans != null)
                msgs = ((InternalEObject)newEnterpriseBeans).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__ENTERPRISE_BEANS, null, msgs);
            msgs = basicSetEnterpriseBeans(newEnterpriseBeans, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__ENTERPRISE_BEANS, newEnterpriseBeans, newEnterpriseBeans));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public RelationshipsType getRelationships() {
        return relationships;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetRelationships(RelationshipsType newRelationships, NotificationChain msgs) {
        RelationshipsType oldRelationships = relationships;
        relationships = newRelationships;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__RELATIONSHIPS, oldRelationships, newRelationships);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setRelationships(RelationshipsType newRelationships) {
        if (newRelationships != relationships) {
            NotificationChain msgs = null;
            if (relationships != null)
                msgs = ((InternalEObject)relationships).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__RELATIONSHIPS, null, msgs);
            if (newRelationships != null)
                msgs = ((InternalEObject)newRelationships).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__RELATIONSHIPS, null, msgs);
            msgs = basicSetRelationships(newRelationships, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__RELATIONSHIPS, newRelationships, newRelationships));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SecurityType getSecurity() {
        return security;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetSecurity(SecurityType newSecurity, NotificationChain msgs) {
        SecurityType oldSecurity = security;
        security = newSecurity;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__SECURITY, oldSecurity, newSecurity);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSecurity(SecurityType newSecurity) {
        if (newSecurity != security) {
            NotificationChain msgs = null;
            if (security != null)
                msgs = ((InternalEObject)security).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__SECURITY, null, msgs);
            if (newSecurity != null)
                msgs = ((InternalEObject)newSecurity).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.OPENEJB_JAR_TYPE__SECURITY, null, msgs);
            msgs = basicSetSecurity(newSecurity, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__SECURITY, newSecurity, newSecurity));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getGbean() {
        if (gbean == null) {
            gbean = new EObjectContainmentEList(GbeanType.class, this, JarPackage.OPENEJB_JAR_TYPE__GBEAN);
        }
        return gbean;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getConfigId() {
        return configId;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setConfigId(String newConfigId) {
        String oldConfigId = configId;
        configId = newConfigId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__CONFIG_ID, oldConfigId, configId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setParentId(String newParentId) {
        String oldParentId = parentId;
        parentId = newParentId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.OPENEJB_JAR_TYPE__PARENT_ID, oldParentId, parentId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case JarPackage.OPENEJB_JAR_TYPE__DEPENDENCY:
                    return ((InternalEList)getDependency()).basicRemove(otherEnd, msgs);
                case JarPackage.OPENEJB_JAR_TYPE__CMP_CONNECTION_FACTORY:
                    return basicSetCmpConnectionFactory(null, msgs);
                case JarPackage.OPENEJB_JAR_TYPE__EJB_QL_COMPILER_FACTORY:
                    return basicSetEjbQlCompilerFactory(null, msgs);
                case JarPackage.OPENEJB_JAR_TYPE__DB_SYNTAX_FACTORY:
                    return basicSetDbSyntaxFactory(null, msgs);
                case JarPackage.OPENEJB_JAR_TYPE__ENFORCE_FOREIGN_KEY_CONSTRAINTS:
                    return basicSetEnforceForeignKeyConstraints(null, msgs);
                case JarPackage.OPENEJB_JAR_TYPE__ENTERPRISE_BEANS:
                    return basicSetEnterpriseBeans(null, msgs);
                case JarPackage.OPENEJB_JAR_TYPE__RELATIONSHIPS:
                    return basicSetRelationships(null, msgs);
                case JarPackage.OPENEJB_JAR_TYPE__SECURITY:
                    return basicSetSecurity(null, msgs);
                case JarPackage.OPENEJB_JAR_TYPE__GBEAN:
                    return ((InternalEList)getGbean()).basicRemove(otherEnd, msgs);
                default:
                    return eDynamicInverseRemove(otherEnd, featureID, baseClass, msgs);
            }
        }
        return eBasicSetContainer(null, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case JarPackage.OPENEJB_JAR_TYPE__DEPENDENCY:
                return getDependency();
            case JarPackage.OPENEJB_JAR_TYPE__CMP_CONNECTION_FACTORY:
                return getCmpConnectionFactory();
            case JarPackage.OPENEJB_JAR_TYPE__EJB_QL_COMPILER_FACTORY:
                return getEjbQlCompilerFactory();
            case JarPackage.OPENEJB_JAR_TYPE__DB_SYNTAX_FACTORY:
                return getDbSyntaxFactory();
            case JarPackage.OPENEJB_JAR_TYPE__ENFORCE_FOREIGN_KEY_CONSTRAINTS:
                return getEnforceForeignKeyConstraints();
            case JarPackage.OPENEJB_JAR_TYPE__ENTERPRISE_BEANS:
                return getEnterpriseBeans();
            case JarPackage.OPENEJB_JAR_TYPE__RELATIONSHIPS:
                return getRelationships();
            case JarPackage.OPENEJB_JAR_TYPE__SECURITY:
                return getSecurity();
            case JarPackage.OPENEJB_JAR_TYPE__GBEAN:
                return getGbean();
            case JarPackage.OPENEJB_JAR_TYPE__CONFIG_ID:
                return getConfigId();
            case JarPackage.OPENEJB_JAR_TYPE__PARENT_ID:
                return getParentId();
        }
        return eDynamicGet(eFeature, resolve);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void eSet(EStructuralFeature eFeature, Object newValue) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case JarPackage.OPENEJB_JAR_TYPE__DEPENDENCY:
                getDependency().clear();
                getDependency().addAll((Collection)newValue);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__CMP_CONNECTION_FACTORY:
                setCmpConnectionFactory((ResourceLocatorType)newValue);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__EJB_QL_COMPILER_FACTORY:
                setEjbQlCompilerFactory((EObject)newValue);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__DB_SYNTAX_FACTORY:
                setDbSyntaxFactory((EObject)newValue);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__ENFORCE_FOREIGN_KEY_CONSTRAINTS:
                setEnforceForeignKeyConstraints((EObject)newValue);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__ENTERPRISE_BEANS:
                setEnterpriseBeans((EnterpriseBeansType)newValue);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__RELATIONSHIPS:
                setRelationships((RelationshipsType)newValue);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__SECURITY:
                setSecurity((SecurityType)newValue);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__GBEAN:
                getGbean().clear();
                getGbean().addAll((Collection)newValue);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__CONFIG_ID:
                setConfigId((String)newValue);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__PARENT_ID:
                setParentId((String)newValue);
                return;
        }
        eDynamicSet(eFeature, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void eUnset(EStructuralFeature eFeature) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case JarPackage.OPENEJB_JAR_TYPE__DEPENDENCY:
                getDependency().clear();
                return;
            case JarPackage.OPENEJB_JAR_TYPE__CMP_CONNECTION_FACTORY:
                setCmpConnectionFactory((ResourceLocatorType)null);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__EJB_QL_COMPILER_FACTORY:
                setEjbQlCompilerFactory((EObject)null);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__DB_SYNTAX_FACTORY:
                setDbSyntaxFactory((EObject)null);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__ENFORCE_FOREIGN_KEY_CONSTRAINTS:
                setEnforceForeignKeyConstraints((EObject)null);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__ENTERPRISE_BEANS:
                setEnterpriseBeans((EnterpriseBeansType)null);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__RELATIONSHIPS:
                setRelationships((RelationshipsType)null);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__SECURITY:
                setSecurity((SecurityType)null);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__GBEAN:
                getGbean().clear();
                return;
            case JarPackage.OPENEJB_JAR_TYPE__CONFIG_ID:
                setConfigId(CONFIG_ID_EDEFAULT);
                return;
            case JarPackage.OPENEJB_JAR_TYPE__PARENT_ID:
                setParentId(PARENT_ID_EDEFAULT);
                return;
        }
        eDynamicUnset(eFeature);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean eIsSet(EStructuralFeature eFeature) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case JarPackage.OPENEJB_JAR_TYPE__DEPENDENCY:
                return dependency != null && !dependency.isEmpty();
            case JarPackage.OPENEJB_JAR_TYPE__CMP_CONNECTION_FACTORY:
                return cmpConnectionFactory != null;
            case JarPackage.OPENEJB_JAR_TYPE__EJB_QL_COMPILER_FACTORY:
                return ejbQlCompilerFactory != null;
            case JarPackage.OPENEJB_JAR_TYPE__DB_SYNTAX_FACTORY:
                return dbSyntaxFactory != null;
            case JarPackage.OPENEJB_JAR_TYPE__ENFORCE_FOREIGN_KEY_CONSTRAINTS:
                return enforceForeignKeyConstraints != null;
            case JarPackage.OPENEJB_JAR_TYPE__ENTERPRISE_BEANS:
                return enterpriseBeans != null;
            case JarPackage.OPENEJB_JAR_TYPE__RELATIONSHIPS:
                return relationships != null;
            case JarPackage.OPENEJB_JAR_TYPE__SECURITY:
                return security != null;
            case JarPackage.OPENEJB_JAR_TYPE__GBEAN:
                return gbean != null && !gbean.isEmpty();
            case JarPackage.OPENEJB_JAR_TYPE__CONFIG_ID:
                return CONFIG_ID_EDEFAULT == null ? configId != null : !CONFIG_ID_EDEFAULT.equals(configId);
            case JarPackage.OPENEJB_JAR_TYPE__PARENT_ID:
                return PARENT_ID_EDEFAULT == null ? parentId != null : !PARENT_ID_EDEFAULT.equals(parentId);
        }
        return eDynamicIsSet(eFeature);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (configId: ");
        result.append(configId);
        result.append(", parentId: ");
        result.append(parentId);
        result.append(')');
        return result.toString();
    }

} //OpenejbJarTypeImpl
