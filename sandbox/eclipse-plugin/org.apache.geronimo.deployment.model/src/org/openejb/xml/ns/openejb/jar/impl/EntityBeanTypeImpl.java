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

import org.apache.geronimo.xml.ns.naming.EjbLocalRefType;
import org.apache.geronimo.xml.ns.naming.EjbRefType;
import org.apache.geronimo.xml.ns.naming.ResourceEnvRefType;
import org.apache.geronimo.xml.ns.naming.ResourceRefType;
import org.apache.geronimo.xml.ns.naming.ServiceRefType;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.openejb.xml.ns.openejb.jar.CmpFieldMappingType;
import org.openejb.xml.ns.openejb.jar.EntityBeanType;
import org.openejb.xml.ns.openejb.jar.JarPackage;
import org.openejb.xml.ns.openejb.jar.PrefetchGroupType;
import org.openejb.xml.ns.openejb.jar.QueryType;
import org.openejb.xml.ns.openejb.jar.TssType;

import org.openejb.xml.ns.pkgen.KeyGeneratorType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Entity Bean Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getEjbName <em>Ejb Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getJndiName <em>Jndi Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getLocalJndiName <em>Local Jndi Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getTssTargetName <em>Tss Target Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getTssLink <em>Tss Link</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getTss <em>Tss</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getTableName <em>Table Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getCmpFieldMapping <em>Cmp Field Mapping</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getPrimkeyField <em>Primkey Field</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getKeyGenerator <em>Key Generator</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getPrefetchGroup <em>Prefetch Group</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getEjbRef <em>Ejb Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getEjbLocalRef <em>Ejb Local Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getServiceRef <em>Service Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getResourceRef <em>Resource Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getResourceEnvRef <em>Resource Env Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getQuery <em>Query</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl#getId <em>Id</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class EntityBeanTypeImpl extends EObjectImpl implements EntityBeanType {
    /**
     * The default value of the '{@link #getEjbName() <em>Ejb Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEjbName()
     * @generated
     * @ordered
     */
    protected static final String EJB_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getEjbName() <em>Ejb Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEjbName()
     * @generated
     * @ordered
     */
    protected String ejbName = EJB_NAME_EDEFAULT;

    /**
     * The cached value of the '{@link #getJndiName() <em>Jndi Name</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getJndiName()
     * @generated
     * @ordered
     */
    protected EList jndiName = null;

    /**
     * The cached value of the '{@link #getLocalJndiName() <em>Local Jndi Name</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLocalJndiName()
     * @generated
     * @ordered
     */
    protected EList localJndiName = null;

    /**
     * The default value of the '{@link #getTssTargetName() <em>Tss Target Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTssTargetName()
     * @generated
     * @ordered
     */
    protected static final String TSS_TARGET_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getTssTargetName() <em>Tss Target Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTssTargetName()
     * @generated
     * @ordered
     */
    protected String tssTargetName = TSS_TARGET_NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getTssLink() <em>Tss Link</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTssLink()
     * @generated
     * @ordered
     */
    protected static final String TSS_LINK_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getTssLink() <em>Tss Link</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTssLink()
     * @generated
     * @ordered
     */
    protected String tssLink = TSS_LINK_EDEFAULT;

    /**
     * The cached value of the '{@link #getTss() <em>Tss</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTss()
     * @generated
     * @ordered
     */
    protected TssType tss = null;

    /**
     * The default value of the '{@link #getTableName() <em>Table Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTableName()
     * @generated
     * @ordered
     */
    protected static final String TABLE_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getTableName() <em>Table Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTableName()
     * @generated
     * @ordered
     */
    protected String tableName = TABLE_NAME_EDEFAULT;

    /**
     * The cached value of the '{@link #getCmpFieldMapping() <em>Cmp Field Mapping</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCmpFieldMapping()
     * @generated
     * @ordered
     */
    protected EList cmpFieldMapping = null;

    /**
     * The default value of the '{@link #getPrimkeyField() <em>Primkey Field</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPrimkeyField()
     * @generated
     * @ordered
     */
    protected static final String PRIMKEY_FIELD_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getPrimkeyField() <em>Primkey Field</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPrimkeyField()
     * @generated
     * @ordered
     */
    protected String primkeyField = PRIMKEY_FIELD_EDEFAULT;

    /**
     * The cached value of the '{@link #getKeyGenerator() <em>Key Generator</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getKeyGenerator()
     * @generated
     * @ordered
     */
    protected KeyGeneratorType keyGenerator = null;

    /**
     * The cached value of the '{@link #getPrefetchGroup() <em>Prefetch Group</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPrefetchGroup()
     * @generated
     * @ordered
     */
    protected PrefetchGroupType prefetchGroup = null;

    /**
     * The cached value of the '{@link #getEjbRef() <em>Ejb Ref</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEjbRef()
     * @generated
     * @ordered
     */
    protected EList ejbRef = null;

    /**
     * The cached value of the '{@link #getEjbLocalRef() <em>Ejb Local Ref</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEjbLocalRef()
     * @generated
     * @ordered
     */
    protected EList ejbLocalRef = null;

    /**
     * The cached value of the '{@link #getServiceRef() <em>Service Ref</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getServiceRef()
     * @generated
     * @ordered
     */
    protected EList serviceRef = null;

    /**
     * The cached value of the '{@link #getResourceRef() <em>Resource Ref</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getResourceRef()
     * @generated
     * @ordered
     */
    protected EList resourceRef = null;

    /**
     * The cached value of the '{@link #getResourceEnvRef() <em>Resource Env Ref</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getResourceEnvRef()
     * @generated
     * @ordered
     */
    protected EList resourceEnvRef = null;

    /**
     * The cached value of the '{@link #getQuery() <em>Query</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getQuery()
     * @generated
     * @ordered
     */
    protected EList query = null;

    /**
     * The default value of the '{@link #getId() <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getId()
     * @generated
     * @ordered
     */
    protected static final String ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getId()
     * @generated
     * @ordered
     */
    protected String id = ID_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EntityBeanTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getEntityBeanType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getEjbName() {
        return ejbName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEjbName(String newEjbName) {
        String oldEjbName = ejbName;
        ejbName = newEjbName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.ENTITY_BEAN_TYPE__EJB_NAME, oldEjbName, ejbName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getJndiName() {
        if (jndiName == null) {
            jndiName = new EDataTypeEList(String.class, this, JarPackage.ENTITY_BEAN_TYPE__JNDI_NAME);
        }
        return jndiName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getLocalJndiName() {
        if (localJndiName == null) {
            localJndiName = new EDataTypeEList(String.class, this, JarPackage.ENTITY_BEAN_TYPE__LOCAL_JNDI_NAME);
        }
        return localJndiName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getTssTargetName() {
        return tssTargetName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setTssTargetName(String newTssTargetName) {
        String oldTssTargetName = tssTargetName;
        tssTargetName = newTssTargetName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.ENTITY_BEAN_TYPE__TSS_TARGET_NAME, oldTssTargetName, tssTargetName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getTssLink() {
        return tssLink;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setTssLink(String newTssLink) {
        String oldTssLink = tssLink;
        tssLink = newTssLink;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.ENTITY_BEAN_TYPE__TSS_LINK, oldTssLink, tssLink));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public TssType getTss() {
        return tss;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetTss(TssType newTss, NotificationChain msgs) {
        TssType oldTss = tss;
        tss = newTss;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.ENTITY_BEAN_TYPE__TSS, oldTss, newTss);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setTss(TssType newTss) {
        if (newTss != tss) {
            NotificationChain msgs = null;
            if (tss != null)
                msgs = ((InternalEObject)tss).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.ENTITY_BEAN_TYPE__TSS, null, msgs);
            if (newTss != null)
                msgs = ((InternalEObject)newTss).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.ENTITY_BEAN_TYPE__TSS, null, msgs);
            msgs = basicSetTss(newTss, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.ENTITY_BEAN_TYPE__TSS, newTss, newTss));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setTableName(String newTableName) {
        String oldTableName = tableName;
        tableName = newTableName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.ENTITY_BEAN_TYPE__TABLE_NAME, oldTableName, tableName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getCmpFieldMapping() {
        if (cmpFieldMapping == null) {
            cmpFieldMapping = new EObjectContainmentEList(CmpFieldMappingType.class, this, JarPackage.ENTITY_BEAN_TYPE__CMP_FIELD_MAPPING);
        }
        return cmpFieldMapping;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getPrimkeyField() {
        return primkeyField;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setPrimkeyField(String newPrimkeyField) {
        String oldPrimkeyField = primkeyField;
        primkeyField = newPrimkeyField;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.ENTITY_BEAN_TYPE__PRIMKEY_FIELD, oldPrimkeyField, primkeyField));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public KeyGeneratorType getKeyGenerator() {
        return keyGenerator;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetKeyGenerator(KeyGeneratorType newKeyGenerator, NotificationChain msgs) {
        KeyGeneratorType oldKeyGenerator = keyGenerator;
        keyGenerator = newKeyGenerator;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.ENTITY_BEAN_TYPE__KEY_GENERATOR, oldKeyGenerator, newKeyGenerator);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setKeyGenerator(KeyGeneratorType newKeyGenerator) {
        if (newKeyGenerator != keyGenerator) {
            NotificationChain msgs = null;
            if (keyGenerator != null)
                msgs = ((InternalEObject)keyGenerator).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.ENTITY_BEAN_TYPE__KEY_GENERATOR, null, msgs);
            if (newKeyGenerator != null)
                msgs = ((InternalEObject)newKeyGenerator).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.ENTITY_BEAN_TYPE__KEY_GENERATOR, null, msgs);
            msgs = basicSetKeyGenerator(newKeyGenerator, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.ENTITY_BEAN_TYPE__KEY_GENERATOR, newKeyGenerator, newKeyGenerator));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PrefetchGroupType getPrefetchGroup() {
        return prefetchGroup;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetPrefetchGroup(PrefetchGroupType newPrefetchGroup, NotificationChain msgs) {
        PrefetchGroupType oldPrefetchGroup = prefetchGroup;
        prefetchGroup = newPrefetchGroup;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.ENTITY_BEAN_TYPE__PREFETCH_GROUP, oldPrefetchGroup, newPrefetchGroup);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setPrefetchGroup(PrefetchGroupType newPrefetchGroup) {
        if (newPrefetchGroup != prefetchGroup) {
            NotificationChain msgs = null;
            if (prefetchGroup != null)
                msgs = ((InternalEObject)prefetchGroup).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.ENTITY_BEAN_TYPE__PREFETCH_GROUP, null, msgs);
            if (newPrefetchGroup != null)
                msgs = ((InternalEObject)newPrefetchGroup).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.ENTITY_BEAN_TYPE__PREFETCH_GROUP, null, msgs);
            msgs = basicSetPrefetchGroup(newPrefetchGroup, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.ENTITY_BEAN_TYPE__PREFETCH_GROUP, newPrefetchGroup, newPrefetchGroup));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new EObjectContainmentEList(EjbRefType.class, this, JarPackage.ENTITY_BEAN_TYPE__EJB_REF);
        }
        return ejbRef;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new EObjectContainmentEList(EjbLocalRefType.class, this, JarPackage.ENTITY_BEAN_TYPE__EJB_LOCAL_REF);
        }
        return ejbLocalRef;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new EObjectContainmentEList(ServiceRefType.class, this, JarPackage.ENTITY_BEAN_TYPE__SERVICE_REF);
        }
        return serviceRef;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new EObjectContainmentEList(ResourceRefType.class, this, JarPackage.ENTITY_BEAN_TYPE__RESOURCE_REF);
        }
        return resourceRef;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new EObjectContainmentEList(ResourceEnvRefType.class, this, JarPackage.ENTITY_BEAN_TYPE__RESOURCE_ENV_REF);
        }
        return resourceEnvRef;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getQuery() {
        if (query == null) {
            query = new EObjectContainmentEList(QueryType.class, this, JarPackage.ENTITY_BEAN_TYPE__QUERY);
        }
        return query;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getId() {
        return id;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setId(String newId) {
        String oldId = id;
        id = newId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.ENTITY_BEAN_TYPE__ID, oldId, id));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case JarPackage.ENTITY_BEAN_TYPE__TSS:
                    return basicSetTss(null, msgs);
                case JarPackage.ENTITY_BEAN_TYPE__CMP_FIELD_MAPPING:
                    return ((InternalEList)getCmpFieldMapping()).basicRemove(otherEnd, msgs);
                case JarPackage.ENTITY_BEAN_TYPE__KEY_GENERATOR:
                    return basicSetKeyGenerator(null, msgs);
                case JarPackage.ENTITY_BEAN_TYPE__PREFETCH_GROUP:
                    return basicSetPrefetchGroup(null, msgs);
                case JarPackage.ENTITY_BEAN_TYPE__EJB_REF:
                    return ((InternalEList)getEjbRef()).basicRemove(otherEnd, msgs);
                case JarPackage.ENTITY_BEAN_TYPE__EJB_LOCAL_REF:
                    return ((InternalEList)getEjbLocalRef()).basicRemove(otherEnd, msgs);
                case JarPackage.ENTITY_BEAN_TYPE__SERVICE_REF:
                    return ((InternalEList)getServiceRef()).basicRemove(otherEnd, msgs);
                case JarPackage.ENTITY_BEAN_TYPE__RESOURCE_REF:
                    return ((InternalEList)getResourceRef()).basicRemove(otherEnd, msgs);
                case JarPackage.ENTITY_BEAN_TYPE__RESOURCE_ENV_REF:
                    return ((InternalEList)getResourceEnvRef()).basicRemove(otherEnd, msgs);
                case JarPackage.ENTITY_BEAN_TYPE__QUERY:
                    return ((InternalEList)getQuery()).basicRemove(otherEnd, msgs);
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
            case JarPackage.ENTITY_BEAN_TYPE__EJB_NAME:
                return getEjbName();
            case JarPackage.ENTITY_BEAN_TYPE__JNDI_NAME:
                return getJndiName();
            case JarPackage.ENTITY_BEAN_TYPE__LOCAL_JNDI_NAME:
                return getLocalJndiName();
            case JarPackage.ENTITY_BEAN_TYPE__TSS_TARGET_NAME:
                return getTssTargetName();
            case JarPackage.ENTITY_BEAN_TYPE__TSS_LINK:
                return getTssLink();
            case JarPackage.ENTITY_BEAN_TYPE__TSS:
                return getTss();
            case JarPackage.ENTITY_BEAN_TYPE__TABLE_NAME:
                return getTableName();
            case JarPackage.ENTITY_BEAN_TYPE__CMP_FIELD_MAPPING:
                return getCmpFieldMapping();
            case JarPackage.ENTITY_BEAN_TYPE__PRIMKEY_FIELD:
                return getPrimkeyField();
            case JarPackage.ENTITY_BEAN_TYPE__KEY_GENERATOR:
                return getKeyGenerator();
            case JarPackage.ENTITY_BEAN_TYPE__PREFETCH_GROUP:
                return getPrefetchGroup();
            case JarPackage.ENTITY_BEAN_TYPE__EJB_REF:
                return getEjbRef();
            case JarPackage.ENTITY_BEAN_TYPE__EJB_LOCAL_REF:
                return getEjbLocalRef();
            case JarPackage.ENTITY_BEAN_TYPE__SERVICE_REF:
                return getServiceRef();
            case JarPackage.ENTITY_BEAN_TYPE__RESOURCE_REF:
                return getResourceRef();
            case JarPackage.ENTITY_BEAN_TYPE__RESOURCE_ENV_REF:
                return getResourceEnvRef();
            case JarPackage.ENTITY_BEAN_TYPE__QUERY:
                return getQuery();
            case JarPackage.ENTITY_BEAN_TYPE__ID:
                return getId();
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
            case JarPackage.ENTITY_BEAN_TYPE__EJB_NAME:
                setEjbName((String)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__JNDI_NAME:
                getJndiName().clear();
                getJndiName().addAll((Collection)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__LOCAL_JNDI_NAME:
                getLocalJndiName().clear();
                getLocalJndiName().addAll((Collection)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__TSS_TARGET_NAME:
                setTssTargetName((String)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__TSS_LINK:
                setTssLink((String)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__TSS:
                setTss((TssType)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__TABLE_NAME:
                setTableName((String)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__CMP_FIELD_MAPPING:
                getCmpFieldMapping().clear();
                getCmpFieldMapping().addAll((Collection)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__PRIMKEY_FIELD:
                setPrimkeyField((String)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__KEY_GENERATOR:
                setKeyGenerator((KeyGeneratorType)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__PREFETCH_GROUP:
                setPrefetchGroup((PrefetchGroupType)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__EJB_REF:
                getEjbRef().clear();
                getEjbRef().addAll((Collection)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__EJB_LOCAL_REF:
                getEjbLocalRef().clear();
                getEjbLocalRef().addAll((Collection)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__SERVICE_REF:
                getServiceRef().clear();
                getServiceRef().addAll((Collection)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__RESOURCE_REF:
                getResourceRef().clear();
                getResourceRef().addAll((Collection)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__RESOURCE_ENV_REF:
                getResourceEnvRef().clear();
                getResourceEnvRef().addAll((Collection)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__QUERY:
                getQuery().clear();
                getQuery().addAll((Collection)newValue);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__ID:
                setId((String)newValue);
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
            case JarPackage.ENTITY_BEAN_TYPE__EJB_NAME:
                setEjbName(EJB_NAME_EDEFAULT);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__JNDI_NAME:
                getJndiName().clear();
                return;
            case JarPackage.ENTITY_BEAN_TYPE__LOCAL_JNDI_NAME:
                getLocalJndiName().clear();
                return;
            case JarPackage.ENTITY_BEAN_TYPE__TSS_TARGET_NAME:
                setTssTargetName(TSS_TARGET_NAME_EDEFAULT);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__TSS_LINK:
                setTssLink(TSS_LINK_EDEFAULT);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__TSS:
                setTss((TssType)null);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__TABLE_NAME:
                setTableName(TABLE_NAME_EDEFAULT);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__CMP_FIELD_MAPPING:
                getCmpFieldMapping().clear();
                return;
            case JarPackage.ENTITY_BEAN_TYPE__PRIMKEY_FIELD:
                setPrimkeyField(PRIMKEY_FIELD_EDEFAULT);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__KEY_GENERATOR:
                setKeyGenerator((KeyGeneratorType)null);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__PREFETCH_GROUP:
                setPrefetchGroup((PrefetchGroupType)null);
                return;
            case JarPackage.ENTITY_BEAN_TYPE__EJB_REF:
                getEjbRef().clear();
                return;
            case JarPackage.ENTITY_BEAN_TYPE__EJB_LOCAL_REF:
                getEjbLocalRef().clear();
                return;
            case JarPackage.ENTITY_BEAN_TYPE__SERVICE_REF:
                getServiceRef().clear();
                return;
            case JarPackage.ENTITY_BEAN_TYPE__RESOURCE_REF:
                getResourceRef().clear();
                return;
            case JarPackage.ENTITY_BEAN_TYPE__RESOURCE_ENV_REF:
                getResourceEnvRef().clear();
                return;
            case JarPackage.ENTITY_BEAN_TYPE__QUERY:
                getQuery().clear();
                return;
            case JarPackage.ENTITY_BEAN_TYPE__ID:
                setId(ID_EDEFAULT);
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
            case JarPackage.ENTITY_BEAN_TYPE__EJB_NAME:
                return EJB_NAME_EDEFAULT == null ? ejbName != null : !EJB_NAME_EDEFAULT.equals(ejbName);
            case JarPackage.ENTITY_BEAN_TYPE__JNDI_NAME:
                return jndiName != null && !jndiName.isEmpty();
            case JarPackage.ENTITY_BEAN_TYPE__LOCAL_JNDI_NAME:
                return localJndiName != null && !localJndiName.isEmpty();
            case JarPackage.ENTITY_BEAN_TYPE__TSS_TARGET_NAME:
                return TSS_TARGET_NAME_EDEFAULT == null ? tssTargetName != null : !TSS_TARGET_NAME_EDEFAULT.equals(tssTargetName);
            case JarPackage.ENTITY_BEAN_TYPE__TSS_LINK:
                return TSS_LINK_EDEFAULT == null ? tssLink != null : !TSS_LINK_EDEFAULT.equals(tssLink);
            case JarPackage.ENTITY_BEAN_TYPE__TSS:
                return tss != null;
            case JarPackage.ENTITY_BEAN_TYPE__TABLE_NAME:
                return TABLE_NAME_EDEFAULT == null ? tableName != null : !TABLE_NAME_EDEFAULT.equals(tableName);
            case JarPackage.ENTITY_BEAN_TYPE__CMP_FIELD_MAPPING:
                return cmpFieldMapping != null && !cmpFieldMapping.isEmpty();
            case JarPackage.ENTITY_BEAN_TYPE__PRIMKEY_FIELD:
                return PRIMKEY_FIELD_EDEFAULT == null ? primkeyField != null : !PRIMKEY_FIELD_EDEFAULT.equals(primkeyField);
            case JarPackage.ENTITY_BEAN_TYPE__KEY_GENERATOR:
                return keyGenerator != null;
            case JarPackage.ENTITY_BEAN_TYPE__PREFETCH_GROUP:
                return prefetchGroup != null;
            case JarPackage.ENTITY_BEAN_TYPE__EJB_REF:
                return ejbRef != null && !ejbRef.isEmpty();
            case JarPackage.ENTITY_BEAN_TYPE__EJB_LOCAL_REF:
                return ejbLocalRef != null && !ejbLocalRef.isEmpty();
            case JarPackage.ENTITY_BEAN_TYPE__SERVICE_REF:
                return serviceRef != null && !serviceRef.isEmpty();
            case JarPackage.ENTITY_BEAN_TYPE__RESOURCE_REF:
                return resourceRef != null && !resourceRef.isEmpty();
            case JarPackage.ENTITY_BEAN_TYPE__RESOURCE_ENV_REF:
                return resourceEnvRef != null && !resourceEnvRef.isEmpty();
            case JarPackage.ENTITY_BEAN_TYPE__QUERY:
                return query != null && !query.isEmpty();
            case JarPackage.ENTITY_BEAN_TYPE__ID:
                return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
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
        result.append(" (ejbName: ");
        result.append(ejbName);
        result.append(", jndiName: ");
        result.append(jndiName);
        result.append(", localJndiName: ");
        result.append(localJndiName);
        result.append(", tssTargetName: ");
        result.append(tssTargetName);
        result.append(", tssLink: ");
        result.append(tssLink);
        result.append(", tableName: ");
        result.append(tableName);
        result.append(", primkeyField: ");
        result.append(primkeyField);
        result.append(", id: ");
        result.append(id);
        result.append(')');
        return result.toString();
    }

} //EntityBeanTypeImpl
