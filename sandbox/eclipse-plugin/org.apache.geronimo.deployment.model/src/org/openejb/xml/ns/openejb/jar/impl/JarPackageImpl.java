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

import org.apache.geronimo.xml.ns.deployment.DeploymentPackage;

import org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl;

import org.apache.geronimo.xml.ns.naming.NamingPackage;

import org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl;

import org.apache.geronimo.xml.ns.security.SecurityPackage;

import org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EValidator;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import org.eclipse.emf.ecore.xml.type.impl.XMLTypePackageImpl;

import org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType;
import org.openejb.xml.ns.openejb.jar.ActivationConfigType;
import org.openejb.xml.ns.openejb.jar.CmpFieldGroupMappingType;
import org.openejb.xml.ns.openejb.jar.CmpFieldMappingType;
import org.openejb.xml.ns.openejb.jar.CmrFieldGroupMappingType;
import org.openejb.xml.ns.openejb.jar.CmrFieldMappingType;
import org.openejb.xml.ns.openejb.jar.CmrFieldType;
import org.openejb.xml.ns.openejb.jar.CmrFieldType1;
import org.openejb.xml.ns.openejb.jar.DocumentRoot;
import org.openejb.xml.ns.openejb.jar.EjbRelationType;
import org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType;
import org.openejb.xml.ns.openejb.jar.EnterpriseBeansType;
import org.openejb.xml.ns.openejb.jar.EntityBeanType;
import org.openejb.xml.ns.openejb.jar.EntityGroupMappingType;
import org.openejb.xml.ns.openejb.jar.GroupType;
import org.openejb.xml.ns.openejb.jar.JarFactory;
import org.openejb.xml.ns.openejb.jar.JarPackage;
import org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType;
import org.openejb.xml.ns.openejb.jar.MethodParamsType;
import org.openejb.xml.ns.openejb.jar.OpenejbJarType;
import org.openejb.xml.ns.openejb.jar.PrefetchGroupType;
import org.openejb.xml.ns.openejb.jar.QueryMethodType;
import org.openejb.xml.ns.openejb.jar.QueryType;
import org.openejb.xml.ns.openejb.jar.RelationshipRoleSourceType;
import org.openejb.xml.ns.openejb.jar.RelationshipsType;
import org.openejb.xml.ns.openejb.jar.RoleMappingType;
import org.openejb.xml.ns.openejb.jar.SessionBeanType;
import org.openejb.xml.ns.openejb.jar.TransportGuaranteeType;
import org.openejb.xml.ns.openejb.jar.TssType;
import org.openejb.xml.ns.openejb.jar.WebServiceSecurityType;

import org.openejb.xml.ns.openejb.jar.util.JarValidator;

import org.openejb.xml.ns.pkgen.PkgenPackage;

import org.openejb.xml.ns.pkgen.impl.PkgenPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class JarPackageImpl extends EPackageImpl implements JarPackage {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass activationConfigPropertyTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass activationConfigTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass cmpFieldGroupMappingTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass cmpFieldMappingTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass cmrFieldGroupMappingTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass cmrFieldMappingTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass cmrFieldTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass cmrFieldType1EClass = null;

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
    private EClass ejbRelationshipRoleTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass ejbRelationTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass enterpriseBeansTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass entityBeanTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass entityGroupMappingTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass groupTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass messageDrivenBeanTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass methodParamsTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass openejbJarTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass prefetchGroupTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass queryMethodTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass queryTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass relationshipRoleSourceTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass relationshipsTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass roleMappingTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass sessionBeanTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass tssTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass webServiceSecurityTypeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum transportGuaranteeTypeEEnum = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EDataType authMethodTypeEDataType = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EDataType transportGuaranteeTypeObjectEDataType = null;

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
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private JarPackageImpl() {
        super(eNS_URI, JarFactory.eINSTANCE);
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
    public static JarPackage init() {
        if (isInited) return (JarPackage)EPackage.Registry.INSTANCE.getEPackage(JarPackage.eNS_URI);

        // Obtain or create and register package
        JarPackageImpl theJarPackage = (JarPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof JarPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new JarPackageImpl());

        isInited = true;

        // Initialize simple dependencies
        NamingPackageImpl.init();
        DeploymentPackageImpl.init();
        SecurityPackageImpl.init();
        XMLTypePackageImpl.init();

        // Obtain or create and register interdependencies
        PkgenPackageImpl thePkgenPackage = (PkgenPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(PkgenPackage.eNS_URI) instanceof PkgenPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(PkgenPackage.eNS_URI) : PkgenPackage.eINSTANCE);

        // Create package meta-data objects
        theJarPackage.createPackageContents();
        thePkgenPackage.createPackageContents();

        // Initialize created meta-data
        theJarPackage.initializePackageContents();
        thePkgenPackage.initializePackageContents();

        // Register package validator
        EValidator.Registry.INSTANCE.put
            (theJarPackage, 
             new EValidator.Descriptor() {
                 public EValidator getEValidator() {
                     return JarValidator.INSTANCE;
                 }
             });

        // Mark meta-data to indicate it can't be changed
        theJarPackage.freeze();

        return theJarPackage;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getActivationConfigPropertyType() {
        return activationConfigPropertyTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getActivationConfigPropertyType_ActivationConfigPropertyName() {
        return (EAttribute)activationConfigPropertyTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getActivationConfigPropertyType_ActivationConfigPropertyValue() {
        return (EAttribute)activationConfigPropertyTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getActivationConfigType() {
        return activationConfigTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getActivationConfigType_Description() {
        return (EAttribute)activationConfigTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getActivationConfigType_ActivationConfigProperty() {
        return (EReference)activationConfigTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getCmpFieldGroupMappingType() {
        return cmpFieldGroupMappingTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmpFieldGroupMappingType_GroupName() {
        return (EAttribute)cmpFieldGroupMappingTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmpFieldGroupMappingType_CmpFieldName() {
        return (EAttribute)cmpFieldGroupMappingTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getCmpFieldMappingType() {
        return cmpFieldMappingTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmpFieldMappingType_CmpFieldName() {
        return (EAttribute)cmpFieldMappingTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmpFieldMappingType_CmpFieldClass() {
        return (EAttribute)cmpFieldMappingTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmpFieldMappingType_TableColumn() {
        return (EAttribute)cmpFieldMappingTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmpFieldMappingType_SqlType() {
        return (EAttribute)cmpFieldMappingTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmpFieldMappingType_TypeConverter() {
        return (EAttribute)cmpFieldMappingTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getCmrFieldGroupMappingType() {
        return cmrFieldGroupMappingTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmrFieldGroupMappingType_GroupName() {
        return (EAttribute)cmrFieldGroupMappingTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmrFieldGroupMappingType_CmrFieldName() {
        return (EAttribute)cmrFieldGroupMappingTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getCmrFieldMappingType() {
        return cmrFieldMappingTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmrFieldMappingType_KeyColumn() {
        return (EAttribute)cmrFieldMappingTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmrFieldMappingType_ForeignKeyColumn() {
        return (EAttribute)cmrFieldMappingTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getCmrFieldType() {
        return cmrFieldTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmrFieldType_CmrFieldName() {
        return (EAttribute)cmrFieldTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getCmrFieldType1() {
        return cmrFieldType1EClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmrFieldType1_CmrFieldName() {
        return (EAttribute)cmrFieldType1EClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getCmrFieldType1_GroupName() {
        return (EAttribute)cmrFieldType1EClass.getEStructuralFeatures().get(1);
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
    public EReference getDocumentRoot_OpenejbJar() {
        return (EReference)documentRootEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getEjbRelationshipRoleType() {
        return ejbRelationshipRoleTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRelationshipRoleType_EjbRelationshipRoleName() {
        return (EAttribute)ejbRelationshipRoleTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEjbRelationshipRoleType_RelationshipRoleSource() {
        return (EReference)ejbRelationshipRoleTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEjbRelationshipRoleType_CmrField() {
        return (EReference)ejbRelationshipRoleTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEjbRelationshipRoleType_ForeignKeyColumnOnSource() {
        return (EReference)ejbRelationshipRoleTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEjbRelationshipRoleType_RoleMapping() {
        return (EReference)ejbRelationshipRoleTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getEjbRelationType() {
        return ejbRelationTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRelationType_EjbRelationName() {
        return (EAttribute)ejbRelationTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEjbRelationType_ManyToManyTableName() {
        return (EAttribute)ejbRelationTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEjbRelationType_EjbRelationshipRole() {
        return (EReference)ejbRelationTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getEnterpriseBeansType() {
        return enterpriseBeansTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEnterpriseBeansType_Group() {
        return (EAttribute)enterpriseBeansTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEnterpriseBeansType_Session() {
        return (EReference)enterpriseBeansTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEnterpriseBeansType_Entity() {
        return (EReference)enterpriseBeansTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEnterpriseBeansType_MessageDriven() {
        return (EReference)enterpriseBeansTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getEntityBeanType() {
        return entityBeanTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEntityBeanType_EjbName() {
        return (EAttribute)entityBeanTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEntityBeanType_JndiName() {
        return (EAttribute)entityBeanTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEntityBeanType_LocalJndiName() {
        return (EAttribute)entityBeanTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEntityBeanType_TssTargetName() {
        return (EAttribute)entityBeanTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEntityBeanType_TssLink() {
        return (EAttribute)entityBeanTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEntityBeanType_Tss() {
        return (EReference)entityBeanTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEntityBeanType_TableName() {
        return (EAttribute)entityBeanTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEntityBeanType_CmpFieldMapping() {
        return (EReference)entityBeanTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEntityBeanType_PrimkeyField() {
        return (EAttribute)entityBeanTypeEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEntityBeanType_KeyGenerator() {
        return (EReference)entityBeanTypeEClass.getEStructuralFeatures().get(9);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEntityBeanType_PrefetchGroup() {
        return (EReference)entityBeanTypeEClass.getEStructuralFeatures().get(10);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEntityBeanType_EjbRef() {
        return (EReference)entityBeanTypeEClass.getEStructuralFeatures().get(11);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEntityBeanType_EjbLocalRef() {
        return (EReference)entityBeanTypeEClass.getEStructuralFeatures().get(12);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEntityBeanType_ServiceRef() {
        return (EReference)entityBeanTypeEClass.getEStructuralFeatures().get(13);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEntityBeanType_ResourceRef() {
        return (EReference)entityBeanTypeEClass.getEStructuralFeatures().get(14);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEntityBeanType_ResourceEnvRef() {
        return (EReference)entityBeanTypeEClass.getEStructuralFeatures().get(15);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getEntityBeanType_Query() {
        return (EReference)entityBeanTypeEClass.getEStructuralFeatures().get(16);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEntityBeanType_Id() {
        return (EAttribute)entityBeanTypeEClass.getEStructuralFeatures().get(17);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getEntityGroupMappingType() {
        return entityGroupMappingTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getEntityGroupMappingType_GroupName() {
        return (EAttribute)entityGroupMappingTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getGroupType() {
        return groupTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGroupType_GroupName() {
        return (EAttribute)groupTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getGroupType_CmpFieldName() {
        return (EAttribute)groupTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getGroupType_CmrField() {
        return (EReference)groupTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getMessageDrivenBeanType() {
        return messageDrivenBeanTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getMessageDrivenBeanType_EjbName() {
        return (EAttribute)messageDrivenBeanTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getMessageDrivenBeanType_ResourceAdapter() {
        return (EReference)messageDrivenBeanTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getMessageDrivenBeanType_ActivationConfig() {
        return (EReference)messageDrivenBeanTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getMessageDrivenBeanType_EjbRef() {
        return (EReference)messageDrivenBeanTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getMessageDrivenBeanType_EjbLocalRef() {
        return (EReference)messageDrivenBeanTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getMessageDrivenBeanType_ServiceRef() {
        return (EReference)messageDrivenBeanTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getMessageDrivenBeanType_ResourceRef() {
        return (EReference)messageDrivenBeanTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getMessageDrivenBeanType_ResourceEnvRef() {
        return (EReference)messageDrivenBeanTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getMessageDrivenBeanType_Id() {
        return (EAttribute)messageDrivenBeanTypeEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getMethodParamsType() {
        return methodParamsTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getMethodParamsType_MethodParam() {
        return (EAttribute)methodParamsTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getOpenejbJarType() {
        return openejbJarTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getOpenejbJarType_Dependency() {
        return (EReference)openejbJarTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getOpenejbJarType_CmpConnectionFactory() {
        return (EReference)openejbJarTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getOpenejbJarType_EjbQlCompilerFactory() {
        return (EReference)openejbJarTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getOpenejbJarType_DbSyntaxFactory() {
        return (EReference)openejbJarTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getOpenejbJarType_EnforceForeignKeyConstraints() {
        return (EReference)openejbJarTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getOpenejbJarType_EnterpriseBeans() {
        return (EReference)openejbJarTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getOpenejbJarType_Relationships() {
        return (EReference)openejbJarTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getOpenejbJarType_Security() {
        return (EReference)openejbJarTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getOpenejbJarType_Gbean() {
        return (EReference)openejbJarTypeEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getOpenejbJarType_ConfigId() {
        return (EAttribute)openejbJarTypeEClass.getEStructuralFeatures().get(9);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getOpenejbJarType_ParentId() {
        return (EAttribute)openejbJarTypeEClass.getEStructuralFeatures().get(10);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getPrefetchGroupType() {
        return prefetchGroupTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getPrefetchGroupType_Group() {
        return (EReference)prefetchGroupTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getPrefetchGroupType_EntityGroupMapping() {
        return (EReference)prefetchGroupTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getPrefetchGroupType_CmpFieldGroupMapping() {
        return (EReference)prefetchGroupTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getPrefetchGroupType_CmrFieldGroupMapping() {
        return (EReference)prefetchGroupTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getQueryMethodType() {
        return queryMethodTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getQueryMethodType_MethodName() {
        return (EAttribute)queryMethodTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getQueryMethodType_MethodParams() {
        return (EReference)queryMethodTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getQueryType() {
        return queryTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getQueryType_QueryMethod() {
        return (EReference)queryTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getQueryType_ResultTypeMapping() {
        return (EAttribute)queryTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getQueryType_EjbQl() {
        return (EAttribute)queryTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getQueryType_NoCacheFlush() {
        return (EReference)queryTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getQueryType_GroupName() {
        return (EAttribute)queryTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getRelationshipRoleSourceType() {
        return relationshipRoleSourceTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getRelationshipRoleSourceType_EjbName() {
        return (EAttribute)relationshipRoleSourceTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getRelationshipsType() {
        return relationshipsTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getRelationshipsType_EjbRelation() {
        return (EReference)relationshipsTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getRoleMappingType() {
        return roleMappingTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getRoleMappingType_CmrFieldMapping() {
        return (EReference)roleMappingTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getSessionBeanType() {
        return sessionBeanTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSessionBeanType_EjbName() {
        return (EAttribute)sessionBeanTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSessionBeanType_JndiName() {
        return (EAttribute)sessionBeanTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSessionBeanType_LocalJndiName() {
        return (EAttribute)sessionBeanTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSessionBeanType_TssTargetName() {
        return (EAttribute)sessionBeanTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSessionBeanType_TssLink() {
        return (EAttribute)sessionBeanTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getSessionBeanType_Tss() {
        return (EReference)sessionBeanTypeEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getSessionBeanType_EjbRef() {
        return (EReference)sessionBeanTypeEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getSessionBeanType_EjbLocalRef() {
        return (EReference)sessionBeanTypeEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getSessionBeanType_ServiceRef() {
        return (EReference)sessionBeanTypeEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getSessionBeanType_ResourceRef() {
        return (EReference)sessionBeanTypeEClass.getEStructuralFeatures().get(9);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getSessionBeanType_ResourceEnvRef() {
        return (EReference)sessionBeanTypeEClass.getEStructuralFeatures().get(10);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSessionBeanType_WebServiceAddress() {
        return (EAttribute)sessionBeanTypeEClass.getEStructuralFeatures().get(11);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getSessionBeanType_WebServiceSecurity() {
        return (EReference)sessionBeanTypeEClass.getEStructuralFeatures().get(12);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getSessionBeanType_Id() {
        return (EAttribute)sessionBeanTypeEClass.getEStructuralFeatures().get(13);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getTssType() {
        return tssTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getTssType_Domain() {
        return (EAttribute)tssTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getTssType_Server() {
        return (EAttribute)tssTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getTssType_Application() {
        return (EAttribute)tssTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getTssType_Module() {
        return (EAttribute)tssTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getTssType_Name() {
        return (EAttribute)tssTypeEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getWebServiceSecurityType() {
        return webServiceSecurityTypeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getWebServiceSecurityType_SecurityRealmName() {
        return (EAttribute)webServiceSecurityTypeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getWebServiceSecurityType_RealmName() {
        return (EAttribute)webServiceSecurityTypeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getWebServiceSecurityType_TransportGuarantee() {
        return (EAttribute)webServiceSecurityTypeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getWebServiceSecurityType_AuthMethod() {
        return (EAttribute)webServiceSecurityTypeEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getTransportGuaranteeType() {
        return transportGuaranteeTypeEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EDataType getAuthMethodType() {
        return authMethodTypeEDataType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EDataType getTransportGuaranteeTypeObject() {
        return transportGuaranteeTypeObjectEDataType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public JarFactory getJarFactory() {
        return (JarFactory)getEFactoryInstance();
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
        activationConfigPropertyTypeEClass = createEClass(ACTIVATION_CONFIG_PROPERTY_TYPE);
        createEAttribute(activationConfigPropertyTypeEClass, ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_NAME);
        createEAttribute(activationConfigPropertyTypeEClass, ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_VALUE);

        activationConfigTypeEClass = createEClass(ACTIVATION_CONFIG_TYPE);
        createEAttribute(activationConfigTypeEClass, ACTIVATION_CONFIG_TYPE__DESCRIPTION);
        createEReference(activationConfigTypeEClass, ACTIVATION_CONFIG_TYPE__ACTIVATION_CONFIG_PROPERTY);

        cmpFieldGroupMappingTypeEClass = createEClass(CMP_FIELD_GROUP_MAPPING_TYPE);
        createEAttribute(cmpFieldGroupMappingTypeEClass, CMP_FIELD_GROUP_MAPPING_TYPE__GROUP_NAME);
        createEAttribute(cmpFieldGroupMappingTypeEClass, CMP_FIELD_GROUP_MAPPING_TYPE__CMP_FIELD_NAME);

        cmpFieldMappingTypeEClass = createEClass(CMP_FIELD_MAPPING_TYPE);
        createEAttribute(cmpFieldMappingTypeEClass, CMP_FIELD_MAPPING_TYPE__CMP_FIELD_NAME);
        createEAttribute(cmpFieldMappingTypeEClass, CMP_FIELD_MAPPING_TYPE__CMP_FIELD_CLASS);
        createEAttribute(cmpFieldMappingTypeEClass, CMP_FIELD_MAPPING_TYPE__TABLE_COLUMN);
        createEAttribute(cmpFieldMappingTypeEClass, CMP_FIELD_MAPPING_TYPE__SQL_TYPE);
        createEAttribute(cmpFieldMappingTypeEClass, CMP_FIELD_MAPPING_TYPE__TYPE_CONVERTER);

        cmrFieldGroupMappingTypeEClass = createEClass(CMR_FIELD_GROUP_MAPPING_TYPE);
        createEAttribute(cmrFieldGroupMappingTypeEClass, CMR_FIELD_GROUP_MAPPING_TYPE__GROUP_NAME);
        createEAttribute(cmrFieldGroupMappingTypeEClass, CMR_FIELD_GROUP_MAPPING_TYPE__CMR_FIELD_NAME);

        cmrFieldMappingTypeEClass = createEClass(CMR_FIELD_MAPPING_TYPE);
        createEAttribute(cmrFieldMappingTypeEClass, CMR_FIELD_MAPPING_TYPE__KEY_COLUMN);
        createEAttribute(cmrFieldMappingTypeEClass, CMR_FIELD_MAPPING_TYPE__FOREIGN_KEY_COLUMN);

        cmrFieldTypeEClass = createEClass(CMR_FIELD_TYPE);
        createEAttribute(cmrFieldTypeEClass, CMR_FIELD_TYPE__CMR_FIELD_NAME);

        cmrFieldType1EClass = createEClass(CMR_FIELD_TYPE1);
        createEAttribute(cmrFieldType1EClass, CMR_FIELD_TYPE1__CMR_FIELD_NAME);
        createEAttribute(cmrFieldType1EClass, CMR_FIELD_TYPE1__GROUP_NAME);

        documentRootEClass = createEClass(DOCUMENT_ROOT);
        createEAttribute(documentRootEClass, DOCUMENT_ROOT__MIXED);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
        createEReference(documentRootEClass, DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
        createEReference(documentRootEClass, DOCUMENT_ROOT__OPENEJB_JAR);

        ejbRelationshipRoleTypeEClass = createEClass(EJB_RELATIONSHIP_ROLE_TYPE);
        createEAttribute(ejbRelationshipRoleTypeEClass, EJB_RELATIONSHIP_ROLE_TYPE__EJB_RELATIONSHIP_ROLE_NAME);
        createEReference(ejbRelationshipRoleTypeEClass, EJB_RELATIONSHIP_ROLE_TYPE__RELATIONSHIP_ROLE_SOURCE);
        createEReference(ejbRelationshipRoleTypeEClass, EJB_RELATIONSHIP_ROLE_TYPE__CMR_FIELD);
        createEReference(ejbRelationshipRoleTypeEClass, EJB_RELATIONSHIP_ROLE_TYPE__FOREIGN_KEY_COLUMN_ON_SOURCE);
        createEReference(ejbRelationshipRoleTypeEClass, EJB_RELATIONSHIP_ROLE_TYPE__ROLE_MAPPING);

        ejbRelationTypeEClass = createEClass(EJB_RELATION_TYPE);
        createEAttribute(ejbRelationTypeEClass, EJB_RELATION_TYPE__EJB_RELATION_NAME);
        createEAttribute(ejbRelationTypeEClass, EJB_RELATION_TYPE__MANY_TO_MANY_TABLE_NAME);
        createEReference(ejbRelationTypeEClass, EJB_RELATION_TYPE__EJB_RELATIONSHIP_ROLE);

        enterpriseBeansTypeEClass = createEClass(ENTERPRISE_BEANS_TYPE);
        createEAttribute(enterpriseBeansTypeEClass, ENTERPRISE_BEANS_TYPE__GROUP);
        createEReference(enterpriseBeansTypeEClass, ENTERPRISE_BEANS_TYPE__SESSION);
        createEReference(enterpriseBeansTypeEClass, ENTERPRISE_BEANS_TYPE__ENTITY);
        createEReference(enterpriseBeansTypeEClass, ENTERPRISE_BEANS_TYPE__MESSAGE_DRIVEN);

        entityBeanTypeEClass = createEClass(ENTITY_BEAN_TYPE);
        createEAttribute(entityBeanTypeEClass, ENTITY_BEAN_TYPE__EJB_NAME);
        createEAttribute(entityBeanTypeEClass, ENTITY_BEAN_TYPE__JNDI_NAME);
        createEAttribute(entityBeanTypeEClass, ENTITY_BEAN_TYPE__LOCAL_JNDI_NAME);
        createEAttribute(entityBeanTypeEClass, ENTITY_BEAN_TYPE__TSS_TARGET_NAME);
        createEAttribute(entityBeanTypeEClass, ENTITY_BEAN_TYPE__TSS_LINK);
        createEReference(entityBeanTypeEClass, ENTITY_BEAN_TYPE__TSS);
        createEAttribute(entityBeanTypeEClass, ENTITY_BEAN_TYPE__TABLE_NAME);
        createEReference(entityBeanTypeEClass, ENTITY_BEAN_TYPE__CMP_FIELD_MAPPING);
        createEAttribute(entityBeanTypeEClass, ENTITY_BEAN_TYPE__PRIMKEY_FIELD);
        createEReference(entityBeanTypeEClass, ENTITY_BEAN_TYPE__KEY_GENERATOR);
        createEReference(entityBeanTypeEClass, ENTITY_BEAN_TYPE__PREFETCH_GROUP);
        createEReference(entityBeanTypeEClass, ENTITY_BEAN_TYPE__EJB_REF);
        createEReference(entityBeanTypeEClass, ENTITY_BEAN_TYPE__EJB_LOCAL_REF);
        createEReference(entityBeanTypeEClass, ENTITY_BEAN_TYPE__SERVICE_REF);
        createEReference(entityBeanTypeEClass, ENTITY_BEAN_TYPE__RESOURCE_REF);
        createEReference(entityBeanTypeEClass, ENTITY_BEAN_TYPE__RESOURCE_ENV_REF);
        createEReference(entityBeanTypeEClass, ENTITY_BEAN_TYPE__QUERY);
        createEAttribute(entityBeanTypeEClass, ENTITY_BEAN_TYPE__ID);

        entityGroupMappingTypeEClass = createEClass(ENTITY_GROUP_MAPPING_TYPE);
        createEAttribute(entityGroupMappingTypeEClass, ENTITY_GROUP_MAPPING_TYPE__GROUP_NAME);

        groupTypeEClass = createEClass(GROUP_TYPE);
        createEAttribute(groupTypeEClass, GROUP_TYPE__GROUP_NAME);
        createEAttribute(groupTypeEClass, GROUP_TYPE__CMP_FIELD_NAME);
        createEReference(groupTypeEClass, GROUP_TYPE__CMR_FIELD);

        messageDrivenBeanTypeEClass = createEClass(MESSAGE_DRIVEN_BEAN_TYPE);
        createEAttribute(messageDrivenBeanTypeEClass, MESSAGE_DRIVEN_BEAN_TYPE__EJB_NAME);
        createEReference(messageDrivenBeanTypeEClass, MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ADAPTER);
        createEReference(messageDrivenBeanTypeEClass, MESSAGE_DRIVEN_BEAN_TYPE__ACTIVATION_CONFIG);
        createEReference(messageDrivenBeanTypeEClass, MESSAGE_DRIVEN_BEAN_TYPE__EJB_REF);
        createEReference(messageDrivenBeanTypeEClass, MESSAGE_DRIVEN_BEAN_TYPE__EJB_LOCAL_REF);
        createEReference(messageDrivenBeanTypeEClass, MESSAGE_DRIVEN_BEAN_TYPE__SERVICE_REF);
        createEReference(messageDrivenBeanTypeEClass, MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_REF);
        createEReference(messageDrivenBeanTypeEClass, MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ENV_REF);
        createEAttribute(messageDrivenBeanTypeEClass, MESSAGE_DRIVEN_BEAN_TYPE__ID);

        methodParamsTypeEClass = createEClass(METHOD_PARAMS_TYPE);
        createEAttribute(methodParamsTypeEClass, METHOD_PARAMS_TYPE__METHOD_PARAM);

        openejbJarTypeEClass = createEClass(OPENEJB_JAR_TYPE);
        createEReference(openejbJarTypeEClass, OPENEJB_JAR_TYPE__DEPENDENCY);
        createEReference(openejbJarTypeEClass, OPENEJB_JAR_TYPE__CMP_CONNECTION_FACTORY);
        createEReference(openejbJarTypeEClass, OPENEJB_JAR_TYPE__EJB_QL_COMPILER_FACTORY);
        createEReference(openejbJarTypeEClass, OPENEJB_JAR_TYPE__DB_SYNTAX_FACTORY);
        createEReference(openejbJarTypeEClass, OPENEJB_JAR_TYPE__ENFORCE_FOREIGN_KEY_CONSTRAINTS);
        createEReference(openejbJarTypeEClass, OPENEJB_JAR_TYPE__ENTERPRISE_BEANS);
        createEReference(openejbJarTypeEClass, OPENEJB_JAR_TYPE__RELATIONSHIPS);
        createEReference(openejbJarTypeEClass, OPENEJB_JAR_TYPE__SECURITY);
        createEReference(openejbJarTypeEClass, OPENEJB_JAR_TYPE__GBEAN);
        createEAttribute(openejbJarTypeEClass, OPENEJB_JAR_TYPE__CONFIG_ID);
        createEAttribute(openejbJarTypeEClass, OPENEJB_JAR_TYPE__PARENT_ID);

        prefetchGroupTypeEClass = createEClass(PREFETCH_GROUP_TYPE);
        createEReference(prefetchGroupTypeEClass, PREFETCH_GROUP_TYPE__GROUP);
        createEReference(prefetchGroupTypeEClass, PREFETCH_GROUP_TYPE__ENTITY_GROUP_MAPPING);
        createEReference(prefetchGroupTypeEClass, PREFETCH_GROUP_TYPE__CMP_FIELD_GROUP_MAPPING);
        createEReference(prefetchGroupTypeEClass, PREFETCH_GROUP_TYPE__CMR_FIELD_GROUP_MAPPING);

        queryMethodTypeEClass = createEClass(QUERY_METHOD_TYPE);
        createEAttribute(queryMethodTypeEClass, QUERY_METHOD_TYPE__METHOD_NAME);
        createEReference(queryMethodTypeEClass, QUERY_METHOD_TYPE__METHOD_PARAMS);

        queryTypeEClass = createEClass(QUERY_TYPE);
        createEReference(queryTypeEClass, QUERY_TYPE__QUERY_METHOD);
        createEAttribute(queryTypeEClass, QUERY_TYPE__RESULT_TYPE_MAPPING);
        createEAttribute(queryTypeEClass, QUERY_TYPE__EJB_QL);
        createEReference(queryTypeEClass, QUERY_TYPE__NO_CACHE_FLUSH);
        createEAttribute(queryTypeEClass, QUERY_TYPE__GROUP_NAME);

        relationshipRoleSourceTypeEClass = createEClass(RELATIONSHIP_ROLE_SOURCE_TYPE);
        createEAttribute(relationshipRoleSourceTypeEClass, RELATIONSHIP_ROLE_SOURCE_TYPE__EJB_NAME);

        relationshipsTypeEClass = createEClass(RELATIONSHIPS_TYPE);
        createEReference(relationshipsTypeEClass, RELATIONSHIPS_TYPE__EJB_RELATION);

        roleMappingTypeEClass = createEClass(ROLE_MAPPING_TYPE);
        createEReference(roleMappingTypeEClass, ROLE_MAPPING_TYPE__CMR_FIELD_MAPPING);

        sessionBeanTypeEClass = createEClass(SESSION_BEAN_TYPE);
        createEAttribute(sessionBeanTypeEClass, SESSION_BEAN_TYPE__EJB_NAME);
        createEAttribute(sessionBeanTypeEClass, SESSION_BEAN_TYPE__JNDI_NAME);
        createEAttribute(sessionBeanTypeEClass, SESSION_BEAN_TYPE__LOCAL_JNDI_NAME);
        createEAttribute(sessionBeanTypeEClass, SESSION_BEAN_TYPE__TSS_TARGET_NAME);
        createEAttribute(sessionBeanTypeEClass, SESSION_BEAN_TYPE__TSS_LINK);
        createEReference(sessionBeanTypeEClass, SESSION_BEAN_TYPE__TSS);
        createEReference(sessionBeanTypeEClass, SESSION_BEAN_TYPE__EJB_REF);
        createEReference(sessionBeanTypeEClass, SESSION_BEAN_TYPE__EJB_LOCAL_REF);
        createEReference(sessionBeanTypeEClass, SESSION_BEAN_TYPE__SERVICE_REF);
        createEReference(sessionBeanTypeEClass, SESSION_BEAN_TYPE__RESOURCE_REF);
        createEReference(sessionBeanTypeEClass, SESSION_BEAN_TYPE__RESOURCE_ENV_REF);
        createEAttribute(sessionBeanTypeEClass, SESSION_BEAN_TYPE__WEB_SERVICE_ADDRESS);
        createEReference(sessionBeanTypeEClass, SESSION_BEAN_TYPE__WEB_SERVICE_SECURITY);
        createEAttribute(sessionBeanTypeEClass, SESSION_BEAN_TYPE__ID);

        tssTypeEClass = createEClass(TSS_TYPE);
        createEAttribute(tssTypeEClass, TSS_TYPE__DOMAIN);
        createEAttribute(tssTypeEClass, TSS_TYPE__SERVER);
        createEAttribute(tssTypeEClass, TSS_TYPE__APPLICATION);
        createEAttribute(tssTypeEClass, TSS_TYPE__MODULE);
        createEAttribute(tssTypeEClass, TSS_TYPE__NAME);

        webServiceSecurityTypeEClass = createEClass(WEB_SERVICE_SECURITY_TYPE);
        createEAttribute(webServiceSecurityTypeEClass, WEB_SERVICE_SECURITY_TYPE__SECURITY_REALM_NAME);
        createEAttribute(webServiceSecurityTypeEClass, WEB_SERVICE_SECURITY_TYPE__REALM_NAME);
        createEAttribute(webServiceSecurityTypeEClass, WEB_SERVICE_SECURITY_TYPE__TRANSPORT_GUARANTEE);
        createEAttribute(webServiceSecurityTypeEClass, WEB_SERVICE_SECURITY_TYPE__AUTH_METHOD);

        // Create enums
        transportGuaranteeTypeEEnum = createEEnum(TRANSPORT_GUARANTEE_TYPE);

        // Create data types
        authMethodTypeEDataType = createEDataType(AUTH_METHOD_TYPE);
        transportGuaranteeTypeObjectEDataType = createEDataType(TRANSPORT_GUARANTEE_TYPE_OBJECT);
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
        PkgenPackageImpl thePkgenPackage = (PkgenPackageImpl)EPackage.Registry.INSTANCE.getEPackage(PkgenPackage.eNS_URI);
        NamingPackageImpl theNamingPackage = (NamingPackageImpl)EPackage.Registry.INSTANCE.getEPackage(NamingPackage.eNS_URI);
        DeploymentPackageImpl theDeploymentPackage = (DeploymentPackageImpl)EPackage.Registry.INSTANCE.getEPackage(DeploymentPackage.eNS_URI);
        SecurityPackageImpl theSecurityPackage = (SecurityPackageImpl)EPackage.Registry.INSTANCE.getEPackage(SecurityPackage.eNS_URI);

        // Add supertypes to classes

        // Initialize classes and features; add operations and parameters
        initEClass(activationConfigPropertyTypeEClass, ActivationConfigPropertyType.class, "ActivationConfigPropertyType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getActivationConfigPropertyType_ActivationConfigPropertyName(), theXMLTypePackage.getString(), "activationConfigPropertyName", null, 1, 1, ActivationConfigPropertyType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getActivationConfigPropertyType_ActivationConfigPropertyValue(), theXMLTypePackage.getString(), "activationConfigPropertyValue", null, 1, 1, ActivationConfigPropertyType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(activationConfigTypeEClass, ActivationConfigType.class, "ActivationConfigType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getActivationConfigType_Description(), theXMLTypePackage.getString(), "description", null, 0, -1, ActivationConfigType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getActivationConfigType_ActivationConfigProperty(), this.getActivationConfigPropertyType(), null, "activationConfigProperty", null, 1, -1, ActivationConfigType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(cmpFieldGroupMappingTypeEClass, CmpFieldGroupMappingType.class, "CmpFieldGroupMappingType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getCmpFieldGroupMappingType_GroupName(), theXMLTypePackage.getString(), "groupName", null, 1, 1, CmpFieldGroupMappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCmpFieldGroupMappingType_CmpFieldName(), theXMLTypePackage.getString(), "cmpFieldName", null, 1, 1, CmpFieldGroupMappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(cmpFieldMappingTypeEClass, CmpFieldMappingType.class, "CmpFieldMappingType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getCmpFieldMappingType_CmpFieldName(), theXMLTypePackage.getString(), "cmpFieldName", null, 1, 1, CmpFieldMappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCmpFieldMappingType_CmpFieldClass(), theXMLTypePackage.getString(), "cmpFieldClass", null, 0, 1, CmpFieldMappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCmpFieldMappingType_TableColumn(), theXMLTypePackage.getString(), "tableColumn", null, 1, 1, CmpFieldMappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCmpFieldMappingType_SqlType(), theXMLTypePackage.getString(), "sqlType", null, 0, 1, CmpFieldMappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCmpFieldMappingType_TypeConverter(), theXMLTypePackage.getString(), "typeConverter", null, 0, 1, CmpFieldMappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(cmrFieldGroupMappingTypeEClass, CmrFieldGroupMappingType.class, "CmrFieldGroupMappingType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getCmrFieldGroupMappingType_GroupName(), theXMLTypePackage.getString(), "groupName", null, 1, 1, CmrFieldGroupMappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCmrFieldGroupMappingType_CmrFieldName(), theXMLTypePackage.getString(), "cmrFieldName", null, 1, 1, CmrFieldGroupMappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(cmrFieldMappingTypeEClass, CmrFieldMappingType.class, "CmrFieldMappingType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getCmrFieldMappingType_KeyColumn(), theXMLTypePackage.getString(), "keyColumn", null, 1, 1, CmrFieldMappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCmrFieldMappingType_ForeignKeyColumn(), theXMLTypePackage.getString(), "foreignKeyColumn", null, 1, 1, CmrFieldMappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(cmrFieldTypeEClass, CmrFieldType.class, "CmrFieldType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getCmrFieldType_CmrFieldName(), theXMLTypePackage.getString(), "cmrFieldName", null, 1, 1, CmrFieldType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(cmrFieldType1EClass, CmrFieldType1.class, "CmrFieldType1", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getCmrFieldType1_CmrFieldName(), theXMLTypePackage.getString(), "cmrFieldName", null, 1, 1, CmrFieldType1.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getCmrFieldType1_GroupName(), theXMLTypePackage.getString(), "groupName", null, 0, 1, CmrFieldType1.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(documentRootEClass, DocumentRoot.class, "DocumentRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getDocumentRoot_Mixed(), ecorePackage.getEFeatureMapEntry(), "mixed", null, 0, -1, null, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XMLNSPrefixMap(), ecorePackage.getEStringToStringMapEntry(), null, "xMLNSPrefixMap", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_XSISchemaLocation(), ecorePackage.getEStringToStringMapEntry(), null, "xSISchemaLocation", null, 0, -1, null, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getDocumentRoot_OpenejbJar(), this.getOpenejbJarType(), null, "openejbJar", null, 0, -2, null, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

        initEClass(ejbRelationshipRoleTypeEClass, EjbRelationshipRoleType.class, "EjbRelationshipRoleType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getEjbRelationshipRoleType_EjbRelationshipRoleName(), theXMLTypePackage.getString(), "ejbRelationshipRoleName", null, 0, 1, EjbRelationshipRoleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEjbRelationshipRoleType_RelationshipRoleSource(), this.getRelationshipRoleSourceType(), null, "relationshipRoleSource", null, 1, 1, EjbRelationshipRoleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEjbRelationshipRoleType_CmrField(), this.getCmrFieldType(), null, "cmrField", null, 0, 1, EjbRelationshipRoleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEjbRelationshipRoleType_ForeignKeyColumnOnSource(), ecorePackage.getEObject(), null, "foreignKeyColumnOnSource", null, 0, 1, EjbRelationshipRoleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEjbRelationshipRoleType_RoleMapping(), this.getRoleMappingType(), null, "roleMapping", null, 1, 1, EjbRelationshipRoleType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(ejbRelationTypeEClass, EjbRelationType.class, "EjbRelationType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getEjbRelationType_EjbRelationName(), theXMLTypePackage.getString(), "ejbRelationName", null, 0, 1, EjbRelationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEjbRelationType_ManyToManyTableName(), theXMLTypePackage.getString(), "manyToManyTableName", null, 0, 1, EjbRelationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEjbRelationType_EjbRelationshipRole(), this.getEjbRelationshipRoleType(), null, "ejbRelationshipRole", null, 1, 2, EjbRelationType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(enterpriseBeansTypeEClass, EnterpriseBeansType.class, "EnterpriseBeansType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getEnterpriseBeansType_Group(), ecorePackage.getEFeatureMapEntry(), "group", null, 0, -1, EnterpriseBeansType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEnterpriseBeansType_Session(), this.getSessionBeanType(), null, "session", null, 0, -1, EnterpriseBeansType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getEnterpriseBeansType_Entity(), this.getEntityBeanType(), null, "entity", null, 0, -1, EnterpriseBeansType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
        initEReference(getEnterpriseBeansType_MessageDriven(), this.getMessageDrivenBeanType(), null, "messageDriven", null, 0, -1, EnterpriseBeansType.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

        initEClass(entityBeanTypeEClass, EntityBeanType.class, "EntityBeanType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getEntityBeanType_EjbName(), theXMLTypePackage.getString(), "ejbName", null, 1, 1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEntityBeanType_JndiName(), theXMLTypePackage.getString(), "jndiName", null, 0, -1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEntityBeanType_LocalJndiName(), theXMLTypePackage.getString(), "localJndiName", null, 0, -1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEntityBeanType_TssTargetName(), theXMLTypePackage.getString(), "tssTargetName", null, 0, 1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEntityBeanType_TssLink(), theXMLTypePackage.getString(), "tssLink", null, 0, 1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEntityBeanType_Tss(), this.getTssType(), null, "tss", null, 0, 1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEntityBeanType_TableName(), theXMLTypePackage.getString(), "tableName", null, 0, 1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEntityBeanType_CmpFieldMapping(), this.getCmpFieldMappingType(), null, "cmpFieldMapping", null, 0, -1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEntityBeanType_PrimkeyField(), theXMLTypePackage.getString(), "primkeyField", null, 0, 1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEntityBeanType_KeyGenerator(), thePkgenPackage.getKeyGeneratorType(), null, "keyGenerator", null, 0, 1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEntityBeanType_PrefetchGroup(), this.getPrefetchGroupType(), null, "prefetchGroup", null, 0, 1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEntityBeanType_EjbRef(), theNamingPackage.getEjbRefType(), null, "ejbRef", null, 0, -1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEntityBeanType_EjbLocalRef(), theNamingPackage.getEjbLocalRefType(), null, "ejbLocalRef", null, 0, -1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEntityBeanType_ServiceRef(), theNamingPackage.getServiceRefType(), null, "serviceRef", null, 0, -1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEntityBeanType_ResourceRef(), theNamingPackage.getResourceRefType(), null, "resourceRef", null, 0, -1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEntityBeanType_ResourceEnvRef(), theNamingPackage.getResourceEnvRefType(), null, "resourceEnvRef", null, 0, -1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getEntityBeanType_Query(), this.getQueryType(), null, "query", null, 0, -1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getEntityBeanType_Id(), theXMLTypePackage.getID(), "id", null, 0, 1, EntityBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(entityGroupMappingTypeEClass, EntityGroupMappingType.class, "EntityGroupMappingType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getEntityGroupMappingType_GroupName(), theXMLTypePackage.getString(), "groupName", null, 1, 1, EntityGroupMappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(groupTypeEClass, GroupType.class, "GroupType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getGroupType_GroupName(), theXMLTypePackage.getString(), "groupName", null, 1, 1, GroupType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGroupType_CmpFieldName(), theXMLTypePackage.getString(), "cmpFieldName", null, 0, -1, GroupType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getGroupType_CmrField(), this.getCmrFieldType1(), null, "cmrField", null, 0, -1, GroupType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(messageDrivenBeanTypeEClass, MessageDrivenBeanType.class, "MessageDrivenBeanType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getMessageDrivenBeanType_EjbName(), theXMLTypePackage.getString(), "ejbName", null, 1, 1, MessageDrivenBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getMessageDrivenBeanType_ResourceAdapter(), theNamingPackage.getResourceLocatorType(), null, "resourceAdapter", null, 1, 1, MessageDrivenBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getMessageDrivenBeanType_ActivationConfig(), this.getActivationConfigType(), null, "activationConfig", null, 0, 1, MessageDrivenBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getMessageDrivenBeanType_EjbRef(), theNamingPackage.getEjbRefType(), null, "ejbRef", null, 0, -1, MessageDrivenBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getMessageDrivenBeanType_EjbLocalRef(), theNamingPackage.getEjbLocalRefType(), null, "ejbLocalRef", null, 0, -1, MessageDrivenBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getMessageDrivenBeanType_ServiceRef(), theNamingPackage.getServiceRefType(), null, "serviceRef", null, 0, -1, MessageDrivenBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getMessageDrivenBeanType_ResourceRef(), theNamingPackage.getResourceRefType(), null, "resourceRef", null, 0, -1, MessageDrivenBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getMessageDrivenBeanType_ResourceEnvRef(), theNamingPackage.getResourceEnvRefType(), null, "resourceEnvRef", null, 0, -1, MessageDrivenBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getMessageDrivenBeanType_Id(), theXMLTypePackage.getID(), "id", null, 0, 1, MessageDrivenBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(methodParamsTypeEClass, MethodParamsType.class, "MethodParamsType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getMethodParamsType_MethodParam(), theXMLTypePackage.getString(), "methodParam", null, 0, -1, MethodParamsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(openejbJarTypeEClass, OpenejbJarType.class, "OpenejbJarType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getOpenejbJarType_Dependency(), theDeploymentPackage.getDependencyType(), null, "dependency", null, 0, -1, OpenejbJarType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getOpenejbJarType_CmpConnectionFactory(), theNamingPackage.getResourceLocatorType(), null, "cmpConnectionFactory", null, 0, 1, OpenejbJarType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getOpenejbJarType_EjbQlCompilerFactory(), ecorePackage.getEObject(), null, "ejbQlCompilerFactory", null, 0, 1, OpenejbJarType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getOpenejbJarType_DbSyntaxFactory(), ecorePackage.getEObject(), null, "dbSyntaxFactory", null, 0, 1, OpenejbJarType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getOpenejbJarType_EnforceForeignKeyConstraints(), ecorePackage.getEObject(), null, "enforceForeignKeyConstraints", null, 0, 1, OpenejbJarType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getOpenejbJarType_EnterpriseBeans(), this.getEnterpriseBeansType(), null, "enterpriseBeans", null, 1, 1, OpenejbJarType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getOpenejbJarType_Relationships(), this.getRelationshipsType(), null, "relationships", null, 0, 1, OpenejbJarType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getOpenejbJarType_Security(), theSecurityPackage.getSecurityType(), null, "security", null, 0, 1, OpenejbJarType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getOpenejbJarType_Gbean(), theDeploymentPackage.getGbeanType(), null, "gbean", null, 0, -1, OpenejbJarType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getOpenejbJarType_ConfigId(), theXMLTypePackage.getString(), "configId", null, 1, 1, OpenejbJarType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getOpenejbJarType_ParentId(), theXMLTypePackage.getString(), "parentId", null, 0, 1, OpenejbJarType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(prefetchGroupTypeEClass, PrefetchGroupType.class, "PrefetchGroupType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getPrefetchGroupType_Group(), this.getGroupType(), null, "group", null, 0, -1, PrefetchGroupType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getPrefetchGroupType_EntityGroupMapping(), this.getEntityGroupMappingType(), null, "entityGroupMapping", null, 0, 1, PrefetchGroupType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getPrefetchGroupType_CmpFieldGroupMapping(), this.getCmpFieldGroupMappingType(), null, "cmpFieldGroupMapping", null, 0, -1, PrefetchGroupType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getPrefetchGroupType_CmrFieldGroupMapping(), this.getCmrFieldGroupMappingType(), null, "cmrFieldGroupMapping", null, 0, -1, PrefetchGroupType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(queryMethodTypeEClass, QueryMethodType.class, "QueryMethodType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getQueryMethodType_MethodName(), theXMLTypePackage.getString(), "methodName", null, 1, 1, QueryMethodType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getQueryMethodType_MethodParams(), this.getMethodParamsType(), null, "methodParams", null, 1, 1, QueryMethodType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(queryTypeEClass, QueryType.class, "QueryType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getQueryType_QueryMethod(), this.getQueryMethodType(), null, "queryMethod", null, 1, 1, QueryType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getQueryType_ResultTypeMapping(), theXMLTypePackage.getString(), "resultTypeMapping", null, 0, 1, QueryType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getQueryType_EjbQl(), theXMLTypePackage.getString(), "ejbQl", null, 0, 1, QueryType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getQueryType_NoCacheFlush(), ecorePackage.getEObject(), null, "noCacheFlush", null, 0, 1, QueryType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getQueryType_GroupName(), theXMLTypePackage.getString(), "groupName", null, 0, 1, QueryType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(relationshipRoleSourceTypeEClass, RelationshipRoleSourceType.class, "RelationshipRoleSourceType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getRelationshipRoleSourceType_EjbName(), theXMLTypePackage.getString(), "ejbName", null, 1, 1, RelationshipRoleSourceType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(relationshipsTypeEClass, RelationshipsType.class, "RelationshipsType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getRelationshipsType_EjbRelation(), this.getEjbRelationType(), null, "ejbRelation", null, 1, -1, RelationshipsType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(roleMappingTypeEClass, RoleMappingType.class, "RoleMappingType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getRoleMappingType_CmrFieldMapping(), this.getCmrFieldMappingType(), null, "cmrFieldMapping", null, 1, -1, RoleMappingType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(sessionBeanTypeEClass, SessionBeanType.class, "SessionBeanType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getSessionBeanType_EjbName(), theXMLTypePackage.getString(), "ejbName", null, 1, 1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSessionBeanType_JndiName(), theXMLTypePackage.getString(), "jndiName", null, 0, -1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSessionBeanType_LocalJndiName(), theXMLTypePackage.getString(), "localJndiName", null, 0, -1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSessionBeanType_TssTargetName(), theXMLTypePackage.getString(), "tssTargetName", null, 0, 1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSessionBeanType_TssLink(), theXMLTypePackage.getString(), "tssLink", null, 0, 1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getSessionBeanType_Tss(), this.getTssType(), null, "tss", null, 0, 1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getSessionBeanType_EjbRef(), theNamingPackage.getEjbRefType(), null, "ejbRef", null, 0, -1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getSessionBeanType_EjbLocalRef(), theNamingPackage.getEjbLocalRefType(), null, "ejbLocalRef", null, 0, -1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getSessionBeanType_ServiceRef(), theNamingPackage.getServiceRefType(), null, "serviceRef", null, 0, -1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getSessionBeanType_ResourceRef(), theNamingPackage.getResourceRefType(), null, "resourceRef", null, 0, -1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getSessionBeanType_ResourceEnvRef(), theNamingPackage.getResourceEnvRefType(), null, "resourceEnvRef", null, 0, -1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSessionBeanType_WebServiceAddress(), theXMLTypePackage.getString(), "webServiceAddress", null, 0, 1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getSessionBeanType_WebServiceSecurity(), this.getWebServiceSecurityType(), null, "webServiceSecurity", null, 0, 1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSessionBeanType_Id(), theXMLTypePackage.getID(), "id", null, 0, 1, SessionBeanType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(tssTypeEClass, TssType.class, "TssType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getTssType_Domain(), theXMLTypePackage.getString(), "domain", null, 0, 1, TssType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getTssType_Server(), theXMLTypePackage.getString(), "server", null, 0, 1, TssType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getTssType_Application(), theXMLTypePackage.getString(), "application", null, 0, 1, TssType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getTssType_Module(), theXMLTypePackage.getString(), "module", null, 0, 1, TssType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getTssType_Name(), theXMLTypePackage.getString(), "name", null, 1, 1, TssType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(webServiceSecurityTypeEClass, WebServiceSecurityType.class, "WebServiceSecurityType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getWebServiceSecurityType_SecurityRealmName(), theXMLTypePackage.getString(), "securityRealmName", null, 1, 1, WebServiceSecurityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getWebServiceSecurityType_RealmName(), theXMLTypePackage.getString(), "realmName", null, 1, 1, WebServiceSecurityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getWebServiceSecurityType_TransportGuarantee(), this.getTransportGuaranteeType(), "transportGuarantee", "NONE", 1, 1, WebServiceSecurityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getWebServiceSecurityType_AuthMethod(), this.getAuthMethodType(), "authMethod", null, 1, 1, WebServiceSecurityType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Initialize enums and add enum literals
        initEEnum(transportGuaranteeTypeEEnum, TransportGuaranteeType.class, "TransportGuaranteeType");
        addEEnumLiteral(transportGuaranteeTypeEEnum, TransportGuaranteeType.NONE_LITERAL);
        addEEnumLiteral(transportGuaranteeTypeEEnum, TransportGuaranteeType.INTEGRAL_LITERAL);
        addEEnumLiteral(transportGuaranteeTypeEEnum, TransportGuaranteeType.CONFIDENTIAL_LITERAL);

        // Initialize data types
        initEDataType(authMethodTypeEDataType, String.class, "AuthMethodType", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
        initEDataType(transportGuaranteeTypeObjectEDataType, TransportGuaranteeType.class, "TransportGuaranteeTypeObject", IS_SERIALIZABLE, IS_GENERATED_INSTANCE_CLASS);

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
          (activationConfigPropertyTypeEClass, 
           source, 
           new String[] {
             "name", "activation-config-propertyType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getActivationConfigPropertyType_ActivationConfigPropertyName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "activation-config-property-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getActivationConfigPropertyType_ActivationConfigPropertyValue(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "activation-config-property-value",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (activationConfigTypeEClass, 
           source, 
           new String[] {
             "name", "activation-configType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getActivationConfigType_Description(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "description",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getActivationConfigType_ActivationConfigProperty(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "activation-config-property",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (authMethodTypeEDataType, 
           source, 
           new String[] {
             "name", "auth-methodType",
             "baseType", "http://www.eclipse.org/emf/2003/XMLType#string",
             "enumeration", "BASIC DIGEST CLIENT-CERT NONE"
           });		
        addAnnotation
          (cmpFieldGroupMappingTypeEClass, 
           source, 
           new String[] {
             "name", "cmp-field-group-mappingType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getCmpFieldGroupMappingType_GroupName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "group-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCmpFieldGroupMappingType_CmpFieldName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmp-field-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (cmpFieldMappingTypeEClass, 
           source, 
           new String[] {
             "name", "cmp-field-mapping_._type",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getCmpFieldMappingType_CmpFieldName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmp-field-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCmpFieldMappingType_CmpFieldClass(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmp-field-class",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCmpFieldMappingType_TableColumn(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "table-column",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCmpFieldMappingType_SqlType(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "sql-type",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCmpFieldMappingType_TypeConverter(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "type-converter",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (cmrFieldGroupMappingTypeEClass, 
           source, 
           new String[] {
             "name", "cmr-field-group-mappingType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getCmrFieldGroupMappingType_GroupName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "group-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCmrFieldGroupMappingType_CmrFieldName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmr-field-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (cmrFieldMappingTypeEClass, 
           source, 
           new String[] {
             "name", "cmr-field-mapping_._type",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getCmrFieldMappingType_KeyColumn(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "key-column",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCmrFieldMappingType_ForeignKeyColumn(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "foreign-key-column",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (cmrFieldTypeEClass, 
           source, 
           new String[] {
             "name", "cmr-field_._type",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getCmrFieldType_CmrFieldName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmr-field-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (cmrFieldType1EClass, 
           source, 
           new String[] {
             "name", "cmr-field_._1_._type",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getCmrFieldType1_CmrFieldName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmr-field-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getCmrFieldType1_GroupName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "group-name",
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
          (getDocumentRoot_OpenejbJar(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "openejb-jar",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (ejbRelationshipRoleTypeEClass, 
           source, 
           new String[] {
             "name", "ejb-relationship-roleType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getEjbRelationshipRoleType_EjbRelationshipRoleName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-relationship-role-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRelationshipRoleType_RelationshipRoleSource(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "relationship-role-source",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRelationshipRoleType_CmrField(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmr-field",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRelationshipRoleType_ForeignKeyColumnOnSource(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "foreign-key-column-on-source",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRelationshipRoleType_RoleMapping(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "role-mapping",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (ejbRelationTypeEClass, 
           source, 
           new String[] {
             "name", "ejb-relationType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getEjbRelationType_EjbRelationName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-relation-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRelationType_ManyToManyTableName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "many-to-many-table-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEjbRelationType_EjbRelationshipRole(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-relationship-role",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (enterpriseBeansTypeEClass, 
           source, 
           new String[] {
             "name", "enterprise-beans_._type",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getEnterpriseBeansType_Group(), 
           source, 
           new String[] {
             "kind", "group",
             "name", "group:0"
           });		
        addAnnotation
          (getEnterpriseBeansType_Session(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "session",
             "namespace", "##targetNamespace",
             "group", "#group:0"
           });		
        addAnnotation
          (getEnterpriseBeansType_Entity(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "entity",
             "namespace", "##targetNamespace",
             "group", "#group:0"
           });		
        addAnnotation
          (getEnterpriseBeansType_MessageDriven(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "message-driven",
             "namespace", "##targetNamespace",
             "group", "#group:0"
           });		
        addAnnotation
          (entityBeanTypeEClass, 
           source, 
           new String[] {
             "name", "entity-beanType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getEntityBeanType_EjbName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEntityBeanType_JndiName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "jndi-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEntityBeanType_LocalJndiName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "local-jndi-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEntityBeanType_TssTargetName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "tss-target-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEntityBeanType_TssLink(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "tss-link",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEntityBeanType_Tss(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "tss",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEntityBeanType_TableName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "table-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEntityBeanType_CmpFieldMapping(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmp-field-mapping",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEntityBeanType_PrimkeyField(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "primkey-field",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEntityBeanType_KeyGenerator(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "key-generator",
             "namespace", "http://www.openejb.org/xml/ns/pkgen"
           });		
        addAnnotation
          (getEntityBeanType_PrefetchGroup(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "prefetch-group",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEntityBeanType_EjbRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getEntityBeanType_EjbLocalRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-local-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getEntityBeanType_ServiceRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "service-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getEntityBeanType_ResourceRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getEntityBeanType_ResourceEnvRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-env-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getEntityBeanType_Query(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "query",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getEntityBeanType_Id(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "id"
           });		
        addAnnotation
          (entityGroupMappingTypeEClass, 
           source, 
           new String[] {
             "name", "entity-group-mappingType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getEntityGroupMappingType_GroupName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "group-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (groupTypeEClass, 
           source, 
           new String[] {
             "name", "groupType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getGroupType_GroupName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "group-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getGroupType_CmpFieldName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmp-field-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getGroupType_CmrField(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmr-field",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (messageDrivenBeanTypeEClass, 
           source, 
           new String[] {
             "name", "message-driven-beanType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getMessageDrivenBeanType_EjbName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getMessageDrivenBeanType_ResourceAdapter(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-adapter",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getMessageDrivenBeanType_ActivationConfig(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "activation-config",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getMessageDrivenBeanType_EjbRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getMessageDrivenBeanType_EjbLocalRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-local-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getMessageDrivenBeanType_ServiceRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "service-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getMessageDrivenBeanType_ResourceRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getMessageDrivenBeanType_ResourceEnvRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-env-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getMessageDrivenBeanType_Id(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "id"
           });		
        addAnnotation
          (methodParamsTypeEClass, 
           source, 
           new String[] {
             "name", "method-params_._type",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getMethodParamsType_MethodParam(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "method-param",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (openejbJarTypeEClass, 
           source, 
           new String[] {
             "name", "openejb-jarType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getOpenejbJarType_Dependency(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "dependency",
             "namespace", "http://geronimo.apache.org/xml/ns/deployment"
           });		
        addAnnotation
          (getOpenejbJarType_CmpConnectionFactory(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmp-connection-factory",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getOpenejbJarType_EjbQlCompilerFactory(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-ql-compiler-factory",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getOpenejbJarType_DbSyntaxFactory(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "db-syntax-factory",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getOpenejbJarType_EnforceForeignKeyConstraints(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "enforce-foreign-key-constraints",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getOpenejbJarType_EnterpriseBeans(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "enterprise-beans",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getOpenejbJarType_Relationships(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "relationships",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getOpenejbJarType_Security(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "security",
             "namespace", "http://geronimo.apache.org/xml/ns/security"
           });		
        addAnnotation
          (getOpenejbJarType_Gbean(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "gbean",
             "namespace", "http://geronimo.apache.org/xml/ns/deployment"
           });		
        addAnnotation
          (getOpenejbJarType_ConfigId(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "configId"
           });		
        addAnnotation
          (getOpenejbJarType_ParentId(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "parentId"
           });		
        addAnnotation
          (prefetchGroupTypeEClass, 
           source, 
           new String[] {
             "name", "prefetch-group_._type",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getPrefetchGroupType_Group(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "group",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPrefetchGroupType_EntityGroupMapping(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "entity-group-mapping",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPrefetchGroupType_CmpFieldGroupMapping(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmp-field-group-mapping",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getPrefetchGroupType_CmrFieldGroupMapping(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmr-field-group-mapping",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (queryMethodTypeEClass, 
           source, 
           new String[] {
             "name", "query-method_._type",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getQueryMethodType_MethodName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "method-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getQueryMethodType_MethodParams(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "method-params",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (queryTypeEClass, 
           source, 
           new String[] {
             "name", "queryType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getQueryType_QueryMethod(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "query-method",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getQueryType_ResultTypeMapping(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "result-type-mapping",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getQueryType_EjbQl(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-ql",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getQueryType_NoCacheFlush(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "no-cache-flush",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getQueryType_GroupName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "group-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (relationshipRoleSourceTypeEClass, 
           source, 
           new String[] {
             "name", "relationship-role-source_._type",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getRelationshipRoleSourceType_EjbName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (relationshipsTypeEClass, 
           source, 
           new String[] {
             "name", "relationshipsType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getRelationshipsType_EjbRelation(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-relation",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (roleMappingTypeEClass, 
           source, 
           new String[] {
             "name", "role-mapping_._type",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getRoleMappingType_CmrFieldMapping(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "cmr-field-mapping",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (sessionBeanTypeEClass, 
           source, 
           new String[] {
             "name", "session-beanType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getSessionBeanType_EjbName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getSessionBeanType_JndiName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "jndi-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getSessionBeanType_LocalJndiName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "local-jndi-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getSessionBeanType_TssTargetName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "tss-target-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getSessionBeanType_TssLink(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "tss-link",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getSessionBeanType_Tss(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "tss",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getSessionBeanType_EjbRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getSessionBeanType_EjbLocalRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "ejb-local-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getSessionBeanType_ServiceRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "service-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getSessionBeanType_ResourceRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getSessionBeanType_ResourceEnvRef(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "resource-env-ref",
             "namespace", "http://geronimo.apache.org/xml/ns/naming"
           });		
        addAnnotation
          (getSessionBeanType_WebServiceAddress(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "web-service-address",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getSessionBeanType_WebServiceSecurity(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "web-service-security",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getSessionBeanType_Id(), 
           source, 
           new String[] {
             "kind", "attribute",
             "name", "id"
           });		
        addAnnotation
          (transportGuaranteeTypeEEnum, 
           source, 
           new String[] {
             "name", "transport-guaranteeType"
           });		
        addAnnotation
          (transportGuaranteeTypeObjectEDataType, 
           source, 
           new String[] {
             "name", "transport-guaranteeType:Object",
             "baseType", "transport-guaranteeType"
           });		
        addAnnotation
          (tssTypeEClass, 
           source, 
           new String[] {
             "name", "tssType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getTssType_Domain(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "domain",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getTssType_Server(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "server",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getTssType_Application(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "application",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getTssType_Module(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "module",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getTssType_Name(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (webServiceSecurityTypeEClass, 
           source, 
           new String[] {
             "name", "web-service-securityType",
             "kind", "elementOnly"
           });		
        addAnnotation
          (getWebServiceSecurityType_SecurityRealmName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "security-realm-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getWebServiceSecurityType_RealmName(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "realm-name",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getWebServiceSecurityType_TransportGuarantee(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "transport-guarantee",
             "namespace", "##targetNamespace"
           });		
        addAnnotation
          (getWebServiceSecurityType_AuthMethod(), 
           source, 
           new String[] {
             "kind", "element",
             "name", "auth-method",
             "namespace", "##targetNamespace"
           });
    }

} //JarPackageImpl
