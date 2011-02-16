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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.AccessException;
import java.rmi.MarshalException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.UtilDelegate;
import javax.rmi.CORBA.ValueHandler;
import javax.transaction.InvalidTransactionException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;
import org.apache.geronimo.corba.AdapterWrapper;
import org.apache.geronimo.corba.CORBAException;
import org.apache.geronimo.corba.RefGenerator;
import org.apache.geronimo.corba.StandardServant;
import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.ivm.BaseEjbProxyHandler;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.core.stateful.StatefulEjbObjectHandler.RegistryId;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.yoko.osgi.ProviderLocator;

/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public final class UtilDelegateImpl implements UtilDelegate {

    private final Logger log = LoggerFactory.getLogger(UtilDelegateImpl.class);
    private final UtilDelegate delegate;
    private static Class<? extends UtilDelegate> delegateClass;
    private static ClassLoader classLoader;

    private final static String DELEGATE_NAME = "org.apache.geronimo.corba.UtilDelegateClass";
    
    private final static String PROVIDERKEY = "javax.rmi.CORBA.UtilClass";

    public UtilDelegateImpl() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
//        String value = System.getProperty(DELEGATE_NAME);
//        if (value == null) {
//            log.error("No delegate specfied via " + DELEGATE_NAME);
//            throw new IllegalStateException("The property " + DELEGATE_NAME + " must be defined!");
//        }
//
//        if (log.isDebugEnabled()) log.debug("Set delegate " + value);
//        delegate = (UtilDelegate) Class.forName(value).newInstance();
    	Class clz = ProviderLocator.locate(PROVIDERKEY);
    	UtilDelegateImpl.setDelegateClass(clz);
        delegate = delegateClass.newInstance();
    }

    public static void setDelegateClass(Class<? extends UtilDelegate> delegateClass) {
        UtilDelegateImpl.delegateClass = delegateClass;
    }

    static void setClassLoader(ClassLoader classLoader) {
        UtilDelegateImpl.classLoader = classLoader;
    }

    public void unexportObject(Remote target) throws NoSuchObjectException {
        delegate.unexportObject(target);
    }

    public boolean isLocal(Stub stub) throws RemoteException {
        return delegate.isLocal(stub);
    }

    public ValueHandler createValueHandler() {
        return delegate.createValueHandler();
    }

    public Object readAny(InputStream in) {
        return delegate.readAny(in);
    }

    public void writeAbstractObject(OutputStream out, Object obj) {
        delegate.writeAbstractObject(out, obj);
    }

    public void writeAny(OutputStream out, Object obj) {
        delegate.writeAny(out, obj);
    }

    public void writeRemoteObject(OutputStream out, Object obj) {
        try {
            if (obj instanceof Tie && ((Tie) obj).getTarget() instanceof Proxy) {
                obj = ((Tie) obj).getTarget();
            }
            if (obj instanceof Proxy) {
                obj = convertEJBToCORBAObject((Proxy) obj);
            }
            if (obj instanceof StandardServant) {
                StandardServant servant = (StandardServant) obj;
                InterfaceType servantType = servant.getInterfaceType();
                String deploymentId = servant.getEjbDeployment().getDeploymentId();
                try {
                    RefGenerator refGenerator = AdapterWrapper.getRefGenerator(deploymentId);
                    if (refGenerator == null) {
                        throw new MARSHAL("Could not find RefGenerator for deployment id: " + deploymentId);
                    }
                    if (InterfaceType.EJB_HOME == servantType) {
                        obj = refGenerator.genHomeReference();
                    } else if (InterfaceType.EJB_OBJECT == servantType) {
                        obj = refGenerator.genObjectReference(servant.getPrimaryKey());
                    } else {
                        log.error("Encountered unknown local invocation handler of type " + servantType + ":" + deploymentId);
                        throw new MARSHAL("Internal server error while marshaling the reply", 0, CompletionStatus.COMPLETED_YES);
                    }
                } catch (CORBAException e) {
                    log.error("Encountered unknown local invocation handler of type " + servantType + ":" + deploymentId);
                    throw (MARSHAL)new MARSHAL("Internal server error while marshaling the reply", 0, CompletionStatus.COMPLETED_YES).initCause(e);
                }
            }
            delegate.writeRemoteObject(out, obj);
        } catch (Throwable e) {
            log.error("Received unexpected exception while marshaling an object reference:", e);
            throw (MARSHAL)new MARSHAL("Internal server error while marshaling the reply", 0, CompletionStatus.COMPLETED_YES).initCause(e);
        }
    }

    public String getCodebase(Class clz) {
        return delegate.getCodebase(clz);
    }

    public void registerTarget(Tie tie, Remote target) {
        delegate.registerTarget(tie, target);
    }

    public RemoteException wrapException(Throwable obj) {
        return delegate.wrapException(obj);
    }

    public RemoteException mapSystemException(SystemException ex) {
        if (ex instanceof TRANSACTION_ROLLEDBACK) {
            TransactionRolledbackException transactionRolledbackException = new TransactionRolledbackException(ex.getMessage());
            transactionRolledbackException.detail = ex;
            return transactionRolledbackException;
        }
        if (ex instanceof TRANSACTION_REQUIRED) {
            TransactionRequiredException transactionRequiredException = new TransactionRequiredException(ex.getMessage());
            transactionRequiredException.detail = ex;
            return transactionRequiredException;
        }
        if (ex instanceof INVALID_TRANSACTION) {
            InvalidTransactionException invalidTransactionException = new InvalidTransactionException(ex.getMessage());
            invalidTransactionException.detail = ex;
            return invalidTransactionException;
        }
        if (ex instanceof OBJECT_NOT_EXIST) {
            NoSuchObjectException noSuchObjectException = new NoSuchObjectException(ex.getMessage());
            noSuchObjectException.detail = ex;
            return noSuchObjectException;
        }
        if (ex instanceof NO_PERMISSION) {
            return new AccessException(ex.getMessage(), ex);
        }
        if (ex instanceof MARSHAL) {
            return new MarshalException(ex.getMessage(), ex);
        }
        if (ex instanceof UNKNOWN) {
            return new RemoteException(ex.getMessage(), ex);
        }
        return delegate.mapSystemException(ex);
    }

    public Tie getTie(Remote target) {
        return delegate.getTie(target);
    }

    public Object copyObject(Object obj, ORB orb) throws RemoteException {
        return delegate.copyObject(obj, orb);
    }

    public Object[] copyObjects(Object[] obj, ORB orb) throws RemoteException {
        return delegate.copyObjects(obj, orb);
    }

    public Class loadClass(String className, String remoteCodebase, ClassLoader loader) throws ClassNotFoundException {
        if (log.isDebugEnabled()) log.debug("Load class: " + className + ", " + remoteCodebase + ", " + loader);

        Class result = null;
        try {
            result = delegate.loadClass(className, remoteCodebase, loader);
        } catch (ClassNotFoundException e) {
            if (log.isDebugEnabled()) log.debug("Unable to load class from delegate");
        }
        if (result == null && classLoader != null) {
            if (log.isDebugEnabled()) log.debug("Attempting to load " + className + " from the static class loader");

            try {
                result = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                if (log.isDebugEnabled()) log.debug("Unable to load " + className + " from the static class loader");
                throw e;
            }

            if (log.isDebugEnabled()) log.debug("result: " + (result == null ? "NULL" : result.getName()));
        }

        return result;
    }

    /**
     * handle activation
     */
    private Object convertEJBToCORBAObject(Proxy proxy) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
        if (!(invocationHandler instanceof BaseEjbProxyHandler)) {
            return proxy;
        }

        BaseEjbProxyHandler ejbProxyHandler = (BaseEjbProxyHandler) invocationHandler;
        BeanContext beanContext = ejbProxyHandler.getBeanContext();
        String deploymentId = (String) beanContext.getDeploymentID();
        try {
            RefGenerator refGenerator = AdapterWrapper.getRefGenerator(deploymentId);
            if (refGenerator == null) {
                throw new MARSHAL("Could not find RefGenerator for deployment id: " +deploymentId);
            }
            if (proxy instanceof EJBHome) {
                return refGenerator.genHomeReference();
            } else if (proxy instanceof EJBObject) {
                Object primaryKey = null;
                if (beanContext.getComponentType() == BeanType.STATEFUL) {
                    RegistryId id = (RegistryId)((EjbObjectProxyHandler)ejbProxyHandler).getRegistryId(); 
                    primaryKey = id.getPrimaryKey(); 
                }
                else if (beanContext.getComponentType() != BeanType.STATELESS) {
                    EJBObject ejbObject = (EJBObject) proxy;
                    primaryKey = ejbObject.getPrimaryKey();
                }
                return refGenerator.genObjectReference(primaryKey);
            } else {
                log.error("Encountered unknown local invocation handler of type " + proxy.getClass().getSuperclass() + ":" + deploymentId);
                throw new MARSHAL("Internal server error while marshaling the reply", 0, CompletionStatus.COMPLETED_YES);
            }
        } catch (CORBAException e) {
            log.error("Encountered unknown local invocation handler of type " + proxy.getClass().getSuperclass() + ":" + deploymentId);
            throw (MARSHAL)new MARSHAL("Encountered unknown local invocation handler of type " + proxy.getClass().getSuperclass() + ":" + deploymentId, 0, CompletionStatus.COMPLETED_YES).initCause(e);
        } catch (RemoteException e) {
            log.error("Unable to get primary key from bean from bean of type " + proxy.getClass().getSuperclass() + ":" + deploymentId);
            throw (MARSHAL)new MARSHAL("Unable to get primary key from bean from bean of type " + proxy.getClass().getSuperclass() + ":" + deploymentId, 0, CompletionStatus.COMPLETED_YES).initCause(e);
        }
    }
}
