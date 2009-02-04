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

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.annotation.Injection;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.credentialstore.CredentialStore;
import org.apache.geronimo.security.deploy.SubjectInfo;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.StaticRecipe;

/**
 * @version $Rev$ $Date$
 */
public final class AppClientContainer implements GBeanLifecycle {
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
    private CallbackHandler callbackHandler;

    public AppClientContainer(String mainClassName,
            AbstractName appClientModuleName,
            String realmName,
            String callbackHandlerClassName,
            SubjectInfo defaultSubject,
            Holder holder,
            AppClientPlugin jndiContext,
            CredentialStore credentialStore,
            ClassLoader classLoader,
            Kernel kernel
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

        try {
            Class mainClass = classLoader.loadClass(mainClassName);
            mainMethod = mainClass.getMethod("main", MAIN_ARGS);
        } catch (ClassNotFoundException e) {
            throw new AppClientInitializationException("Unable to load Main-Class " + mainClassName, e);
        } catch (NoSuchMethodException e) {
            throw new AppClientInitializationException("Main-Class " + mainClassName + " does not have a main method", e);
        }
    }

    public AbstractName getAppClientModuleName() {
        return appClientModuleName;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public void main(final String[] args) throws Exception {
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
            ObjectRecipe objectRecipe = new ObjectRecipe(mainClassName);
            objectRecipe.allow(Option.FIELD_INJECTION);
            objectRecipe.allow(Option.PRIVATE_PROPERTIES);
            objectRecipe.allow(Option.STATIC_PROPERTIES);
            Class mainClass = classLoader.loadClass(mainClassName);
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
                    try {
                        String jndiName = injection.getJndiName();
                        //our componentContext is attached to jndi at "java:comp" so we remove that when looking stuff up in it
                        Object object = componentContext.lookup("env/" + jndiName);
                        if (object instanceof String) {
                            String string = (String) object;
                            // Pass it in raw so it could be potentially converted to
                            // another data type by an xbean-reflect property editor
                            objectRecipe.setProperty(injection.getTargetName(), string);
                        } else {
                            objectRecipe.setProperty(injection.getTargetName(), new StaticRecipe(object));
                        }
                    } catch (NamingException e) {
                        problems.add(e);
                    }
                }
                if (!problems.isEmpty()) {
                    throw new Exception("Some objects to be injected were not found in jndi: " + problems);
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


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(AppClientContainer.class, NameFactory.APP_CLIENT);


        infoFactory.addAttribute("mainClassName", String.class, true);
        infoFactory.addAttribute("appClientModuleName", AbstractName.class, true);
        infoFactory.addAttribute("realmName", String.class, true);
        infoFactory.addAttribute("callbackHandlerClassName", String.class, true);
        infoFactory.addAttribute("defaultSubject", SubjectInfo.class, true);
        infoFactory.addAttribute("holder", Holder.class, true);

        infoFactory.addReference("JNDIContext", AppClientPlugin.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("CredentialStore", CredentialStore.class, NameFactory.GERONIMO_SERVICE);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);


        infoFactory.setConstructor(new String[]{"mainClassName",
                "appClientModuleName",
                "realmName",
                "callbackHandlerClassName",
                "defaultSubject",
                "holder",
                "JNDIContext",
                "CredentialStore",
                "classLoader",
                "kernel"
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
