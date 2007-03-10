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

import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee.JavaIdentifierType;
import org.apache.geronimo.xbeans.javaee.JndiNameType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;
import org.apache.geronimo.xbeans.javaee.XsdAnyURIType;
import org.apache.xbean.finder.ClassFinder;


/**
 * Static helper class used to encapsulate all the functions related to the translation of
 * <strong>@WebServieRef</strong> and <strong>@WebServieRef</strong> annotations to deployment
 * descriptor tags. The WebServiceRefAnnotationHelper class can be used as part of the deployment of
 * a module into the Geronimo server. It performs the following major functions:
 *
 * <ol>
 *      <li>Translates annotations into corresponding deployment descriptor elements (so that the
 *      actual deployment descriptor in the module can be updated or even created if necessary)
 * </ol>
 *
 * <p><strong>Note(s):</strong>
 * <ul>
 *      <li>The user is responsible for invoking change to metadata-complete
 *      <li>This helper class will validate any changes it makes to the deployment descriptor. An
 *      exception will be thrown if it fails to parse
 * </ul>
 *
 * <p><strong>Remaining ToDo(s):</strong>
 * <ul>
 *      <li>None
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
     * Determine if there are any annotations present
     *
     * @return true or false
     */
    public static boolean annotationsPresent(ClassFinder classFinder) {
        if ( classFinder.isAnnotationPresent(WebServiceRef.class) ) return true;
        if ( classFinder.isAnnotationPresent(WebServiceRefs.class) ) return true;
        return false;
    }

    /**
     * Process the annotations
     *
     * @return Updated deployment descriptor
     * @throws Exception if parsing or validation error
     */
    public static void processAnnotations(AnnotatedApp annotatedApp, ClassFinder classFinder) throws Exception {
        if ( annotatedApp != null ) {
            processWebServiceRefs(annotatedApp, classFinder);
            processWebServiceRef(annotatedApp, classFinder);
        }
    }


    /**
     * Process annotations
     *
     * @param annotatedApp
     * @param classFinder
     * @throws Exception
     */
    private static void processWebServiceRef(AnnotatedApp annotatedApp, ClassFinder classFinder) throws Exception {
        log.debug("processWebServiceRef(): Entry: AnnotatedApp: " + annotatedApp.toString());

        List<Class> classeswithWebServiceRef = classFinder.findAnnotatedClasses(WebServiceRef.class);
        List<Method> methodswithWebServiceRef = classFinder.findAnnotatedMethods(WebServiceRef.class);
        List<Field> fieldswithWebServiceRef = classFinder.findAnnotatedFields(WebServiceRef.class);

        // Class-level annotation
        for ( Class cls : classeswithWebServiceRef ) {
            WebServiceRef webServiceRef = (WebServiceRef) cls.getAnnotation(WebServiceRef.class);
            if ( webServiceRef != null ) {
                addWebServiceRef(annotatedApp, webServiceRef, cls, null, null);
            }
        }

        // Method-level annotation
        for ( Method method : methodswithWebServiceRef ) {
            WebServiceRef webServiceRef = (WebServiceRef) method.getAnnotation(WebServiceRef.class);
            if ( webServiceRef != null ) {
                addWebServiceRef(annotatedApp, webServiceRef, null, method, null);
            }
        }

        // Field-level annotation
        for ( Field field : fieldswithWebServiceRef ) {
            WebServiceRef webServiceRef = (WebServiceRef) field.getAnnotation(WebServiceRef.class);
            if ( webServiceRef != null ) {
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
     * @param annotatedApp
     * @param classFinder
     * @exception Exception
     */
    private static void processWebServiceRefs(AnnotatedApp annotatedApp, ClassFinder classFinder) throws Exception {
        log.debug("processWebServiceRefs(): Entry");

        List<Class> classeswithWebServiceRefs = classFinder.findAnnotatedClasses(WebServiceRefs.class);

        // Class-level annotation(s)
        List<WebServiceRef> webServiceRefList = new ArrayList<WebServiceRef>();
        for ( Class cls : classeswithWebServiceRefs ) {
            WebServiceRefs webServiceRefs = (WebServiceRefs) cls.getAnnotation(WebServiceRefs.class);
            if ( webServiceRefs != null ) {
                webServiceRefList.addAll(Arrays.asList(webServiceRefs.value()));
            }
            for ( WebServiceRef webServiceRef : webServiceRefList ) {
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
     *
     * <ol>
     *      <li><service-ref> -- Declares a reference to a Web Service
     * </ol>
     *
     * <p><strong>Note(s):</strong>
     * <ul>
     *      <li>The deployment descriptor is the authoritative source so this method ensures that
     *      existing elements in it are not overwritten by annoations
     * </ul>
     *
     * @param annotation @WebServiceRef annotation
     * @param cls        Class name with the @WebServiceRef annoation
     * @param method     Method name with the @WebServiceRef annoation
     * @param field      Field name with the @WebServiceRef annoation
     */
    private static void addWebServiceRef(AnnotatedApp annotatedApp, WebServiceRef annotation, Class cls, Method method, Field field) {
        log.debug("addWebServiceRef( " + annotatedApp.toString() + "," + '\n' +
                           annotation.name() + "," + '\n' +
                           (cls != null ? cls.getName() : null) + "," + '\n' +
                           (method != null ? method.getName() : null) + "," + '\n' +
                           (field != null ? field.getName() : null) + " ): Entry");

        //------------------------------------------------------------------------------------------
        // WebServiceRef name:
        // -- When annotation is applied on a class:    Name must be provided (cannot be inferred)
        // -- When annotation is applied on a method:   Name is JavaBeans property name qualified
        //                                              by the class (or as provided on the
        //                                              annotation)
        // -- When annotation is applied on a field:    Name is the field name qualified by the
        //                                              class (or as provided on the annotation)
        //------------------------------------------------------------------------------------------
        String webServiceRefName = annotation.name();
        if ( webServiceRefName.equals("") ) {
            if ( method != null ) {
                StringBuilder stringBuilder = new StringBuilder(method.getName().substring(3));
                stringBuilder.setCharAt(0, Character.toLowerCase(stringBuilder.charAt(0)));
                webServiceRefName = method.getDeclaringClass().getName() + "/" + stringBuilder.toString();
            }
            else if ( field != null ) {
                webServiceRefName = field.getDeclaringClass().getName() + "/" + field.getName();
            }
        }
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
        String webServiceRefType = annotation.type().getCanonicalName();
        Class webServiceRefValue = annotation.value().getClass();
        if (webServiceRefType.equals("") || webServiceRefType.equals(Object.class.getName())) {
            if (method != null) {
                webServiceRefType = method.getParameterTypes()[0].getCanonicalName();
            }
            else if (field != null) {
                webServiceRefType = field.getType().getName();
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
        String injectionJavaType = getInjectionJavaType(method, field);
        String injectionClass = getInjectionClass(method, field);
        log.debug("addWebServiceRef(): injectionJavaType: " + injectionJavaType);
        log.debug("addWebServiceRef(): injectionClass   : " + injectionClass);


        //------------------------------------------------------------------------------------------
        // 1. <service-ref>
        //------------------------------------------------------------------------------------------
        boolean exists = false;
        ServiceRefType[] serviceRefs = annotatedApp.getServiceRefArray();
        for ( ServiceRefType serviceRef : serviceRefs ) {
            if ( serviceRef.getServiceRefName().getStringValue().trim().equals(webServiceRefName) ) {
                if (method != null || field != null) {
                    InjectionTargetType[] targets = serviceRef.getInjectionTargetArray();
                    if (!hasTarget(method, field, targets)) {
                        configureInjectionTarget(serviceRef.addNewInjectionTarget(), method, field);
                    }
                }
                return;
            }
        }
        if ( !exists ) {
            try {

                log.debug("addWebServiceRef(): Does not exist in DD: " + webServiceRefName);

                // Doesn't exist in deployment descriptor -- add new
                ServiceRefType serviceRef = annotatedApp.addNewServiceRef();

                //------------------------------------------------------------------------------
                // <service-ref> required elements:
                //------------------------------------------------------------------------------

                // service-ref-name
                JndiNameType serviceRefName = serviceRef.addNewServiceRefName();
                serviceRefName.setStringValue(webServiceRefName);

                // service-ref-type
                if ( !webServiceRefType.equals("") ) {
                    FullyQualifiedClassType qualifiedClass = serviceRef.addNewServiceInterface();
                    qualifiedClass.setStringValue(webServiceRefType);
                    serviceRef.setServiceInterface(qualifiedClass);
                }
                else if ( !webServiceRefValue.equals("") ) {
                    // service-ref-type
                    FullyQualifiedClassType qualifiedClass = serviceRef.addNewServiceInterface();
                    qualifiedClass.setStringValue(webServiceRefValue.getName());
                    serviceRef.setServiceInterface(qualifiedClass);
                }

                //------------------------------------------------------------------------------
                // <service-ref> optional elements:
                //------------------------------------------------------------------------------

                // WSDL document location
                String documentAnnotation = annotation.wsdlLocation();
                if ( !documentAnnotation.equals("") ) {
                    XsdAnyURIType wsdlFile = serviceRef.addNewWsdlFile();
                    wsdlFile.setStringValue(documentAnnotation);
                    serviceRef.setWsdlFile(wsdlFile);
                }

                if ( !injectionJavaType.equals("") ) {
                    // injectionTarget
                    InjectionTargetType injectionTarget = serviceRef.addNewInjectionTarget();
                    FullyQualifiedClassType qualifiedClass = injectionTarget.addNewInjectionTargetClass();
                    JavaIdentifierType javaType = injectionTarget.addNewInjectionTargetName();
                    qualifiedClass.setStringValue(injectionClass);
                    javaType.setStringValue(injectionJavaType);
                    injectionTarget.setInjectionTargetClass(qualifiedClass);
                    injectionTarget.setInjectionTargetName(javaType);
                }

            }
            catch ( Exception anyException ) {
                log.debug("WebServiceRefAnnotationHelper: Exception caught while processing <service-ref>");
                anyException.printStackTrace();
            }
        }
        log.debug("addWebServiceRef(): Exit");
    }



    /**
     * Validate deployment descriptor
     *
     * @parama annotatedApp
     * @throws Exception thrown if deployment descriptor cannot be parsed
     */
    private static void validateDD(AnnotatedApp annotatedApp) throws Exception {
        log.debug("validateDD( " + annotatedApp.toString() + " ): Entry");

        XmlBeansUtil.parse(annotatedApp.toString());

        log.debug("validateDD(): Exit");
    }

}
