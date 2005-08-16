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
package org.apache.geronimo.xml.ns.security.impl;

import java.util.Collection;

import org.apache.geronimo.xml.ns.security.DefaultPrincipalType;
import org.apache.geronimo.xml.ns.security.DescriptionType;
import org.apache.geronimo.xml.ns.security.RoleMappingsType;
import org.apache.geronimo.xml.ns.security.SecurityPackage;
import org.apache.geronimo.xml.ns.security.SecurityType;

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

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.SecurityTypeImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.SecurityTypeImpl#getDefaultPrincipal <em>Default Principal</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.SecurityTypeImpl#getRoleMappings <em>Role Mappings</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.SecurityTypeImpl#getDefaultRole <em>Default Role</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.SecurityTypeImpl#isDoasCurrentCaller <em>Doas Current Caller</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.impl.SecurityTypeImpl#isUseContextHandler <em>Use Context Handler</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SecurityTypeImpl extends EObjectImpl implements SecurityType {
    /**
     * The cached value of the '{@link #getDescription() <em>Description</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDescription()
     * @generated
     * @ordered
     */
    protected EList description = null;

    /**
     * The cached value of the '{@link #getDefaultPrincipal() <em>Default Principal</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDefaultPrincipal()
     * @generated
     * @ordered
     */
    protected DefaultPrincipalType defaultPrincipal = null;

    /**
     * The cached value of the '{@link #getRoleMappings() <em>Role Mappings</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getRoleMappings()
     * @generated
     * @ordered
     */
    protected RoleMappingsType roleMappings = null;

    /**
     * The default value of the '{@link #getDefaultRole() <em>Default Role</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDefaultRole()
     * @generated
     * @ordered
     */
    protected static final String DEFAULT_ROLE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getDefaultRole() <em>Default Role</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDefaultRole()
     * @generated
     * @ordered
     */
    protected String defaultRole = DEFAULT_ROLE_EDEFAULT;

    /**
     * The default value of the '{@link #isDoasCurrentCaller() <em>Doas Current Caller</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isDoasCurrentCaller()
     * @generated
     * @ordered
     */
    protected static final boolean DOAS_CURRENT_CALLER_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isDoasCurrentCaller() <em>Doas Current Caller</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isDoasCurrentCaller()
     * @generated
     * @ordered
     */
    protected boolean doasCurrentCaller = DOAS_CURRENT_CALLER_EDEFAULT;

    /**
     * This is true if the Doas Current Caller attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean doasCurrentCallerESet = false;

    /**
     * The default value of the '{@link #isUseContextHandler() <em>Use Context Handler</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isUseContextHandler()
     * @generated
     * @ordered
     */
    protected static final boolean USE_CONTEXT_HANDLER_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isUseContextHandler() <em>Use Context Handler</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isUseContextHandler()
     * @generated
     * @ordered
     */
    protected boolean useContextHandler = USE_CONTEXT_HANDLER_EDEFAULT;

    /**
     * This is true if the Use Context Handler attribute has been set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    protected boolean useContextHandlerESet = false;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected SecurityTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return SecurityPackage.eINSTANCE.getSecurityType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getDescription() {
        if (description == null) {
            description = new EObjectContainmentEList(DescriptionType.class, this, SecurityPackage.SECURITY_TYPE__DESCRIPTION);
        }
        return description;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DefaultPrincipalType getDefaultPrincipal() {
        return defaultPrincipal;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetDefaultPrincipal(DefaultPrincipalType newDefaultPrincipal, NotificationChain msgs) {
        DefaultPrincipalType oldDefaultPrincipal = defaultPrincipal;
        defaultPrincipal = newDefaultPrincipal;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SecurityPackage.SECURITY_TYPE__DEFAULT_PRINCIPAL, oldDefaultPrincipal, newDefaultPrincipal);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setDefaultPrincipal(DefaultPrincipalType newDefaultPrincipal) {
        if (newDefaultPrincipal != defaultPrincipal) {
            NotificationChain msgs = null;
            if (defaultPrincipal != null)
                msgs = ((InternalEObject)defaultPrincipal).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - SecurityPackage.SECURITY_TYPE__DEFAULT_PRINCIPAL, null, msgs);
            if (newDefaultPrincipal != null)
                msgs = ((InternalEObject)newDefaultPrincipal).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - SecurityPackage.SECURITY_TYPE__DEFAULT_PRINCIPAL, null, msgs);
            msgs = basicSetDefaultPrincipal(newDefaultPrincipal, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SecurityPackage.SECURITY_TYPE__DEFAULT_PRINCIPAL, newDefaultPrincipal, newDefaultPrincipal));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public RoleMappingsType getRoleMappings() {
        return roleMappings;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetRoleMappings(RoleMappingsType newRoleMappings, NotificationChain msgs) {
        RoleMappingsType oldRoleMappings = roleMappings;
        roleMappings = newRoleMappings;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SecurityPackage.SECURITY_TYPE__ROLE_MAPPINGS, oldRoleMappings, newRoleMappings);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setRoleMappings(RoleMappingsType newRoleMappings) {
        if (newRoleMappings != roleMappings) {
            NotificationChain msgs = null;
            if (roleMappings != null)
                msgs = ((InternalEObject)roleMappings).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - SecurityPackage.SECURITY_TYPE__ROLE_MAPPINGS, null, msgs);
            if (newRoleMappings != null)
                msgs = ((InternalEObject)newRoleMappings).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - SecurityPackage.SECURITY_TYPE__ROLE_MAPPINGS, null, msgs);
            msgs = basicSetRoleMappings(newRoleMappings, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SecurityPackage.SECURITY_TYPE__ROLE_MAPPINGS, newRoleMappings, newRoleMappings));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getDefaultRole() {
        return defaultRole;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setDefaultRole(String newDefaultRole) {
        String oldDefaultRole = defaultRole;
        defaultRole = newDefaultRole;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SecurityPackage.SECURITY_TYPE__DEFAULT_ROLE, oldDefaultRole, defaultRole));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isDoasCurrentCaller() {
        return doasCurrentCaller;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setDoasCurrentCaller(boolean newDoasCurrentCaller) {
        boolean oldDoasCurrentCaller = doasCurrentCaller;
        doasCurrentCaller = newDoasCurrentCaller;
        boolean oldDoasCurrentCallerESet = doasCurrentCallerESet;
        doasCurrentCallerESet = true;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SecurityPackage.SECURITY_TYPE__DOAS_CURRENT_CALLER, oldDoasCurrentCaller, doasCurrentCaller, !oldDoasCurrentCallerESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetDoasCurrentCaller() {
        boolean oldDoasCurrentCaller = doasCurrentCaller;
        boolean oldDoasCurrentCallerESet = doasCurrentCallerESet;
        doasCurrentCaller = DOAS_CURRENT_CALLER_EDEFAULT;
        doasCurrentCallerESet = false;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.UNSET, SecurityPackage.SECURITY_TYPE__DOAS_CURRENT_CALLER, oldDoasCurrentCaller, DOAS_CURRENT_CALLER_EDEFAULT, oldDoasCurrentCallerESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetDoasCurrentCaller() {
        return doasCurrentCallerESet;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isUseContextHandler() {
        return useContextHandler;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setUseContextHandler(boolean newUseContextHandler) {
        boolean oldUseContextHandler = useContextHandler;
        useContextHandler = newUseContextHandler;
        boolean oldUseContextHandlerESet = useContextHandlerESet;
        useContextHandlerESet = true;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, SecurityPackage.SECURITY_TYPE__USE_CONTEXT_HANDLER, oldUseContextHandler, useContextHandler, !oldUseContextHandlerESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void unsetUseContextHandler() {
        boolean oldUseContextHandler = useContextHandler;
        boolean oldUseContextHandlerESet = useContextHandlerESet;
        useContextHandler = USE_CONTEXT_HANDLER_EDEFAULT;
        useContextHandlerESet = false;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.UNSET, SecurityPackage.SECURITY_TYPE__USE_CONTEXT_HANDLER, oldUseContextHandler, USE_CONTEXT_HANDLER_EDEFAULT, oldUseContextHandlerESet));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSetUseContextHandler() {
        return useContextHandlerESet;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case SecurityPackage.SECURITY_TYPE__DESCRIPTION:
                    return ((InternalEList)getDescription()).basicRemove(otherEnd, msgs);
                case SecurityPackage.SECURITY_TYPE__DEFAULT_PRINCIPAL:
                    return basicSetDefaultPrincipal(null, msgs);
                case SecurityPackage.SECURITY_TYPE__ROLE_MAPPINGS:
                    return basicSetRoleMappings(null, msgs);
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
            case SecurityPackage.SECURITY_TYPE__DESCRIPTION:
                return getDescription();
            case SecurityPackage.SECURITY_TYPE__DEFAULT_PRINCIPAL:
                return getDefaultPrincipal();
            case SecurityPackage.SECURITY_TYPE__ROLE_MAPPINGS:
                return getRoleMappings();
            case SecurityPackage.SECURITY_TYPE__DEFAULT_ROLE:
                return getDefaultRole();
            case SecurityPackage.SECURITY_TYPE__DOAS_CURRENT_CALLER:
                return isDoasCurrentCaller() ? Boolean.TRUE : Boolean.FALSE;
            case SecurityPackage.SECURITY_TYPE__USE_CONTEXT_HANDLER:
                return isUseContextHandler() ? Boolean.TRUE : Boolean.FALSE;
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
            case SecurityPackage.SECURITY_TYPE__DESCRIPTION:
                getDescription().clear();
                getDescription().addAll((Collection)newValue);
                return;
            case SecurityPackage.SECURITY_TYPE__DEFAULT_PRINCIPAL:
                setDefaultPrincipal((DefaultPrincipalType)newValue);
                return;
            case SecurityPackage.SECURITY_TYPE__ROLE_MAPPINGS:
                setRoleMappings((RoleMappingsType)newValue);
                return;
            case SecurityPackage.SECURITY_TYPE__DEFAULT_ROLE:
                setDefaultRole((String)newValue);
                return;
            case SecurityPackage.SECURITY_TYPE__DOAS_CURRENT_CALLER:
                setDoasCurrentCaller(((Boolean)newValue).booleanValue());
                return;
            case SecurityPackage.SECURITY_TYPE__USE_CONTEXT_HANDLER:
                setUseContextHandler(((Boolean)newValue).booleanValue());
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
            case SecurityPackage.SECURITY_TYPE__DESCRIPTION:
                getDescription().clear();
                return;
            case SecurityPackage.SECURITY_TYPE__DEFAULT_PRINCIPAL:
                setDefaultPrincipal((DefaultPrincipalType)null);
                return;
            case SecurityPackage.SECURITY_TYPE__ROLE_MAPPINGS:
                setRoleMappings((RoleMappingsType)null);
                return;
            case SecurityPackage.SECURITY_TYPE__DEFAULT_ROLE:
                setDefaultRole(DEFAULT_ROLE_EDEFAULT);
                return;
            case SecurityPackage.SECURITY_TYPE__DOAS_CURRENT_CALLER:
                unsetDoasCurrentCaller();
                return;
            case SecurityPackage.SECURITY_TYPE__USE_CONTEXT_HANDLER:
                unsetUseContextHandler();
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
            case SecurityPackage.SECURITY_TYPE__DESCRIPTION:
                return description != null && !description.isEmpty();
            case SecurityPackage.SECURITY_TYPE__DEFAULT_PRINCIPAL:
                return defaultPrincipal != null;
            case SecurityPackage.SECURITY_TYPE__ROLE_MAPPINGS:
                return roleMappings != null;
            case SecurityPackage.SECURITY_TYPE__DEFAULT_ROLE:
                return DEFAULT_ROLE_EDEFAULT == null ? defaultRole != null : !DEFAULT_ROLE_EDEFAULT.equals(defaultRole);
            case SecurityPackage.SECURITY_TYPE__DOAS_CURRENT_CALLER:
                return isSetDoasCurrentCaller();
            case SecurityPackage.SECURITY_TYPE__USE_CONTEXT_HANDLER:
                return isSetUseContextHandler();
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
        result.append(" (defaultRole: ");
        result.append(defaultRole);
        result.append(", doasCurrentCaller: ");
        if (doasCurrentCallerESet) result.append(doasCurrentCaller); else result.append("<unset>");
        result.append(", useContextHandler: ");
        if (useContextHandlerESet) result.append(useContextHandler); else result.append("<unset>");
        result.append(')');
        return result.toString();
    }

} //SecurityTypeImpl
