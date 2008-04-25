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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.util;

import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class DynamicStubClassLoader extends ClassLoader implements GBeanLifecycle {
    private final static Logger log = LoggerFactory.getLogger(DynamicStubClassLoader.class);
    private final static String PACKAGE_PREFIX = "org.omg.stub.";

    private boolean stopped = true;

    public synchronized Class loadClass(final String name) throws ClassNotFoundException {
        if (stopped) {
            throw new ClassNotFoundException("DynamicStubClassLoader is stopped");
        }

        if (log.isDebugEnabled()) {
            log.debug("Load class " + name);
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // check if the stub already exists first
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to load class from the context class loader");
            }
        }

        // if this is not a class from the org.omb.stub name space don't attempt to generate
        if (!name.startsWith(PACKAGE_PREFIX)) {
            if (log.isDebugEnabled()) {
                log.debug("Could not load class: " + name);
            }
            throw new ClassNotFoundException("Could not load class: " + name);
        }

        // load the interfaces class we are attempting to create a stub for
        Class iface = loadStubInterfaceClass(name, classLoader);

        // create the stub builder
        try {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(ClientContextHolderStub.class);
            enhancer.setInterfaces(new Class[]{iface});
            enhancer.setCallbackFilter(FILTER);
            enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class, FixedValue.class});
            enhancer.setUseFactory(false);
            enhancer.setClassLoader(classLoader);
            enhancer.setNamingPolicy(new NamingPolicy() {
                public String getClassName(String s, String s1, Object o, Predicate predicate) {
                    return name;
                }
            });

            // generate the class
            Class result = enhancer.createClass();
            assert result != null;

            StubMethodInterceptor interceptor = new StubMethodInterceptor(iface);
            Ids ids = new Ids(iface);
            Enhancer.registerStaticCallbacks(result, new Callback[]{NoOp.INSTANCE, interceptor, ids});

            if (log.isDebugEnabled()) {
                log.debug("result: " + result.getName());
            }
            return result;
        } catch (RuntimeException e) {
            log.error("Unable to generate stub: " + name, e);
            throw e;
        } catch (Error e) {
            log.error("Unable to generate stub: " + name, e);
            throw e;
        }
    }

    private Class loadStubInterfaceClass(String name, ClassLoader classLoader) throws ClassNotFoundException {
        try {
            int begin = name.lastIndexOf('.') + 1;
            String iPackage = name.substring(13, begin);
            String iName = iPackage + name.substring(begin + 1, name.length() - 5);

            return classLoader.loadClass(iName);
        } catch (ClassNotFoundException e) {
            // don't log exceptions from CosNaming because it attempts to load every
            // class bound into the name server
            boolean shouldLog = true;
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (int i = 0; i < stackTrace.length; i++) {
                StackTraceElement stackTraceElement = stackTrace[i];
                if (stackTraceElement.getClassName().equals("org.omg.CosNaming.NamingContextExtPOA") &&
                        stackTraceElement.getMethodName().equals("_invoke")) {
                    shouldLog = false;
                    break;
                }
            }
            if (shouldLog) {
                log.error("Unable to generate stub", e);
            }

            throw new ClassNotFoundException("Unable to generate stub", e);
        }
    }

    private static final CallbackFilter FILTER = new CallbackFilter() {
        public int accept(Method method) {
            // we don't intercept non-public methods like finalize
            if (!Modifier.isPublic(method.getModifiers())) {
                return 0;
            }

            if (method.getReturnType().equals(String[].class) && method.getParameterTypes().length == 0 && method.getName().equals("_ids")) {
                return 2;
            }

            if (Modifier.isAbstract(method.getModifiers())) {
                return 1;
            }

            return 0;
        }
    };

    private static final class Ids implements FixedValue {
        private final String[] typeIds;

        public Ids(Class type) {
            typeIds = Util.createCorbaIds(type);
        }

        public Object loadObject() throws Exception {
            return typeIds;
        }
    }

    public synchronized void doStart() throws Exception {
        UtilDelegateImpl.setClassLoader(this);
        stopped = false;
    }

    public synchronized void doStop() throws Exception {
        stopped = true;
        log.debug("Stopped");
    }

    public synchronized void doFail() {
        stopped = true;
        log.warn("Failed");
    }

}
