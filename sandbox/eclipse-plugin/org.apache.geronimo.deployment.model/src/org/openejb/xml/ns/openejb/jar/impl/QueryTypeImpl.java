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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.openejb.xml.ns.openejb.jar.JarPackage;
import org.openejb.xml.ns.openejb.jar.QueryMethodType;
import org.openejb.xml.ns.openejb.jar.QueryType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Query Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.QueryTypeImpl#getQueryMethod <em>Query Method</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.QueryTypeImpl#getResultTypeMapping <em>Result Type Mapping</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.QueryTypeImpl#getEjbQl <em>Ejb Ql</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.QueryTypeImpl#getNoCacheFlush <em>No Cache Flush</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.impl.QueryTypeImpl#getGroupName <em>Group Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class QueryTypeImpl extends EObjectImpl implements QueryType {
    /**
     * The cached value of the '{@link #getQueryMethod() <em>Query Method</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getQueryMethod()
     * @generated
     * @ordered
     */
    protected QueryMethodType queryMethod = null;

    /**
     * The default value of the '{@link #getResultTypeMapping() <em>Result Type Mapping</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getResultTypeMapping()
     * @generated
     * @ordered
     */
    protected static final String RESULT_TYPE_MAPPING_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getResultTypeMapping() <em>Result Type Mapping</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getResultTypeMapping()
     * @generated
     * @ordered
     */
    protected String resultTypeMapping = RESULT_TYPE_MAPPING_EDEFAULT;

    /**
     * The default value of the '{@link #getEjbQl() <em>Ejb Ql</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEjbQl()
     * @generated
     * @ordered
     */
    protected static final String EJB_QL_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getEjbQl() <em>Ejb Ql</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getEjbQl()
     * @generated
     * @ordered
     */
    protected String ejbQl = EJB_QL_EDEFAULT;

    /**
     * The cached value of the '{@link #getNoCacheFlush() <em>No Cache Flush</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getNoCacheFlush()
     * @generated
     * @ordered
     */
    protected EObject noCacheFlush = null;

    /**
     * The default value of the '{@link #getGroupName() <em>Group Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGroupName()
     * @generated
     * @ordered
     */
    protected static final String GROUP_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getGroupName() <em>Group Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGroupName()
     * @generated
     * @ordered
     */
    protected String groupName = GROUP_NAME_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected QueryTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return JarPackage.eINSTANCE.getQueryType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public QueryMethodType getQueryMethod() {
        return queryMethod;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetQueryMethod(QueryMethodType newQueryMethod, NotificationChain msgs) {
        QueryMethodType oldQueryMethod = queryMethod;
        queryMethod = newQueryMethod;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.QUERY_TYPE__QUERY_METHOD, oldQueryMethod, newQueryMethod);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setQueryMethod(QueryMethodType newQueryMethod) {
        if (newQueryMethod != queryMethod) {
            NotificationChain msgs = null;
            if (queryMethod != null)
                msgs = ((InternalEObject)queryMethod).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.QUERY_TYPE__QUERY_METHOD, null, msgs);
            if (newQueryMethod != null)
                msgs = ((InternalEObject)newQueryMethod).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.QUERY_TYPE__QUERY_METHOD, null, msgs);
            msgs = basicSetQueryMethod(newQueryMethod, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.QUERY_TYPE__QUERY_METHOD, newQueryMethod, newQueryMethod));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getResultTypeMapping() {
        return resultTypeMapping;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setResultTypeMapping(String newResultTypeMapping) {
        String oldResultTypeMapping = resultTypeMapping;
        resultTypeMapping = newResultTypeMapping;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.QUERY_TYPE__RESULT_TYPE_MAPPING, oldResultTypeMapping, resultTypeMapping));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getEjbQl() {
        return ejbQl;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEjbQl(String newEjbQl) {
        String oldEjbQl = ejbQl;
        ejbQl = newEjbQl;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.QUERY_TYPE__EJB_QL, oldEjbQl, ejbQl));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject getNoCacheFlush() {
        return noCacheFlush;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetNoCacheFlush(EObject newNoCacheFlush, NotificationChain msgs) {
        EObject oldNoCacheFlush = noCacheFlush;
        noCacheFlush = newNoCacheFlush;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JarPackage.QUERY_TYPE__NO_CACHE_FLUSH, oldNoCacheFlush, newNoCacheFlush);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setNoCacheFlush(EObject newNoCacheFlush) {
        if (newNoCacheFlush != noCacheFlush) {
            NotificationChain msgs = null;
            if (noCacheFlush != null)
                msgs = ((InternalEObject)noCacheFlush).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JarPackage.QUERY_TYPE__NO_CACHE_FLUSH, null, msgs);
            if (newNoCacheFlush != null)
                msgs = ((InternalEObject)newNoCacheFlush).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - JarPackage.QUERY_TYPE__NO_CACHE_FLUSH, null, msgs);
            msgs = basicSetNoCacheFlush(newNoCacheFlush, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.QUERY_TYPE__NO_CACHE_FLUSH, newNoCacheFlush, newNoCacheFlush));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setGroupName(String newGroupName) {
        String oldGroupName = groupName;
        groupName = newGroupName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, JarPackage.QUERY_TYPE__GROUP_NAME, oldGroupName, groupName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case JarPackage.QUERY_TYPE__QUERY_METHOD:
                    return basicSetQueryMethod(null, msgs);
                case JarPackage.QUERY_TYPE__NO_CACHE_FLUSH:
                    return basicSetNoCacheFlush(null, msgs);
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
            case JarPackage.QUERY_TYPE__QUERY_METHOD:
                return getQueryMethod();
            case JarPackage.QUERY_TYPE__RESULT_TYPE_MAPPING:
                return getResultTypeMapping();
            case JarPackage.QUERY_TYPE__EJB_QL:
                return getEjbQl();
            case JarPackage.QUERY_TYPE__NO_CACHE_FLUSH:
                return getNoCacheFlush();
            case JarPackage.QUERY_TYPE__GROUP_NAME:
                return getGroupName();
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
            case JarPackage.QUERY_TYPE__QUERY_METHOD:
                setQueryMethod((QueryMethodType)newValue);
                return;
            case JarPackage.QUERY_TYPE__RESULT_TYPE_MAPPING:
                setResultTypeMapping((String)newValue);
                return;
            case JarPackage.QUERY_TYPE__EJB_QL:
                setEjbQl((String)newValue);
                return;
            case JarPackage.QUERY_TYPE__NO_CACHE_FLUSH:
                setNoCacheFlush((EObject)newValue);
                return;
            case JarPackage.QUERY_TYPE__GROUP_NAME:
                setGroupName((String)newValue);
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
            case JarPackage.QUERY_TYPE__QUERY_METHOD:
                setQueryMethod((QueryMethodType)null);
                return;
            case JarPackage.QUERY_TYPE__RESULT_TYPE_MAPPING:
                setResultTypeMapping(RESULT_TYPE_MAPPING_EDEFAULT);
                return;
            case JarPackage.QUERY_TYPE__EJB_QL:
                setEjbQl(EJB_QL_EDEFAULT);
                return;
            case JarPackage.QUERY_TYPE__NO_CACHE_FLUSH:
                setNoCacheFlush((EObject)null);
                return;
            case JarPackage.QUERY_TYPE__GROUP_NAME:
                setGroupName(GROUP_NAME_EDEFAULT);
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
            case JarPackage.QUERY_TYPE__QUERY_METHOD:
                return queryMethod != null;
            case JarPackage.QUERY_TYPE__RESULT_TYPE_MAPPING:
                return RESULT_TYPE_MAPPING_EDEFAULT == null ? resultTypeMapping != null : !RESULT_TYPE_MAPPING_EDEFAULT.equals(resultTypeMapping);
            case JarPackage.QUERY_TYPE__EJB_QL:
                return EJB_QL_EDEFAULT == null ? ejbQl != null : !EJB_QL_EDEFAULT.equals(ejbQl);
            case JarPackage.QUERY_TYPE__NO_CACHE_FLUSH:
                return noCacheFlush != null;
            case JarPackage.QUERY_TYPE__GROUP_NAME:
                return GROUP_NAME_EDEFAULT == null ? groupName != null : !GROUP_NAME_EDEFAULT.equals(groupName);
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
        result.append(" (resultTypeMapping: ");
        result.append(resultTypeMapping);
        result.append(", ejbQl: ");
        result.append(ejbQl);
        result.append(", groupName: ");
        result.append(groupName);
        result.append(')');
        return result.toString();
    }

} //QueryTypeImpl
