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

import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.Local;
import javax.ejb.Remote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.xbeans.javaee.DescriptionType;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.JavaIdentifierType;
import org.apache.geronimo.xbeans.javaee.EjbLinkType;
import org.apache.geronimo.xbeans.javaee.EjbLocalRefType;
import org.apache.geronimo.xbeans.javaee.EjbRefNameType;
import org.apache.geronimo.xbeans.javaee.EjbRefType;
import org.apache.geronimo.xbeans.javaee.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee.LocalType;
import org.apache.geronimo.xbeans.javaee.RemoteType;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.geronimo.xbeans.javaee.XsdStringType;
import org.apache.xbean.finder.ClassFinder;

/**
 * Static helper class used to encapsulate all the functions related to the translation of
 *
 * @EJB and @EBJs annotations to deployment descriptor tags. The EJBAnnotationHelper class can be
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
public final class EJBAnnotationHelper {

    // Private instance variables
    private static final Log log = LogFactory.getLog( EJBAnnotationHelper.class );

    // Private constructor to prevent instantiation
    private EJBAnnotationHelper() {
    }


    /**
     * Determine if there are any @EJB, @EJBs annotations present
     *
     * @return true or false
     */
    public static boolean annotationsPresent( ClassFinder classFinder ) {
        if ( classFinder.isAnnotationPresent(EJB.class) ) return true;
        if ( classFinder.isAnnotationPresent(EJBs.class) ) return true;
        return false;
    }


    /**
     * Process the @EJB, @EJBs annotations
     *
     * @return Updated deployment descriptor
     * @exception Exception if parsing or validation error
     */
    public static WebAppType processAnnotations( WebAppType webApp, ClassFinder classFinder ) throws Exception {
        processEJBs( webApp,classFinder );
        processEJB( webApp,classFinder );
        return webApp;
    }


    /**
     * Process @EJB annotations
     *
     * @param webApp
     * @param classFinder
     * @exception Exception
     */
    private static void processEJB( WebAppType webApp, ClassFinder classFinder ) throws Exception {
        log.debug( "processEJB(): Entry" );

        // Save the EJB lists
        List<Class>  classesWithEJB = classFinder.findAnnotatedClasses( EJB.class );
        List<Method> methodsWithEJB = classFinder.findAnnotatedMethods( EJB.class );
        List<Field>  fieldsWithEJB  = classFinder.findAnnotatedFields ( EJB.class );

        // Class-level EJB
        for ( Class cls : classesWithEJB ) {
            EJB ejb = (EJB)cls.getAnnotation( EJB.class );
            if ( ejb != null ) {
                addEJB( webApp, ejb, cls, null, null );
            }
        }

        // Method-level EJB
        for ( Method method : methodsWithEJB ) {
            EJB ejb = (EJB)method.getAnnotation( EJB.class );
            if ( ejb != null ) {
                addEJB( webApp, ejb, null, method, null );
            }
        }

        // Field-level EJB
        for ( Field field : fieldsWithEJB ) {
            EJB ejb = (EJB)field.getAnnotation( EJB.class );
            if ( ejb != null ) {
                addEJB( webApp, ejb, null, null, field );
            }
        }

        // Validate deployment descriptor to ensure it's still okay
        validateDD( webApp );

        log.debug( "processEJB(): Exit: webApp: " + webApp.toString() );
    }


    /**
     * Process @EJBs annotations
     *
     * @param webApp
     * @param classFinder
     * @exception Exception
     */
    private static void processEJBs( WebAppType webApp, ClassFinder classFinder ) throws Exception {
        log.debug( "processEJBs(): Entry" );

        // Save the EJBs list
        List<Class> classesWithEJBs = classFinder.findAnnotatedClasses( EJBs.class );

        // Class-level EJBs
        List<EJB> ejbList = new ArrayList<EJB>();
        for ( Class cls : classesWithEJBs ) {
            EJBs ejbs = (EJBs) cls.getAnnotation( EJBs.class );
            if ( ejbs != null ) {
                ejbList.addAll( Arrays.asList(ejbs.value()) );
            }
            for ( EJB ejb : ejbList ) {
                addEJB( webApp, ejb, cls, null, null );
            }
            ejbList.clear();
        }

        log.debug( "processEJBs(): Exit" );
    }


    /**
     * Add @EJB and @EJBs annotations to the deployment descriptor. XMLBeans are used to read and
     * manipulate the deployment descriptor as necessary. The EJB annotation(s) will be converted to
     * one of the following deployment descriptors:
     *
     * <ol>
     *      <li><ejb-ref>
     *      <li><ejb-local-ref>
     * </ol>
     *
     * <p><strong>Note(s):</strong>
     * <ul>
     *      <li>The deployment descriptor is the authoritative source so this method ensures that
     *      existing elements in it are not overwritten by annoations
     * </ul>
     *
     * @param webApp
     * @param annotation @EJB annotation
     * @param cls        Class name with the @EJB annoation
     * @param method     Method name with the @EJB annoation
     * @param field      Field name with the @EJB annoation
     */
    private static void addEJB( WebAppType webApp, EJB annotation, Class cls, Method method, Field field ) {
        log.debug( "addEJB( " + webApp.toString() + ","          + '\n' +
                  annotation.name() + ","                       + '\n' +
                  (cls!=null?cls.getName():null) + ","          + '\n' +
                  (method!=null?method.getName():null) + ","    + '\n' +
                  (field!=null?field.getName():null) + " ): Entry" );

        // First determine if the interface is "Local" or "Remote"
        boolean localFlag = true;
        Class interfce = annotation.beanInterface();
        if ( interfce.equals(Object.class) ) {
            if ( method != null ) {
                interfce = method.getParameterTypes()[0];
            }
            else if ( field != null ) {
                interfce = field.getType();
            }
            else {
                interfce = null;
            }
        }

        // Just in case local and/or remote homes are still being implemented (even though
        // they are optional in EJB 3.0)
        if ( interfce != null && !interfce.equals(Object.class) ) {
            if ( EJBHome.class.isAssignableFrom(interfce) ) {
                localFlag = false;
            }
            else if ( EJBLocalHome.class.isAssignableFrom(interfce) ) {
                localFlag = true;
            }
            else {
                if ( interfce.getAnnotation(Local.class) != null ) {
                    localFlag = true;
                }
                else if ( interfce.getAnnotation(Remote.class) != null ) {
                    localFlag = false;
                }
            }
        }

        //------------------------------------------------------------------------------------------
        // 1. <ejb-local-ref>
        //------------------------------------------------------------------------------------------
        if ( localFlag ) {

            log.debug( "addEJB(): <ejb-local-ref> found");

            String localRefName = annotation.name();
            if ( localRefName.equals("") ) {
                if ( method != null ) {
                    localRefName = method.getDeclaringClass().getName() + "/" + method.getName().substring(3);  // method should start with "set"
                }
                else if ( field != null ) {
                    localRefName = field.getDeclaringClass().getName() + "/" + field.getName();
                }
            }

            boolean exists = false;
            EjbLocalRefType[] ejbLocalRefEntries = webApp.getEjbLocalRefArray();
            for ( EjbLocalRefType ejbLocalRefEntry : ejbLocalRefEntries ) {
                if ( ejbLocalRefEntry.getEjbRefName().getStringValue().equals( localRefName ) ) {
                    exists = true;
                    break;
                }
            }
            if ( !exists ) {
                try {

                    // Doesn't exist in deployment descriptor -- add new
                    EjbLocalRefType ejbLocalRef = webApp.addNewEjbLocalRef();

                    //------------------------------------------------------------------------------
                    // <ejb-local-ref> required elements:
                    //------------------------------------------------------------------------------

                    // ejb-ref-name
                    EjbRefNameType ejbRefName = EjbRefNameType.Factory.newInstance();
                    ejbRefName.setStringValue( localRefName );
                    ejbLocalRef.setEjbRefName( ejbRefName );

                    //------------------------------------------------------------------------------
                    // <ejb-local-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // local
                    String localAnnotation = interfce.getName();
                    if ( localAnnotation.length() > 0 ) {
                        LocalType local = LocalType.Factory.newInstance();
                        local.setStringValue( localAnnotation );
                        ejbLocalRef.setLocal( local );
                    }

                    // ejb-link
                    String beanName = annotation.beanName();
                    if ( beanName.length() > 0 ) {
                        EjbLinkType ejbLink = EjbLinkType.Factory.newInstance();
                        ejbLink.setStringValue( beanName );
                        ejbLocalRef.setEjbLink( ejbLink );
                    }

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if ( mappdedNameAnnotation.length() > 0 ) {
                        XsdStringType mappedName = XsdStringType.Factory.newInstance();
                        mappedName.setStringValue( mappdedNameAnnotation );
                        ejbLocalRef.setMappedName( mappedName );
                    }

                    // description
                    String descriptionAnnotation = annotation.description();
                    if ( descriptionAnnotation.length() > 0 ) {
                        DescriptionType description = DescriptionType.Factory.newInstance();
                        description.setStringValue( descriptionAnnotation );
                        int arraySize = ejbLocalRef.sizeOfDescriptionArray();
                        ejbLocalRef.insertNewDescription( arraySize );
                        ejbLocalRef.setDescriptionArray( arraySize,description );
                    }

                    // injectionTarget
                    InjectionTargetType injectionTarget = InjectionTargetType.Factory.newInstance();
                    FullyQualifiedClassType qualifiedClass = FullyQualifiedClassType.Factory.newInstance();
                    JavaIdentifierType javaType = JavaIdentifierType.Factory.newInstance();
                    if ( method != null ) {
                        qualifiedClass.setStringValue( method.getDeclaringClass().getName() );
                        javaType.setStringValue( method.getName().substring(3) );   // method should start with "set"
                        injectionTarget.setInjectionTargetClass( qualifiedClass );
                        injectionTarget.setInjectionTargetName( javaType );
                        int arraySize = ejbLocalRef.sizeOfInjectionTargetArray();
                        ejbLocalRef.insertNewInjectionTarget( arraySize );
                        ejbLocalRef.setInjectionTargetArray( arraySize,injectionTarget );
                    }
                    else if ( field !=null ) {
                        qualifiedClass.setStringValue( field.getDeclaringClass().getName() );
                        javaType.setStringValue( field.getName() );
                        injectionTarget.setInjectionTargetClass( qualifiedClass );
                        injectionTarget.setInjectionTargetName( javaType );
                        int arraySize = ejbLocalRef.sizeOfInjectionTargetArray();
                        ejbLocalRef.insertNewInjectionTarget( arraySize );
                        ejbLocalRef.setInjectionTargetArray( arraySize,injectionTarget );
                    }

                }
                catch ( Exception anyException ) {
                    log.debug( "EJBAnnotationHelper: Exception caught while processing <ejb-local-ref>" );
                    anyException.printStackTrace();
                }
            }
        }                                                                           // end if local
        else {                                                                      // else remote

            //--------------------------------------------------------------------------------------
            // 2. <ejb-ref>
            //--------------------------------------------------------------------------------------

            log.debug( "addEJB(): <ejb-ref> found");

            String remoteRefName = annotation.name();
            if ( remoteRefName.equals("") ) {
                if ( method != null ) {
                    remoteRefName = method.getDeclaringClass().getName() + "/" + method.getName().substring(3); // method should start with "set"
                }
                else if ( field != null ) {
                    remoteRefName = field.getDeclaringClass().getName() + "/" + field.getName();
                }
            }

            boolean exists = false;
            EjbRefType[] ejbRefEntries = webApp.getEjbRefArray();
            for ( EjbRefType ejbRefEntry : ejbRefEntries ) {
                if ( ejbRefEntry.getEjbRefName().getStringValue().equals( remoteRefName ) ) {
                    exists = true;
                    break;
                }
            }
            if ( !exists ) {
                try {

                    // Doesn't exist in deployment descriptor -- add new
                    EjbRefType ejbRef = webApp.addNewEjbRef();

                    //------------------------------------------------------------------------------
                    // <ejb-ref> required elements:
                    //------------------------------------------------------------------------------

                    // ejb-ref-name
                    EjbRefNameType ejbRefName = EjbRefNameType.Factory.newInstance();
                    ejbRefName.setStringValue( remoteRefName );
                    ejbRef.setEjbRefName( ejbRefName );

                    //------------------------------------------------------------------------------
                    // <ejb-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // remote
                    String remoteAnnotation = interfce.getName();
                    if ( remoteAnnotation.length() > 0 ) {
                        RemoteType remote = RemoteType.Factory.newInstance();
                        remote.setStringValue( remoteAnnotation );
                        ejbRef.setRemote( remote );
                    }

                    // ejb-link
                    String beanName = annotation.beanName();
                    if ( beanName.length() > 0 ) {
                        EjbLinkType ejbLink = EjbLinkType.Factory.newInstance();
                        ejbLink.setStringValue( beanName );
                        ejbRef.setEjbLink( ejbLink );
                    }

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if ( mappdedNameAnnotation.length() > 0 ) {
                        XsdStringType mappedName = XsdStringType.Factory.newInstance();
                        mappedName.setStringValue( mappdedNameAnnotation );
                        ejbRef.setMappedName( mappedName );
                    }

                    // description
                    String descriptionAnnotation = annotation.description();
                    if ( descriptionAnnotation.length() > 0 ) {
                        DescriptionType description = DescriptionType.Factory.newInstance();
                        description.setStringValue( descriptionAnnotation );
                        int arraySize = ejbRef.sizeOfDescriptionArray();
                        ejbRef.insertNewDescription( arraySize );
                        ejbRef.setDescriptionArray( arraySize,description );
                    }

                    // injectionTarget
                    InjectionTargetType injectionTarget = InjectionTargetType.Factory.newInstance();
                    FullyQualifiedClassType qualifiedClass = FullyQualifiedClassType.Factory.newInstance();
                    JavaIdentifierType javaType = JavaIdentifierType.Factory.newInstance();
                    if ( method != null ) {
                        qualifiedClass.setStringValue( method.getDeclaringClass().getName() );
                        javaType.setStringValue( method.getName().substring(3) );   // method should start with "set"
                        injectionTarget.setInjectionTargetClass( qualifiedClass );
                        injectionTarget.setInjectionTargetName( javaType );
                        int arraySize = ejbRef.sizeOfInjectionTargetArray();
                        ejbRef.insertNewInjectionTarget( arraySize );
                        ejbRef.setInjectionTargetArray( arraySize, injectionTarget );
                    }
                    else if ( field !=null ) {
                        qualifiedClass.setStringValue( field.getDeclaringClass().getName() );
                        javaType.setStringValue( field.getName() );
                        injectionTarget.setInjectionTargetClass( qualifiedClass );
                        injectionTarget.setInjectionTargetName( javaType );
                        int arraySize = ejbRef.sizeOfInjectionTargetArray();
                        ejbRef.insertNewInjectionTarget( arraySize );
                        ejbRef.setInjectionTargetArray( arraySize, injectionTarget );
                    }

                }
                catch ( Exception anyException ) {
                    log.debug( "EJBAnnotationHelper: Exception caught while <processing ejb-ref>" );
                    anyException.printStackTrace();
                }
            }
        }
        log.debug( "addEJB(): Exit" );
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
