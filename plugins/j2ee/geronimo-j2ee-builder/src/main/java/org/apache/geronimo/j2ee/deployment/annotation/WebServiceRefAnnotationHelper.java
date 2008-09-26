/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.j2ee.deployment.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jws.HandlerChain;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.JndiNameType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;
import org.apache.geronimo.xbeans.javaee.XsdAnyURIType;
import org.apache.geronimo.xbeans.javaee.XsdStringType;
import org.apache.xbean.finder.ClassFinder;


/**
 * Static helper class used to encapsulate all the functions related to the translation of
 * <strong>@WebServieRef</strong> and <strong>@WebServieRef</strong> annotations to deployment
 * descriptor tags. The WebServiceRefAnnotationHelper class can be used as part of the deployment of
 * a module into the Geronimo server. It performs the following major functions:
 * <p/>
 * <ol>
 * <li>Translates annotations into corresponding deployment descriptor elements (so that the
 * actual deployment descriptor in the module can be updated or even created if necessary)
 * </ol>
 * <p/>
 * <p><strong>Note(s):</strong>
 * <ul>
 * <li>The user is responsible for invoking change to metadata-complete
 * <li>This helper class will validate any changes it makes to the deployment descriptor. An
 * exception will be thrown if it fails to parse
 * </ul>
 * <p/>
 * <p><strong>Remaining ToDo(s):</strong>
 * <ul>
 * <li>None
 * </ul>
 *
 * @version $Rev $Date
 * @since 03-2007
 */
public final class WebServiceRefAnnotationHelper extends AnnotationHelper {

    // Private instance variables
    private static final Log log = LogFactory.getLog(WebServiceRefAnnotationHelper.class);

    // Private constructor to prevent instantiation
    private WebServiceRefAnnotationHelper() {
    }

    /**
     * Update the deployment descriptor from the WebServiceRef and WebServiceRefs annotations
     *
     * @param annotatedApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    public static void processAnnotations(AnnotatedApp annotatedApp, ClassFinder classFinder) throws DeploymentException {
        if (annotatedApp != null) {
            if (classFinder.isAnnotationPresent(WebServiceRefs.class)) {
                processWebServiceRefs(annotatedApp, classFinder);
            }
            if (classFinder.isAnnotationPresent(WebServiceRef.class)) {
                processWebServiceRef(annotatedApp, classFinder);
            }
        }
    }


    /**
     * Process annotations
     *
     * @param annotatedApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    private static void processWebServiceRef(AnnotatedApp annotatedApp, ClassFinder classFinder) throws DeploymentException {
        log.debug("processWebServiceRef(): Entry: AnnotatedApp: " + annotatedApp.toString());

        List<Class> classeswithWebServiceRef = classFinder.findAnnotatedClasses(WebServiceRef.class);
        List<Method> methodswithWebServiceRef = classFinder.findAnnotatedMethods(WebServiceRef.class);
        List<Field> fieldswithWebServiceRef = classFinder.findAnnotatedFields(WebServiceRef.class);

        // Class-level annotation
        for (Class cls : classeswithWebServiceRef) {
            WebServiceRef webServiceRef = (WebServiceRef) cls.getAnnotation(WebServiceRef.class);
            if (webServiceRef != null) {
                addWebServiceRef(annotatedApp, webServiceRef, cls, null, null);
            }
        }

        // Method-level annotation
        for (Method method : methodswithWebServiceRef) {
            WebServiceRef webServiceRef = method.getAnnotation(WebServiceRef.class);
            if (webServiceRef != null) {
                addWebServiceRef(annotatedApp, webServiceRef, null, method, null);
            }
        }

        // Field-level annotation
        for (Field field : fieldswithWebServiceRef) {
            WebServiceRef webServiceRef = field.getAnnotation(WebServiceRef.class);
            if (webServiceRef != null) {
                addWebServiceRef(annotatedApp, webServiceRef, null, null, field);
            }
        }

        // Validate deployment descriptor to ensure it's still okay
        validateDD(annotatedApp);

        log.debug("processWebServiceRef(): Exit: AnnotatedApp: " + annotatedApp.toString());
    }


    /**
     * Process multiple annotations
     *
     * @param annotatedApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    private static void processWebServiceRefs(AnnotatedApp annotatedApp, ClassFinder classFinder) throws DeploymentException {
        log.debug("processWebServiceRefs(): Entry");

        List<Class> classeswithWebServiceRefs = classFinder.findAnnotatedClasses(WebServiceRefs.class);

        // Class-level annotation(s)
        List<WebServiceRef> webServiceRefList = new ArrayList<WebServiceRef>();
        for (Class cls : classeswithWebServiceRefs) {
            WebServiceRefs webServiceRefs = (WebServiceRefs) cls.getAnnotation(WebServiceRefs.class);
            if (webServiceRefs != null) {
                webServiceRefList.addAll(Arrays.asList(webServiceRefs.value()));
            }
            for (WebServiceRef webServiceRef : webServiceRefList) {
                addWebServiceRef(annotatedApp, webServiceRef, cls, null, null);
            }
            webServiceRefList.clear();
        }

        log.debug("processWebServiceRefs(): Exit");
    }


    /**
     * Add @WebServiceRef and @WebServiceRefs annotations to the deployment descriptor. XMLBeans are used to
     * read and manipulate the deployment descriptor as necessary. The WebServiceRef annotation(s) will be
     * converted to one of the following deployment descriptors:
     * <p/>
     * <ol>
     * <li><service-ref> -- Declares a reference to a Web Service
     * </ol>
     * <p/>
     * <p><strong>Note(s):</strong>
     * <ul>
     * <li>The deployment descriptor is the authoritative source so this method ensures that
     * existing elements in it are not overwritten by annoations
     * </ul>
     *
     * @param annotation @WebServiceRef annotation
     * @param cls        Class name with the @WebServiceRef annoation
     * @param method     Method name with the @WebServiceRef annoation
     * @param field      Field name with the @WebServiceRef annoation
     * @param annotatedApp  Access to the specc dd
     */
    private static void addWebServiceRef(AnnotatedApp annotatedApp, WebServiceRef annotation, Class cls, Method method, Field field) {
        log.debug("addWebServiceRef( [annotatedApp] " + annotatedApp.toString() + "," + '\n' +
                "[annotation] " + annotation.toString() + "," + '\n' +
                "[cls] " + (cls != null ? cls.getName() : null) + "," + '\n' +
                "[method] " + (method != null ? method.getName() : null) + "," + '\n' +
                "[field] " + (field != null ? field.getName() : null) + " ): Entry");

        //------------------------------------------------------------------------------------------
        // WebServiceRef name:
        // -- When annotation is applied on a class:    Name must be provided (cannot be inferred)
        // -- When annotation is applied on a method:   Name is JavaBeans property name qualified
        //                                              by the class (or as provided on the
        //                                              annotation)
        // -- When annotation is applied on a field:    Name is the field name qualified by the
        //                                              class (or as provided on the annotation)
        //------------------------------------------------------------------------------------------
        String webServiceRefName = getName(annotation.name(), method, field);

        log.debug("addWebServiceRef(): webServiceRefName: " + webServiceRefName);

        //------------------------------------------------------------------------------------------
        // WebServiceRef types:
        //
        // 1. Generated Service Class (extends javax.xml.ws.Service)
        // 2. Service Endpoint Interface (SEI)
        //
        // -- When annotation is applied on a class:    Type and Value must be provided (cannot be
        //                                              inferred)
        // -- When annotation is applied on a method:   Type is the JavaBeans property type (or as
        //                                              provided on the annotation)
        // -- When annotation is applied on a field:    Type is the field type (or as provided on
        //                                              the annotation)
        //------------------------------------------------------------------------------------------
        Class webServiceRefType = annotation.type();
        Class webServiceRefValue = annotation.value();
        if (webServiceRefType == null || webServiceRefType.equals(Object.class)) {
            if (method != null) {
                webServiceRefType = method.getParameterTypes()[0];
            } else if (field != null) {
                webServiceRefType = field.getType();
            }
        }
        log.debug("addWebServiceRef(): webServiceRefType: " + webServiceRefType);
        log.debug("addWebServiceRef(): webServiceRefValue: " + webServiceRefValue);

        //------------------------------------------------------------------------------------------
        // Method name (for setter-based injection) must follow JavaBeans conventions:
        // -- Must start with "set"
        // -- Have one parameter
        // -- Return void
        //------------------------------------------------------------------------------------------

        //------------------------------------------------------------------------------------------
        // 1. <service-ref>
        //------------------------------------------------------------------------------------------

        ServiceRefType serviceRef = null;

        ServiceRefType[] serviceRefs = annotatedApp.getServiceRefArray();
        for (ServiceRefType currServiceRef : serviceRefs) {
            if (currServiceRef.getServiceRefName().getStringValue().trim().equals(webServiceRefName)) {
                serviceRef = currServiceRef;
                break;
            }
        }

        if (serviceRef == null) {
            // Doesn't exist in deployment descriptor -- add new
            serviceRef = annotatedApp.addNewServiceRef();

            // ------------------------------------------------------------------------------
            // <service-ref> required elements:
            // ------------------------------------------------------------------------------

            // service-ref-name
            JndiNameType serviceRefName = serviceRef.addNewServiceRefName();
            serviceRefName.setStringValue(webServiceRefName);
            serviceRef.setServiceRefName(serviceRefName);

            // service-ref-interface
            if (!webServiceRefValue.equals(Object.class)) {
                FullyQualifiedClassType qualifiedClass = serviceRef.addNewServiceInterface();
                qualifiedClass.setStringValue(webServiceRefValue.getName());
                serviceRef.setServiceInterface(qualifiedClass);
            } else {
                FullyQualifiedClassType qualifiedClass = serviceRef.addNewServiceInterface();
                qualifiedClass.setStringValue(webServiceRefType.getName());
                serviceRef.setServiceInterface(qualifiedClass);
            }
        }

        //------------------------------------------------------------------------------
        // <service-ref> optional elements:
        //------------------------------------------------------------------------------

        // service-ref-type
        if (!serviceRef.isSetServiceRefType() && !webServiceRefType.equals(Object.class)) {
            FullyQualifiedClassType qualifiedClass = serviceRef.addNewServiceRefType();
            qualifiedClass.setStringValue(webServiceRefType.getName());
            serviceRef.setServiceRefType(qualifiedClass);
        }

        // mapped-name
        if (!serviceRef.isSetMappedName() && annotation.mappedName().trim().length() > 0) {
            XsdStringType mappedName = serviceRef.addNewMappedName();
            mappedName.setStringValue(annotation.mappedName().trim());
            serviceRef.setMappedName(mappedName);
        }

        // WSDL document location
        if (!serviceRef.isSetWsdlFile()) {
            String wsdlLocation = annotation.wsdlLocation();

            if (wsdlLocation == null || wsdlLocation.trim().length() == 0) {
                WebServiceClient wsClient = null;
                if (Object.class.equals(webServiceRefValue)) {
                    wsClient = (WebServiceClient) webServiceRefType.getAnnotation(WebServiceClient.class);
                } else {
                    wsClient = (WebServiceClient) webServiceRefValue.getAnnotation(WebServiceClient.class);
                }
                if (wsClient == null) {
                    wsdlLocation = null;
                } else {
                    wsdlLocation = wsClient.wsdlLocation();
                }
            }

            if (wsdlLocation != null && wsdlLocation.trim().length() > 0) {
                XsdAnyURIType wsdlFile = serviceRef.addNewWsdlFile();
                wsdlFile.setStringValue(wsdlLocation);
                serviceRef.setWsdlFile(wsdlFile);
            }
        }

        // handler-chains
        if (!serviceRef.isSetHandlerChains()) {
            HandlerChain handlerChain = null;
            Class annotatedClass = null;
            if (method != null) {
                handlerChain = method.getAnnotation(HandlerChain.class);
                annotatedClass = method.getDeclaringClass();
            } else if (field != null) {
                handlerChain = field.getAnnotation(HandlerChain.class);
                annotatedClass = field.getDeclaringClass();
            }
            
            // if not specified on method or field, try to get it from Service class
            if (handlerChain == null) {
                if (Object.class.equals(webServiceRefValue)) {
                    handlerChain = (HandlerChain) webServiceRefType.getAnnotation(HandlerChain.class);
                    annotatedClass = webServiceRefType;
                } else {
                    handlerChain = (HandlerChain) webServiceRefValue.getAnnotation(HandlerChain.class);
                    annotatedClass = webServiceRefValue;
                }
            }
            
            if (handlerChain != null) {
                HandlerChainAnnotationHelper.insertHandlers(serviceRef, handlerChain, annotatedClass);
            }
        }
        
        if (method != null || field != null) {
            configureInjectionTarget(serviceRef.addNewInjectionTarget(), method, field);
        }

    }

}
