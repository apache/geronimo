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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.jws.HandlerChain;
import javax.xml.ws.RespectBinding;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.spi.WebServiceFeatureAnnotation;

import org.apache.geronimo.common.DeploymentException;
import org.apache.openejb.jee.Addressing;
import org.apache.openejb.jee.AddressingResponses;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.PortComponentRef;
import org.apache.openejb.jee.ServiceRef;
import org.apache.xbean.finder.AbstractFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    private static final Logger log = LoggerFactory.getLogger(WebServiceRefAnnotationHelper.class);

    // Private constructor to prevent instantiation
    private WebServiceRefAnnotationHelper() {
    }

    /**
     * Update the deployment descriptor from the WebServiceRef and WebServiceRefs annotations
     *
     * @param specDD Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    public static void processAnnotations(JndiConsumer specDD, AbstractFinder classFinder) throws DeploymentException {
        if (specDD != null) {
            if (classFinder.isAnnotationPresent(WebServiceRefs.class)) {
                processWebServiceRefs(specDD, classFinder);
            }
            if (classFinder.isAnnotationPresent(WebServiceRef.class)) {
                processWebServiceRef(specDD, classFinder);
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
    private static void processWebServiceRef(JndiConsumer annotatedApp, AbstractFinder classFinder) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("processWebServiceRef(): Entry: AnnotatedApp: " + annotatedApp.toString());
        }
        List<Class<?>> classeswithWebServiceRef = classFinder.findAnnotatedClasses(WebServiceRef.class);
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
//        validateDD(annotatedApp);
        if (log.isDebugEnabled()) {
            log.debug("processWebServiceRef(): Exit: AnnotatedApp: " + annotatedApp.toString());
        }
    }


    /**
     * Process multiple annotations
     *
     * @param annotatedApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    private static void processWebServiceRefs(JndiConsumer annotatedApp, AbstractFinder classFinder) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("processWebServiceRefs(): Entry");
        }

        List<Class<?>> classeswithWebServiceRefs = classFinder.findAnnotatedClasses(WebServiceRefs.class);

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
        if (log.isDebugEnabled()) {
            log.debug("processWebServiceRefs(): Exit");
        }
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
    private static void addWebServiceRef(JndiConsumer annotatedApp, WebServiceRef annotation, Class cls, Method method, Field field) {
        if (log.isDebugEnabled()) {
            log.debug("addWebServiceRef( [annotatedApp] " + annotatedApp.toString() + "," + '\n' +
                    "[annotation] " + annotation.toString() + "," + '\n' +
                    "[cls] " + (cls != null ? cls.getName() : null) + "," + '\n' +
                    "[method] " + (method != null ? method.getName() : null) + "," + '\n' +
                    "[field] " + (field != null ? field.getName() : null) + " ): Entry");
        }
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

        if (log.isDebugEnabled()) {
            log.debug("addWebServiceRef(): webServiceRefName: " + webServiceRefName);
        }
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
        if (log.isDebugEnabled()) {
            log.debug("addWebServiceRef(): webServiceRefType: " + webServiceRefType);
            log.debug("addWebServiceRef(): webServiceRefValue: " + webServiceRefValue);
        }
        //------------------------------------------------------------------------------------------
        // Method name (for setter-based injection) must follow JavaBeans conventions:
        // -- Must start with "set"
        // -- Have one parameter
        // -- Return void
        //------------------------------------------------------------------------------------------

        //------------------------------------------------------------------------------------------
        // 1. <service-ref>
        //------------------------------------------------------------------------------------------

        ServiceRef serviceRef = null;

        Collection<ServiceRef> serviceRefs = annotatedApp.getServiceRef();
        for (ServiceRef currServiceRef : serviceRefs) {
            if (currServiceRef.getServiceRefName().trim().equals(webServiceRefName)) {
                serviceRef = currServiceRef;
                break;
            }
        }

        if (serviceRef == null) {
            // Doesn't exist in deployment descriptor -- add new
            serviceRef = new ServiceRef();

            // ------------------------------------------------------------------------------
            // <service-ref> required elements:
            // ------------------------------------------------------------------------------

            // service-ref-name
            serviceRef.setServiceRefName(webServiceRefName);

            // service-ref-interface
            if (webServiceRefValue == javax.xml.ws.Service.class) {
                serviceRef.setServiceInterface(webServiceRefType.getName());
            } else {
                serviceRef.setServiceInterface(webServiceRefValue.getName());
            }
            annotatedApp.getServiceRef().add(serviceRef);
        }

        //------------------------------------------------------------------------------
        // <service-ref> optional elements:
        //------------------------------------------------------------------------------

        // Look-up
        if (serviceRef.getLookupName() == null && !annotation.lookup().trim().isEmpty()) {
            serviceRef.setLookupName(annotation.lookup().trim());
        }

        // service-ref-type
        if (serviceRef.getServiceRefType() == null && !webServiceRefType.equals(Object.class)) {
            serviceRef.setServiceRefType(webServiceRefType.getName());
        }

        // mapped-name
        if (serviceRef.getMappedName() == null && annotation.mappedName().trim().length() > 0) {
            serviceRef.setMappedName(annotation.mappedName().trim());
        }

        // WSDL document location
        if (serviceRef.getWsdlFile() == null) {
            String wsdlLocation = annotation.wsdlLocation();

            if (wsdlLocation == null || wsdlLocation.trim().length() == 0) {
                WebServiceClient wsClient = null;
                if (javax.xml.ws.Service.class == webServiceRefValue) {
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
                serviceRef.setWsdlFile(wsdlLocation);
            }
        }

        // handler-chains
        if (serviceRef.getHandlerChains() == null) {
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
                if (javax.xml.ws.Service.class == webServiceRefValue) {
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
            serviceRef.getInjectionTarget().add(configureInjectionTarget(method, field));
        }

        // web service features
        Annotation[] candidateAnnotations = null;
        if (cls != null) {
            candidateAnnotations = cls.getAnnotations();
        } else if (method != null) {
            candidateAnnotations = method.getAnnotations();
        } else if (field != null) {
            candidateAnnotations = field.getAnnotations();
        }
        if (candidateAnnotations != null && candidateAnnotations.length > 0) {
            List<Annotation> webServiceFeatureAnnotations = new ArrayList<Annotation>();
            for (Annotation anno : candidateAnnotations) {
                if (anno.annotationType().isAnnotationPresent(WebServiceFeatureAnnotation.class)) {
                    webServiceFeatureAnnotations.add(anno);
                }
            }
            if (webServiceFeatureAnnotations.size() > 0) {
                if (javax.xml.ws.Service.class.isAssignableFrom(webServiceRefType)) {
                    log.warn("In current JAX-WS spec, no standard web service feature is supported on service creation, " + webServiceFeatureAnnotations + " are ignored");
                } else {
                    PortComponentRef portComponentRef = getPortComponentRef(serviceRef, webServiceRefType.getName());

                    for (Annotation webServiceFeatureAnnotation : webServiceFeatureAnnotations) {
                        Class<? extends Annotation> webServiceFeatureAnnotationType = webServiceFeatureAnnotation.annotationType();
                        if (webServiceFeatureAnnotationType == MTOM.class) {
                            MTOM mtom = (MTOM) webServiceFeatureAnnotation;
                            if (portComponentRef.getMtomThreshold() == null) {
                                portComponentRef.setMtomThreshold(mtom.threshold());
                            }
                            if (portComponentRef.getEnableMtom() == null) {
                                portComponentRef.setEnableMtom(mtom.enabled());
                            }
                        } else if (webServiceFeatureAnnotationType == javax.xml.ws.soap.Addressing.class) {
                            javax.xml.ws.soap.Addressing addressingAnnotation = (javax.xml.ws.soap.Addressing) webServiceFeatureAnnotation;
                            Addressing addressing = portComponentRef.getAddressing();
                            if (addressing == null) {
                                addressing = new Addressing();
                                addressing.setEnabled(addressingAnnotation.enabled());
                                addressing.setRequired(addressingAnnotation.required());
                                addressing.setResponses(AddressingResponses.valueOf(addressingAnnotation.responses().toString()));
                                portComponentRef.setAddressing(addressing);
                            } else {
                                if (addressing.getEnabled() == null) {
                                    addressing.setEnabled(addressingAnnotation.enabled());
                                }
                                if (addressing.getRequired() == null) {
                                    addressing.setRequired(addressingAnnotation.required());
                                }
                                if (addressing.getResponses() == null) {
                                    addressing.setResponses(AddressingResponses.valueOf(addressingAnnotation.responses().toString()));
                                }
                            }
                        } else if (webServiceFeatureAnnotationType == RespectBinding.class) {
                            RespectBinding respectBinding = (RespectBinding) webServiceFeatureAnnotation;
                            if (portComponentRef.getRespectBinding() == null) {
                                org.apache.openejb.jee.RespectBinding respectBindingValue = new org.apache.openejb.jee.RespectBinding();
                                respectBindingValue.setEnabled(respectBinding.enabled());
                                portComponentRef.setRespectBinding(respectBindingValue);
                            }
                        } else {
                            log.warn("Unsupport web service feature annotation " + webServiceFeatureAnnotation + " on " + webServiceRefName);
                        }
                    }

                }
            }
        }
    }

    private static PortComponentRef getPortComponentRef(ServiceRef serviceRef, String seiInterfaceName) {
        List<PortComponentRef> portComponentRefs = serviceRef.getPortComponentRef();
        for (PortComponentRef portComponentRef : portComponentRefs) {
            if (portComponentRef.getServiceEndpointInterface().equals(seiInterfaceName)) {
                return portComponentRef;
            }
        }
        PortComponentRef portComponentRef = new PortComponentRef();
        portComponentRef.setServiceEndpointInterface(seiInterfaceName);
        portComponentRefs.add(portComponentRef);
        return portComponentRef;
    }
}
