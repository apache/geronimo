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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.xbeans.javaee.DescriptionType;
import org.apache.geronimo.xbeans.javaee.EnvEntryType;
import org.apache.geronimo.xbeans.javaee.EnvEntryTypeValuesType;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee.JavaIdentifierType;
import org.apache.geronimo.xbeans.javaee.JndiNameType;
import org.apache.geronimo.xbeans.javaee.ListenerType;
import org.apache.geronimo.xbeans.javaee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.javaee.MessageDestinationTypeType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;
import org.apache.geronimo.xbeans.javaee.ServletType;
import org.apache.geronimo.xbeans.javaee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.javaee.ResourceRefType;
import org.apache.geronimo.xbeans.javaee.ResAuthType;
import org.apache.geronimo.xbeans.javaee.ResSharingScopeType;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.geronimo.xbeans.javaee.XsdAnyURIType;
import org.apache.geronimo.xbeans.javaee.XsdStringType;
import org.apache.xbean.finder.ClassFinder;


/**
 * Static helper class used to encapsulate all the functions related to the translation of
 *
 * @EJB and @EBJs annotations to deployment descriptor tags. The ResourceAnnotationHelper class can be
 * used as part of the deployment of a module into the Geronimo server. It performs the following
 * major functions:
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
 *      <li>How to determine Session/Entity remote/local for @EJB
 * </ul>
 *
 * @version $Rev$ $Date$
 * @since 02-2007
 */
public final class ResourceAnnotationHelper {

    // Private instance variables
    private static final Log log = LogFactory.getLog( ResourceAnnotationHelper.class );

    // Private constructor to prevent instantiation
    private ResourceAnnotationHelper() {
    }


    /**
     * Determine if there are any Resource annotations present
     *
     * @return true or false
     */
    public static boolean annotationsPresent( ClassFinder classFinder ) {
        if ( classFinder.isAnnotationPresent(Resource.class) ) return true;
        if ( classFinder.isAnnotationPresent(Resources.class) ) return true;
        return false;
    }

    /**
     * Process the Resource set of annotations
     *
     * @return Updated deployment descriptor
     * @exception Exception if parsing or validation error
     */
    public static WebAppType processAnnotations( WebAppType webApp, ClassFinder classFinder ) throws Exception {
        processResources( webApp,classFinder );
        processResource( webApp,classFinder );
        return webApp;
    }


    /**
     *  Process @Resource annotations
     *
     * @return
     * @exception Exception
     */
    private static void processResource( WebAppType webApp, ClassFinder classFinder ) throws Exception {
        log.debug( "processResource(): Entry: webApp: " + webApp.toString() );

        //-----------------------------
        // Save the Resource lists
        //-----------------------------
        List<Class>  classeswithResource = classFinder.findAnnotatedClasses( Resource.class );
        List<Method> methodswithResource = classFinder.findAnnotatedMethods( Resource.class );
        List<Field>  fieldswithResource  = classFinder.findAnnotatedFields ( Resource.class );

        //--------------------------
        // Class-level Resource
        //--------------------------
        for ( Class cls : classeswithResource ) {
            Resource resource = (Resource)cls.getAnnotation( Resource.class );
            if ( resource != null ) {
                addResource( webApp, resource, cls, null, null );
            }
        }

        //---------------------------
        // Method-level Resource
        //---------------------------
        for ( Method method : methodswithResource ) {
            Resource resource = (Resource)method.getAnnotation( Resource.class );
            if ( resource != null ) {
                addResource( webApp, resource, null, method, null );
            }
        }

        //--------------------------
        // Field-level Resource
        //--------------------------
        for ( Field field : fieldswithResource ) {
            Resource resource = (Resource)field.getAnnotation( Resource.class );
            if ( resource != null ) {
                addResource( webApp, resource, null, null, field );
            }
        }

        //--------------------------------------------------------------
        // Validate deployment descriptor to ensure it's still okay
        //--------------------------------------------------------------
        validateDD( webApp );

        log.debug( "processResource(): Exit: webApp: " + webApp.toString() );
    }



    /**
     *  Process @Resources annotations
     */
    private static void processResources( WebAppType webApp, ClassFinder classFinder ) throws Exception {
        log.debug( "processResources(): Entry" );

        //-----------------------------
        // Save the Resources list
        //-----------------------------
        List<Class> classeswithResources = classFinder.findAnnotatedClasses( Resources.class );

        //---------------------------
        // Class-level Resources
        //---------------------------
        List<Resource> ResourceList = new ArrayList<Resource>();
        for ( Class cls : classeswithResources ) {
            Resources resources = (Resources) cls.getAnnotation( Resources.class );
            if ( resources != null ) {
                ResourceList.addAll( Arrays.asList(resources.value()) );
            }
            for ( Resource resource : ResourceList ) {
                addResource( webApp, resource, cls, null, null );
            }
            ResourceList.clear();
        }

        log.debug( "processResources(): Exit" );
    }



    /**
     * Add @Resource and @Resources annotations to the deployment descriptor. XMLBeans are used to read and
     * manipulate the deployment descriptor as necessary. The Resource annotation(s) will be converted to
     * one of the following deployment descriptors:
     *
     * <ol>
     *      <li><env-entry> -- Used to declare an application's environment entry
     *      <li><service-ref> -- Declares a reference to a Web service
     *      <li><resource-ref> -- Contains a declaration of a Deployment Component's reference to an
     *      external resource
     *      <li><message-destination-ref> -- Contains a declaration of Deployment Component's
     *      reference to a message destination associated with a resource in Deployment Component's
     *      environment
     *      <li><resource-env-ref> -- Contains a declaration of a Deployment Component's reference to
     *      an administered object associated with a resource in the Deployment Component's
     *      environment
     * </ol>
     *
     * <p><strong>Note(s):</strong>
     * <ul>
     *      <li>The deployment descriptor is the authoritative source so this method ensures that
     *      existing elements in it are not overwritten by annoations
     * </ul>
     *
     * @param annotation @Resource annotation
     * @param cls        Class name with the @Resource annoation
     * @param method     Method name with the @Resource annoation
     * @param field      Field name with the @Resource annoation
     */
    private static void addResource( WebAppType webApp, Resource annotation, Class cls, Method method, Field field ) {
        log.debug( "addResource( " + webApp.toString() + ","     + '\n' +
                  annotation.name() + ","                       + '\n' +
                  (cls!=null?cls.getName():null) + ","          + '\n' +
                  (method!=null?method.getName():null) + ","    + '\n' +
                  (field!=null?field.getName():null) + " ): Entry" );

        //------------------------------------------------------------------------------------------
        // Resource name:
        // -- When annotation is applied on a class:    name must be provided (cannot be inferred)
        // -- When annotation is applied on a method:   name is JavaBeans property name qualified by
        //                                              the class (or as provided on the annotation)
        // -- When annotation is applied on a field:    name is the field name qualified by the class
        //                                              (or as provided on the annotation)
        //------------------------------------------------------------------------------------------
        String resourceName = annotation.name();
        if ( resourceName.equals("") ) {
            if ( method != null ) {
                StringBuilder stringBuilder = new StringBuilder( method.getName().substring(3) );
                stringBuilder.setCharAt( 0,Character.toLowerCase(stringBuilder.charAt(0)) );
                resourceName = method.getDeclaringClass().getName() + "/" + stringBuilder.toString();
            }
            else if ( field != null ) {
                resourceName = field.getDeclaringClass().getName() + "/" + field.getName();
            }
        }

        //------------------------------------------------------------------------------------------
        // Resource type:
        // -- When annotation is applied on a class:    type must be provided (cannot be inferred)
        // -- When annotation is applied on a method:   type is the JavaBeans property type (or as
        //                                              provided on the annotation)
        // -- When annotation is applied on a field:    type is the field type (or as provided on the
        //                                              annotation)
        //------------------------------------------------------------------------------------------
        String resourceType = annotation.name();
        if ( resourceType.equals("") ) {
            if ( method != null ) {
                resourceType = method.getParameterTypes()[0].getCanonicalName();
            }
            else if ( field != null ) {
                resourceType = field.getType().toString();
            }
        }

        //------------------------------------------------------------------------------------------
        // Method name (for setter-based injection) must follow JavaBeans conventions:
        // -- Must start with "set"
        // -- Have one parameter
        // -- Return void
        //------------------------------------------------------------------------------------------
        String injectionJavaType = "";
        String injectionClass  = null;
        if ( method != null ) {
            injectionJavaType = method.getName().substring(3);
            StringBuilder stringBuilder = new StringBuilder( injectionJavaType );
            stringBuilder.setCharAt( 0,Character.toLowerCase(stringBuilder.charAt(0)) );
            injectionJavaType = stringBuilder.toString();
            injectionClass = method.getDeclaringClass().getName();
        }
        else if ( field != null ) {
            injectionJavaType = field.getName();
            injectionClass = field.getDeclaringClass().getName();
        }

        //------------------------------------------------------------------------------------------
        // 1. <env-entry>
        //------------------------------------------------------------------------------------------
        if ( annotation.type().getCanonicalName().equals( "java.lang.String" )      ||
             annotation.type().getCanonicalName().equals( "java.lang.Character" )   ||
             annotation.type().getCanonicalName().equals( "java.lang.Integer" )     ||
             annotation.type().getCanonicalName().equals( "java.lang.Boolean" )     ||
             annotation.type().getCanonicalName().equals( "java.lang.Double" )      ||
             annotation.type().getCanonicalName().equals( "java.lang.Byte" )        ||
             annotation.type().getCanonicalName().equals( "java.lang.Short" )       ||
             annotation.type().getCanonicalName().equals( "java.lang.Long" )        ||
             annotation.type().getCanonicalName().equals( "java.lang.Float" ) ) {

            boolean exists = false;
            EnvEntryType[] envEntries = webApp.getEnvEntryArray();
            for ( EnvEntryType envEntry : envEntries ) {
                if ( envEntry.getEnvEntryName().equals(resourceName) ) {
                    exists = true;
                    break;
                }
            }
            if ( !exists ) {
                try {

                    // Doesn't exist in deployment descriptor -- add new
                    EnvEntryType envEntry = webApp.addNewEnvEntry();

                    //------------------------------------------------------------------------------
                    // <env-entry> required elements:
                    //------------------------------------------------------------------------------

                    // env-entry-name
                    JndiNameType envEntryName = JndiNameType.Factory.newInstance();
                    envEntryName.setStringValue( resourceName );
                    envEntry.setEnvEntryName( envEntryName );

                    if ( !resourceType.equals("") ) {
                        // env-entry-type
                        EnvEntryTypeValuesType envEntryType = EnvEntryTypeValuesType.Factory.newInstance();
                        envEntryType.setStringValue( resourceType );
                        envEntry.setEnvEntryType( envEntryType );
                    }
                    else if ( !injectionJavaType.equals("") ) {
                        // injectionTarget
                        InjectionTargetType injectionTarget = InjectionTargetType.Factory.newInstance();
                        FullyQualifiedClassType qualifiedClass = FullyQualifiedClassType.Factory.newInstance();
                        JavaIdentifierType javaType = JavaIdentifierType.Factory.newInstance();
                        qualifiedClass.setStringValue( injectionClass );
                        javaType.setStringValue( injectionJavaType );
                        injectionTarget.setInjectionTargetClass( qualifiedClass );
                        injectionTarget.setInjectionTargetName( javaType );
                        int arraySize = envEntry.sizeOfInjectionTargetArray();
                        envEntry.insertNewInjectionTarget( arraySize );
                        envEntry.setInjectionTargetArray( arraySize, injectionTarget );
                    }

                    // env-entry-value
                    XsdStringType value = XsdStringType.Factory.newInstance();
                    value.setStringValue( annotation.mappedName() );
                    envEntry.setEnvEntryValue( value);

                    //------------------------------------------------------------------------------
                    // <env-entry> optional elements:
                    //------------------------------------------------------------------------------

                    // description
                    String descriptionAnnotation = annotation.description();
                    if ( descriptionAnnotation.length() > 0 ) {
                        DescriptionType description = DescriptionType.Factory.newInstance();
                        description.setStringValue( descriptionAnnotation );
                        int arraySize = envEntry.sizeOfDescriptionArray();
                        envEntry.insertNewDescription( arraySize );
                        envEntry.setDescriptionArray( arraySize,description );
                    }

                }
                catch ( Exception anyException ) {
                    log.debug( "ResourceAnnotationHelper: Exception caught while processing <env-entry>" );
                    anyException.printStackTrace();
                }
            }
        }

        //------------------------------------------------------------------------------------------
        // 2. <service-ref>
        //------------------------------------------------------------------------------------------
        else if ( annotation.type().getCanonicalName().equals("javax.xml.rpc.Service") ) {

            boolean exists = false;
            ServiceRefType[] serviceRefs = webApp.getServiceRefArray();
            for ( ServiceRefType serviceRef : serviceRefs ) {
                if ( serviceRef.getServiceRefName().equals(resourceName) ) {
                    exists = true;
                    break;
                }
            }
            if ( !exists ) {
                try {

                    // Doesn't exist in deployment descriptor -- add new
                    ServiceRefType serviceRef = webApp.addNewServiceRef();

                    //------------------------------------------------------------------------------
                    // <service-ref> required elements:
                    //------------------------------------------------------------------------------

                    // service-ref-name
                    JndiNameType serviceRefName = JndiNameType.Factory.newInstance();
                    serviceRefName.setStringValue( resourceName );
                    serviceRef.setServiceRefName( serviceRefName );

                    if ( !resourceType.equals("") ) {
                        // service-ref-type
                        FullyQualifiedClassType qualifiedClass = FullyQualifiedClassType.Factory.newInstance();
                        qualifiedClass.setStringValue( resourceType );
                        serviceRef.setServiceInterface( qualifiedClass );
                    }
                    else if ( !injectionJavaType.equals("") ) {
                        // injectionTarget
                        InjectionTargetType injectionTarget = InjectionTargetType.Factory.newInstance();
                        FullyQualifiedClassType qualifiedClass = FullyQualifiedClassType.Factory.newInstance();
                        JavaIdentifierType javaType = JavaIdentifierType.Factory.newInstance();
                        qualifiedClass.setStringValue( injectionClass );
                        javaType.setStringValue( injectionJavaType );
                        injectionTarget.setInjectionTargetClass( qualifiedClass );
                        injectionTarget.setInjectionTargetName( javaType );
                        int arraySize = serviceRef.sizeOfInjectionTargetArray();
                        serviceRef.insertNewInjectionTarget( arraySize );
                        serviceRef.setInjectionTargetArray( arraySize, injectionTarget );
                    }

                    //------------------------------------------------------------------------------
                    // <service-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // description
                    String descriptionAnnotation = annotation.description();
                    if ( descriptionAnnotation.length() > 0 ) {
                        DescriptionType description = DescriptionType.Factory.newInstance();
                        description.setStringValue( descriptionAnnotation );
                        int arraySize = serviceRef.sizeOfDescriptionArray();
                        serviceRef.insertNewDescription( arraySize );
                        serviceRef.setDescriptionArray( arraySize,description );
                    }

                    // WSDL document location
                    String documentAnnotation = annotation.mappedName();
                    if ( documentAnnotation.length() > 0 ) {
                        XsdAnyURIType wsdlFile = XsdAnyURIType.Factory.newInstance();
                        wsdlFile.setStringValue( annotation.mappedName() );
                        serviceRef.setWsdlFile( wsdlFile );
                    }

                }
                catch ( Exception anyException ) {
                    log.debug( "ResourceAnnotationHelper: Exception caught while processing <service-ref>" );
                    anyException.printStackTrace();
                }
            }
        }

        //------------------------------------------------------------------------------------------
        // 3. <resource-ref>
        //------------------------------------------------------------------------------------------
        else if ( annotation.type().getCanonicalName().equals("javax.sql.DataSource")                   ||
                  annotation.type().getCanonicalName().equals("javax.jms.ConnectionFactory")            ||
                  annotation.type().getCanonicalName().equals("javax.jms.QueueConnectionFactory")       ||
                  annotation.type().getCanonicalName().equals("javax.jms.TopicConnectionFactory")       ||
                  annotation.type().getCanonicalName().equals("javax.mail.Session")                     ||
                  annotation.type().getCanonicalName().equals("java.net.URL")                           ||
                  annotation.type().getCanonicalName().equals("javax.resource.cci.ConnectionFactory")   ||
                  annotation.type().getCanonicalName().equals("org.omg.CORBA_2_3.ORB")                  ||
                  annotation.type().getCanonicalName().endsWith("ConnectionFactory") ) {

            boolean exists = false;
            ResourceRefType[] resourceRefs = webApp.getResourceRefArray();
            for ( ResourceRefType resourceRef : resourceRefs ) {
                if ( resourceRef.getResRefName().equals(resourceName) ) {
                    exists = true;
                    break;
                }
            }
            if ( !exists ) {
                try {

                    // Doesn't exist in deployment descriptor -- add new
                    ResourceRefType resourceRef = webApp.addNewResourceRef();

                    //------------------------------------------------------------------------------
                    // <resource-ref> required elements:
                    //------------------------------------------------------------------------------

                    // resource-ref-name
                    JndiNameType resourceRefName = JndiNameType.Factory.newInstance();
                    resourceRefName.setStringValue( resourceName );
                    resourceRef.setResRefName( resourceRefName );

                    if ( !resourceType.equals("") ) {
                        // resource-ref-type
                        FullyQualifiedClassType qualifiedClass = FullyQualifiedClassType.Factory.newInstance();
                        qualifiedClass.setStringValue( resourceType );
                        resourceRef.setResType( qualifiedClass );
                    }
                    else if ( !injectionJavaType.equals("") ) {
                        // injectionTarget
                        InjectionTargetType injectionTarget = InjectionTargetType.Factory.newInstance();
                        FullyQualifiedClassType qualifiedClass = FullyQualifiedClassType.Factory.newInstance();
                        JavaIdentifierType javaType = JavaIdentifierType.Factory.newInstance();
                        qualifiedClass.setStringValue( injectionClass );
                        javaType.setStringValue( injectionJavaType );
                        injectionTarget.setInjectionTargetClass( qualifiedClass );
                        injectionTarget.setInjectionTargetName( javaType );
                        int arraySize = resourceRef.sizeOfInjectionTargetArray();
                        resourceRef.insertNewInjectionTarget( arraySize );
                        resourceRef.setInjectionTargetArray( arraySize, injectionTarget );
                    }

                    //------------------------------------------------------------------------------
                    // <resource-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // description
                    String descriptionAnnotation = annotation.description();
                    if ( descriptionAnnotation.length() > 0 ) {
                        DescriptionType description = DescriptionType.Factory.newInstance();
                        description.setStringValue( descriptionAnnotation );
                        int arraySize = resourceRef.sizeOfDescriptionArray();
                        resourceRef.insertNewDescription( arraySize );
                        resourceRef.setDescriptionArray( arraySize,description );
                    }

                    // authentication
                    ResAuthType resAuth = ResAuthType.Factory.newInstance();
                    if ( annotation.authenticationType() == Resource.AuthenticationType.CONTAINER ) {
                        resAuth.setStringValue( "Container" );
                    }
                    else if ( annotation.authenticationType() == Resource.AuthenticationType.APPLICATION ) {
                        resAuth.setStringValue( "Application" );
                    }
                    resourceRef.setResAuth( resAuth );

                    // sharing scope
                    ResSharingScopeType resScope = ResSharingScopeType.Factory.newInstance();
                    resScope.setStringValue( annotation.shareable() ? "Shareable" : "Unshareable" );
                    resourceRef.setResSharingScope( resScope );

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if ( mappdedNameAnnotation.length() > 0 ) {
                        XsdStringType mappedName = XsdStringType.Factory.newInstance();
                        mappedName.setStringValue( mappdedNameAnnotation );
                        resourceRef.setMappedName( mappedName );
                    }

                }
                catch ( Exception anyException ) {
                    log.debug( "ResourceAnnotationHelper: Exception caught while processing <resource-ref>" );
                    anyException.printStackTrace();
                }
            }
        }

        //------------------------------------------------------------------------------------------
        // 4. <message-destination-ref>
        //------------------------------------------------------------------------------------------
        else if ( annotation.type().getCanonicalName().equals("javax.jms.Queue")    ||
                  annotation.type().getCanonicalName().equals("javax.jms.Topic") ) {

            boolean exists = false;
            MessageDestinationRefType[] messageDestinationRefs = webApp.getMessageDestinationRefArray();
            for ( MessageDestinationRefType messageDestinationRef : messageDestinationRefs ) {
                if ( messageDestinationRef.getMessageDestinationRefName().equals(resourceName) ) {
                    exists = true;
                    break;
                }
            }
            if ( !exists ) {
                try {

                    // Doesn't exist in deployment descriptor -- add new
                    MessageDestinationRefType messageDestinationRef = webApp.addNewMessageDestinationRef();

                    //------------------------------------------------------------------------------
                    // <message-destination-ref> required elements:
                    //------------------------------------------------------------------------------

                    // message-destination-ref-name
                    JndiNameType messageDestinationRefName = JndiNameType.Factory.newInstance();
                    messageDestinationRefName.setStringValue( resourceName );
                    messageDestinationRef.setMessageDestinationRefName( messageDestinationRefName );

                    if ( !resourceType.equals("") ) {
                        // message-destination-ref-type
                        MessageDestinationTypeType msgDestType = MessageDestinationTypeType.Factory.newInstance();
                        msgDestType.setStringValue( resourceType );
                        messageDestinationRef.setMessageDestinationType( msgDestType );
                    }
                    else if ( !injectionJavaType.equals("") ) {
                        // injectionTarget
                        InjectionTargetType injectionTarget = InjectionTargetType.Factory.newInstance();
                        FullyQualifiedClassType qualifiedClass = FullyQualifiedClassType.Factory.newInstance();
                        JavaIdentifierType javaType = JavaIdentifierType.Factory.newInstance();
                        qualifiedClass.setStringValue( injectionClass );
                        javaType.setStringValue( injectionJavaType );
                        injectionTarget.setInjectionTargetClass( qualifiedClass );
                        injectionTarget.setInjectionTargetName( javaType );
                        int arraySize = messageDestinationRef.sizeOfInjectionTargetArray();
                        messageDestinationRef.insertNewInjectionTarget( arraySize );
                        messageDestinationRef.setInjectionTargetArray( arraySize, injectionTarget );
                    }

                    //------------------------------------------------------------------------------
                    // <message-destination-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // description
                    String descriptionAnnotation = annotation.description();
                    if ( descriptionAnnotation.length() > 0 ) {
                        DescriptionType description = DescriptionType.Factory.newInstance();
                        description.setStringValue( descriptionAnnotation );
                        int arraySize = messageDestinationRef.sizeOfDescriptionArray();
                        messageDestinationRef.insertNewDescription( arraySize );
                        messageDestinationRef.setDescriptionArray( arraySize,description );
                    }

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if ( mappdedNameAnnotation.length() > 0 ) {
                        XsdStringType mappedName = XsdStringType.Factory.newInstance();
                        mappedName.setStringValue( mappdedNameAnnotation );
                        messageDestinationRef.setMappedName( mappedName );
                    }

                }
                catch ( Exception anyException ) {
                    log.debug( "ResourceAnnotationHelper: Exception caught while processing <message-destination-ref>" );
                    anyException.printStackTrace();
                }
            }
        }

        //------------------------------------------------------------------------------------------
        // 5. Everything else must be a <resource-env-ref>
        //------------------------------------------------------------------------------------------
        else if ( annotation.type().getCanonicalName().equals("javax.resource.cci.InteractionSpec") ||
                  annotation.type().getCanonicalName().equals("javax.transaction.UserTransaction")  || true ) {

            boolean exists = false;
            ResourceEnvRefType[] resourceEnvRefs = webApp.getResourceEnvRefArray();
            for ( ResourceEnvRefType resourceEnvRef : resourceEnvRefs ) {
                if ( resourceEnvRef.getResourceEnvRefName().equals(resourceName) ) {
                    exists = true;
                    break;
                }
            }
            if ( !exists ) {
                try {

                    // Doesn't exist in deployment descriptor -- add new
                    ResourceEnvRefType resourceEnvRef = webApp.addNewResourceEnvRef();

                    //------------------------------------------------------------------------------
                    // <resource-env-ref> required elements:
                    //------------------------------------------------------------------------------

                    // resource-env-ref-name
                    JndiNameType resourceEnvRefName = JndiNameType.Factory.newInstance();
                    resourceEnvRefName.setStringValue( resourceName );
                    resourceEnvRef.setResourceEnvRefName( resourceEnvRefName );

                    if ( !resourceType.equals("") ) {
                        // resource-env-ref-type
                        FullyQualifiedClassType classType = FullyQualifiedClassType.Factory.newInstance();
                        classType.setStringValue( resourceType );
                        resourceEnvRef.setResourceEnvRefType( classType );
                    }
                    else if ( !injectionJavaType.equals("") ) {
                        // injectionTarget
                        InjectionTargetType injectionTarget = InjectionTargetType.Factory.newInstance();
                        FullyQualifiedClassType qualifiedClass = FullyQualifiedClassType.Factory.newInstance();
                        JavaIdentifierType javaType = JavaIdentifierType.Factory.newInstance();
                        qualifiedClass.setStringValue( injectionClass );
                        javaType.setStringValue( injectionJavaType );
                        injectionTarget.setInjectionTargetClass( qualifiedClass );
                        injectionTarget.setInjectionTargetName( javaType );
                        int arraySize = resourceEnvRef.sizeOfInjectionTargetArray();
                        resourceEnvRef.insertNewInjectionTarget( arraySize );
                        resourceEnvRef.setInjectionTargetArray( arraySize, injectionTarget );
                    }

                    //------------------------------------------------------------------------------
                    // <resource-env-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // description
                    String descriptionAnnotation = annotation.description();
                    if ( descriptionAnnotation.length() > 0 ) {
                        DescriptionType description = DescriptionType.Factory.newInstance();
                        description.setStringValue( descriptionAnnotation );
                        int arraySize = resourceEnvRef.sizeOfDescriptionArray();
                        resourceEnvRef.insertNewDescription( arraySize );
                        resourceEnvRef.setDescriptionArray( arraySize,description );
                    }

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if ( mappdedNameAnnotation.length() > 0 ) {
                        XsdStringType mappedName = XsdStringType.Factory.newInstance();
                        mappedName.setStringValue( mappdedNameAnnotation );
                        resourceEnvRef.setMappedName( mappedName );
                    }

                }
                catch ( Exception anyException ) {
                    log.debug( "ResourceAnnotationHelper: Exception caught while processing <resource-env-ref>" );
                    anyException.printStackTrace();
                }
            }
        }
        log.debug( "addResource(): Exit" );
    }


    /**
     * Validate deployment descriptor
     *
     * @param webApp
     * @exception Exception  thrown if deployment descriptor cannot be parsed
     */
    private static void validateDD( WebAppType webApp ) throws Exception {
        log.debug( "validateDD( " + webApp.toString() + " ): Entry" );

        XmlBeansUtil.parse( webApp.toString() );

        log.debug( "validateDD(): Exit" );
    }
}
