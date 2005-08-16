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

import org.apache.geronimo.xml.ns.deployment.DeploymentPackage;

import org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl;

import org.apache.geronimo.xml.ns.naming.CssType;
import org.apache.geronimo.xml.ns.naming.DocumentRoot;
import org.apache.geronimo.xml.ns.naming.EjbLocalRefType;
import org.apache.geronimo.xml.ns.naming.EjbRefType;
import org.apache.geronimo.xml.ns.naming.GbeanLocatorType;
import org.apache.geronimo.xml.ns.naming.GbeanRefType;
import org.apache.geronimo.xml.ns.naming.NamingFactory;
import org.apache.geronimo.xml.ns.naming.NamingPackage;
import org.apache.geronimo.xml.ns.naming.PortCompletionType;
import org.apache.geronimo.xml.ns.naming.PortType;
import org.apache.geronimo.xml.ns.naming.ResourceEnvRefType;
import org.apache.geronimo.xml.ns.naming.ResourceLocatorType;
import org.apache.geronimo.xml.ns.naming.ResourceRefType;
import org.apache.geronimo.xml.ns.naming.ServiceCompletionType;
import org.apache.geronimo.xml.ns.naming.ServiceRefType;

import org.apache.geronimo.xml.ns.security.SecurityPackage;

import org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl;

import org.apache.geronimo.xml.ns.web.WebPackage;

import org.apache.geronimo.xml.ns.web.impl.WebPackageImpl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import org.eclipse.emf.ecore.xml.type.impl.XMLTypePackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class NamingPackageImpl extends EPackageImpl implements NamingPackage {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass cssTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass documentRootEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass ejbLocalRefTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass ejbRefTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass gbeanLocatorTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass gbeanRefTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass portCompletionTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass portTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass resourceEnvRefTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass resourceLocatorTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass resourceRefTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass serviceCompletionTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass serviceRefTypeEClass = null;

    /**
     * Creates an instance of the model <b>Package</b>, registered with
     * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
     * package URI value.
     * <p>Note: the correct way to create the package is via the static
     * factory method {@link #init init()}, which also performs
     * initialization of the package, or returns the registered package,
     * if one already exists.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private NamingPackageImpl() {
        super(eNS_URI, NamingFactory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this
     * model, and for any others upon which it depends.  Simple
     * dependencies are satisfied by calling this method on all
     * dependent packages before doing anything else.  This method drives
     * initialization for interdependent packages directly, in parallel
     * with this package, itself.
     * <p>Of this package and its interdependencies, all packages which
     * have not yet been registered by their URI values are first created
     * and registered.  The packages are then initialized in two steps:
     * meta-model objects for all of the packages are created before any
     * are initialized, since one package's meta-model objects may refer to
     * those of another.
     * <p>Invocation of this method will not affect any packages that have
     * already been initialized.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     * @generated
     */
    public static NamingPackage init() {
        if (isInited) return (NamingPackage)EPackage.Registry.INSTANCE.getEPackage(NamingPackage.eNS_URI);

        // Obtain or create and register package
        NamingPackageImpl theNamingPackage = (NamingPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof NamingPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new NamingPackageImpl());

        isInited = true;

        // Initialize simple dependencies
        XMLTypePackageImpl.init();

        // Obtain or create and register interdependencies
        DeploymentPackageImpl theDeploymentPackage = (DeploymentPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(DeploymentPackage.eNS_URI) instanceof DeploymentPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(DeploymentPackage.eNS_URI) : DeploymentPackage.eINSTANCE);
        WebPackageImpl theWebPackage = (WebPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(WebPackage.eNS_URI) instanceof WebPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(WebPackage.eNS_URI) : WebPackage.eINSTANCE);
        SecurityPackageImpl theSecurityPackage = (SecurityPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(SecurityPackage.eNS_URI) instanceof SecurityPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(SecurityPackage.eNS_URI) : SecurityPackage.eINSTANCE);

        // Create package meta-data objects
        theNamingPackage.createPackageContents();
        theDeploymentPackage.createPackageContents();
        theWebPackage.createPackageContents();
        theSecurityPackage.createPackageContents();

        // Initialize created meta-data
        theNamingPackage.initializePackageContents();
        theDeploymentPackage.initializePackageContents();
        theWebPackage.initializePackageContents();
        theSecurityPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theNamingPackage.freeze();

        return theNamingPackage;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getCssType() {
        return cssTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCssType_Domain() {
        return (EAttribute)cssTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCssType_Server() {
        return (EAttribute)cssTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCssType_Application() {
        return (EAttribute)cssTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCssType_Module() {
        return (EAttribute)cssTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCssType_Type() {
        return (EAttribute)cssTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCssType_Name() {
        return (EAttribute)cssTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getDocumentRoot() {
        return documentRootEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getDocumentRoot_Mixed() {
        return (EAttribute)documentRootEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_XMLNSPrefixMap() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_XSISchemaLocation() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_CmpConnectionFactory() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_EjbLocalRef() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_EjbRef() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_ResourceAdapter() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_ResourceEnvRef() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_ResourceRef() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_ServiceRef() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(9);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getDocumentRoot_Workmanager() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(10);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getEjbLocalRefType() {
        return ejbLocalRefTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbLocalRefType_RefName() {
        return (EAttribute)ejbLocalRefTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbLocalRefType_Domain() {
        return (EAttribute)ejbLocalRefTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbLocalRefType_Server() {
        return (EAttribute)ejbLocalRefTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbLocalRefType_Application() {
        return (EAttribute)ejbLocalRefTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbLocalRefType_Module() {
        return (EAttribute)ejbLocalRefTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbLocalRefType_Type() {
        return (EAttribute)ejbLocalRefTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbLocalRefType_Name() {
        return (EAttribute)ejbLocalRefTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbLocalRefType_EjbLink() {
        return (EAttribute)ejbLocalRefTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbLocalRefType_TargetName() {
        return (EAttribute)ejbLocalRefTypeEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getEjbRefType() {
        return ejbRefTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRefType_RefName() {
        return (EAttribute)ejbRefTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRefType_Domain() {
        return (EAttribute)ejbRefTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRefType_Server() {
        return (EAttribute)ejbRefTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRefType_Application() {
        return (EAttribute)ejbRefTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRefType_Module() {
        return (EAttribute)ejbRefTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRefType_Type() {
        return (EAttribute)ejbRefTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRefType_Name() {
        return (EAttribute)ejbRefTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRefType_NsCorbaloc() {
        return (EAttribute)ejbRefTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRefType_Name1() {
        return (EAttribute)ejbRefTypeEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEjbRefType_Css() {
        return (EReference)ejbRefTypeEClass.getEStructuralFeatures().get(9);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRefType_CssLink() {
        return (EAttribute)ejbRefTypeEClass.getEStructuralFeatures().get(10);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRefType_CssName() {
        return (EAttribute)ejbRefTypeEClass.getEStructuralFeatures().get(11);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRefType_EjbLink() {
        return (EAttribute)ejbRefTypeEClass.getEStructuralFeatures().get(12);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRefType_TargetName() {
        return (EAttribute)ejbRefTypeEClass.getEStructuralFeatures().get(13);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getGbeanLocatorType() {
        return gbeanLocatorTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanLocatorType_Domain() {
        return (EAttribute)gbeanLocatorTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanLocatorType_Server() {
        return (EAttribute)gbeanLocatorTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanLocatorType_Application() {
        return (EAttribute)gbeanLocatorTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanLocatorType_Module() {
        return (EAttribute)gbeanLocatorTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanLocatorType_Type() {
        return (EAttribute)gbeanLocatorTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanLocatorType_Name() {
        return (EAttribute)gbeanLocatorTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanLocatorType_GbeanLink() {
        return (EAttribute)gbeanLocatorTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanLocatorType_TargetName() {
        return (EAttribute)gbeanLocatorTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getGbeanRefType() {
        return gbeanRefTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanRefType_RefName() {
        return (EAttribute)gbeanRefTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanRefType_RefType() {
        return (EAttribute)gbeanRefTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanRefType_ProxyType() {
        return (EAttribute)gbeanRefTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanRefType_Group() {
        return (EAttribute)gbeanRefTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanRefType_Domain() {
        return (EAttribute)gbeanRefTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanRefType_Server() {
        return (EAttribute)gbeanRefTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanRefType_Application() {
        return (EAttribute)gbeanRefTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanRefType_Module() {
        return (EAttribute)gbeanRefTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanRefType_Type() {
        return (EAttribute)gbeanRefTypeEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanRefType_Name() {
        return (EAttribute)gbeanRefTypeEClass.getEStructuralFeatures().get(9);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGbeanRefType_TargetName() {
        return (EAttribute)gbeanRefTypeEClass.getEStructuralFeatures().get(10);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getPortCompletionType() {
        return portCompletionTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPortCompletionType_BindingName() {
        return (EAttribute)portCompletionTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getPortType() {
        return portTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPortType_PortName() {
        return (EAttribute)portTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPortType_Protocol() {
        return (EAttribute)portTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPortType_Host() {
        return (EAttribute)portTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPortType_Port() {
        return (EAttribute)portTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPortType_Uri() {
        return (EAttribute)portTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getPortType_CredentialsName() {
        return (EAttribute)portTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getResourceEnvRefType() {
        return resourceEnvRefTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceEnvRefType_RefName() {
        return (EAttribute)resourceEnvRefTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceEnvRefType_Domain() {
        return (EAttribute)resourceEnvRefTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceEnvRefType_Server() {
        return (EAttribute)resourceEnvRefTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceEnvRefType_Application() {
        return (EAttribute)resourceEnvRefTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceEnvRefType_Module() {
        return (EAttribute)resourceEnvRefTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceEnvRefType_Type() {
        return (EAttribute)resourceEnvRefTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceEnvRefType_Name() {
        return (EAttribute)resourceEnvRefTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceEnvRefType_MessageDestinationLink() {
        return (EAttribute)resourceEnvRefTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceEnvRefType_TargetName() {
        return (EAttribute)resourceEnvRefTypeEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getResourceLocatorType() {
        return resourceLocatorTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceLocatorType_Domain() {
        return (EAttribute)resourceLocatorTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceLocatorType_Server() {
        return (EAttribute)resourceLocatorTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceLocatorType_Application() {
        return (EAttribute)resourceLocatorTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceLocatorType_Module() {
        return (EAttribute)resourceLocatorTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceLocatorType_Type() {
        return (EAttribute)resourceLocatorTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceLocatorType_Name() {
        return (EAttribute)resourceLocatorTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceLocatorType_ResourceLink() {
        return (EAttribute)resourceLocatorTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceLocatorType_TargetName() {
        return (EAttribute)resourceLocatorTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceLocatorType_Url() {
        return (EAttribute)resourceLocatorTypeEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getResourceRefType() {
        return resourceRefTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceRefType_RefName() {
        return (EAttribute)resourceRefTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceRefType_Domain() {
        return (EAttribute)resourceRefTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceRefType_Server() {
        return (EAttribute)resourceRefTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceRefType_Application() {
        return (EAttribute)resourceRefTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceRefType_Module() {
        return (EAttribute)resourceRefTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceRefType_Type() {
        return (EAttribute)resourceRefTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceRefType_Name() {
        return (EAttribute)resourceRefTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceRefType_ResourceLink() {
        return (EAttribute)resourceRefTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceRefType_TargetName() {
        return (EAttribute)resourceRefTypeEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getResourceRefType_Url() {
        return (EAttribute)resourceRefTypeEClass.getEStructuralFeatures().get(9);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getServiceCompletionType() {
        return serviceCompletionTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getServiceCompletionType_ServiceName() {
        return (EAttribute)serviceCompletionTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getServiceCompletionType_Port() {
        return (EReference)serviceCompletionTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getServiceRefType() {
        return serviceRefTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getServiceRefType_ServiceRefName() {
        return (EAttribute)serviceRefTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getServiceRefType_ServiceCompletion() {
        return (EReference)serviceRefTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getServiceRefType_Port() {
        return (EReference)serviceRefTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NamingFactory getNamingFactory() {
        return (NamingFactory)getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package.  This method is
     * guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void createPackageContents() {
        if (isCreated) return;
        isCreated = true;

        // Create classes and their features
        cssTypeEClass = createEClass(CSS_TYPE);
        createEAttribute(cssTypeEClass, CSS_TYPE__DOMAIN);
        createEAttribute(cssTypeEClass, CSS_TYPE__SERVER);
        createEAttribute(cssTypeEClass, CSS_TYPE__APPLICATION);
        createEAttribute(cssTypeEClass, CSS_TYPE__MODULE);
        createEAttribute(cssTypeEClass, CSS_TYPE__TYPE);
        createEAttribute(cssTypeEClass, CSS_TYPE__NAME);

        documentRootEClass = createEClass(DOCUMENT_ROOT);
        createEAttribute(documentRootEClass, DOCUMENT_ROOT__MIXED);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
        createEReference(documentRootEClass, DOCUMENT_ROOT__CMP_CONNECTION_FACTORY);
        createEReference(documentRootEClass, DOCUMENT_ROOT__EJB_LOCAL_REF);
        createEReference(documentRootEClass, DOCUMENT_ROOT__EJB_REF);
        createEReference(documentRootEClass, DOCUMENT_ROOT__RESOURCE_ADAPTER);
        createEReference(documentRootEClass, DOCUMENT_ROOT__RESOURCE_ENV_REF);
        createEReference(documentRootEClass, DOCUMENT_ROOT__RESOURCE_REF);
        createEReference(documentRootEClass, DOCUMENT_ROOT__SERVICE_REF);
        createEReference(documentRootEClass, DOCUMENT_ROOT__WORKMANAGER);

        ejbLocalRefTypeEClass = createEClass(EJB_LOCAL_REF_TYPE);
        createEAttribute(ejbLocalRefTypeEClass, EJB_LOCAL_REF_TYPE__REF_NAME);
        createEAttribute(ejbLocalRefTypeEClass, EJB_LOCAL_REF_TYPE__DOMAIN);
        createEAttribute(ejbLocalRefTypeEClass, EJB_LOCAL_REF_TYPE__SERVER);
        createEAttribute(ejbLocalRefTypeEClass, EJB_LOCAL_REF_TYPE__APPLICATION);
        createEAttribute(ejbLocalRefTypeEClass, EJB_LOCAL_REF_TYPE__MODULE);
        createEAttribute(ejbLocalRefTypeEClass, EJB_LOCAL_REF_TYPE__TYPE);
        createEAttribute(ejbLocalRefTypeEClass, EJB_LOCAL_REF_TYPE__NAME);
        createEAttribute(ejbLocalRefTypeEClass, EJB_LOCAL_REF_TYPE__EJB_LINK);
        createEAttribute(ejbLocalRefTypeEClass, EJB_LOCAL_REF_TYPE__TARGET_NAME);

        ejbRefTypeEClass = createEClass(EJB_REF_TYPE);
        createEAttribute(ejbRefTypeEClass, EJB_REF_TYPE__REF_NAME);
        createEAttribute(ejbRefTypeEClass, EJB_REF_TYPE__DOMAIN);
        createEAttribute(ejbRefTypeEClass, EJB_REF_TYPE__SERVER);
        createEAttribute(ejbRefTypeEClass, EJB_REF_TYPE__APPLICATION);
        createEAttribute(ejbRefTypeEClass, EJB_REF_TYPE__MODULE);
        createEAttribute(ejbRefTypeEClass, EJB_REF_TYPE__TYPE);
        createEAttribute(ejbRefTypeEClass, EJB_REF_TYPE__NAME);
        createEAttribute(ejbRefTypeEClass, EJB_REF_TYPE__NS_CORBALOC);
        createEAttribute(ejbRefTypeEClass, EJB_REF_TYPE__NAME1);
        createEReference(ejbRefTypeEClass, EJB_REF_TYPE__CSS);
        createEAttribute(ejbRefTypeEClass, EJB_REF_TYPE__CSS_LINK);
        createEAttribute(ejbRefTypeEClass, EJB_REF_TYPE__CSS_NAME);
        createEAttribute(ejbRefTypeEClass, EJB_REF_TYPE__EJB_LINK);
        createEAttribute(ejbRefTypeEClass, EJB_REF_TYPE__TARGET_NAME);

        gbeanLocatorTypeEClass = createEClass(GBEAN_LOCATOR_TYPE);
        createEAttribute(gbeanLocatorTypeEClass, GBEAN_LOCATOR_TYPE__DOMAIN);
        createEAttribute(gbeanLocatorTypeEClass, GBEAN_LOCATOR_TYPE__SERVER);
        createEAttribute(gbeanLocatorTypeEClass, GBEAN_LOCATOR_TYPE__APPLICATION);
        createEAttribute(gbeanLocatorTypeEClass, GBEAN_LOCATOR_TYPE__MODULE);
        createEAttribute(gbeanLocatorTypeEClass, GBEAN_LOCATOR_TYPE__TYPE);
        createEAttribute(gbeanLocatorTypeEClass, GBEAN_LOCATOR_TYPE__NAME);
        createEAttribute(gbeanLocatorTypeEClass, GBEAN_LOCATOR_TYPE__GBEAN_LINK);
        createEAttribute(gbeanLocatorTypeEClass, GBEAN_LOCATOR_TYPE__TARGET_NAME);

        gbeanRefTypeEClass = createEClass(GBEAN_REF_TYPE);
        createEAttribute(gbeanRefTypeEClass, GBEAN_REF_TYPE__REF_NAME);
        createEAttribute(gbeanRefTypeEClass, GBEAN_REF_TYPE__REF_TYPE);
        createEAttribute(gbeanRefTypeEClass, GBEAN_REF_TYPE__PROXY_TYPE);
        createEAttribute(gbeanRefTypeEClass, GBEAN_REF_TYPE__GROUP);
        createEAttribute(gbeanRefTypeEClass, GBEAN_REF_TYPE__DOMAIN);
        createEAttribute(gbeanRefTypeEClass, GBEAN_REF_TYPE__SERVER);
        createEAttribute(gbeanRefTypeEClass, GBEAN_REF_TYPE__APPLICATION);
        createEAttribute(gbeanRefTypeEClass, GBEAN_REF_TYPE__MODULE);
        createEAttribute(gbeanRefTypeEClass, GBEAN_REF_TYPE__TYPE);
        createEAttribute(gbeanRefTypeEClass, GBEAN_REF_TYPE__NAME);
        createEAttribute(gbeanRefTypeEClass, GBEAN_REF_TYPE__TARGET_NAME);

        portCompletionTypeEClass = createEClass(PORT_COMPLETION_TYPE);
        createEAttribute(portCompletionTypeEClass, PORT_COMPLETION_TYPE__BINDING_NAME);

        portTypeEClass = createEClass(PORT_TYPE);
        createEAttribute(portTypeEClass, PORT_TYPE__PORT_NAME);
        createEAttribute(portTypeEClass, PORT_TYPE__PROTOCOL);
        createEAttribute(portTypeEClass, PORT_TYPE__HOST);
        createEAttribute(portTypeEClass, PORT_TYPE__PORT);
        createEAttribute(portTypeEClass, PORT_TYPE__URI);
        createEAttribute(portTypeEClass, PORT_TYPE__CREDENTIALS_NAME);

        resourceEnvRefTypeEClass = createEClass(RESOURCE_ENV_REF_TYPE);
        createEAttribute(resourceEnvRefTypeEClass, RESOURCE_ENV_REF_TYPE__REF_NAME);
        createEAttribute(resourceEnvRefTypeEClass, RESOURCE_ENV_REF_TYPE__DOMAIN);
        createEAttribute(resourceEnvRefTypeEClass, RESOURCE_ENV_REF_TYPE__SERVER);
        createEAttribute(resourceEnvRefTypeEClass, RESOURCE_ENV_REF_TYPE__APPLICATION);
        createEAttribute(resourceEnvRefTypeEClass, RESOURCE_ENV_REF_TYPE__MODULE);
        createEAttribute(resourceEnvRefTypeEClass, RESOURCE_ENV_REF_TYPE__TYPE);
        createEAttribute(resourceEnvRefTypeEClass, RESOURCE_ENV_REF_TYPE__NAME);
        createEAttribute(resourceEnvRefTypeEClass, RESOURCE_ENV_REF_TYPE__MESSAGE_DESTINATION_LINK);
        createEAttribute(resourceEnvRefTypeEClass, RESOURCE_ENV_REF_TYPE__TARGET_NAME);

        resourceLocatorTypeEClass = createEClass(RESOURCE_LOCATOR_TYPE);
        createEAttribute(resourceLocatorTypeEClass, RESOURCE_LOCATOR_TYPE__DOMAIN);
        createEAttribute(resourceLocatorTypeEClass, RESOURCE_LOCATOR_TYPE__SERVER);
        createEAttribute(resourceLocatorTypeEClass, RESOURCE_LOCATOR_TYPE__APPLICATION);
        createEAttribute(resourceLocatorTypeEClass, RESOURCE_LOCATOR_TYPE__MODULE);
        createEAttribute(resourceLocatorTypeEClass, RESOURCE_LOCATOR_TYPE__TYPE);
        createEAttribute(resourceLocatorTypeEClass, RESOURCE_LOCATOR_TYPE__NAME);
        createEAttribute(resourceLocatorTypeEClass, RESOURCE_LOCATOR_TYPE__RESOURCE_LINK);
        createEAttribute(resourceLocatorTypeEClass, RESOURCE_LOCATOR_TYPE__TARGET_NAME);
        createEAttribute(resourceLocatorTypeEClass, RESOURCE_LOCATOR_TYPE__URL);

        resourceRefTypeEClass = createEClass(RESOURCE_REF_TYPE);
        createEAttribute(resourceRefTypeEClass, RESOURCE_REF_TYPE__REF_NAME);
        createEAttribute(resourceRefTypeEClass, RESOURCE_REF_TYPE__DOMAIN);
        createEAttribute(resourceRefTypeEClass, RESOURCE_REF_TYPE__SERVER);
        createEAttribute(resourceRefTypeEClass, RESOURCE_REF_TYPE__APPLICATION);
        createEAttribute(resourceRefTypeEClass, RESOURCE_REF_TYPE__MODULE);
        createEAttribute(resourceRefTypeEClass, RESOURCE_REF_TYPE__TYPE);
        createEAttribute(resourceRefTypeEClass, RESOURCE_REF_TYPE__NAME);
        createEAttribute(resourceRefTypeEClass, RESOURCE_REF_TYPE__RESOURCE_LINK);
        createEAttribute(resourceRefTypeEClass, RESOURCE_REF_TYPE__TARGET_NAME);
        createEAttribute(resourceRefTypeEClass, RESOURCE_REF_TYPE__URL);

        serviceCompletionTypeEClass = createEClass(SERVICE_COMPLETION_TYPE);
        createEAttribute(serviceCompletionTypeEClass, SERVICE_COMPLETION_TYPE__SERVICE_NAME);
        createEReference(serviceCompletionTypeEClass, SERVICE_COMPLETION_TYPE__PORT);

        serviceRefTypeEClass = createEClass(SERVICE_REF_TYPE);
        createEAttribute(serviceRefTypeEClass, SERVICE_REF_TYPE__SERVICE_REF_NAME);
        createEReference(serviceRefTypeEClass, SERVICE_REF_TYPE__SERVICE_COMPLETION);
        createEReference(serviceRefTypeEClass, SERVICE_REF_TYPE__PORT);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model.  This
     * method is guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void initializePackageContents() {
        if (isInitialized) return;
        isInitialized = true;

        // Initialize package
        setName(eNAME);
        setNsPrefix(eNS_PREFIX);
        setNsURI(eNS_URI);

        // Obtain other dependent packages
        XMLTypePackageImpl theXMLTypePackage = (XMLTypePackageImpl)EPackage.Registry.INSTANCE.getEPackage(XMLTypePackage.eNS_URI);

        // Add supertypes to classes
        portCompletionTypeEClass.getESuperTypes().add(this.getPortType());

        // Initialize classes and features; add operations and parameters
        initEClass(cssTypeEClass, CssType.class, "CssType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getCssType_Domain(), theXMLTypePackage.getString(), "domain", null, 0, 1, CssType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCssType_Server(), theXMLTypePackage.getString(), "server", null, 0, 1, CssType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCssType_Application(), theXMLTypePackage.getString(), "application", null, 0, 1, CssType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCssType_Module(), theXMLTypePackage.getString(), "module", null, 0, 1, CssType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCssType_Type(), theXMLTypePackage.getString(), "type", null, 0, 1, CssType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCssType_Name(), theXMLTypePackage.getString(), "name", null, 1, 1, CssType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(documentRootEClass, DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDocumentRoot_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, null, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XMLNSPrefixMap(), ecorePackage.getEStringToStringMapEntry(), null, "xMLNSPrefixMap", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XSISchemaLocation(), ecorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_CmpConnectionFactory(), this.getResourceLocatorType(), null, "cmpConnectionFactory", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_EjbLocalRef(), this.getEjbLocalRefType(), null, "ejbLocalRef", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_EjbRef(), this.getEjbRefType(), null, "ejbRef", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_ResourceAdapter(), this.getResourceLocatorType(), null, "resourceAdapter", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_ResourceEnvRef(), this.getResourceEnvRefType(), null, "resourceEnvRef", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_ResourceRef(), this.getResourceRefType(), null, "resourceRef", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_ServiceRef(), this.getServiceRefType(), null, "serviceRef", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_Workmanager(), this.getGbeanLocatorType(), null, "workmanager", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

        initEClass(ejbLocalRefTypeEClass, EjbLocalRefType.class, "EjbLocalRefType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getEjbLocalRefType_RefName(), theXMLTypePackage.getString(), "refName", null, 1, 1, EjbLocalRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbLocalRefType_Domain(), theXMLTypePackage.getString(), "domain", null, 0, 1, EjbLocalRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbLocalRefType_Server(), theXMLTypePackage.getString(), "server", null, 0, 1, EjbLocalRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbLocalRefType_Application(), theXMLTypePackage.getString(), "application", null, 0, 1, EjbLocalRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbLocalRefType_Module(), theXMLTypePackage.getString(), "module", null, 0, 1, EjbLocalRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbLocalRefType_Type(), theXMLTypePackage.getString(), "type", null, 0, 1, EjbLocalRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbLocalRefType_Name(), theXMLTypePackage.getString(), "name", null, 0, 1, EjbLocalRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbLocalRefType_EjbLink(), theXMLTypePackage.getString(), "ejbLink", null, 0, 1, EjbLocalRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbLocalRefType_TargetName(), theXMLTypePackage.getString(), "targetName", null, 0, 1, EjbLocalRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(ejbRefTypeEClass, EjbRefType.class, "EjbRefType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getEjbRefType_RefName(), theXMLTypePackage.getString(), "refName", null, 1, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbRefType_Domain(), theXMLTypePackage.getString(), "domain", null, 0, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbRefType_Server(), theXMLTypePackage.getString(), "server", null, 0, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbRefType_Application(), theXMLTypePackage.getString(), "application", null, 0, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbRefType_Module(), theXMLTypePackage.getString(), "module", null, 0, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbRefType_Type(), theXMLTypePackage.getString(), "type", null, 0, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbRefType_Name(), theXMLTypePackage.getString(), "name", null, 0, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbRefType_NsCorbaloc(), theXMLTypePackage.getAnyURI(), "nsCorbaloc", null, 0, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbRefType_Name1(), theXMLTypePackage.getString(), "name1", null, 0, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEjbRefType_Css(), this.getCssType(), null, "css", null, 0, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbRefType_CssLink(), theXMLTypePackage.getString(), "cssLink", null, 0, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbRefType_CssName(), theXMLTypePackage.getString(), "cssName", null, 0, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbRefType_EjbLink(), theXMLTypePackage.getString(), "ejbLink", null, 0, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbRefType_TargetName(), theXMLTypePackage.getString(), "targetName", null, 0, 1, EjbRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(gbeanLocatorTypeEClass, GbeanLocatorType.class, "GbeanLocatorType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getGbeanLocatorType_Domain(), theXMLTypePackage.getString(), "domain", null, 0, 1, GbeanLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanLocatorType_Server(), theXMLTypePackage.getString(), "server", null, 0, 1, GbeanLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanLocatorType_Application(), theXMLTypePackage.getString(), "application", null, 0, 1, GbeanLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanLocatorType_Module(), theXMLTypePackage.getString(), "module", null, 0, 1, GbeanLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanLocatorType_Type(), theXMLTypePackage.getString(), "type", null, 0, 1, GbeanLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanLocatorType_Name(), theXMLTypePackage.getString(), "name", null, 0, 1, GbeanLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanLocatorType_GbeanLink(), theXMLTypePackage.getString(), "gbeanLink", null, 0, 1, GbeanLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanLocatorType_TargetName(), theXMLTypePackage.getString(), "targetName", null, 0, 1, GbeanLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(gbeanRefTypeEClass, GbeanRefType.class, "GbeanRefType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getGbeanRefType_RefName(), theXMLTypePackage.getString(), "refName", null, 1, 1, GbeanRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanRefType_RefType(), theXMLTypePackage.getString(), "refType", null, 1, 1, GbeanRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanRefType_ProxyType(), theXMLTypePackage.getString(), "proxyType", null, 0, 1, GbeanRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanRefType_Group(), ecorePackage.getEFeatureMapEntry(), "group", null, 0, -1, GbeanRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanRefType_Domain(), theXMLTypePackage.getString(), "domain", null, 0, -1, GbeanRefType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanRefType_Server(), theXMLTypePackage.getString(), "server", null, 0, -1, GbeanRefType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanRefType_Application(), theXMLTypePackage.getString(), "application", null, 0, -1, GbeanRefType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanRefType_Module(), theXMLTypePackage.getString(), "module", null, 0, -1, GbeanRefType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanRefType_Type(), theXMLTypePackage.getString(), "type", null, 0, -1, GbeanRefType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanRefType_Name(), theXMLTypePackage.getString(), "name", null, 0, -1, GbeanRefType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEAttribute(getGbeanRefType_TargetName(), theXMLTypePackage.getString(), "targetName", null, 0, -1, GbeanRefType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, IS_DERIVED, IS_ORDERED);

        initEClass(portCompletionTypeEClass, PortCompletionType.class, "PortCompletionType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getPortCompletionType_BindingName(), theXMLTypePackage.getString(), "bindingName", null, 1, 1, PortCompletionType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(portTypeEClass, PortType.class, "PortType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getPortType_PortName(), theXMLTypePackage.getString(), "portName", null, 1, 1, PortType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPortType_Protocol(), theXMLTypePackage.getString(), "protocol", null, 0, 1, PortType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPortType_Host(), theXMLTypePackage.getString(), "host", null, 0, 1, PortType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPortType_Port(), theXMLTypePackage.getInt(), "port", null, 0, 1, PortType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPortType_Uri(), theXMLTypePackage.getString(), "uri", null, 1, 1, PortType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getPortType_CredentialsName(), theXMLTypePackage.getString(), "credentialsName", null, 0, 1, PortType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(resourceEnvRefTypeEClass, ResourceEnvRefType.class, "ResourceEnvRefType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getResourceEnvRefType_RefName(), theXMLTypePackage.getString(), "refName", null, 1, 1, ResourceEnvRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceEnvRefType_Domain(), theXMLTypePackage.getString(), "domain", null, 0, 1, ResourceEnvRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceEnvRefType_Server(), theXMLTypePackage.getString(), "server", null, 0, 1, ResourceEnvRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceEnvRefType_Application(), theXMLTypePackage.getString(), "application", null, 0, 1, ResourceEnvRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceEnvRefType_Module(), theXMLTypePackage.getString(), "module", null, 0, 1, ResourceEnvRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceEnvRefType_Type(), theXMLTypePackage.getString(), "type", null, 0, 1, ResourceEnvRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceEnvRefType_Name(), theXMLTypePackage.getString(), "name", null, 0, 1, ResourceEnvRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceEnvRefType_MessageDestinationLink(), theXMLTypePackage.getString(), "messageDestinationLink", null, 0, 1, ResourceEnvRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceEnvRefType_TargetName(), theXMLTypePackage.getString(), "targetName", null, 0, 1, ResourceEnvRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(resourceLocatorTypeEClass, ResourceLocatorType.class, "ResourceLocatorType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getResourceLocatorType_Domain(), theXMLTypePackage.getString(), "domain", null, 0, 1, ResourceLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceLocatorType_Server(), theXMLTypePackage.getString(), "server", null, 0, 1, ResourceLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceLocatorType_Application(), theXMLTypePackage.getString(), "application", null, 0, 1, ResourceLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceLocatorType_Module(), theXMLTypePackage.getString(), "module", null, 0, 1, ResourceLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceLocatorType_Type(), theXMLTypePackage.getString(), "type", null, 0, 1, ResourceLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceLocatorType_Name(), theXMLTypePackage.getString(), "name", null, 0, 1, ResourceLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceLocatorType_ResourceLink(), theXMLTypePackage.getString(), "resourceLink", null, 0, 1, ResourceLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceLocatorType_TargetName(), theXMLTypePackage.getString(), "targetName", null, 0, 1, ResourceLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceLocatorType_Url(), theXMLTypePackage.getString(), "url", null, 0, 1, ResourceLocatorType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(resourceRefTypeEClass, ResourceRefType.class, "ResourceRefType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getResourceRefType_RefName(), theXMLTypePackage.getString(), "refName", null, 1, 1, ResourceRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceRefType_Domain(), theXMLTypePackage.getString(), "domain", null, 0, 1, ResourceRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceRefType_Server(), theXMLTypePackage.getString(), "server", null, 0, 1, ResourceRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceRefType_Application(), theXMLTypePackage.getString(), "application", null, 0, 1, ResourceRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceRefType_Module(), theXMLTypePackage.getString(), "module", null, 0, 1, ResourceRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceRefType_Type(), theXMLTypePackage.getString(), "type", null, 0, 1, ResourceRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceRefType_Name(), theXMLTypePackage.getString(), "name", null, 0, 1, ResourceRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceRefType_ResourceLink(), theXMLTypePackage.getString(), "resourceLink", null, 0, 1, ResourceRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceRefType_TargetName(), theXMLTypePackage.getString(), "targetName", null, 0, 1, ResourceRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getResourceRefType_Url(), theXMLTypePackage.getString(), "url", null, 0, 1, ResourceRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(serviceCompletionTypeEClass, ServiceCompletionType.class, "ServiceCompletionType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getServiceCompletionType_ServiceName(), theXMLTypePackage.getString(), "serviceName", null, 1, 1, ServiceCompletionType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getServiceCompletionType_Port(), this.getPortCompletionType(), null, "port", null, 1, -1, ServiceCompletionType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(serviceRefTypeEClass, ServiceRefType.class, "ServiceRefType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getServiceRefType_ServiceRefName(), theXMLTypePackage.getString(), "serviceRefName", null, 1, 1, ServiceRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getServiceRefType_ServiceCompletion(), this.getServiceCompletionType(), null, "serviceCompletion", null, 0, 1, ServiceRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getServiceRefType_Port(), this.getPortType(), null, "port", null, 0, -1, ServiceRefType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Create resource
        createResource(eNS_URI);

        // Create annotations
        // http:///org/eclipse/emf/ecore/util/ExtendedMetaData
        createExtendedMetaDataAnnotations();
    }

    /**
     * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected void createExtendedMetaDataAnnotations() {
        String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";			
        addAnnotation
          (cssTypeEClass, 
           source, 
           new String[] {
             "name", "cssType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getCssType_Domain(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "domain",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCssType_Server(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "server",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCssType_Application(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "application",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCssType_Module(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "module",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCssType_Type(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "type",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCssType_Name(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (documentRootEClass, 
           source, 
           new String[] {
             "name", "",
             "kind", "mixed"
           });		
        addAnnotation
          (getDocumentRoot_Mixed(), 
           source, 
           new String[] {
             "kind", "elementWildcard",
             "name", ":mixed"
           });		
        addAnnotation
          (getDocumentRoot_XMLNSPrefixMap(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "xmlns:prefix"
           });		
        addAnnotation
          (getDocumentRoot_XSISchemaLocation(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "xsi:schemaLocation"
           });		
        addAnnotation
          (getDocumentRoot_CmpConnectionFactory(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmp-connection-factory",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDocumentRoot_EjbLocalRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-local-ref",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDocumentRoot_EjbRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-ref",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDocumentRoot_ResourceAdapter(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-adapter",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDocumentRoot_ResourceEnvRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-env-ref",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDocumentRoot_ResourceRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-ref",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDocumentRoot_ServiceRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "service-ref",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getDocumentRoot_Workmanager(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "workmanager",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (ejbLocalRefTypeEClass, 
           source, 
           new String[] {
             "name", "ejb-local-refType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getEjbLocalRefType_RefName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ref-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbLocalRefType_Domain(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "domain",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbLocalRefType_Server(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "server",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbLocalRefType_Application(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "application",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbLocalRefType_Module(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "module",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbLocalRefType_Type(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "type",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbLocalRefType_Name(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbLocalRefType_EjbLink(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-link",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbLocalRefType_TargetName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "target-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (ejbRefTypeEClass, 
           source, 
           new String[] {
             "name", "ejb-refType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getEjbRefType_RefName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ref-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRefType_Domain(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "domain",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRefType_Server(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "server",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRefType_Application(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "application",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRefType_Module(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "module",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRefType_Type(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "type",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRefType_Name(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "name",
             "namespace", "##targetNamespace"
           });			
        addAnnotation
          (getEjbRefType_NsCorbaloc(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ns-corbaloc",
             "namespace", "##targetNamespace"
           });			
        addAnnotation
          (getEjbRefType_Name1(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRefType_Css(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "css",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRefType_CssLink(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "css-link",
             "namespace", "##targetNamespace"
           });			
        addAnnotation
          (getEjbRefType_CssName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "css-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRefType_EjbLink(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-link",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRefType_TargetName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "target-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (gbeanLocatorTypeEClass, 
           source, 
           new String[] {
             "name", "gbean-locatorType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getGbeanLocatorType_Domain(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "domain",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getGbeanLocatorType_Server(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "server",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getGbeanLocatorType_Application(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "application",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getGbeanLocatorType_Module(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "module",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getGbeanLocatorType_Type(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "type",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getGbeanLocatorType_Name(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getGbeanLocatorType_GbeanLink(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "gbean-link",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getGbeanLocatorType_TargetName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "target-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (gbeanRefTypeEClass, 
           source, 
           new String[] {
             "name", "gbean-refType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getGbeanRefType_RefName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ref-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getGbeanRefType_RefType(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ref-type",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getGbeanRefType_ProxyType(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "proxy-type",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getGbeanRefType_Group(), 
           source, 
           new String[] {
             "kind", "group",
             "name", "group:3"
           });		
        addAnnotation
          (getGbeanRefType_Domain(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "domain",
             "namespace", "##targetNamespace",
             "group", "#group:3"
           });		
        addAnnotation
          (getGbeanRefType_Server(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "server",
             "namespace", "##targetNamespace",
             "group", "#group:3"
           });		
        addAnnotation
          (getGbeanRefType_Application(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "application",
             "namespace", "##targetNamespace",
             "group", "#group:3"
           });		
        addAnnotation
          (getGbeanRefType_Module(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "module",
             "namespace", "##targetNamespace",
             "group", "#group:3"
           });		
        addAnnotation
          (getGbeanRefType_Type(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "type",
             "namespace", "##targetNamespace",
             "group", "#group:3"
           });		
        addAnnotation
          (getGbeanRefType_Name(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "name",
             "namespace", "##targetNamespace",
             "group", "#group:3"
           });		
        addAnnotation
          (getGbeanRefType_TargetName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "target-name",
             "namespace", "##targetNamespace",
             "group", "#group:3"
           });		
        addAnnotation
          (portCompletionTypeEClass, 
           source, 
           new String[] {
             "name", "port-completionType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getPortCompletionType_BindingName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "binding-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (portTypeEClass, 
           source, 
           new String[] {
             "name", "portType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getPortType_PortName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "port-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPortType_Protocol(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "protocol",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPortType_Host(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "host",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPortType_Port(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "port",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPortType_Uri(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "uri",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPortType_CredentialsName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "credentials-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (resourceEnvRefTypeEClass, 
           source, 
           new String[] {
             "name", "resource-env-refType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getResourceEnvRefType_RefName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ref-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceEnvRefType_Domain(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "domain",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceEnvRefType_Server(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "server",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceEnvRefType_Application(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "application",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceEnvRefType_Module(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "module",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceEnvRefType_Type(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "type",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceEnvRefType_Name(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceEnvRefType_MessageDestinationLink(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "message-destination-link",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceEnvRefType_TargetName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "target-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (resourceLocatorTypeEClass, 
           source, 
           new String[] {
             "name", "resource-locatorType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getResourceLocatorType_Domain(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "domain",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceLocatorType_Server(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "server",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceLocatorType_Application(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "application",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceLocatorType_Module(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "module",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceLocatorType_Type(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "type",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceLocatorType_Name(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceLocatorType_ResourceLink(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-link",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceLocatorType_TargetName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "target-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceLocatorType_Url(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "url",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (resourceRefTypeEClass, 
           source, 
           new String[] {
             "name", "resource-refType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getResourceRefType_RefName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ref-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceRefType_Domain(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "domain",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceRefType_Server(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "server",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceRefType_Application(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "application",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceRefType_Module(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "module",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceRefType_Type(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "type",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceRefType_Name(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceRefType_ResourceLink(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-link",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceRefType_TargetName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "target-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getResourceRefType_Url(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "url",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (serviceCompletionTypeEClass, 
           source, 
           new String[] {
             "name", "service-completionType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getServiceCompletionType_ServiceName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "service-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getServiceCompletionType_Port(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "port",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (serviceRefTypeEClass, 
           source, 
           new String[] {
             "name", "service-refType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getServiceRefType_ServiceRefName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "service-ref-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getServiceRefType_ServiceCompletion(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "service-completion",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getServiceRefType_Port(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "port",
             "namespace", "##targetNamespace"
           });
    }

} //NamingPackageImpl
