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

import org.openejb.xml.ns.openejb.jar.JarPackage;
import org.openejb.xml.ns.openejb.jar.SessionBeanType;
import org.openejb.xml.ns.openejb.jar.TssType;
import org.openejb.xml.ns.openejb.jar.WebServiceSecurityType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Session Bean Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getEjbName <em>Ejb Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getJndiName <em>Jndi Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getLocalJndiName <em>Local Jndi Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getTssTargetName <em>Tss Target Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getTssLink <em>Tss Link</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getTss <em>Tss</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getEjbRef <em>Ejb Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getEjbLocalRef <em>Ejb Local Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getServiceRef <em>Service Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getResourceRef <em>Resource Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getResourceEnvRef <em>Resource Env Ref</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getWebServiceAddress <em>Web Service Address</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getWebServiceSecurity <em>Web Service Security</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl#getId <em>Id</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SessionBeanTypeImpl extends EObjectImpl implements SessionBeanType {
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
     * The default value of the '{@link #getWebServiceAddress() <em>Web Service Address</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getWebServiceAddress()
     * @generated
     * @ordered
     */
    protected static final String WEB_SERVICE_ADDRESS_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getWebServiceAddress() <em>Web Service Address</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getWebServiceAddress()
     * @generated
     * @ordered
     */
    protected String webServiceAddress = WEB_SERVICE_ADDRESS_EDEFAULT;

    /**
     * The cached value of the '{@link #getWebServiceSecurity() <em>Web Service Security</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getWebServiceSecurity()
     * @generated
     * @ordered
     */
    protected WebServiceSecurityType webServiceSecurity = null;

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
    protected SessionBeanTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getSessionBeanType();
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
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.SESSION_BEAN_TYPE__EJB_NAME, oldEjbName, ejbName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getJndiName() {
        if (jndiName == null) {
            jndiName = new EDataTypeEList(String.class, this, JarPackage.SESSION_BEAN_TYPE__JNDI_NAME);
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
            localJndiName = new EDataTypeEList(String.class, this, JarPackage.SESSION_BEAN_TYPE__LOCAL_JNDI_NAME);
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
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.SESSION_BEAN_TYPE__TSS_TARGET_NAME, oldTssTargetName, tssTargetName));
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
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.SESSION_BEAN_TYPE__TSS_LINK, oldTssLink, tssLink));
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
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.SESSION_BEAN_TYPE__TSS, oldTss, newTss);
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
                msgs = ((InternalEObject)tss).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.SESSION_BEAN_TYPE__TSS, null, msgs);
            if (newTss != null)
                msgs = ((InternalEObject)newTss).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.SESSION_BEAN_TYPE__TSS, null, msgs);
            msgs = basicSetTss(newTss, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.SESSION_BEAN_TYPE__TSS, newTss, newTss));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new EObjectContainmentEList(EjbRefType.class, this, JarPackage.SESSION_BEAN_TYPE__EJB_REF);
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
            ejbLocalRef = new EObjectContainmentEList(EjbLocalRefType.class, this, JarPackage.SESSION_BEAN_TYPE__EJB_LOCAL_REF);
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
            serviceRef = new EObjectContainmentEList(ServiceRefType.class, this, JarPackage.SESSION_BEAN_TYPE__SERVICE_REF);
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
            resourceRef = new EObjectContainmentEList(ResourceRefType.class, this, JarPackage.SESSION_BEAN_TYPE__RESOURCE_REF);
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
            resourceEnvRef = new EObjectContainmentEList(ResourceEnvRefType.class, this, JarPackage.SESSION_BEAN_TYPE__RESOURCE_ENV_REF);
        }
        return resourceEnvRef;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getWebServiceAddress() {
        return webServiceAddress;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setWebServiceAddress(String newWebServiceAddress) {
        String oldWebServiceAddress = webServiceAddress;
        webServiceAddress = newWebServiceAddress;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_ADDRESS, oldWebServiceAddress, webServiceAddress));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public WebServiceSecurityType getWebServiceSecurity() {
        return webServiceSecurity;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetWebServiceSecurity(WebServiceSecurityType newWebServiceSecurity, NotificationChain msgs) {
        WebServiceSecurityType oldWebServiceSecurity = webServiceSecurity;
        webServiceSecurity = newWebServiceSecurity;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_SECURITY, oldWebServiceSecurity, newWebServiceSecurity);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setWebServiceSecurity(WebServiceSecurityType newWebServiceSecurity) {
        if (newWebServiceSecurity != webServiceSecurity) {
            NotificationChain msgs = null;
            if (webServiceSecurity != null)
                msgs = ((InternalEObject)webServiceSecurity).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_SECURITY, null, msgs);
            if (newWebServiceSecurity != null)
                msgs = ((InternalEObject)newWebServiceSecurity).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_SECURITY, null, msgs);
            msgs = basicSetWebServiceSecurity(newWebServiceSecurity, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_SECURITY, newWebServiceSecurity, newWebServiceSecurity));
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
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.SESSION_BEAN_TYPE__ID, oldId, id));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case JarPackage.SESSION_BEAN_TYPE__TSS:
                    return basicSetTss(null, msgs);
                case JarPackage.SESSION_BEAN_TYPE__EJB_REF:
                    return ((InternalEList)getEjbRef()).basicRemove(otherEnd, msgs);
                case JarPackage.SESSION_BEAN_TYPE__EJB_LOCAL_REF:
                    return ((InternalEList)getEjbLocalRef()).basicRemove(otherEnd, msgs);
                case JarPackage.SESSION_BEAN_TYPE__SERVICE_REF:
                    return ((InternalEList)getServiceRef()).basicRemove(otherEnd, msgs);
                case JarPackage.SESSION_BEAN_TYPE__RESOURCE_REF:
                    return ((InternalEList)getResourceRef()).basicRemove(otherEnd, msgs);
                case JarPackage.SESSION_BEAN_TYPE__RESOURCE_ENV_REF:
                    return ((InternalEList)getResourceEnvRef()).basicRemove(otherEnd, msgs);
                case JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_SECURITY:
                    return basicSetWebServiceSecurity(null, msgs);
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
            case JarPackage.SESSION_BEAN_TYPE__EJB_NAME:
                return getEjbName();
            case JarPackage.SESSION_BEAN_TYPE__JNDI_NAME:
                return getJndiName();
            case JarPackage.SESSION_BEAN_TYPE__LOCAL_JNDI_NAME:
                return getLocalJndiName();
            case JarPackage.SESSION_BEAN_TYPE__TSS_TARGET_NAME:
                return getTssTargetName();
            case JarPackage.SESSION_BEAN_TYPE__TSS_LINK:
                return getTssLink();
            case JarPackage.SESSION_BEAN_TYPE__TSS:
                return getTss();
            case JarPackage.SESSION_BEAN_TYPE__EJB_REF:
                return getEjbRef();
            case JarPackage.SESSION_BEAN_TYPE__EJB_LOCAL_REF:
                return getEjbLocalRef();
            case JarPackage.SESSION_BEAN_TYPE__SERVICE_REF:
                return getServiceRef();
            case JarPackage.SESSION_BEAN_TYPE__RESOURCE_REF:
                return getResourceRef();
            case JarPackage.SESSION_BEAN_TYPE__RESOURCE_ENV_REF:
                return getResourceEnvRef();
            case JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_ADDRESS:
                return getWebServiceAddress();
            case JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_SECURITY:
                return getWebServiceSecurity();
            case JarPackage.SESSION_BEAN_TYPE__ID:
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
            case JarPackage.SESSION_BEAN_TYPE__EJB_NAME:
                setEjbName((String)newValue);
                return;
            case JarPackage.SESSION_BEAN_TYPE__JNDI_NAME:
                getJndiName().clear();
                getJndiName().addAll((Collection)newValue);
                return;
            case JarPackage.SESSION_BEAN_TYPE__LOCAL_JNDI_NAME:
                getLocalJndiName().clear();
                getLocalJndiName().addAll((Collection)newValue);
                return;
            case JarPackage.SESSION_BEAN_TYPE__TSS_TARGET_NAME:
                setTssTargetName((String)newValue);
                return;
            case JarPackage.SESSION_BEAN_TYPE__TSS_LINK:
                setTssLink((String)newValue);
                return;
            case JarPackage.SESSION_BEAN_TYPE__TSS:
                setTss((TssType)newValue);
                return;
            case JarPackage.SESSION_BEAN_TYPE__EJB_REF:
                getEjbRef().clear();
                getEjbRef().addAll((Collection)newValue);
                return;
            case JarPackage.SESSION_BEAN_TYPE__EJB_LOCAL_REF:
                getEjbLocalRef().clear();
                getEjbLocalRef().addAll((Collection)newValue);
                return;
            case JarPackage.SESSION_BEAN_TYPE__SERVICE_REF:
                getServiceRef().clear();
                getServiceRef().addAll((Collection)newValue);
                return;
            case JarPackage.SESSION_BEAN_TYPE__RESOURCE_REF:
                getResourceRef().clear();
                getResourceRef().addAll((Collection)newValue);
                return;
            case JarPackage.SESSION_BEAN_TYPE__RESOURCE_ENV_REF:
                getResourceEnvRef().clear();
                getResourceEnvRef().addAll((Collection)newValue);
                return;
            case JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_ADDRESS:
                setWebServiceAddress((String)newValue);
                return;
            case JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_SECURITY:
                setWebServiceSecurity((WebServiceSecurityType)newValue);
                return;
            case JarPackage.SESSION_BEAN_TYPE__ID:
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
            case JarPackage.SESSION_BEAN_TYPE__EJB_NAME:
                setEjbName(EJB_NAME_EDEFAULT);
                return;
            case JarPackage.SESSION_BEAN_TYPE__JNDI_NAME:
                getJndiName().clear();
                return;
            case JarPackage.SESSION_BEAN_TYPE__LOCAL_JNDI_NAME:
                getLocalJndiName().clear();
                return;
            case JarPackage.SESSION_BEAN_TYPE__TSS_TARGET_NAME:
                setTssTargetName(TSS_TARGET_NAME_EDEFAULT);
                return;
            case JarPackage.SESSION_BEAN_TYPE__TSS_LINK:
                setTssLink(TSS_LINK_EDEFAULT);
                return;
            case JarPackage.SESSION_BEAN_TYPE__TSS:
                setTss((TssType)null);
                return;
            case JarPackage.SESSION_BEAN_TYPE__EJB_REF:
                getEjbRef().clear();
                return;
            case JarPackage.SESSION_BEAN_TYPE__EJB_LOCAL_REF:
                getEjbLocalRef().clear();
                return;
            case JarPackage.SESSION_BEAN_TYPE__SERVICE_REF:
                getServiceRef().clear();
                return;
            case JarPackage.SESSION_BEAN_TYPE__RESOURCE_REF:
                getResourceRef().clear();
                return;
            case JarPackage.SESSION_BEAN_TYPE__RESOURCE_ENV_REF:
                getResourceEnvRef().clear();
                return;
            case JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_ADDRESS:
                setWebServiceAddress(WEB_SERVICE_ADDRESS_EDEFAULT);
                return;
            case JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_SECURITY:
                setWebServiceSecurity((WebServiceSecurityType)null);
                return;
            case JarPackage.SESSION_BEAN_TYPE__ID:
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
            case JarPackage.SESSION_BEAN_TYPE__EJB_NAME:
                return EJB_NAME_EDEFAULT == null ? ejbName != null : !EJB_NAME_EDEFAULT.equals(ejbName);
            case JarPackage.SESSION_BEAN_TYPE__JNDI_NAME:
                return jndiName != null && !jndiName.isEmpty();
            case JarPackage.SESSION_BEAN_TYPE__LOCAL_JNDI_NAME:
                return localJndiName != null && !localJndiName.isEmpty();
            case JarPackage.SESSION_BEAN_TYPE__TSS_TARGET_NAME:
                return TSS_TARGET_NAME_EDEFAULT == null ? tssTargetName != null : !TSS_TARGET_NAME_EDEFAULT.equals(tssTargetName);
            case JarPackage.SESSION_BEAN_TYPE__TSS_LINK:
                return TSS_LINK_EDEFAULT == null ? tssLink != null : !TSS_LINK_EDEFAULT.equals(tssLink);
            case JarPackage.SESSION_BEAN_TYPE__TSS:
                return tss != null;
            case JarPackage.SESSION_BEAN_TYPE__EJB_REF:
                return ejbRef != null && !ejbRef.isEmpty();
            case JarPackage.SESSION_BEAN_TYPE__EJB_LOCAL_REF:
                return ejbLocalRef != null && !ejbLocalRef.isEmpty();
            case JarPackage.SESSION_BEAN_TYPE__SERVICE_REF:
                return serviceRef != null && !serviceRef.isEmpty();
            case JarPackage.SESSION_BEAN_TYPE__RESOURCE_REF:
                return resourceRef != null && !resourceRef.isEmpty();
            case JarPackage.SESSION_BEAN_TYPE__RESOURCE_ENV_REF:
                return resourceEnvRef != null && !resourceEnvRef.isEmpty();
            case JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_ADDRESS:
                return WEB_SERVICE_ADDRESS_EDEFAULT == null ? webServiceAddress != null : !WEB_SERVICE_ADDRESS_EDEFAULT.equals(webServiceAddress);
            case JarPackage.SESSION_BEAN_TYPE__WEB_SERVICE_SECURITY:
                return webServiceSecurity != null;
            case JarPackage.SESSION_BEAN_TYPE__ID:
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
        result.append(", webServiceAddress: ");
        result.append(webServiceAddress);
        result.append(", id: ");
        result.append(id);
        result.append(')');
        return result.toString();
    }

} //SessionBeanTypeImpl
