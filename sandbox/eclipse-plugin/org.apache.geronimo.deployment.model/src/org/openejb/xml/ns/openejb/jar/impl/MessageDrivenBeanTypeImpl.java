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
import org.apache.geronimo.xml.ns.naming.ResourceLocatorType;
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

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.openejb.xml.ns.openejb.jar.ActivationConfigType;
import org.openejb.xml.ns.openejb.jar.JarPackage;
import org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Message Driven Bean Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.MessageDrivenBeanTypeImpl#getEjbName <em>Ejb Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.MessageDrivenBeanTypeImpl#getResourceAdapter <em>Resource Adapter</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.MessageDrivenBeanTypeImpl#getActivationConfig <em>Activation Config</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.MessageDrivenBeanTypeImpl#getEjbRef <em>Ejb Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.MessageDrivenBeanTypeImpl#getEjbLocalRef <em>Ejb Local Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.MessageDrivenBeanTypeImpl#getServiceRef <em>Service Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.MessageDrivenBeanTypeImpl#getResourceRef <em>Resource Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.MessageDrivenBeanTypeImpl#getResourceEnvRef <em>Resource Env Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.MessageDrivenBeanTypeImpl#getId <em>Id</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MessageDrivenBeanTypeImpl extends EObjectImpl implements MessageDrivenBeanType {
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
     * The cached value of the '{@link #getResourceAdapter() <em>Resource Adapter</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getResourceAdapter()
     * @generated
     * @ordered
     */
    protected ResourceLocatorType resourceAdapter = null;

    /**
     * The cached value of the '{@link #getActivationConfig() <em>Activation Config</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getActivationConfig()
     * @generated
     * @ordered
     */
    protected ActivationConfigType activationConfig = null;

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
    protected MessageDrivenBeanTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getMessageDrivenBeanType();
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
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_NAME, oldEjbName, ejbName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ResourceLocatorType getResourceAdapter() {
        return resourceAdapter;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetResourceAdapter(ResourceLocatorType newResourceAdapter, NotificationChain msgs) {
        ResourceLocatorType oldResourceAdapter = resourceAdapter;
        resourceAdapter = newResourceAdapter;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ADAPTER, oldResourceAdapter, newResourceAdapter);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setResourceAdapter(ResourceLocatorType newResourceAdapter) {
        if (newResourceAdapter != resourceAdapter) {
            NotificationChain msgs = null;
            if (resourceAdapter != null)
                msgs = ((InternalEObject)resourceAdapter).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ADAPTER, null, msgs);
            if (newResourceAdapter != null)
                msgs = ((InternalEObject)newResourceAdapter).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ADAPTER, null, msgs);
            msgs = basicSetResourceAdapter(newResourceAdapter, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ADAPTER, newResourceAdapter, newResourceAdapter));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ActivationConfigType getActivationConfig() {
        return activationConfig;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetActivationConfig(ActivationConfigType newActivationConfig, NotificationChain msgs) {
        ActivationConfigType oldActivationConfig = activationConfig;
        activationConfig = newActivationConfig;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ACTIVATION_CONFIG, oldActivationConfig, newActivationConfig);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setActivationConfig(ActivationConfigType newActivationConfig) {
        if (newActivationConfig != activationConfig) {
            NotificationChain msgs = null;
            if (activationConfig != null)
                msgs = ((InternalEObject)activationConfig).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ACTIVATION_CONFIG, null, msgs);
            if (newActivationConfig != null)
                msgs = ((InternalEObject)newActivationConfig).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ACTIVATION_CONFIG, null, msgs);
            msgs = basicSetActivationConfig(newActivationConfig, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ACTIVATION_CONFIG, newActivationConfig, newActivationConfig));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new EObjectContainmentEList(EjbRefType.class, this, JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_REF);
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
            ejbLocalRef = new EObjectContainmentEList(EjbLocalRefType.class, this, JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_LOCAL_REF);
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
            serviceRef = new EObjectContainmentEList(ServiceRefType.class, this, JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__SERVICE_REF);
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
            resourceRef = new EObjectContainmentEList(ResourceRefType.class, this, JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_REF);
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
            resourceEnvRef = new EObjectContainmentEList(ResourceEnvRefType.class, this, JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ENV_REF);
        }
        return resourceEnvRef;
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
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ID, oldId, id));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ADAPTER:
                    return basicSetResourceAdapter(null, msgs);
                case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ACTIVATION_CONFIG:
                    return basicSetActivationConfig(null, msgs);
                case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_REF:
                    return ((InternalEList)getEjbRef()).basicRemove(otherEnd, msgs);
                case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_LOCAL_REF:
                    return ((InternalEList)getEjbLocalRef()).basicRemove(otherEnd, msgs);
                case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__SERVICE_REF:
                    return ((InternalEList)getServiceRef()).basicRemove(otherEnd, msgs);
                case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_REF:
                    return ((InternalEList)getResourceRef()).basicRemove(otherEnd, msgs);
                case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ENV_REF:
                    return ((InternalEList)getResourceEnvRef()).basicRemove(otherEnd, msgs);
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
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_NAME:
                return getEjbName();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ADAPTER:
                return getResourceAdapter();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ACTIVATION_CONFIG:
                return getActivationConfig();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_REF:
                return getEjbRef();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_LOCAL_REF:
                return getEjbLocalRef();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__SERVICE_REF:
                return getServiceRef();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_REF:
                return getResourceRef();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ENV_REF:
                return getResourceEnvRef();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ID:
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
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_NAME:
                setEjbName((String)newValue);
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ADAPTER:
                setResourceAdapter((ResourceLocatorType)newValue);
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ACTIVATION_CONFIG:
                setActivationConfig((ActivationConfigType)newValue);
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_REF:
                getEjbRef().clear();
                getEjbRef().addAll((Collection)newValue);
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_LOCAL_REF:
                getEjbLocalRef().clear();
                getEjbLocalRef().addAll((Collection)newValue);
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__SERVICE_REF:
                getServiceRef().clear();
                getServiceRef().addAll((Collection)newValue);
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_REF:
                getResourceRef().clear();
                getResourceRef().addAll((Collection)newValue);
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ENV_REF:
                getResourceEnvRef().clear();
                getResourceEnvRef().addAll((Collection)newValue);
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ID:
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
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_NAME:
                setEjbName(EJB_NAME_EDEFAULT);
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ADAPTER:
                setResourceAdapter((ResourceLocatorType)null);
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ACTIVATION_CONFIG:
                setActivationConfig((ActivationConfigType)null);
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_REF:
                getEjbRef().clear();
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_LOCAL_REF:
                getEjbLocalRef().clear();
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__SERVICE_REF:
                getServiceRef().clear();
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_REF:
                getResourceRef().clear();
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ENV_REF:
                getResourceEnvRef().clear();
                return;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ID:
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
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_NAME:
                return EJB_NAME_EDEFAULT == null ? ejbName != null : !EJB_NAME_EDEFAULT.equals(ejbName);
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ADAPTER:
                return resourceAdapter != null;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ACTIVATION_CONFIG:
                return activationConfig != null;
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_REF:
                return ejbRef != null && !ejbRef.isEmpty();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__EJB_LOCAL_REF:
                return ejbLocalRef != null && !ejbLocalRef.isEmpty();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__SERVICE_REF:
                return serviceRef != null && !serviceRef.isEmpty();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_REF:
                return resourceRef != null && !resourceRef.isEmpty();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ENV_REF:
                return resourceEnvRef != null && !resourceEnvRef.isEmpty();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE__ID:
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
        result.append(", id: ");
        result.append(id);
        result.append(')');
        return result.toString();
    }

} //MessageDrivenBeanTypeImpl
