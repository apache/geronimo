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

import java.util.Collection;

import org.apache.geronimo.xml.ns.deployment.DeploymentPackage;
import org.apache.geronimo.xml.ns.deployment.GbeanType;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

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
 * An implementation of the model object '<em><b>Gbean Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.GbeanTypeImpl#getGroup <em>Group</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.GbeanTypeImpl#getAttribute <em>Attribute</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.GbeanTypeImpl#getXmlAttribute <em>Xml Attribute</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.GbeanTypeImpl#getReference <em>Reference</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.GbeanTypeImpl#getReferences <em>References</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.GbeanTypeImpl#getXmlReference <em>Xml Reference</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.GbeanTypeImpl#getClass_ <em>Class</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.GbeanTypeImpl#getGbeanName <em>Gbean Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.GbeanTypeImpl#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GbeanTypeImpl extends EObjectImpl implements GbeanType {
    /**
     * The cached value of the '{@link #getGroup() <em>Group</em>}' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGroup()
     * @generated
     * @ordered
     */
    protected FeatureMap group = null;

    /**
     * The default value of the '{@link #getClass_() <em>Class</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getClass_()
     * @generated
     * @ordered
     */
    protected static final String CLASS_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getClass_() <em>Class</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getClass_()
     * @generated
     * @ordered
     */
    protected String class_ = CLASS_EDEFAULT;

    /**
     * The default value of the '{@link #getGbeanName() <em>Gbean Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGbeanName()
     * @generated
     * @ordered
     */
    protected static final String GBEAN_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getGbeanName() <em>Gbean Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGbeanName()
     * @generated
     * @ordered
     */
    protected String gbeanName = GBEAN_NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getName()
     * @generated
     * @ordered
     */
    protected static final String NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getName()
     * @generated
     * @ordered
     */
    protected String name = NAME_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected GbeanTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return DeploymentPackage.eINSTANCE.getGbeanType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FeatureMap getGroup() {
        if (group == null) {
            group = new BasicFeatureMap(this, DeploymentPackage.GBEAN_TYPE__GROUP);
        }
        return group;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getAttribute() {
        return ((FeatureMap)getGroup()).list(DeploymentPackage.eINSTANCE.getGbeanType_Attribute());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getXmlAttribute() {
        return ((FeatureMap)getGroup()).list(DeploymentPackage.eINSTANCE.getGbeanType_XmlAttribute());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getReference() {
        return ((FeatureMap)getGroup()).list(DeploymentPackage.eINSTANCE.getGbeanType_Reference());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getReferences() {
        return ((FeatureMap)getGroup()).list(DeploymentPackage.eINSTANCE.getGbeanType_References());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList getXmlReference() {
        return ((FeatureMap)getGroup()).list(DeploymentPackage.eINSTANCE.getGbeanType_XmlReference());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getClass_() {
        return class_;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setClass(String newClass) {
        String oldClass = class_;
        class_ = newClass;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.GBEAN_TYPE__CLASS, oldClass, class_));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getGbeanName() {
        return gbeanName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setGbeanName(String newGbeanName) {
        String oldGbeanName = gbeanName;
        gbeanName = newGbeanName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.GBEAN_TYPE__GBEAN_NAME, oldGbeanName, gbeanName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getName() {
        return name;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.GBEAN_TYPE__NAME, oldName, name));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
        if (featureID >= 0) {
            switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
                case DeploymentPackage.GBEAN_TYPE__GROUP:
                    return ((InternalEList)getGroup()).basicRemove(otherEnd, msgs);
                case DeploymentPackage.GBEAN_TYPE__ATTRIBUTE:
                    return ((InternalEList)getAttribute()).basicRemove(otherEnd, msgs);
                case DeploymentPackage.GBEAN_TYPE__XML_ATTRIBUTE:
                    return ((InternalEList)getXmlAttribute()).basicRemove(otherEnd, msgs);
                case DeploymentPackage.GBEAN_TYPE__REFERENCE:
                    return ((InternalEList)getReference()).basicRemove(otherEnd, msgs);
                case DeploymentPackage.GBEAN_TYPE__REFERENCES:
                    return ((InternalEList)getReferences()).basicRemove(otherEnd, msgs);
                case DeploymentPackage.GBEAN_TYPE__XML_REFERENCE:
                    return ((InternalEList)getXmlReference()).basicRemove(otherEnd, msgs);
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
            case DeploymentPackage.GBEAN_TYPE__GROUP:
                return getGroup();
            case DeploymentPackage.GBEAN_TYPE__ATTRIBUTE:
                return getAttribute();
            case DeploymentPackage.GBEAN_TYPE__XML_ATTRIBUTE:
                return getXmlAttribute();
            case DeploymentPackage.GBEAN_TYPE__REFERENCE:
                return getReference();
            case DeploymentPackage.GBEAN_TYPE__REFERENCES:
                return getReferences();
            case DeploymentPackage.GBEAN_TYPE__XML_REFERENCE:
                return getXmlReference();
            case DeploymentPackage.GBEAN_TYPE__CLASS:
                return getClass_();
            case DeploymentPackage.GBEAN_TYPE__GBEAN_NAME:
                return getGbeanName();
            case DeploymentPackage.GBEAN_TYPE__NAME:
                return getName();
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
            case DeploymentPackage.GBEAN_TYPE__GROUP:
                getGroup().clear();
                getGroup().addAll((Collection)newValue);
                return;
            case DeploymentPackage.GBEAN_TYPE__ATTRIBUTE:
                getAttribute().clear();
                getAttribute().addAll((Collection)newValue);
                return;
            case DeploymentPackage.GBEAN_TYPE__XML_ATTRIBUTE:
                getXmlAttribute().clear();
                getXmlAttribute().addAll((Collection)newValue);
                return;
            case DeploymentPackage.GBEAN_TYPE__REFERENCE:
                getReference().clear();
                getReference().addAll((Collection)newValue);
                return;
            case DeploymentPackage.GBEAN_TYPE__REFERENCES:
                getReferences().clear();
                getReferences().addAll((Collection)newValue);
                return;
            case DeploymentPackage.GBEAN_TYPE__XML_REFERENCE:
                getXmlReference().clear();
                getXmlReference().addAll((Collection)newValue);
                return;
            case DeploymentPackage.GBEAN_TYPE__CLASS:
                setClass((String)newValue);
                return;
            case DeploymentPackage.GBEAN_TYPE__GBEAN_NAME:
                setGbeanName((String)newValue);
                return;
            case DeploymentPackage.GBEAN_TYPE__NAME:
                setName((String)newValue);
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
            case DeploymentPackage.GBEAN_TYPE__GROUP:
                getGroup().clear();
                return;
            case DeploymentPackage.GBEAN_TYPE__ATTRIBUTE:
                getAttribute().clear();
                return;
            case DeploymentPackage.GBEAN_TYPE__XML_ATTRIBUTE:
                getXmlAttribute().clear();
                return;
            case DeploymentPackage.GBEAN_TYPE__REFERENCE:
                getReference().clear();
                return;
            case DeploymentPackage.GBEAN_TYPE__REFERENCES:
                getReferences().clear();
                return;
            case DeploymentPackage.GBEAN_TYPE__XML_REFERENCE:
                getXmlReference().clear();
                return;
            case DeploymentPackage.GBEAN_TYPE__CLASS:
                setClass(CLASS_EDEFAULT);
                return;
            case DeploymentPackage.GBEAN_TYPE__GBEAN_NAME:
                setGbeanName(GBEAN_NAME_EDEFAULT);
                return;
            case DeploymentPackage.GBEAN_TYPE__NAME:
                setName(NAME_EDEFAULT);
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
            case DeploymentPackage.GBEAN_TYPE__GROUP:
                return group != null && !group.isEmpty();
            case DeploymentPackage.GBEAN_TYPE__ATTRIBUTE:
                return !getAttribute().isEmpty();
            case DeploymentPackage.GBEAN_TYPE__XML_ATTRIBUTE:
                return !getXmlAttribute().isEmpty();
            case DeploymentPackage.GBEAN_TYPE__REFERENCE:
                return !getReference().isEmpty();
            case DeploymentPackage.GBEAN_TYPE__REFERENCES:
                return !getReferences().isEmpty();
            case DeploymentPackage.GBEAN_TYPE__XML_REFERENCE:
                return !getXmlReference().isEmpty();
            case DeploymentPackage.GBEAN_TYPE__CLASS:
                return CLASS_EDEFAULT == null ? class_ != null : !CLASS_EDEFAULT.equals(class_);
            case DeploymentPackage.GBEAN_TYPE__GBEAN_NAME:
                return GBEAN_NAME_EDEFAULT == null ? gbeanName != null : !GBEAN_NAME_EDEFAULT.equals(gbeanName);
            case DeploymentPackage.GBEAN_TYPE__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
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
        result.append(" (group: ");
        result.append(group);
        result.append(", class: ");
        result.append(class_);
        result.append(", gbeanName: ");
        result.append(gbeanName);
        result.append(", name: ");
        result.append(name);
        result.append(')');
        return result.toString();
    }

} //GbeanTypeImpl
