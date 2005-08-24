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
package org.apache.geronimo.xml.ns.deployment.impl;

import org.apache.geronimo.xml.ns.deployment.DependencyType;
import org.apache.geronimo.xml.ns.deployment.DeploymentPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Dependency Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.DependencyTypeImpl#getGroupId <em>Group Id</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.DependencyTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.DependencyTypeImpl#getArtifactId <em>Artifact Id</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.DependencyTypeImpl#getVersion <em>Version</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.DependencyTypeImpl#getUri <em>Uri</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DependencyTypeImpl extends EObjectImpl implements DependencyType {
    /**
     * The default value of the '{@link #getGroupId() <em>Group Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGroupId()
     * @generated
     * @ordered
     */
    protected static final String GROUP_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getGroupId() <em>Group Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGroupId()
     * @generated
     * @ordered
     */
    protected String groupId = GROUP_ID_EDEFAULT;

    /**
     * The default value of the '{@link #getType() <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getType()
     * @generated
     * @ordered
     */
    protected static final String TYPE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getType()
     * @generated
     * @ordered
     */
    protected String type = TYPE_EDEFAULT;

    /**
     * The default value of the '{@link #getArtifactId() <em>Artifact Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getArtifactId()
     * @generated
     * @ordered
     */
    protected static final String ARTIFACT_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getArtifactId() <em>Artifact Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getArtifactId()
     * @generated
     * @ordered
     */
    protected String artifactId = ARTIFACT_ID_EDEFAULT;

    /**
     * The default value of the '{@link #getVersion() <em>Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getVersion()
     * @generated
     * @ordered
     */
    protected static final String VERSION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getVersion() <em>Version</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getVersion()
     * @generated
     * @ordered
     */
    protected String version = VERSION_EDEFAULT;

    /**
     * The default value of the '{@link #getUri() <em>Uri</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getUri()
     * @generated
     * @ordered
     */
    protected static final String URI_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getUri() <em>Uri</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getUri()
     * @generated
     * @ordered
     */
    protected String uri = URI_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected DependencyTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return DeploymentPackage.eINSTANCE.getDependencyType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setGroupId(String newGroupId) {
        String oldGroupId = groupId;
        groupId = newGroupId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.DEPENDENCY_TYPE__GROUP_ID, oldGroupId, groupId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getType() {
        return type;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setType(String newType) {
        String oldType = type;
        type = newType;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.DEPENDENCY_TYPE__TYPE, oldType, type));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setArtifactId(String newArtifactId) {
        String oldArtifactId = artifactId;
        artifactId = newArtifactId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.DEPENDENCY_TYPE__ARTIFACT_ID, oldArtifactId, artifactId));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getVersion() {
        return version;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setVersion(String newVersion) {
        String oldVersion = version;
        version = newVersion;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.DEPENDENCY_TYPE__VERSION, oldVersion, version));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getUri() {
        return uri;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setUri(String newUri) {
        String oldUri = uri;
        uri = newUri;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.DEPENDENCY_TYPE__URI, oldUri, uri));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case DeploymentPackage.DEPENDENCY_TYPE__GROUP_ID:
                return getGroupId();
            case DeploymentPackage.DEPENDENCY_TYPE__TYPE:
                return getType();
            case DeploymentPackage.DEPENDENCY_TYPE__ARTIFACT_ID:
                return getArtifactId();
            case DeploymentPackage.DEPENDENCY_TYPE__VERSION:
                return getVersion();
            case DeploymentPackage.DEPENDENCY_TYPE__URI:
                return getUri();
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
            case DeploymentPackage.DEPENDENCY_TYPE__GROUP_ID:
                setGroupId((String)newValue);
                return;
            case DeploymentPackage.DEPENDENCY_TYPE__TYPE:
                setType((String)newValue);
                return;
            case DeploymentPackage.DEPENDENCY_TYPE__ARTIFACT_ID:
                setArtifactId((String)newValue);
                return;
            case DeploymentPackage.DEPENDENCY_TYPE__VERSION:
                setVersion((String)newValue);
                return;
            case DeploymentPackage.DEPENDENCY_TYPE__URI:
                setUri((String)newValue);
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
            case DeploymentPackage.DEPENDENCY_TYPE__GROUP_ID:
                setGroupId(GROUP_ID_EDEFAULT);
                return;
            case DeploymentPackage.DEPENDENCY_TYPE__TYPE:
                setType(TYPE_EDEFAULT);
                return;
            case DeploymentPackage.DEPENDENCY_TYPE__ARTIFACT_ID:
                setArtifactId(ARTIFACT_ID_EDEFAULT);
                return;
            case DeploymentPackage.DEPENDENCY_TYPE__VERSION:
                setVersion(VERSION_EDEFAULT);
                return;
            case DeploymentPackage.DEPENDENCY_TYPE__URI:
                setUri(URI_EDEFAULT);
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
            case DeploymentPackage.DEPENDENCY_TYPE__GROUP_ID:
                return GROUP_ID_EDEFAULT == null ? groupId != null : !GROUP_ID_EDEFAULT.equals(groupId);
            case DeploymentPackage.DEPENDENCY_TYPE__TYPE:
                return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
            case DeploymentPackage.DEPENDENCY_TYPE__ARTIFACT_ID:
                return ARTIFACT_ID_EDEFAULT == null ? artifactId != null : !ARTIFACT_ID_EDEFAULT.equals(artifactId);
            case DeploymentPackage.DEPENDENCY_TYPE__VERSION:
                return VERSION_EDEFAULT == null ? version != null : !VERSION_EDEFAULT.equals(version);
            case DeploymentPackage.DEPENDENCY_TYPE__URI:
                return URI_EDEFAULT == null ? uri != null : !URI_EDEFAULT.equals(uri);
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
        result.append(" (groupId: ");
        result.append(groupId);
        result.append(", type: ");
        result.append(type);
        result.append(", artifactId: ");
        result.append(artifactId);
        result.append(", version: ");
        result.append(version);
        result.append(", uri: ");
        result.append(uri);
        result.append(')');
        return result.toString();
    }

} //DependencyTypeImpl
