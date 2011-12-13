/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.myfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.visit.VisitContextFactory;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.ExternalContextFactory;
import javax.faces.context.FacesContextFactory;
import javax.faces.context.PartialViewContextFactory;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.render.RenderKitFactory;
import javax.faces.view.ViewDeclarationLanguageFactory;
import javax.faces.view.facelets.TagHandlerDelegateFactory;

import org.apache.geronimo.web.WebApplicationName;
import org.apache.myfaces.spi.FactoryFinderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GeronimoFactoryFinderProvider is forked from javax.faces.FactoryFinder in MyFaces package, including the changes below :
 * a. Use WebApplicationIdentity as key to hold different map for each web application
 * b. Remove the codes of FactoryFinderProviderFactory
 * @version $Rev$ $Date$
 */

public class GeronimoFactoryFinderProvider implements FactoryFinderProvider {

    private static final Logger logger = LoggerFactory.getLogger(GeronimoFactoryFinderProvider.class);

    public static final String APPLICATION_FACTORY = "javax.faces.application.ApplicationFactory";

    public static final String EXCEPTION_HANDLER_FACTORY = "javax.faces.context.ExceptionHandlerFactory";

    public static final String EXTERNAL_CONTEXT_FACTORY = "javax.faces.context.ExternalContextFactory";

    public static final String FACES_CONTEXT_FACTORY = "javax.faces.context.FacesContextFactory";

    public static final String LIFECYCLE_FACTORY = "javax.faces.lifecycle.LifecycleFactory";

    public static final String PARTIAL_VIEW_CONTEXT_FACTORY = "javax.faces.context.PartialViewContextFactory";

    public static final String RENDER_KIT_FACTORY = "javax.faces.render.RenderKitFactory";

    public static final String TAG_HANDLER_DELEGATE_FACTORY = "javax.faces.view.facelets.TagHandlerDelegateFactory";

    public static final String VIEW_DECLARATION_LANGUAGE_FACTORY = "javax.faces.view.ViewDeclarationLanguageFactory";

    public static final String VISIT_CONTEXT_FACTORY = "javax.faces.component.visit.VisitContextFactory";

    /**
     * used as a monitor for itself and _factories. Maps in this map are used as monitors for themselves and the
     * corresponding maps in _factories.
     */
    private Map<String, Map<String, List<String>>> _registeredFactoryNames = new HashMap<String, Map<String, List<String>>>();

    private Map<String, Map<String, Object>> _factories = new HashMap<String, Map<String, Object>>();

    private static final Set<String> VALID_FACTORY_NAMES = new HashSet<String>();

    private static final Map<String, Class<?>> ABSTRACT_FACTORY_CLASSES = new HashMap<String, Class<?>>();

    private static final ClassLoader myFacesClassLoader;

    static {
        VALID_FACTORY_NAMES.add(APPLICATION_FACTORY);
        VALID_FACTORY_NAMES.add(EXCEPTION_HANDLER_FACTORY);
        VALID_FACTORY_NAMES.add(EXTERNAL_CONTEXT_FACTORY);
        VALID_FACTORY_NAMES.add(FACES_CONTEXT_FACTORY);
        VALID_FACTORY_NAMES.add(LIFECYCLE_FACTORY);
        VALID_FACTORY_NAMES.add(PARTIAL_VIEW_CONTEXT_FACTORY);
        VALID_FACTORY_NAMES.add(RENDER_KIT_FACTORY);
        VALID_FACTORY_NAMES.add(TAG_HANDLER_DELEGATE_FACTORY);
        VALID_FACTORY_NAMES.add(VIEW_DECLARATION_LANGUAGE_FACTORY);
        VALID_FACTORY_NAMES.add(VISIT_CONTEXT_FACTORY);

        ABSTRACT_FACTORY_CLASSES.put(APPLICATION_FACTORY, ApplicationFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(EXCEPTION_HANDLER_FACTORY, ExceptionHandlerFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(EXTERNAL_CONTEXT_FACTORY, ExternalContextFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(FACES_CONTEXT_FACTORY, FacesContextFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(LIFECYCLE_FACTORY, LifecycleFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(PARTIAL_VIEW_CONTEXT_FACTORY, PartialViewContextFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(RENDER_KIT_FACTORY, RenderKitFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(TAG_HANDLER_DELEGATE_FACTORY, TagHandlerDelegateFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(VIEW_DECLARATION_LANGUAGE_FACTORY, ViewDeclarationLanguageFactory.class);
        ABSTRACT_FACTORY_CLASSES.put(VISIT_CONTEXT_FACTORY, VisitContextFactory.class);
        try {
            ClassLoader classLoader;
            if (System.getSecurityManager() != null) {
                classLoader = (ClassLoader) AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {

                    public Object run() {
                        return FactoryFinder.class.getClassLoader();
                    }
                });
            } else {
                classLoader = FactoryFinder.class.getClassLoader();
            }

            if (classLoader == null) {
                throw new FacesException("jsf api class loader cannot be identified", null);
            }
            myFacesClassLoader = classLoader;
        } catch (Exception e) {
            throw new FacesException("jsf api class loader cannot be identified", e);
        }
    }

    public Object getFactory(String factoryName) throws FacesException {
        if (factoryName == null) {
            throw new NullPointerException("factoryName may not be null");
        }

        ClassLoader classLoader = getClassLoader();

        // This code must be synchronized because this could cause a problem when
        // using update feature each time of myfaces (org.apache.myfaces.CONFIG_REFRESH_PERIOD)
        // In this moment, a concurrency problem could happen
        Map<String, List<String>> factoryClassNames = null;
        Map<String, Object> factoryMap = null;

        String webApplicationName = WebApplicationName.getName();
        if(webApplicationName == null) {
            throw new IllegalStateException("No web identity is attached to current request thread " + Thread.currentThread().getName());
        }

        synchronized (_registeredFactoryNames) {
            factoryClassNames = _registeredFactoryNames.get(webApplicationName);

            if (factoryClassNames == null) {
                String message = "No Factories configured for this Application. This happens if the faces-initialization "
                        + "does not work at all - make sure that you properly include all configuration settings necessary for a basic faces application "
                        + "and that all the necessary libs are included. Also check the logging output of your web application and your container for any exceptions!"
                        + "\nIf you did that and find nothing, the mistake might be due to the fact that you use some special web-containers which "
                        + "do not support registering context-listeners via TLD files and " + "a context listener is not setup in your web.xml.\n" + "A typical config looks like this;\n<listener>\n"
                        + "  <listener-class>org.apache.myfaces.webapp.StartupServletContextListener</listener-class>\n" + "</listener>\n";
                throw new IllegalStateException(message);
            }

            if (!factoryClassNames.containsKey(factoryName)) {
                throw new IllegalArgumentException("no factory " + factoryName + " configured for this application.");
            }

            factoryMap = _factories.get(webApplicationName);

            if (factoryMap == null) {
                factoryMap = new HashMap<String, Object>();
                _factories.put(webApplicationName, factoryMap);
            }
        }

        List<String> classNames;
        Object factory;
        synchronized (factoryClassNames) {
            factory = factoryMap.get(factoryName);
            if (factory != null) {
                return factory;
            }

            classNames = factoryClassNames.get(factoryName);
        }

        // release lock while calling out
        factory = newFactoryInstance(ABSTRACT_FACTORY_CLASSES.get(factoryName), classNames.iterator(), classLoader);

        synchronized (factoryClassNames) {
            // check if someone else already installed the factory
            if (factoryMap.get(factoryName) == null) {
                factoryMap.put(factoryName, factory);
            }
        }

        return factory;
    }

    private Object newFactoryInstance(Class<?> interfaceClass, Iterator<String> classNamesIterator, ClassLoader classLoader) {
        try {
            Object current = null;

            while (classNamesIterator.hasNext()) {
                String implClassName = classNamesIterator.next();
                Class<?> implClass = null;
                try {
                    implClass = classLoader.loadClass(implClassName);
                } catch (ClassNotFoundException e) {
                    implClass = myFacesClassLoader.loadClass(implClassName);
                }

                // check, if class is of expected interface type
                if (!interfaceClass.isAssignableFrom(implClass)) {
                    throw new IllegalArgumentException("Class " + implClassName + " is no " + interfaceClass.getName());
                }

                if (current == null) {
                    // nothing to decorate
                    current = implClass.newInstance();
                } else {
                    // let's check if class supports the decorator pattern
                    try {
                        Constructor<?> delegationConstructor = implClass.getConstructor(new Class[] { interfaceClass });
                        // impl class supports decorator pattern,
                        try {
                            // create new decorator wrapping current
                            current = delegationConstructor.newInstance(new Object[] { current });
                        } catch (InstantiationException e) {
                            throw new FacesException(e);
                        } catch (IllegalAccessException e) {
                            throw new FacesException(e);
                        } catch (InvocationTargetException e) {
                            throw new FacesException(e);
                        }
                    } catch (NoSuchMethodException e) {
                        // no decorator pattern support
                        current = implClass.newInstance();
                    }
                }
            }
            return current;
        } catch (ClassNotFoundException e) {
            throw new FacesException(e);
        } catch (InstantiationException e) {
            throw new FacesException(e);
        } catch (IllegalAccessException e) {
            throw new FacesException(e);
        }
    }

    public void setFactory(String factoryName, String implName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Enter setFactory " + factoryName + " = " + implName);
        }
        checkFactoryName(factoryName);

        String webApplicationIdentity = WebApplicationName.getName();
        if(webApplicationIdentity == null) {
            throw new IllegalStateException("No web identity is attached to current request thread " + Thread.currentThread().getName());
        }

        Map<String, List<String>> factoryClassNames = null;
        synchronized (_registeredFactoryNames) {
            Map<String, Object> factories = _factories.get(webApplicationIdentity);
            if (factories != null && factories.containsKey(factoryName)) {
                // Javadoc says ... This method has no effect if getFactory() has already been
                // called looking for a factory for this factoryName.
                return;
            }
            factoryClassNames = _registeredFactoryNames.get(webApplicationIdentity);
            if (factoryClassNames == null) {
                factoryClassNames = new HashMap<String, List<String>>();
                _registeredFactoryNames.put(webApplicationIdentity, factoryClassNames);
            }
        }
        synchronized (factoryClassNames) {
            List<String> classNameList = factoryClassNames.get(factoryName);
            if (classNameList == null) {
                classNameList = new ArrayList<String>();
                factoryClassNames.put(factoryName, classNameList);
            }
            classNameList.add(implName);
            if (logger.isDebugEnabled()) {
                logger.debug("Factory map of web application [" + webApplicationIdentity + "] is " + factoryClassNames);
            }
        }
    }

    public void releaseFactories() throws FacesException {
        String webApplicationIdentity = WebApplicationName.getName();
        if(webApplicationIdentity == null) {
            throw new IllegalStateException("No web identity is attached to current request thread " + Thread.currentThread().getName());
        }

        // This code must be synchronized
        synchronized (_registeredFactoryNames) {
            _factories.remove(webApplicationIdentity);
            // _registeredFactoryNames has as value type Map<String,List> and this must
            // be cleaned before release (for gc).
            Map<String, List<String>> factoryClassNames = _registeredFactoryNames.get(webApplicationIdentity);
            if(logger.isDebugEnabled()) {
                logger.debug("Web application [" + webApplicationIdentity + "] releases the factory map " + factoryClassNames);
            }
            if (factoryClassNames != null) {
                factoryClassNames.clear();
            }
            _registeredFactoryNames.remove(webApplicationIdentity);
        }
    }

    private void checkFactoryName(String factoryName) {
        if (!VALID_FACTORY_NAMES.contains(factoryName)) {
            throw new IllegalArgumentException("factoryName '" + factoryName + "'");
        }
    }

    private ClassLoader getClassLoader() {
        try {
            ClassLoader classLoader = null;
            if (System.getSecurityManager() != null) {
                classLoader = (ClassLoader) AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {

                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
            } else {
                classLoader = Thread.currentThread().getContextClassLoader();
            }

            if (classLoader == null) {
                throw new FacesException("web application class loader cannot be identified", null);
            }
            return classLoader;
        } catch (Exception e) {
            throw new FacesException("web application class loader cannot be identified", e);
        }
    }

}
