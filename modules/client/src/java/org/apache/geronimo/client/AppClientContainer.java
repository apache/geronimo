/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.callback.CallbackHandler;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.security.ContextManager;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public final class AppClientContainer {
    private static final Class[] MAIN_ARGS = {String[].class};

    private final String mainClassName;
    private final AppClientPlugin jndiContext;
    private final ObjectName appClientModuleName;
    private final String realmName;
    private final Class callbackHandlerClass;
    private final Subject defaultSubject;
    private final Method mainMethod;
    private final ClassLoader classLoader;
    private final Kernel kernel;
    private final TransactionContextManager transactionContextManager;

    public AppClientContainer(String mainClassName,
                              ObjectName appClientModuleName,
                              String realmName,
                              String callbackHandlerClassName,
                              DefaultPrincipal defaultPrincipal,
                              AppClientPlugin jndiContext,
                              TransactionContextManager transactionContextManager,
                              ClassLoader classLoader,
                              Kernel kernel
                              ) throws Exception {
        this.mainClassName = mainClassName;
        this.appClientModuleName = appClientModuleName;
        if ((realmName == null) != (callbackHandlerClassName == null)) {
            throw new IllegalArgumentException("You must supply both realmName and callbackHandlerClass or neither");
        }
        this.realmName = realmName;
        if (callbackHandlerClassName != null) {
            try {
                this.callbackHandlerClass = classLoader.loadClass(callbackHandlerClassName);
            } catch (ClassNotFoundException e) {
                throw new AppClientInitializationException("Could not load callbackHandlerClass", e);
            }
        } else {
            callbackHandlerClass = null;
        }
        if (defaultPrincipal != null) {
            defaultSubject = ConfigurationUtil.generateDefaultSubject(defaultPrincipal);
        } else {
            defaultSubject = null;
        }
        this.classLoader = classLoader;
        this.kernel = kernel;
        this.jndiContext = jndiContext;
        this.transactionContextManager = transactionContextManager;

        try {
            Class mainClass = classLoader.loadClass(mainClassName);
            mainMethod = mainClass.getMethod("main", MAIN_ARGS);
        } catch (ClassNotFoundException e) {
            throw new AppClientInitializationException("Unable to load Main-Class " + mainClassName, e);
        } catch (NoSuchMethodException e) {
            throw new AppClientInitializationException("Main-Class " + mainClassName + " does not have a main method", e);
        }
    }

    public ObjectName getAppClientModuleName() {
        return appClientModuleName;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public void main(final String[] args) throws Exception {
        Thread thread = Thread.currentThread();

        ClassLoader oldClassLoader = thread.getContextClassLoader();
        TransactionContext oldTransactionContext = transactionContextManager.getContext();
        TransactionContext currentTransactionContext = null;
        Subject oldCurrentCaller = ContextManager.getCurrentCaller();
        Subject clientSubject = defaultSubject;
        LoginContext loginContext = null;
        try {
            thread.setContextClassLoader(classLoader);
            if (callbackHandlerClass != null) {
                //look for a constructor taking the args
                CallbackHandler callbackHandler;
                try {
                    Constructor cArgs = callbackHandlerClass.getConstructor(new Class[] {String[].class});
                    callbackHandler = (CallbackHandler) cArgs.newInstance(new Object[] {args});
                } catch (NoSuchMethodException e) {
                    callbackHandler = (CallbackHandler) callbackHandlerClass.newInstance();
                }
                loginContext = new LoginContext(realmName, callbackHandler);
                loginContext.login();
                clientSubject = loginContext.getSubject();
            }
            ContextManager.setCurrentCaller(clientSubject);
            jndiContext.startClient(appClientModuleName, kernel, classLoader);
            currentTransactionContext = transactionContextManager.newUnspecifiedTransactionContext();
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
            if (loginContext != null) {
                loginContext.logout();
            }
            jndiContext.stopClient(appClientModuleName);

            thread.setContextClassLoader(oldClassLoader);
            transactionContextManager.setContext(oldTransactionContext);
            currentTransactionContext.commit();
            ContextManager.setCurrentCaller(oldCurrentCaller);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(AppClientContainer.class, NameFactory.APP_CLIENT);

        infoFactory.addOperation("main", new Class[]{String[].class});

        infoFactory.addAttribute("mainClassName", String.class, true);
        infoFactory.addAttribute("appClientModuleName", ObjectName.class, true);
        infoFactory.addAttribute("realmName", String.class, true);
        infoFactory.addAttribute("callbackHandlerClassName", String.class, true);
        infoFactory.addAttribute("defaultPrincipal", DefaultPrincipal.class, true);

        infoFactory.addReference("JNDIContext", AppClientPlugin.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.JTA_RESOURCE);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);


        infoFactory.setConstructor(new String[]{"mainClassName",
                                                "appClientModuleName",
                                                "realmName",
                                                "callbackHandlerClassName",
                                                "defaultPrincipal",
                                                "JNDIContext",
                                                "TransactionContextManager",
                                                "classLoader",
                                                "kernel"
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
