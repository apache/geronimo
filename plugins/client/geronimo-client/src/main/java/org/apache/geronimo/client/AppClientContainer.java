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
package org.apache.geronimo.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.cli.CommandLineParser;
import org.apache.geronimo.cli.client.ClientCLParser;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.annotation.Injection;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.credentialstore.CredentialStore;
import org.apache.geronimo.security.deploy.SubjectInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.StaticRecipe;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.APP_CLIENT)
public final class AppClientContainer implements GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(AppClientContainer.class);
    private static final Class[] MAIN_ARGS = {String[].class};
    
    private LoginContext loginContext;

    private final String mainClassName;
    private final AppClientPlugin jndiContext;
    private final AbstractName appClientModuleName;
    private final String realmName;
    private final String callbackHandlerClass;
    private final Subject defaultSubject;
    private final Method mainMethod;
    private final ClassLoader classLoader;
    private final Kernel kernel;
    private final Holder holder;
    private final ServerInfo serverInfo;
    private final Bundle bundle;
    private CallbackHandler callbackHandler;

    public AppClientContainer(@ParamAttribute(name = "mainClassName") String mainClassName,
            @ParamAttribute(name = "appClientModuleName") AbstractName appClientModuleName,
            @ParamAttribute(name = "realmName") String realmName,
            @ParamAttribute(name = "callbackHandlerClassName") String callbackHandlerClassName,
            @ParamAttribute(name = "defaultSubject") SubjectInfo defaultSubject,
            @ParamAttribute(name = "holder") Holder holder,
            @ParamReference(name = "JNDIContext") AppClientPlugin jndiContext,
            @ParamReference(name = "CredentialStore") CredentialStore credentialStore,
            @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
            @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
            @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
            @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle
    ) throws Exception {
        // set the geronimo identity resolver hook for openejb
        System.setProperty("openejb.client.identityResolver", "geronimo");

        this.mainClassName = mainClassName;
        this.appClientModuleName = appClientModuleName;
        if ((realmName == null) != (callbackHandlerClassName == null)) {
            throw new IllegalArgumentException("You must supply both realmName and callbackHandlerClass or neither");
        }
        this.realmName = realmName;
        this.callbackHandlerClass = callbackHandlerClassName;

        if (defaultSubject != null) {
            this.defaultSubject = credentialStore.getSubject(defaultSubject.getRealm(), defaultSubject.getId());
        } else {
            this.defaultSubject = null;
        }
        this.holder = holder == null ? Holder.EMPTY : holder;
        this.classLoader = classLoader;
        this.kernel = kernel;
        this.jndiContext = jndiContext;
        this.serverInfo = serverInfo;
        this.bundle = bundle;

        try {
            Class mainClass = classLoader.loadClass(mainClassName);
            mainMethod = mainClass.getMethod("main", MAIN_ARGS);
        } catch (ClassNotFoundException e) {
            throw new AppClientInitializationException("Unable to load Main-Class " + mainClassName, e);
        } catch (NoSuchMethodException e) {
            throw new AppClientInitializationException("Main-Class " + mainClassName + " does not have a main method", e);
        }
        main();
    }

    public AbstractName getAppClientModuleName() {
        return appClientModuleName;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public void main() throws Exception {
        String[] originalArgs = serverInfo.getArgs();
        ClientCLParser parser = new ClientCLParser(System.out);
        parser.parse(originalArgs);
        final String[] args = parser.getApplicationClientArgs();
        //TODO reorganize this so it makes more sense.  maybe use an interceptor stack.
        //TODO track resource ref shared and app managed security
        Thread thread = Thread.currentThread();

        ClassLoader oldClassLoader = thread.getContextClassLoader();
        Callers oldCallers = ContextManager.getCallers();
        Subject clientSubject = defaultSubject;
        try {
            thread.setContextClassLoader(classLoader);
            jndiContext.startClient(appClientModuleName, kernel, classLoader);
            Context componentContext = jndiContext.getJndiContext();

            if (callbackHandlerClass != null) {
                callbackHandler = (CallbackHandler) holder.newInstance(callbackHandlerClass, classLoader, componentContext);
                loginContext = ContextManager.login(realmName, callbackHandler);
                clientSubject = loginContext.getSubject();
            }
            ContextManager.setCallers(clientSubject, clientSubject);
            Class mainClass = classLoader.loadClass(mainClassName);
            ObjectRecipe objectRecipe = new ObjectRecipe(mainClass);
            objectRecipe.allow(Option.FIELD_INJECTION);
            objectRecipe.allow(Option.PRIVATE_PROPERTIES);
            objectRecipe.allow(Option.STATIC_PROPERTIES);
            List<Injection> injections = new ArrayList<Injection>();
            while (mainClass != null && mainClass != Object.class) {
                List<Injection> perClass = holder.getInjections(mainClass.getName());
                if (perClass != null) {
                    injections.addAll(perClass);
                }
                mainClass = mainClass.getSuperclass();
            }
            if (injections != null) {
                List<NamingException> problems = new ArrayList<NamingException>();
                for (Injection injection : injections) {
                    String jndiName = injection.getJndiName();
                    try {
                        Object object = componentContext.lookup(jndiName);
                        objectRecipe.setProperty(injection.getTargetName(), object);
                    } catch (NamingException e) {
                        log.info("Injection problem for jndiName: " + jndiName, e);
                        problems.add(e);
                    }
                }
                if (!problems.isEmpty()) {
                    //TODO fix the problems with trying to lookup resource refs from the client.
//                    throw new Exception("Some objects to be injected were not found in jndi: " + problems);
                    log.error("Some objects to be injected were not found in jndi: " + problems);
                }
            }
            Class clazz = objectRecipe.setStaticProperties();
            if (holder.getPostConstruct() != null) {
                Holder.apply(null, clazz, holder.getPostConstruct());
            }

            if (clientSubject == null) {
                mainMethod.invoke(null, new Object[]{args});
            } else {
                Subject.doAs(clientSubject, new PrivilegedAction() {
                    public Object run() {
                        try {
                            mainMethod.invoke(null, new Object[]{args});
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                        return null;
                    }
                });
            }
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new Error(e);
        } finally {
            //How can this work??
            thread.setContextClassLoader(oldClassLoader);
            ContextManager.popCallers(oldCallers);
        }
        //shut down the server
        BundleContext bundleContext = bundle.getBundleContext();
        Bundle framework = bundleContext.getBundle(0);
        if (framework != null) {
            framework.stop();
        }
    }

    public void doStart() throws Exception {
    }

    public void doStop() throws Exception {
        if (callbackHandler != null) {
            holder.destroyInstance(callbackHandler);
        }
        if (loginContext != null) {
            ContextManager.logout(loginContext);
        }
        jndiContext.stopClient(appClientModuleName);
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            //ignore
        }
    }

}
