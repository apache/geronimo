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
package org.apache.geronimo.xml.ns.j2ee.application.impl;

import java.util.Collection;

import org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage;
import org.apache.geronimo.xml.ns.j2ee.application.ModuleType;
import org.apache.geronimo.xml.ns.j2ee.application.PathType;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Module Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ModuleTypeImpl#getConnector <em>Connector</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ModuleTypeImpl#getEjb <em>Ejb</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ModuleTypeImpl#getJava <em>Java</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ModuleTypeImpl#getWeb <em>Web</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ModuleTypeImpl#getAltDd <em>Alt Dd</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ModuleTypeImpl#getAny <em>Any</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ModuleTypeImpl extends EObjectImpl implements ModuleType {
    /**
     * The cached value of the '{@link #getConnector() <em>Connector</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getConnector()
     * @generated
     * @ordered
     */
    protected PathType connector = null;

    /**
     * The cached value of the '{@link #getEjb() <em>Ejb</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEjb()
     * @generated
     * @ordered
     */
    protected PathType ejb = null;

    /**
     * The cached value of the '{@link #getJava() <em>Java</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getJava()
     * @generated
     * @ordered
     */
    protected PathType java = null;

    /**
     * The cached value of the '{@link #getWeb() <em>Web</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getWeb()
     * @generated
     * @ordered
     */
    protected PathType web = null;

    /**
     * The cached value of the '{@link #getAltDd() <em>Alt Dd</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAltDd()
     * @generated
     * @ordered
     */
    protected PathType altDd = null;

    /**
     * The cached value of the '{@link #getAny() <em>Any</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAny()
     * @generated
     * @ordered
     */
    protected FeatureMap any = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected ModuleTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return ApplicationPackage.eINSTANCE.getModuleType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PathType getConnector() {
        return connector;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetConnector(PathType newConnector, NotificationChain msgs) {
        PathType oldConnector = connector;
        connector = newConnector;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.MODULE_TYPE__CONNECTOR, oldConnector, newConnector);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setConnector(PathType newConnector) {
        if (newConnector != connector) {
            NotificationChain msgs = null;
            if (connector != null)
                msgs = ((InternalEObject)connector).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MODULE_TYPE__CONNECTOR, null, msgs);
            if (newConnector != null)
                msgs = ((InternalEObject)newConnector).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MODULE_TYPE__CONNECTOR, null, msgs);
            msgs = basicSetConnector(newConnector, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MODULE_TYPE__CONNECTOR, newConnector, newConnector));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PathType getEjb() {
        return ejb;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetEjb(PathType newEjb, NotificationChain msgs) {
        PathType oldEjb = ejb;
        ejb = newEjb;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.MODULE_TYPE__EJB, oldEjb, newEjb);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEjb(PathType newEjb) {
        if (newEjb != ejb) {
            NotificationChain msgs = null;
            if (ejb != null)
                msgs = ((InternalEObject)ejb).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MODULE_TYPE__EJB, null, msgs);
            if (newEjb != null)
                msgs = ((InternalEObject)newEjb).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MODULE_TYPE__EJB, null, msgs);
            msgs = basicSetEjb(newEjb, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MODULE_TYPE__EJB, newEjb, newEjb));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PathType getJava() {
        return java;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetJava(PathType newJava, NotificationChain msgs) {
        PathType oldJava = java;
        java = newJava;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.MODULE_TYPE__JAVA, oldJava, newJava);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setJava(PathType newJava) {
        if (newJava != java) {
            NotificationChain msgs = null;
            if (java != null)
                msgs = ((InternalEObject)java).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MODULE_TYPE__JAVA, null, msgs);
            if (newJava != null)
                msgs = ((InternalEObject)newJava).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MODULE_TYPE__JAVA, null, msgs);
            msgs = basicSetJava(newJava, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MODULE_TYPE__JAVA, newJava, newJava));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PathType getWeb() {
        return web;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetWeb(PathType newWeb, NotificationChain msgs) {
        PathType oldWeb = web;
        web = newWeb;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.MODULE_TYPE__WEB, oldWeb, newWeb);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setWeb(PathType newWeb) {
        if (newWeb != web) {
            NotificationChain msgs = null;
            if (web != null)
                msgs = ((InternalEObject)web).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MODULE_TYPE__WEB, null, msgs);
            if (newWeb != null)
                msgs = ((InternalEObject)newWeb).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MODULE_TYPE__WEB, null, msgs);
            msgs = basicSetWeb(newWeb, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MODULE_TYPE__WEB, newWeb, newWeb));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PathType getAltDd() {
        return altDd;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetAltDd(PathType newAltDd, NotificationChain msgs) {
        PathType oldAltDd = altDd;
        altDd = newAltDd;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.MODULE_TYPE__ALT_DD, oldAltDd, newAltDd);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setAltDd(PathType newAltDd) {
        if (newAltDd != altDd) {
            NotificationChain msgs = null;
            if (altDd != null)
                msgs = ((InternalEObject)altDd).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MODULE_TYPE__ALT_DD, null, msgs);
            if (newAltDd != null)
                msgs = ((InternalEObject)newAltDd).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MODULE_TYPE__ALT_DD, null, msgs);
            msgs = basicSetAltDd(newAltDd, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MODULE_TYPE__ALT_DD, newAltDd, newAltDd));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FeatureMap getAny() {
        if (any == null) {
            any = new BasicFeatureMap(this, ApplicationPackage.MODULE_TYPE__ANY);
        }
        return any;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case ApplicationPackage.MODULE_TYPE__CONNECTOR:
                    return basicSetConnector(null, msgs);
                case ApplicationPackage.MODULE_TYPE__EJB:
                    return basicSetEjb(null, msgs);
                case ApplicationPackage.MODULE_TYPE__JAVA:
                    return basicSetJava(null, msgs);
                case ApplicationPackage.MODULE_TYPE__WEB:
                    return basicSetWeb(null, msgs);
                case ApplicationPackage.MODULE_TYPE__ALT_DD:
                    return basicSetAltDd(null, msgs);
                case ApplicationPackage.MODULE_TYPE__ANY:
                    return ((InternalEList)getAny()).basicRemove(otherEnd, msgs);
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
            case ApplicationPackage.MODULE_TYPE__CONNECTOR:
                return getConnector();
            case ApplicationPackage.MODULE_TYPE__EJB:
                return getEjb();
            case ApplicationPackage.MODULE_TYPE__JAVA:
                return getJava();
            case ApplicationPackage.MODULE_TYPE__WEB:
                return getWeb();
            case ApplicationPackage.MODULE_TYPE__ALT_DD:
                return getAltDd();
            case ApplicationPackage.MODULE_TYPE__ANY:
                return getAny();
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
            case ApplicationPackage.MODULE_TYPE__CONNECTOR:
                setConnector((PathType)newValue);
                return;
            case ApplicationPackage.MODULE_TYPE__EJB:
                setEjb((PathType)newValue);
                return;
            case ApplicationPackage.MODULE_TYPE__JAVA:
                setJava((PathType)newValue);
                return;
            case ApplicationPackage.MODULE_TYPE__WEB:
                setWeb((PathType)newValue);
                return;
            case ApplicationPackage.MODULE_TYPE__ALT_DD:
                setAltDd((PathType)newValue);
                return;
            case ApplicationPackage.MODULE_TYPE__ANY:
                getAny().clear();
                getAny().addAll((Collection)newValue);
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
            case ApplicationPackage.MODULE_TYPE__CONNECTOR:
                setConnector((PathType)null);
                return;
            case ApplicationPackage.MODULE_TYPE__EJB:
                setEjb((PathType)null);
                return;
            case ApplicationPackage.MODULE_TYPE__JAVA:
                setJava((PathType)null);
                return;
            case ApplicationPackage.MODULE_TYPE__WEB:
                setWeb((PathType)null);
                return;
            case ApplicationPackage.MODULE_TYPE__ALT_DD:
                setAltDd((PathType)null);
                return;
            case ApplicationPackage.MODULE_TYPE__ANY:
                getAny().clear();
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
            case ApplicationPackage.MODULE_TYPE__CONNECTOR:
                return connector != null;
            case ApplicationPackage.MODULE_TYPE__EJB:
                return ejb != null;
            case ApplicationPackage.MODULE_TYPE__JAVA:
                return java != null;
            case ApplicationPackage.MODULE_TYPE__WEB:
                return web != null;
            case ApplicationPackage.MODULE_TYPE__ALT_DD:
                return altDd != null;
            case ApplicationPackage.MODULE_TYPE__ANY:
                return any != null && !any.isEmpty();
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
        result.append(" (any: ");
        result.append(any);
        result.append(')');
        return result.toString();
    }

} //ModuleTypeImpl
