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
package org.apache.geronimo.xml.ns.naming.util;

import java.util.List;

import org.apache.geronimo.xml.ns.naming.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.apache.geronimo.xml.ns.naming.NamingPackage
 * @generated
 */
public class NamingSwitch {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static final String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

    /**
     * The cached model package
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static NamingPackage modelPackage;

    /**
     * Creates an instance of the switch.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NamingSwitch() {
        if (modelPackage == null) {
            modelPackage = NamingPackage.eINSTANCE;
        }
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
    public Object doSwitch(EObject theEObject) {
        return doSwitch(theEObject.eClass(), theEObject);
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
    protected Object doSwitch(EClass theEClass, EObject theEObject) {
        if (theEClass.eContainer() == modelPackage) {
            return doSwitch(theEClass.getClassifierID(), theEObject);
        }
        else {
            List eSuperTypes = theEClass.getESuperTypes();
            return
                eSuperTypes.isEmpty() ?
                    defaultCase(theEObject) :
                    doSwitch((EClass)eSuperTypes.get(0), theEObject);
        }
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
    protected Object doSwitch(int classifierID, EObject theEObject) {
        switch (classifierID) {
            case NamingPackage.CSS_TYPE: {
                CssType cssType = (CssType)theEObject;
                Object result = caseCssType(cssType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case NamingPackage.DOCUMENT_ROOT: {
                DocumentRoot documentRoot = (DocumentRoot)theEObject;
                Object result = caseDocumentRoot(documentRoot);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case NamingPackage.EJB_LOCAL_REF_TYPE: {
                EjbLocalRefType ejbLocalRefType = (EjbLocalRefType)theEObject;
                Object result = caseEjbLocalRefType(ejbLocalRefType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case NamingPackage.EJB_REF_TYPE: {
                EjbRefType ejbRefType = (EjbRefType)theEObject;
                Object result = caseEjbRefType(ejbRefType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case NamingPackage.GBEAN_LOCATOR_TYPE: {
                GbeanLocatorType gbeanLocatorType = (GbeanLocatorType)theEObject;
                Object result = caseGbeanLocatorType(gbeanLocatorType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case NamingPackage.GBEAN_REF_TYPE: {
                GbeanRefType gbeanRefType = (GbeanRefType)theEObject;
                Object result = caseGbeanRefType(gbeanRefType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case NamingPackage.PORT_COMPLETION_TYPE: {
                PortCompletionType portCompletionType = (PortCompletionType)theEObject;
                Object result = casePortCompletionType(portCompletionType);
                if (result == null) result = casePortType(portCompletionType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case NamingPackage.PORT_TYPE: {
                PortType portType = (PortType)theEObject;
                Object result = casePortType(portType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case NamingPackage.RESOURCE_ENV_REF_TYPE: {
                ResourceEnvRefType resourceEnvRefType = (ResourceEnvRefType)theEObject;
                Object result = caseResourceEnvRefType(resourceEnvRefType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case NamingPackage.RESOURCE_LOCATOR_TYPE: {
                ResourceLocatorType resourceLocatorType = (ResourceLocatorType)theEObject;
                Object result = caseResourceLocatorType(resourceLocatorType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case NamingPackage.RESOURCE_REF_TYPE: {
                ResourceRefType resourceRefType = (ResourceRefType)theEObject;
                Object result = caseResourceRefType(resourceRefType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case NamingPackage.SERVICE_COMPLETION_TYPE: {
                ServiceCompletionType serviceCompletionType = (ServiceCompletionType)theEObject;
                Object result = caseServiceCompletionType(serviceCompletionType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case NamingPackage.SERVICE_REF_TYPE: {
                ServiceRefType serviceRefType = (ServiceRefType)theEObject;
                Object result = caseServiceRefType(serviceRefType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            default: return defaultCase(theEObject);
        }
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Css Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Css Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseCssType(CssType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Document Root</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Document Root</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseDocumentRoot(DocumentRoot object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Ejb Local Ref Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Ejb Local Ref Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseEjbLocalRefType(EjbLocalRefType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Ejb Ref Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Ejb Ref Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseEjbRefType(EjbRefType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Gbean Locator Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Gbean Locator Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseGbeanLocatorType(GbeanLocatorType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Gbean Ref Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Gbean Ref Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseGbeanRefType(GbeanRefType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Port Completion Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Port Completion Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object casePortCompletionType(PortCompletionType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Port Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Port Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object casePortType(PortType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Resource Env Ref Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Resource Env Ref Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseResourceEnvRefType(ResourceEnvRefType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Resource Locator Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Resource Locator Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseResourceLocatorType(ResourceLocatorType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Resource Ref Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Resource Ref Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseResourceRefType(ResourceRefType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Service Completion Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Service Completion Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseServiceCompletionType(ServiceCompletionType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Service Ref Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Service Ref Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseServiceRefType(ServiceRefType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>EObject</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch, but this is the last case anyway.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>EObject</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject)
     * @generated
     */
    public Object defaultCase(EObject object) {
        return null;
    }

} //NamingSwitch
