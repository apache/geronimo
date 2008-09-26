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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.jws.HandlerChain;
import javax.xml.ws.WebServiceRef;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.xbeans.javaee.HandlerChainType;
import org.apache.geronimo.xbeans.javaee.HandlerChainsDocument;
import org.apache.geronimo.xbeans.javaee.HandlerChainsType;
import org.apache.geronimo.xbeans.javaee.PortComponentHandlerType;
import org.apache.geronimo.xbeans.javaee.ServiceRefHandlerChainType;
import org.apache.geronimo.xbeans.javaee.ServiceRefHandlerChainsType;
import org.apache.geronimo.xbeans.javaee.ServiceRefHandlerType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlObject;


/**
 * Static helper class used to encapsulate all the functions related to the translation of
 * <strong>@HandlerChain</strong> annotations to deployment descriptor tags. The
 * HandlerChainAnnotationHelper class can be used as part of the deployment of a module into the
 * Geronimo server. It performs the following major functions:
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
 * @version $Rev $Date
 * @since 03-2007
 */
public final class HandlerChainAnnotationHelper extends AnnotationHelper {

    // Private instance variables
    private static final Log log = LogFactory.getLog(HandlerChainAnnotationHelper.class);

    // Private constructor to prevent instantiation
    private HandlerChainAnnotationHelper() {
    }


    /**
     * Updates the deployment descriptor with handler chain info from HandlerChain annotations
     *
     * @param annotatedApp Wrapper around spec dd
     * @param classFinder ClassFinder containing classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    public static void processAnnotations(AnnotatedApp annotatedApp, ClassFinder classFinder) throws DeploymentException {
        if ( annotatedApp != null && classFinder.isAnnotationPresent(HandlerChain.class)) {
            processHandlerChain(annotatedApp, classFinder);
        }
    }


    /**
     * Updates the deployment descriptor with handler chain info from HandlerChain annotations
     *
     * @param annotatedApp Wrapper around spec dd
     * @param classFinder ClassFinder containing classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    private static void processHandlerChain(AnnotatedApp annotatedApp, ClassFinder classFinder) throws DeploymentException {
        log.debug("processHandlerChain(): Entry: AnnotatedApp: " + annotatedApp.toString());

        List<Method> methodswithHandlerChain = classFinder.findAnnotatedMethods(HandlerChain.class);
        List<Field> fieldswithHandlerChain = classFinder.findAnnotatedFields(HandlerChain.class);


        // Method-level annotation
        for ( Method method : methodswithHandlerChain ) {
            HandlerChain handlerChain = method.getAnnotation(HandlerChain.class);
            if ( handlerChain != null ) {
                addHandlerChain(annotatedApp, handlerChain, null, method, null);
            }
        }

        // Field-level annotation
        for ( Field field : fieldswithHandlerChain ) {
            HandlerChain handlerChain = field.getAnnotation(HandlerChain.class);
            if ( handlerChain != null ) {
                addHandlerChain(annotatedApp, handlerChain, null, null, field);
            }
        }

        // Validate deployment descriptor to ensure it's still okay
        validateDD(annotatedApp);

        log.debug("processHandlerChain(): Exit: AnnotatedApp: " + annotatedApp.toString());
    }


    /**
     * Add to the deployment descriptor for a single @HandlerChain annotation. XMLBeans are used to read and
     * manipulate the deployment descriptor as necessary. The HandlerChain annotation(s) will be
     * converted to one of the following deployment descriptors:
     *
     * <ol>
     *      <li><handler-chain> -- Associates the Web Service with an externally defined handler
     *      chain
     * </ol>
     *
     * <p><strong>Note(s):</strong>
     * <ul>
     *      <li>If a field/method has the @HandlerChain annotation then The corresponding
     *      <service-ref> is obtained via the @WebServiceRef annotation
     * </ul>
     *
     * @param annotatedApp wrapper around spec dd
     * @param annotation @HandlerChain annotation
     * @param cls        Class name with the @HandlerChain annotation
     * @param method     Method name with the @HandlerChain annotation
     * @param field      Field name with the @HandlerChain annotation
     */
    private static void addHandlerChain(AnnotatedApp annotatedApp, final HandlerChain annotation, Class cls, Method method, Field field) {
        log.debug("addHandlerChain( [annotatedApp] " + annotatedApp.toString() + "," + '\n' +
                "[annotation] " + annotation.toString() + "," + '\n' +
                "[cls] " + (cls != null ? cls.getName() : null) + "," + '\n' +
                "[method] " + (method != null ? method.getName() : null) + "," + '\n' +
                "[field] " + (field != null ? field.getName() : null) + " ): Entry");

        //------------------------------------------------------------------------------------------
        // HandlerChain members:
        // -- name: Deprecated -- must be empty string
        // -- file: Location of handler chain file in either absolute URL format or a relative path
        //          from the class file. Cannot be emptry string.
        //------------------------------------------------------------------------------------------
        String handlerChainFile = annotation.file();
        log.debug("addHandlerChain(): handlerChainFile: " + handlerChainFile);

        // Determine ServiceRef name
        String serviceRefName;
        WebServiceRef webServiceRef = null;
        if ( method != null ) {
            webServiceRef = method.getAnnotation(WebServiceRef.class);
        }
        else if ( field != null ) {
            webServiceRef = field.getAnnotation(WebServiceRef.class);
        }
        if ( webServiceRef != null ) {
            serviceRefName = webServiceRef.name();
        }
        else {
            //TODO is this guaranteed to be ""? If so, simplify the code here
            serviceRefName = annotation.name();
        }
        if ( serviceRefName.equals("") ) {
            serviceRefName = getInjectionJavaType(method, field);
        }
        log.debug("addHandlerChain().serviceRefName : " + serviceRefName);

        if (!serviceRefName.equals("") && !handlerChainFile.equals("")) {
            try {
                // Locate the handler chain XML file
                URL url = null;
                try {
                    // Assume URL format first
                    url = new URL(handlerChainFile);
                }
                catch (MalformedURLException mfe) {
                    log.debug("addHandlerChain().MalformedURLException" );

                    // Not URL format -- see if it's relative to the annotated class
                    if (cls != null) {
                        url = getURL(cls.getClass(), handlerChainFile);
                    }
                    else if (method != null) {
                        url = getURL(method.getDeclaringClass(), handlerChainFile);
                    }
                    else if (field != null) {
                        url = getURL(field.getDeclaringClass(), handlerChainFile);
                    }
                }

                if (url != null) {
                    // Find the <service-ref> entry this handler chain belongs to and insert it
                    ServiceRefType[] serviceRefs = annotatedApp.getServiceRefArray();
                    boolean exists = false;
                    for ( ServiceRefType serviceRef : serviceRefs ) {
                        if ( serviceRef.getServiceRefName().getStringValue().trim().equals(serviceRefName) && !serviceRef.isSetHandlerChains()) {
                            insertHandlers(serviceRef, url);
                            exists = true;
                            break;
                        }
                    }
                    if (exists) {
                        log.debug("HandlerChainAnnotationHelper: <service-ref> entry found: " + serviceRefName);
                    }
                    else {
                        log.debug("HandlerChainAnnotationHelper: <service-ref> entry NOT found: " + serviceRefName);
                    }
                }
                else {
                    log.debug("HandlerChainAnnotationHelper: Handler chain file NOT found: " + handlerChainFile );
                }
            }
            catch ( Exception anyException ) {
                log.debug("HandlerChainAnnotationHelper: Exception caught while processing <handler-chain>");
            }
        }
        log.debug("addHandlerChain(): Exit");
    }


    private static URL getURL(Class clazz, String file) {
        log.debug("getURL( " + clazz.getName() + ", " + file + " ): Entry");

        URL url = clazz.getResource(file);
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource(file);
        }
        if (url == null) {
            String loc= clazz.getPackage().getName().replace('.', '/') + "/" + file;
            url = Thread.currentThread().getContextClassLoader().getResource(loc);
        }

        log.debug("getURL(): Exit: url: " + (url != null ? url.toString() : null) );
        return url;
    }
    
    public static void insertHandlers(ServiceRefType serviceRef, HandlerChain annotation, Class clazz) {
        String handlerChainFile = annotation.file();
        log.debug("handlerChainFile: " + handlerChainFile);
        if (handlerChainFile == null || handlerChainFile.trim().length() == 0) {
            return;
        }
        URL url = null;
        try {
            // Assume URL format first
            url = new URL(handlerChainFile);
        } catch (MalformedURLException mfe) {
            // Not URL format -- see if it's relative to the annotated class
            url = getURL(clazz, handlerChainFile);
        }
        if (url != null) {
            try {
                insertHandlers(serviceRef, url);
            } catch (Exception e) {
                log.debug("Error while processing <handler-chain>", e);
            }
        }
    }
    
    public static void insertHandlers(ServiceRefType serviceRef, URL url) throws Exception {
        // Bind the XML handler chain file to an XMLBeans document
        XmlObject xml = XmlBeansUtil.parse(url, null);
        HandlerChainsDocument hcd = (HandlerChainsDocument) XmlBeansUtil.typedCopy(xml, HandlerChainsDocument.type);
        HandlerChainsType handlerChains = hcd.getHandlerChains();
        
        ServiceRefHandlerChainsType serviceRefHandlerChains = serviceRef.addNewHandlerChains();
        for (HandlerChainType handlerChain : handlerChains.getHandlerChainArray()) {
            ServiceRefHandlerChainType serviceRefHandlerChain = serviceRefHandlerChains.addNewHandlerChain();
            if (handlerChain.getPortNamePattern() != null) {
                serviceRefHandlerChain.setPortNamePattern(handlerChain.getPortNamePattern());
            }
            if (handlerChain.getServiceNamePattern() != null) {
                serviceRefHandlerChain.setServiceNamePattern(handlerChain.getServiceNamePattern());
            }
            if (handlerChain.getProtocolBindings() != null) {
                serviceRefHandlerChain.setProtocolBindings(handlerChain.getProtocolBindings());
            }
            for ( PortComponentHandlerType handler : handlerChain.getHandlerArray()) {
                ServiceRefHandlerType serviceRefHandler = serviceRefHandlerChain.addNewHandler();
                serviceRefHandler.setHandlerName(handler.getHandlerName());
                serviceRefHandler.setHandlerClass(handler.getHandlerClass());
                if (handler.getDescriptionArray().length>0) {
                    serviceRefHandler.setDescriptionArray(handler.getDescriptionArray());
                }
                if (handler.getInitParamArray().length>0) {
                    serviceRefHandler.setInitParamArray(handler.getInitParamArray());
                }
                if (handler.getSoapHeaderArray().length>0) {
                    serviceRefHandler.setSoapHeaderArray(handler.getSoapHeaderArray());
                }
                if (handler.getSoapRoleArray().length>0) {
                    serviceRefHandler.setSoapRoleArray(handler.getSoapRoleArray());
                }
            }
        }
    }

}
