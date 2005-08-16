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
package org.apache.geronimo.xml.ns.naming.impl;

import java.util.Collection;

import org.apache.geronimo.xml.ns.naming.DocumentRoot;
import org.apache.geronimo.xml.ns.naming.EjbLocalRefType;
import org.apache.geronimo.xml.ns.naming.EjbRefType;
import org.apache.geronimo.xml.ns.naming.GbeanLocatorType;
import org.apache.geronimo.xml.ns.naming.NamingPackage;
import org.apache.geronimo.xml.ns.naming.ResourceEnvRefType;
import org.apache.geronimo.xml.ns.naming.ResourceLocatorType;
import org.apache.geronimo.xml.ns.naming.ResourceRefType;
import org.apache.geronimo.xml.ns.naming.ServiceRefType;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.impl.EStringToStringMapEntryImpl;

import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Document Root</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.DocumentRootImpl#getMixed <em>Mixed</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.DocumentRootImpl#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.DocumentRootImpl#getXSISchemaLocation <em>XSI Schema Location</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.DocumentRootImpl#getCmpConnectionFactory <em>Cmp Connection Factory</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.DocumentRootImpl#getEjbLocalRef <em>Ejb Local Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.DocumentRootImpl#getEjbRef <em>Ejb Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.DocumentRootImpl#getResourceAdapter <em>Resource Adapter</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.DocumentRootImpl#getResourceEnvRef <em>Resource Env Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.DocumentRootImpl#getResourceRef <em>Resource Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.DocumentRootImpl#getServiceRef <em>Service Ref</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.impl.DocumentRootImpl#getWorkmanager <em>Workmanager</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DocumentRootImpl extends EObjectImpl implements DocumentRoot {
    /**
     * The cached value of the '{@link #getMixed() <em>Mixed</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getMixed()
     * @generated
     * @ordered
     */
    protected FeatureMap mixed = null;

    /**
     * The cached value of the '{@link #getXMLNSPrefixMap() <em>XMLNS Prefix Map</em>}' map.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getXMLNSPrefixMap()
     * @generated
     * @ordered
     */
    protected EMap xMLNSPrefixMap = null;

    /**
     * The cached value of the '{@link #getXSISchemaLocation() <em>XSI Schema Location</em>}' map.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getXSISchemaLocation()
     * @generated
     * @ordered
     */
    protected EMap xSISchemaLocation = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected DocumentRootImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return NamingPackage.eINSTANCE.getDocumentRoot();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FeatureMap getMixed() {
        if (mixed == null) {
            mixed = new BasicFeatureMap(this, NamingPackage.DOCUMENT_ROOT__MIXED);
        }
        return mixed;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EMap getXMLNSPrefixMap() {
        if (xMLNSPrefixMap == null) {
            xMLNSPrefixMap = new EcoreEMap(EcorePackage.eINSTANCE.getEStringToStringMapEntry(), EStringToStringMapEntryImpl.class, this, NamingPackage.DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
        }
        return xMLNSPrefixMap;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EMap getXSISchemaLocation() {
        if (xSISchemaLocation == null) {
            xSISchemaLocation = new EcoreEMap(EcorePackage.eINSTANCE.getEStringToStringMapEntry(), EStringToStringMapEntryImpl.class, this, NamingPackage.DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
        }
        return xSISchemaLocation;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ResourceLocatorType getCmpConnectionFactory() {
        return (ResourceLocatorType)getMixed().get(NamingPackage.eINSTANCE.getDocumentRoot_CmpConnectionFactory(), true);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetCmpConnectionFactory(ResourceLocatorType newCmpConnectionFactory, NotificationChain msgs) {
        return ((FeatureMap.Internal)getMixed()).basicAdd(NamingPackage.eINSTANCE.getDocumentRoot_CmpConnectionFactory(), newCmpConnectionFactory, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setCmpConnectionFactory(ResourceLocatorType newCmpConnectionFactory) {
        ((FeatureMap.Internal)getMixed()).set(NamingPackage.eINSTANCE.getDocumentRoot_CmpConnectionFactory(), newCmpConnectionFactory);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EjbLocalRefType getEjbLocalRef() {
        return (EjbLocalRefType)getMixed().get(NamingPackage.eINSTANCE.getDocumentRoot_EjbLocalRef(), true);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetEjbLocalRef(EjbLocalRefType newEjbLocalRef, NotificationChain msgs) {
        return ((FeatureMap.Internal)getMixed()).basicAdd(NamingPackage.eINSTANCE.getDocumentRoot_EjbLocalRef(), newEjbLocalRef, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEjbLocalRef(EjbLocalRefType newEjbLocalRef) {
        ((FeatureMap.Internal)getMixed()).set(NamingPackage.eINSTANCE.getDocumentRoot_EjbLocalRef(), newEjbLocalRef);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EjbRefType getEjbRef() {
        return (EjbRefType)getMixed().get(NamingPackage.eINSTANCE.getDocumentRoot_EjbRef(), true);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetEjbRef(EjbRefType newEjbRef, NotificationChain msgs) {
        return ((FeatureMap.Internal)getMixed()).basicAdd(NamingPackage.eINSTANCE.getDocumentRoot_EjbRef(), newEjbRef, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEjbRef(EjbRefType newEjbRef) {
        ((FeatureMap.Internal)getMixed()).set(NamingPackage.eINSTANCE.getDocumentRoot_EjbRef(), newEjbRef);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ResourceLocatorType getResourceAdapter() {
        return (ResourceLocatorType)getMixed().get(NamingPackage.eINSTANCE.getDocumentRoot_ResourceAdapter(), true);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetResourceAdapter(ResourceLocatorType newResourceAdapter, NotificationChain msgs) {
        return ((FeatureMap.Internal)getMixed()).basicAdd(NamingPackage.eINSTANCE.getDocumentRoot_ResourceAdapter(), newResourceAdapter, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setResourceAdapter(ResourceLocatorType newResourceAdapter) {
        ((FeatureMap.Internal)getMixed()).set(NamingPackage.eINSTANCE.getDocumentRoot_ResourceAdapter(), newResourceAdapter);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ResourceEnvRefType getResourceEnvRef() {
        return (ResourceEnvRefType)getMixed().get(NamingPackage.eINSTANCE.getDocumentRoot_ResourceEnvRef(), true);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetResourceEnvRef(ResourceEnvRefType newResourceEnvRef, NotificationChain msgs) {
        return ((FeatureMap.Internal)getMixed()).basicAdd(NamingPackage.eINSTANCE.getDocumentRoot_ResourceEnvRef(), newResourceEnvRef, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setResourceEnvRef(ResourceEnvRefType newResourceEnvRef) {
        ((FeatureMap.Internal)getMixed()).set(NamingPackage.eINSTANCE.getDocumentRoot_ResourceEnvRef(), newResourceEnvRef);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ResourceRefType getResourceRef() {
        return (ResourceRefType)getMixed().get(NamingPackage.eINSTANCE.getDocumentRoot_ResourceRef(), true);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetResourceRef(ResourceRefType newResourceRef, NotificationChain msgs) {
        return ((FeatureMap.Internal)getMixed()).basicAdd(NamingPackage.eINSTANCE.getDocumentRoot_ResourceRef(), newResourceRef, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setResourceRef(ResourceRefType newResourceRef) {
        ((FeatureMap.Internal)getMixed()).set(NamingPackage.eINSTANCE.getDocumentRoot_ResourceRef(), newResourceRef);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ServiceRefType getServiceRef() {
        return (ServiceRefType)getMixed().get(NamingPackage.eINSTANCE.getDocumentRoot_ServiceRef(), true);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetServiceRef(ServiceRefType newServiceRef, NotificationChain msgs) {
        return ((FeatureMap.Internal)getMixed()).basicAdd(NamingPackage.eINSTANCE.getDocumentRoot_ServiceRef(), newServiceRef, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setServiceRef(ServiceRefType newServiceRef) {
        ((FeatureMap.Internal)getMixed()).set(NamingPackage.eINSTANCE.getDocumentRoot_ServiceRef(), newServiceRef);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public GbeanLocatorType getWorkmanager() {
        return (GbeanLocatorType)getMixed().get(NamingPackage.eINSTANCE.getDocumentRoot_Workmanager(), true);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetWorkmanager(GbeanLocatorType newWorkmanager, NotificationChain msgs) {
        return ((FeatureMap.Internal)getMixed()).basicAdd(NamingPackage.eINSTANCE.getDocumentRoot_Workmanager(), newWorkmanager, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setWorkmanager(GbeanLocatorType newWorkmanager) {
        ((FeatureMap.Internal)getMixed()).set(NamingPackage.eINSTANCE.getDocumentRoot_Workmanager(), newWorkmanager);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case NamingPackage.DOCUMENT_ROOT__MIXED:
                    return ((InternalEList)getMixed()).basicRemove(otherEnd, msgs);
                case NamingPackage.DOCUMENT_ROOT__XMLNS_PREFIX_MAP:
                    return ((InternalEList)getXMLNSPrefixMap()).basicRemove(otherEnd, msgs);
                case NamingPackage.DOCUMENT_ROOT__XSI_SCHEMA_LOCATION:
                    return ((InternalEList)getXSISchemaLocation()).basicRemove(otherEnd, msgs);
                case NamingPackage.DOCUMENT_ROOT__CMP_CONNECTION_FACTORY:
                    return basicSetCmpConnectionFactory(null, msgs);
                case NamingPackage.DOCUMENT_ROOT__EJB_LOCAL_REF:
                    return basicSetEjbLocalRef(null, msgs);
                case NamingPackage.DOCUMENT_ROOT__EJB_REF:
                    return basicSetEjbRef(null, msgs);
                case NamingPackage.DOCUMENT_ROOT__RESOURCE_ADAPTER:
                    return basicSetResourceAdapter(null, msgs);
                case NamingPackage.DOCUMENT_ROOT__RESOURCE_ENV_REF:
                    return basicSetResourceEnvRef(null, msgs);
                case NamingPackage.DOCUMENT_ROOT__RESOURCE_REF:
                    return basicSetResourceRef(null, msgs);
                case NamingPackage.DOCUMENT_ROOT__SERVICE_REF:
                    return basicSetServiceRef(null, msgs);
                case NamingPackage.DOCUMENT_ROOT__WORKMANAGER:
                    return basicSetWorkmanager(null, msgs);
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
            case NamingPackage.DOCUMENT_ROOT__MIXED:
                return getMixed();
            case NamingPackage.DOCUMENT_ROOT__XMLNS_PREFIX_MAP:
                return getXMLNSPrefixMap();
            case NamingPackage.DOCUMENT_ROOT__XSI_SCHEMA_LOCATION:
                return getXSISchemaLocation();
            case NamingPackage.DOCUMENT_ROOT__CMP_CONNECTION_FACTORY:
                return getCmpConnectionFactory();
            case NamingPackage.DOCUMENT_ROOT__EJB_LOCAL_REF:
                return getEjbLocalRef();
            case NamingPackage.DOCUMENT_ROOT__EJB_REF:
                return getEjbRef();
            case NamingPackage.DOCUMENT_ROOT__RESOURCE_ADAPTER:
                return getResourceAdapter();
            case NamingPackage.DOCUMENT_ROOT__RESOURCE_ENV_REF:
                return getResourceEnvRef();
            case NamingPackage.DOCUMENT_ROOT__RESOURCE_REF:
                return getResourceRef();
            case NamingPackage.DOCUMENT_ROOT__SERVICE_REF:
                return getServiceRef();
            case NamingPackage.DOCUMENT_ROOT__WORKMANAGER:
                return getWorkmanager();
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
            case NamingPackage.DOCUMENT_ROOT__MIXED:
                getMixed().clear();
                getMixed().addAll((Collection)newValue);
                return;
            case NamingPackage.DOCUMENT_ROOT__XMLNS_PREFIX_MAP:
                getXMLNSPrefixMap().clear();
                getXMLNSPrefixMap().addAll((Collection)newValue);
                return;
            case NamingPackage.DOCUMENT_ROOT__XSI_SCHEMA_LOCATION:
                getXSISchemaLocation().clear();
                getXSISchemaLocation().addAll((Collection)newValue);
                return;
            case NamingPackage.DOCUMENT_ROOT__CMP_CONNECTION_FACTORY:
                setCmpConnectionFactory((ResourceLocatorType)newValue);
                return;
            case NamingPackage.DOCUMENT_ROOT__EJB_LOCAL_REF:
                setEjbLocalRef((EjbLocalRefType)newValue);
                return;
            case NamingPackage.DOCUMENT_ROOT__EJB_REF:
                setEjbRef((EjbRefType)newValue);
                return;
            case NamingPackage.DOCUMENT_ROOT__RESOURCE_ADAPTER:
                setResourceAdapter((ResourceLocatorType)newValue);
                return;
            case NamingPackage.DOCUMENT_ROOT__RESOURCE_ENV_REF:
                setResourceEnvRef((ResourceEnvRefType)newValue);
                return;
            case NamingPackage.DOCUMENT_ROOT__RESOURCE_REF:
                setResourceRef((ResourceRefType)newValue);
                return;
            case NamingPackage.DOCUMENT_ROOT__SERVICE_REF:
                setServiceRef((ServiceRefType)newValue);
                return;
            case NamingPackage.DOCUMENT_ROOT__WORKMANAGER:
                setWorkmanager((GbeanLocatorType)newValue);
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
            case NamingPackage.DOCUMENT_ROOT__MIXED:
                getMixed().clear();
                return;
            case NamingPackage.DOCUMENT_ROOT__XMLNS_PREFIX_MAP:
                getXMLNSPrefixMap().clear();
                return;
            case NamingPackage.DOCUMENT_ROOT__XSI_SCHEMA_LOCATION:
                getXSISchemaLocation().clear();
                return;
            case NamingPackage.DOCUMENT_ROOT__CMP_CONNECTION_FACTORY:
                setCmpConnectionFactory((ResourceLocatorType)null);
                return;
            case NamingPackage.DOCUMENT_ROOT__EJB_LOCAL_REF:
                setEjbLocalRef((EjbLocalRefType)null);
                return;
            case NamingPackage.DOCUMENT_ROOT__EJB_REF:
                setEjbRef((EjbRefType)null);
                return;
            case NamingPackage.DOCUMENT_ROOT__RESOURCE_ADAPTER:
                setResourceAdapter((ResourceLocatorType)null);
                return;
            case NamingPackage.DOCUMENT_ROOT__RESOURCE_ENV_REF:
                setResourceEnvRef((ResourceEnvRefType)null);
                return;
            case NamingPackage.DOCUMENT_ROOT__RESOURCE_REF:
                setResourceRef((ResourceRefType)null);
                return;
            case NamingPackage.DOCUMENT_ROOT__SERVICE_REF:
                setServiceRef((ServiceRefType)null);
                return;
            case NamingPackage.DOCUMENT_ROOT__WORKMANAGER:
                setWorkmanager((GbeanLocatorType)null);
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
            case NamingPackage.DOCUMENT_ROOT__MIXED:
                return mixed != null && !mixed.isEmpty();
            case NamingPackage.DOCUMENT_ROOT__XMLNS_PREFIX_MAP:
                return xMLNSPrefixMap != null && !xMLNSPrefixMap.isEmpty();
            case NamingPackage.DOCUMENT_ROOT__XSI_SCHEMA_LOCATION:
                return xSISchemaLocation != null && !xSISchemaLocation.isEmpty();
            case NamingPackage.DOCUMENT_ROOT__CMP_CONNECTION_FACTORY:
                return getCmpConnectionFactory() != null;
            case NamingPackage.DOCUMENT_ROOT__EJB_LOCAL_REF:
                return getEjbLocalRef() != null;
            case NamingPackage.DOCUMENT_ROOT__EJB_REF:
                return getEjbRef() != null;
            case NamingPackage.DOCUMENT_ROOT__RESOURCE_ADAPTER:
                return getResourceAdapter() != null;
            case NamingPackage.DOCUMENT_ROOT__RESOURCE_ENV_REF:
                return getResourceEnvRef() != null;
            case NamingPackage.DOCUMENT_ROOT__RESOURCE_REF:
                return getResourceRef() != null;
            case NamingPackage.DOCUMENT_ROOT__SERVICE_REF:
                return getServiceRef() != null;
            case NamingPackage.DOCUMENT_ROOT__WORKMANAGER:
                return getWorkmanager() != null;
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
        result.append(" (mixed: ");
        result.append(mixed);
        result.append(')');
        return result.toString();
    }

} //DocumentRootImpl
