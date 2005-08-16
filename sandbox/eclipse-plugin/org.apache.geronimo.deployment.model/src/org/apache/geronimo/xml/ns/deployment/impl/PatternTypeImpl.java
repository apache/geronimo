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

import org.apache.geronimo.xml.ns.deployment.DeploymentPackage;
import org.apache.geronimo.xml.ns.deployment.PatternType;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Pattern Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.PatternTypeImpl#getDomain <em>Domain</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.PatternTypeImpl#getServer <em>Server</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.PatternTypeImpl#getApplication <em>Application</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.PatternTypeImpl#getModuleType <em>Module Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.PatternTypeImpl#getModule <em>Module</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.PatternTypeImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.PatternTypeImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.impl.PatternTypeImpl#getGbeanName <em>Gbean Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PatternTypeImpl extends EObjectImpl implements PatternType {
    /**
     * The default value of the '{@link #getDomain() <em>Domain</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDomain()
     * @generated
     * @ordered
     */
    protected static final String DOMAIN_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getDomain() <em>Domain</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDomain()
     * @generated
     * @ordered
     */
    protected String domain = DOMAIN_EDEFAULT;

    /**
     * The default value of the '{@link #getServer() <em>Server</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getServer()
     * @generated
     * @ordered
     */
    protected static final String SERVER_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getServer() <em>Server</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getServer()
     * @generated
     * @ordered
     */
    protected String server = SERVER_EDEFAULT;

    /**
     * The default value of the '{@link #getApplication() <em>Application</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getApplication()
     * @generated
     * @ordered
     */
    protected static final String APPLICATION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getApplication() <em>Application</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getApplication()
     * @generated
     * @ordered
     */
    protected String application = APPLICATION_EDEFAULT;

    /**
     * The default value of the '{@link #getModuleType() <em>Module Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getModuleType()
     * @generated
     * @ordered
     */
    protected static final String MODULE_TYPE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getModuleType() <em>Module Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getModuleType()
     * @generated
     * @ordered
     */
    protected String moduleType = MODULE_TYPE_EDEFAULT;

    /**
     * The default value of the '{@link #getModule() <em>Module</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getModule()
     * @generated
     * @ordered
     */
    protected static final String MODULE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getModule() <em>Module</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getModule()
     * @generated
     * @ordered
     */
    protected String module = MODULE_EDEFAULT;

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
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected PatternTypeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return DeploymentPackage.eINSTANCE.getPatternType();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getDomain() {
        return domain;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setDomain(String newDomain) {
        String oldDomain = domain;
        domain = newDomain;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.PATTERN_TYPE__DOMAIN, oldDomain, domain));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getServer() {
        return server;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setServer(String newServer) {
        String oldServer = server;
        server = newServer;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.PATTERN_TYPE__SERVER, oldServer, server));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getApplication() {
        return application;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setApplication(String newApplication) {
        String oldApplication = application;
        application = newApplication;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.PATTERN_TYPE__APPLICATION, oldApplication, application));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getModuleType() {
        return moduleType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setModuleType(String newModuleType) {
        String oldModuleType = moduleType;
        moduleType = newModuleType;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.PATTERN_TYPE__MODULE_TYPE, oldModuleType, moduleType));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getModule() {
        return module;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setModule(String newModule) {
        String oldModule = module;
        module = newModule;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.PATTERN_TYPE__MODULE, oldModule, module));
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
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.PATTERN_TYPE__TYPE, oldType, type));
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
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.PATTERN_TYPE__NAME, oldName, name));
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
            eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.PATTERN_TYPE__GBEAN_NAME, oldGbeanName, gbeanName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object eGet(EStructuralFeature eFeature, boolean resolve) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case DeploymentPackage.PATTERN_TYPE__DOMAIN:
                return getDomain();
            case DeploymentPackage.PATTERN_TYPE__SERVER:
                return getServer();
            case DeploymentPackage.PATTERN_TYPE__APPLICATION:
                return getApplication();
            case DeploymentPackage.PATTERN_TYPE__MODULE_TYPE:
                return getModuleType();
            case DeploymentPackage.PATTERN_TYPE__MODULE:
                return getModule();
            case DeploymentPackage.PATTERN_TYPE__TYPE:
                return getType();
            case DeploymentPackage.PATTERN_TYPE__NAME:
                return getName();
            case DeploymentPackage.PATTERN_TYPE__GBEAN_NAME:
                return getGbeanName();
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
            case DeploymentPackage.PATTERN_TYPE__DOMAIN:
                setDomain((String)newValue);
                return;
            case DeploymentPackage.PATTERN_TYPE__SERVER:
                setServer((String)newValue);
                return;
            case DeploymentPackage.PATTERN_TYPE__APPLICATION:
                setApplication((String)newValue);
                return;
            case DeploymentPackage.PATTERN_TYPE__MODULE_TYPE:
                setModuleType((String)newValue);
                return;
            case DeploymentPackage.PATTERN_TYPE__MODULE:
                setModule((String)newValue);
                return;
            case DeploymentPackage.PATTERN_TYPE__TYPE:
                setType((String)newValue);
                return;
            case DeploymentPackage.PATTERN_TYPE__NAME:
                setName((String)newValue);
                return;
            case DeploymentPackage.PATTERN_TYPE__GBEAN_NAME:
                setGbeanName((String)newValue);
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
            case DeploymentPackage.PATTERN_TYPE__DOMAIN:
                setDomain(DOMAIN_EDEFAULT);
                return;
            case DeploymentPackage.PATTERN_TYPE__SERVER:
                setServer(SERVER_EDEFAULT);
                return;
            case DeploymentPackage.PATTERN_TYPE__APPLICATION:
                setApplication(APPLICATION_EDEFAULT);
                return;
            case DeploymentPackage.PATTERN_TYPE__MODULE_TYPE:
                setModuleType(MODULE_TYPE_EDEFAULT);
                return;
            case DeploymentPackage.PATTERN_TYPE__MODULE:
                setModule(MODULE_EDEFAULT);
                return;
            case DeploymentPackage.PATTERN_TYPE__TYPE:
                setType(TYPE_EDEFAULT);
                return;
            case DeploymentPackage.PATTERN_TYPE__NAME:
                setName(NAME_EDEFAULT);
                return;
            case DeploymentPackage.PATTERN_TYPE__GBEAN_NAME:
                setGbeanName(GBEAN_NAME_EDEFAULT);
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
            case DeploymentPackage.PATTERN_TYPE__DOMAIN:
                return DOMAIN_EDEFAULT == null ? domain != null : !DOMAIN_EDEFAULT.equals(domain);
            case DeploymentPackage.PATTERN_TYPE__SERVER:
                return SERVER_EDEFAULT == null ? server != null : !SERVER_EDEFAULT.equals(server);
            case DeploymentPackage.PATTERN_TYPE__APPLICATION:
                return APPLICATION_EDEFAULT == null ? application != null : !APPLICATION_EDEFAULT.equals(application);
            case DeploymentPackage.PATTERN_TYPE__MODULE_TYPE:
                return MODULE_TYPE_EDEFAULT == null ? moduleType != null : !MODULE_TYPE_EDEFAULT.equals(moduleType);
            case DeploymentPackage.PATTERN_TYPE__MODULE:
                return MODULE_EDEFAULT == null ? module != null : !MODULE_EDEFAULT.equals(module);
            case DeploymentPackage.PATTERN_TYPE__TYPE:
                return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
            case DeploymentPackage.PATTERN_TYPE__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case DeploymentPackage.PATTERN_TYPE__GBEAN_NAME:
                return GBEAN_NAME_EDEFAULT == null ? gbeanName != null : !GBEAN_NAME_EDEFAULT.equals(gbeanName);
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
        result.append(" (domain: ");
        result.append(domain);
        result.append(", server: ");
        result.append(server);
        result.append(", application: ");
        result.append(application);
        result.append(", moduleType: ");
        result.append(moduleType);
        result.append(", module: ");
        result.append(module);
        result.append(", type: ");
        result.append(type);
        result.append(", name: ");
        result.append(name);
        result.append(", gbeanName: ");
        result.append(gbeanName);
        result.append(')');
        return result.toString();
    }

} //PatternTypeImpl
