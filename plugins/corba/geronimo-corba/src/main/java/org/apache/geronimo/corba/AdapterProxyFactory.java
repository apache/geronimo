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
package org.apache.geronimo.corba;

import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.MarshalException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.UNKNOWN;


/**
 * @version $Revision: 465108 $ $Date: 2006-10-17 17:23:40 -0700 (Tue, 17 Oct 2006) $
 */
public class AdapterProxyFactory {

    private final static Logger log = LoggerFactory.getLogger(AdapterProxyFactory.class);
    private final static AdapterMethodInterceptor interceptor = new AdapterMethodInterceptor();
    private final Enhancer enhancer;

    public AdapterProxyFactory(Class clientInterface) {
        this(clientInterface, clientInterface.getClassLoader());
    }

    public AdapterProxyFactory(Class clientInterface, ClassLoader classLoader) {
        this(new Class[]{clientInterface}, classLoader);
    }


    public AdapterProxyFactory(Class[] clientInterfaces, ClassLoader classLoader) {
        assert clientInterfaces != null;
        assert areAllInterfaces(clientInterfaces);

        enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setSuperclass(AdapterDelegate.class);
        enhancer.setInterfaces(clientInterfaces);
        enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class});
        enhancer.setCallbackFilter(FILTER);
        enhancer.setUseFactory(false);
    }

    private static boolean areAllInterfaces(Class[] clientInterfaces) {
        for (int i = 0; i < clientInterfaces.length; i++) {
            if (clientInterfaces[i] == null || !clientInterfaces[i].isInterface()) {
                return false;
            }
        }
        return true;
    }

    public Object create(Remote delegate, ClassLoader classLoader) {
        return create(new Class[]{Remote.class, ClassLoader.class}, new Object[]{delegate, classLoader});
    }

    public synchronized Object create(Class[] types, Object[] arguments) {
        enhancer.setCallbacks(new Callback[]{NoOp.INSTANCE, interceptor});
        return enhancer.create(types, arguments);
    }

    private final static class AdapterMethodInterceptor implements MethodInterceptor {

        public final Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            ClassLoader savedCL = Thread.currentThread().getContextClassLoader();
            try {
                AdapterDelegate delegate = (AdapterDelegate) o;
                Thread.currentThread().setContextClassLoader(delegate.getClassLoader());

                if (log.isDebugEnabled()) log.debug("Calling " + method.getName());

                return methodProxy.invoke(delegate.getDelegate(), args);
            } catch (TransactionRolledbackException e) {
                log.debug("TransactionRolledbackException", e);
                throw new TRANSACTION_ROLLEDBACK(e.toString()).initCause(e);
            } catch (TransactionRequiredException e) {
                log.debug("TransactionRequiredException", e);
                throw new TRANSACTION_REQUIRED(e.toString()).initCause(e);
            } catch (InvalidTransactionException e) {
                log.debug("InvalidTransactionException", e);
                throw new INVALID_TRANSACTION(e.toString()).initCause(e);
            } catch (NoSuchObjectException e) {
                log.debug("NoSuchObjectException", e);
                throw new OBJECT_NOT_EXIST(e.toString()).initCause(e);
            } catch (AccessException e) {
                log.debug("AccessException", e);
                throw new NO_PERMISSION(e.toString()).initCause(e);
            } catch (MarshalException e) {
                log.debug("MarshalException", e);
                throw new MARSHAL(e.toString()).initCause(e);
            } catch (RemoteException e) {
                log.debug("RemoteException", e);
                throw new UNKNOWN(e.toString()).initCause(e);
            } finally {
                Thread.currentThread().setContextClassLoader(savedCL);
            }
        }
    }

    private static final CallbackFilter FILTER = new CallbackFilter() {
        public int accept(Method method) {
            if (method.getName().equals("finalize") &&
                method.getParameterTypes().length == 0 &&
                method.getReturnType() == Void.TYPE) {
                return 0;
            }
            return 1;
        }
    };
}

