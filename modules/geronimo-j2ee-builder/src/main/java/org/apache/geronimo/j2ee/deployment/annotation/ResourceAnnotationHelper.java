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

import javax.annotation.Resource;
import javax.annotation.Resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.xbeans.javaee.DescriptionType;
import org.apache.geronimo.xbeans.javaee.EnvEntryType;
import org.apache.geronimo.xbeans.javaee.EnvEntryTypeValuesType;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee.JavaIdentifierType;
import org.apache.geronimo.xbeans.javaee.JndiNameType;
import org.apache.geronimo.xbeans.javaee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.javaee.MessageDestinationTypeType;
import org.apache.geronimo.xbeans.javaee.ResAuthType;
import org.apache.geronimo.xbeans.javaee.ResSharingScopeType;
import org.apache.geronimo.xbeans.javaee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.javaee.ResourceRefType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;
import org.apache.geronimo.xbeans.javaee.XsdAnyURIType;
import org.apache.geronimo.xbeans.javaee.XsdStringType;
import org.apache.xbean.finder.ClassFinder;


/**
 * Static helper class used to encapsulate all the functions related to the translation of
 * <strong>@Resource</strong> and <strong>@Resources</strong> annotations to deployment descriptor
 * tags. The ResourceAnnotationHelper class can be used as part of the deployment of a module into
 * the Geronimo server. It performs the following major functions:
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
 * @version $Rev$ $Date$
 * @since 02-2007
 */
public final class ResourceAnnotationHelper {

    // Private instance variables
    private static final Log log = LogFactory.getLog(ResourceAnnotationHelper.class);

    // Private constructor to prevent instantiation
    private ResourceAnnotationHelper() {
    }


    /**
     * Determine if there are any Resource annotations present
     *
     * @return true or false
     */
    public static boolean annotationsPresent(ClassFinder classFinder) {
        if (classFinder.isAnnotationPresent(Resource.class)) return true;
        if (classFinder.isAnnotationPresent(Resources.class)) return true;
        return false;
    }

    /**
     * Process the Resource set of annotations
     *
     * @return Updated deployment descriptor
     * @throws Exception if parsing or validation error
     */
    public static void processAnnotations(AnnotatedApp annotatedApp, ClassFinder classFinder) throws Exception {
        if (annotatedApp != null) {
            processResources(annotatedApp, classFinder);
            processResource(annotatedApp, classFinder);
        }
    }


    /**
     * Process @Resource annotations
     *
     * @return
     * @throws Exception
     */
    private static void processResource(AnnotatedApp annotatedApp, ClassFinder classFinder) throws Exception {
        log.debug("processResource(): Entry: AnnotatedApp: " + annotatedApp.toString());

        //-----------------------------
        // Save the Resource lists
        //-----------------------------
        List<Class> classeswithResource = classFinder.findAnnotatedClasses(Resource.class);
        List<Method> methodswithResource = classFinder.findAnnotatedMethods(Resource.class);
        List<Field> fieldswithResource = classFinder.findAnnotatedFields(Resource.class);

        //--------------------------
        // Class-level Resource
        //--------------------------
        for (Class cls : classeswithResource) {
            Resource resource = (Resource) cls.getAnnotation(Resource.class);
            if (resource != null) {
                addResource(annotatedApp, resource, cls, null, null);
            }
        }

        //---------------------------
        // Method-level Resource
        //---------------------------
        for (Method method : methodswithResource) {
            Resource resource = (Resource) method.getAnnotation(Resource.class);
            if (resource != null) {
                addResource(annotatedApp, resource, null, method, null);
            }
        }

        //--------------------------
        // Field-level Resource
        //--------------------------
        for (Field field : fieldswithResource) {
            Resource resource = (Resource) field.getAnnotation(Resource.class);
            if (resource != null) {
                addResource(annotatedApp, resource, null, null, field);
            }
        }

        //--------------------------------------------------------------
        // Validate deployment descriptor to ensure it's still okay
        //--------------------------------------------------------------
        validateDD(annotatedApp);

        log.debug("processResource(): Exit: AnnotatedApp: " + annotatedApp.toString());
    }


    /**
     * Process @Resources annotations
     */
    private static void processResources(AnnotatedApp annotatedApp, ClassFinder classFinder) throws Exception {
        log.debug("processResources(): Entry");

        //-----------------------------
        // Save the Resources list
        //-----------------------------
        List<Class> classeswithResources = classFinder.findAnnotatedClasses(Resources.class);

        //---------------------------
        // Class-level Resources
        //---------------------------
        List<Resource> ResourceList = new ArrayList<Resource>();
        for (Class cls : classeswithResources) {
            Resources resources = (Resources) cls.getAnnotation(Resources.class);
            if (resources != null) {
                ResourceList.addAll(Arrays.asList(resources.value()));
            }
            for (Resource resource : ResourceList) {
                addResource(annotatedApp, resource, cls, null, null);
            }
            ResourceList.clear();
        }

        log.debug("processResources(): Exit");
    }


    /**
     * Add @Resource and @Resources annotations to the deployment descriptor. XMLBeans are used to
     * read and manipulate the deployment descriptor as necessary. The Resource annotation(s) will be
     * converted to one of the following deployment descriptors:
     * <p/>
     * <ol>
     * <li><env-entry> -- Used to declare an application's environment entry
     * <li><service-ref> -- Declares a reference to a Web service
     * <li><resource-ref> -- Contains a declaration of a Deployment Component's reference to an
     * external resource
     * <li><message-destination-ref> -- Contains a declaration of Deployment Component's
     * reference to a message destination associated with a resource in Deployment Component's
     * environment
     * <li><resource-env-ref> -- Contains a declaration of a Deployment Component's reference to
     * an administered object associated with a resource in the Deployment Component's
     * environment
     * </ol>
     * <p/>
     * <p><strong>Note(s):</strong>
     * <ul>
     * <li>The deployment descriptor is the authoritative source so this method ensures that
     * existing elements in it are not overwritten by annoations
     * </ul>
     *
     * @param annotation @Resource annotation
     * @param cls        Class name with the @Resource annoation
     * @param method     Method name with the @Resource annoation
     * @param field      Field name with the @Resource annoation
     */
    private static void addResource(AnnotatedApp annotatedApp, Resource annotation, Class cls, Method method, Field field) {
        log.debug("addResource( " + annotatedApp.toString() + "," + '\n' +
                annotation.name() + "," + '\n' +
                (cls != null ? cls.getName() : null) + "," + '\n' +
                (method != null ? method.getName() : null) + "," + '\n' +
                (field != null ? field.getName() : null) + " ): Entry");

        //------------------------------------------------------------------------------------------
        // Resource name:
        // -- When annotation is applied on a class:    name must be provided (cannot be inferred)
        // -- When annotation is applied on a method:   name is JavaBeans property name qualified by
        //                                              the class (or as provided on the annotation)
        // -- When annotation is applied on a field:    name is the field name qualified by the class
        //                                              (or as provided on the annotation)
        //------------------------------------------------------------------------------------------
        String resourceName = annotation.name();
        if (resourceName.equals("")) {
            if (method != null) {
                StringBuilder stringBuilder = new StringBuilder(method.getName().substring(3));
                stringBuilder.setCharAt(0, Character.toLowerCase(stringBuilder.charAt(0)));
                resourceName = method.getDeclaringClass().getName() + "/" + stringBuilder.toString();
            } else if (field != null) {
                resourceName = field.getDeclaringClass().getName() + "/" + field.getName();
            }
        }
        log.debug("addResource(): resourceName: " + resourceName);

        //------------------------------------------------------------------------------------------
        // Resource type:
        // -- When annotation is applied on a class:    type must be provided (cannot be inferred)
        // -- When annotation is applied on a method:   type is the JavaBeans property type (or as
        //                                              provided on the annotation)
        // -- When annotation is applied on a field:    type is the field type (or as provided on the
        //                                              annotation)
        //------------------------------------------------------------------------------------------
        String resourceType = annotation.type().getCanonicalName();
        if (resourceType.equals("") || resourceType.equals(Object.class.getName())) {
            if (method != null) {
                resourceType = method.getParameterTypes()[0].getCanonicalName();
            } else if (field != null) {
                resourceType = field.getType().getName();
            }
        }
        log.debug("addResource(): resourceType: " + resourceType);

        //------------------------------------------------------------------------------------------
        // Method name (for setter-based injection) must follow JavaBeans conventions:
        // -- Must start with "set"
        // -- Have one parameter
        // -- Return void
        //------------------------------------------------------------------------------------------
        String injectionJavaType = "";
        String injectionClass = null;
        if (method != null) {
            injectionJavaType = method.getName().substring(3);
            StringBuilder stringBuilder = new StringBuilder(injectionJavaType);
            stringBuilder.setCharAt(0, Character.toLowerCase(stringBuilder.charAt(0)));
            injectionJavaType = stringBuilder.toString();
            injectionClass = method.getDeclaringClass().getName();
        } else if (field != null) {
            injectionJavaType = field.getName();
            injectionClass = field.getDeclaringClass().getName();
        }
        log.debug("addResource(): injectionJavaType: " + injectionJavaType);
        log.debug("addResource(): injectionClass   : " + injectionClass);

        // 0. exclusions
        //  WebServiceContext
        if (resourceType.equals("javax.xml.ws.WebServiceContext")) {
            return;
        }

        //------------------------------------------------------------------------------------------
        // 1. <env-entry>
        //------------------------------------------------------------------------------------------
        if (resourceType.equals("java.lang.String") ||
                resourceType.equals("java.lang.Character") ||
                resourceType.equals("java.lang.Integer") ||
                resourceType.equals("java.lang.Boolean") ||
                resourceType.equals("java.lang.Double") ||
                resourceType.equals("java.lang.Byte") ||
                resourceType.equals("java.lang.Short") ||
                resourceType.equals("java.lang.Long") ||
                resourceType.equals("java.lang.Float")) {

            log.debug("addResource(): <env-entry> found");

            boolean exists = false;
            EnvEntryType[] envEntries = annotatedApp.getEnvEntryArray();
            for (EnvEntryType envEntry : envEntries) {
                if (envEntry.getEnvEntryName().getStringValue().equals(resourceName)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                try {

                    // Doesn't exist in deployment descriptor -- add new
                    EnvEntryType envEntry = annotatedApp.addNewEnvEntry();

                    //------------------------------------------------------------------------------
                    // <env-entry> required elements:
                    //------------------------------------------------------------------------------

                    // env-entry-name
                    JndiNameType envEntryName = envEntry.addNewEnvEntryName();
                    envEntryName.setStringValue(resourceName);

                    if (!resourceType.equals("")) {
                        // env-entry-type
                        EnvEntryTypeValuesType envEntryType = envEntry.addNewEnvEntryType();
                        envEntryType.setStringValue(resourceType);
                    } else if (!injectionJavaType.equals("")) {
                        // injectionTarget
                        InjectionTargetType injectionTarget = envEntry.addNewInjectionTarget();
                        configureInjectionTarget(injectionTarget, injectionClass, injectionJavaType);
                    }

                    // env-entry-value
                    XsdStringType value = envEntry.addNewEnvEntryValue();
                    value.setStringValue(annotation.mappedName());

                    //------------------------------------------------------------------------------
                    // <env-entry> optional elements:
                    //------------------------------------------------------------------------------

                    // description
                    String descriptionAnnotation = annotation.description();
                    if (descriptionAnnotation != null && descriptionAnnotation.length() > 0) {
                        DescriptionType description = envEntry.addNewDescription();
                        description.setStringValue(descriptionAnnotation);
                    }

                }
                catch (Exception anyException) {
                    log.debug("ResourceAnnotationHelper: Exception caught while processing <env-entry>");
                }
            }
        }

        //------------------------------------------------------------------------------------------
        // 2. <service-ref>
        //------------------------------------------------------------------------------------------
        else if (resourceType.equals("javax.xml.rpc.Service")) {

            log.debug("addResource(): <service-ref> found");

            boolean exists = false;
            ServiceRefType[] serviceRefs = annotatedApp.getServiceRefArray();
            for (ServiceRefType serviceRef : serviceRefs) {
                if (serviceRef.getServiceRefName().getStringValue().equals(resourceName)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                try {

                    // Doesn't exist in deployment descriptor -- add new
                    ServiceRefType serviceRef = annotatedApp.addNewServiceRef();

                    //------------------------------------------------------------------------------
                    // <service-ref> required elements:
                    //------------------------------------------------------------------------------

                    // service-ref-name
                    JndiNameType serviceRefName = serviceRef.addNewServiceRefName();
                    serviceRefName.setStringValue(resourceName);

                    if (!resourceType.equals("")) {
                        // service-ref-type
                        FullyQualifiedClassType qualifiedClass = serviceRef.addNewServiceInterface();
                        qualifiedClass.setStringValue(resourceType);
                    } else if (!injectionJavaType.equals("")) {
                        // injectionTarget
                        InjectionTargetType injectionTarget = serviceRef.addNewInjectionTarget();
                        configureInjectionTarget(injectionTarget, injectionClass, injectionJavaType);
                    }

                    //------------------------------------------------------------------------------
                    // <service-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // description
                    String descriptionAnnotation = annotation.description();
                    if (descriptionAnnotation.length() > 0) {
                        DescriptionType description = serviceRef.addNewDescription();
                        description.setStringValue(descriptionAnnotation);
                    }

                    // WSDL document location
                    String documentAnnotation = annotation.mappedName();
                    if (documentAnnotation.length() > 0) {
                        XsdAnyURIType wsdlFile = XsdAnyURIType.Factory.newInstance();
                        wsdlFile.setStringValue(annotation.mappedName());
                        serviceRef.setWsdlFile(wsdlFile);
                    }

                }
                catch (Exception anyException) {
                    log.debug("ResourceAnnotationHelper: Exception caught while processing <service-ref>");
                    anyException.printStackTrace();
                }
            }
        }

        //------------------------------------------------------------------------------------------
        // 3. <resource-ref>
        //------------------------------------------------------------------------------------------
        else if (resourceType.equals("javax.sql.DataSource") ||
                resourceType.equals("javax.jms.ConnectionFactory") ||
                resourceType.equals("javax.jms.QueueConnectionFactory") ||
                resourceType.equals("javax.jms.TopicConnectionFactory") ||
                resourceType.equals("javax.mail.Session") ||
                resourceType.equals("java.net.URL") ||
                resourceType.equals("javax.resource.cci.ConnectionFactory") ||
                resourceType.equals("org.omg.CORBA_2_3.ORB") ||
                resourceType.endsWith("ConnectionFactory")) {

            log.debug("addResource(): <resource-ref> found");

            boolean exists = false;
            ResourceRefType[] resourceRefs = annotatedApp.getResourceRefArray();
            for (ResourceRefType resourceRef : resourceRefs) {
                if (resourceRef.getResRefName().getStringValue().equals(resourceName)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                try {

                    // Doesn't exist in deployment descriptor -- add new
                    ResourceRefType resourceRef = annotatedApp.addNewResourceRef();

                    //------------------------------------------------------------------------------
                    // <resource-ref> required elements:
                    //------------------------------------------------------------------------------

                    // resource-ref-name
                    JndiNameType resourceRefName = JndiNameType.Factory.newInstance();
                    resourceRefName.setStringValue(resourceName);
                    resourceRef.setResRefName(resourceRefName);

                    if (!resourceType.equals("")) {
                        // resource-ref-type
                        FullyQualifiedClassType qualifiedClass = FullyQualifiedClassType.Factory.newInstance();
                        qualifiedClass.setStringValue(resourceType);
                        resourceRef.setResType(qualifiedClass);
                    } else if (!injectionJavaType.equals("")) {
                        // injectionTarget
                        InjectionTargetType injectionTarget = InjectionTargetType.Factory.newInstance();
                        FullyQualifiedClassType qualifiedClass = FullyQualifiedClassType.Factory.newInstance();
                        JavaIdentifierType javaType = JavaIdentifierType.Factory.newInstance();
                        qualifiedClass.setStringValue(injectionClass);
                        javaType.setStringValue(injectionJavaType);
                        injectionTarget.setInjectionTargetClass(qualifiedClass);
                        injectionTarget.setInjectionTargetName(javaType);
                        int arraySize = resourceRef.sizeOfInjectionTargetArray();
                        resourceRef.insertNewInjectionTarget(arraySize);
                        resourceRef.setInjectionTargetArray(arraySize, injectionTarget);
                    }

                    //------------------------------------------------------------------------------
                    // <resource-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // description
                    String descriptionAnnotation = annotation.description();
                    if (descriptionAnnotation.length() > 0) {
                        DescriptionType description = DescriptionType.Factory.newInstance();
                        description.setStringValue(descriptionAnnotation);
                        int arraySize = resourceRef.sizeOfDescriptionArray();
                        resourceRef.insertNewDescription(arraySize);
                        resourceRef.setDescriptionArray(arraySize, description);
                    }

                    // authentication
                    ResAuthType resAuth = ResAuthType.Factory.newInstance();
                    if (annotation.authenticationType() == Resource.AuthenticationType.CONTAINER) {
                        resAuth.setStringValue("Container");
                    } else if (annotation.authenticationType() == Resource.AuthenticationType.APPLICATION) {
                        resAuth.setStringValue("Application");
                    }
                    resourceRef.setResAuth(resAuth);

                    // sharing scope
                    ResSharingScopeType resScope = ResSharingScopeType.Factory.newInstance();
                    resScope.setStringValue(annotation.shareable() ? "Shareable" : "Unshareable");
                    resourceRef.setResSharingScope(resScope);

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if (mappdedNameAnnotation.length() > 0) {
                        XsdStringType mappedName = XsdStringType.Factory.newInstance();
                        mappedName.setStringValue(mappdedNameAnnotation);
                        resourceRef.setMappedName(mappedName);
                    }

                }
                catch (Exception anyException) {
                    log.debug("ResourceAnnotationHelper: Exception caught while processing <resource-ref>");
                    anyException.printStackTrace();
                }
            }
        }

        //------------------------------------------------------------------------------------------
        // 4. <message-destination-ref>
        //------------------------------------------------------------------------------------------
        else if (resourceType.equals("javax.jms.Queue") ||
                resourceType.equals("javax.jms.Topic")) {

            log.debug("addResource(): <message-destination-ref> found");

            boolean exists = false;
            MessageDestinationRefType[] messageDestinationRefs = annotatedApp.getMessageDestinationRefArray();
            for (MessageDestinationRefType messageDestinationRef : messageDestinationRefs) {
                if (messageDestinationRef.getMessageDestinationRefName().getStringValue().equals(resourceName)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                try {

                    // Doesn't exist in deployment descriptor -- add new
                    MessageDestinationRefType messageDestinationRef = annotatedApp.addNewMessageDestinationRef();

                    //------------------------------------------------------------------------------
                    // <message-destination-ref> required elements:
                    //------------------------------------------------------------------------------

                    // message-destination-ref-name
                    JndiNameType messageDestinationRefName = JndiNameType.Factory.newInstance();
                    messageDestinationRefName.setStringValue(resourceName);
                    messageDestinationRef.setMessageDestinationRefName(messageDestinationRefName);

                    if (!resourceType.equals("")) {
                        // message-destination-ref-type
                        MessageDestinationTypeType msgDestType = MessageDestinationTypeType.Factory.newInstance();
                        msgDestType.setStringValue(resourceType);
                        messageDestinationRef.setMessageDestinationType(msgDestType);
                    } else if (!injectionJavaType.equals("")) {
                        // injectionTarget
                        InjectionTargetType injectionTarget = InjectionTargetType.Factory.newInstance();
                        FullyQualifiedClassType qualifiedClass = FullyQualifiedClassType.Factory.newInstance();
                        JavaIdentifierType javaType = JavaIdentifierType.Factory.newInstance();
                        qualifiedClass.setStringValue(injectionClass);
                        javaType.setStringValue(injectionJavaType);
                        injectionTarget.setInjectionTargetClass(qualifiedClass);
                        injectionTarget.setInjectionTargetName(javaType);
                        int arraySize = messageDestinationRef.sizeOfInjectionTargetArray();
                        messageDestinationRef.insertNewInjectionTarget(arraySize);
                        messageDestinationRef.setInjectionTargetArray(arraySize, injectionTarget);
                    }

                    //------------------------------------------------------------------------------
                    // <message-destination-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // description
                    String descriptionAnnotation = annotation.description();
                    if (descriptionAnnotation.length() > 0) {
                        DescriptionType description = DescriptionType.Factory.newInstance();
                        description.setStringValue(descriptionAnnotation);
                        int arraySize = messageDestinationRef.sizeOfDescriptionArray();
                        messageDestinationRef.insertNewDescription(arraySize);
                        messageDestinationRef.setDescriptionArray(arraySize, description);
                    }

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if (mappdedNameAnnotation.length() > 0) {
                        XsdStringType mappedName = XsdStringType.Factory.newInstance();
                        mappedName.setStringValue(mappdedNameAnnotation);
                        messageDestinationRef.setMappedName(mappedName);
                    }

                }
                catch (Exception anyException) {
                    log.debug("ResourceAnnotationHelper: Exception caught while processing <message-destination-ref>");
                    anyException.printStackTrace();
                }
            }
        }

        //------------------------------------------------------------------------------------------
        // 5. Everything else must be a <resource-env-ref>
        //------------------------------------------------------------------------------------------
        else if (annotation.type().getCanonicalName().equals("javax.resource.cci.InteractionSpec") ||
                annotation.type().getCanonicalName().equals("javax.transaction.UserTransaction") || true) {

            log.debug("addResource(): <resource-env-ref> found");

            boolean exists = false;
            ResourceEnvRefType[] resourceEnvRefs = annotatedApp.getResourceEnvRefArray();
            for (ResourceEnvRefType resourceEnvRef : resourceEnvRefs) {
                if (resourceEnvRef.getResourceEnvRefName().getStringValue().equals(resourceName)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                try {

                    // Doesn't exist in deployment descriptor -- add new
                    ResourceEnvRefType resourceEnvRef = annotatedApp.addNewResourceEnvRef();

                    //------------------------------------------------------------------------------
                    // <resource-env-ref> required elements:
                    //------------------------------------------------------------------------------

                    // resource-env-ref-name
                    JndiNameType resourceEnvRefName = JndiNameType.Factory.newInstance();
                    resourceEnvRefName.setStringValue(resourceName);
                    resourceEnvRef.setResourceEnvRefName(resourceEnvRefName);

                    if (!resourceType.equals("")) {
                        // resource-env-ref-type
                        FullyQualifiedClassType classType = FullyQualifiedClassType.Factory.newInstance();
                        classType.setStringValue(resourceType);
                        resourceEnvRef.setResourceEnvRefType(classType);
                    } else if (!injectionJavaType.equals("")) {
                        // injectionTarget
                        InjectionTargetType injectionTarget = InjectionTargetType.Factory.newInstance();
                        FullyQualifiedClassType qualifiedClass = FullyQualifiedClassType.Factory.newInstance();
                        JavaIdentifierType javaType = JavaIdentifierType.Factory.newInstance();
                        qualifiedClass.setStringValue(injectionClass);
                        javaType.setStringValue(injectionJavaType);
                        injectionTarget.setInjectionTargetClass(qualifiedClass);
                        injectionTarget.setInjectionTargetName(javaType);
                        int arraySize = resourceEnvRef.sizeOfInjectionTargetArray();
                        resourceEnvRef.insertNewInjectionTarget(arraySize);
                        resourceEnvRef.setInjectionTargetArray(arraySize, injectionTarget);
                    }

                    //------------------------------------------------------------------------------
                    // <resource-env-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // description
                    String descriptionAnnotation = annotation.description();
                    if (descriptionAnnotation.length() > 0) {
                        DescriptionType description = DescriptionType.Factory.newInstance();
                        description.setStringValue(descriptionAnnotation);
                        int arraySize = resourceEnvRef.sizeOfDescriptionArray();
                        resourceEnvRef.insertNewDescription(arraySize);
                        resourceEnvRef.setDescriptionArray(arraySize, description);
                    }

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if (mappdedNameAnnotation.length() > 0) {
                        XsdStringType mappedName = XsdStringType.Factory.newInstance();
                        mappedName.setStringValue(mappdedNameAnnotation);
                        resourceEnvRef.setMappedName(mappedName);
                    }

                }
                catch (Exception anyException) {
                    log.debug("ResourceAnnotationHelper: Exception caught while processing <resource-env-ref>");
                    anyException.printStackTrace();
                }
            }
        }
        log.debug("addResource(): Exit");
    }

    private static void configureInjectionTarget(InjectionTargetType injectionTarget, String injectionClass, String injectionJavaType) {
        FullyQualifiedClassType qualifiedClass = injectionTarget.addNewInjectionTargetClass();
        JavaIdentifierType javaType = injectionTarget.addNewInjectionTargetName();
        qualifiedClass.setStringValue(injectionClass);
        javaType.setStringValue(injectionJavaType);
        injectionTarget.setInjectionTargetClass(qualifiedClass);
        injectionTarget.setInjectionTargetName(javaType);
    }


    /**
     * Validate deployment descriptor
     *
     * @param AnnotatedApp
     * @throws Exception thrown if deployment descriptor cannot be parsed
     */
    private static void validateDD(AnnotatedApp annotatedApp) throws Exception {
        log.debug("validateDD( " + annotatedApp.toString() + " ): Entry");

        XmlBeansUtil.parse(annotatedApp.toString());

        log.debug("validateDD(): Exit");
    }
}
