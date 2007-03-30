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
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.transaction.InvalidTransactionException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.openejb.EjbDeployment;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.BeanType;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.geronimo.corba.util.Util;
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
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.UnknownException;
import org.omg.PortableServer.Servant;

/**
 * @version $Revision: 494431 $ $Date: 2007-01-09 07:18:14 -0800 (Tue, 09 Jan 2007) $
 */
public class StandardServant extends Servant implements InvokeHandler {
    private static final Log log = LogFactory.getLog(StandardServant.class);

    private static final Method GETEJBMETADATA = getMethod(EJBHome.class, "getEJBMetaData", null);
    private static final Method GETHOMEHANDLE = getMethod(EJBHome.class, "getHomeHandle", null);
    private static final Method REMOVE_W_KEY = getMethod(EJBHome.class, "remove", new Class[]{Object.class});
    private static final Method REMOVE_W_HAND = getMethod(EJBHome.class, "remove", new Class[]{Handle.class});
    private static final Method GETEJBHOME = getMethod(EJBObject.class, "getEJBHome", null);
    private static final Method GETHANDLE = getMethod(EJBObject.class, "getHandle", null);
    private static final Method GETPRIMARYKEY = getMethod(EJBObject.class, "getPrimaryKey", null);
    private static final Method ISIDENTICAL = getMethod(EJBObject.class, "isIdentical", new Class[]{EJBObject.class});
    private static final Method REMOVE = getMethod(EJBObject.class, "remove", null);


    private final InterfaceType interfaceType;
    private final EjbDeployment ejbDeployment;
    private final Object primaryKey;
    private final String[] typeIds;
    private final Map operations;
    private final Context enc;

    public StandardServant(ORB orb, InterfaceType ejbInterfaceType, EjbDeployment ejbDeployment) {
        this(orb, ejbInterfaceType, ejbDeployment, null);
    }

    public StandardServant(ORB orb, InterfaceType ejbInterfaceType, EjbDeployment ejbDeployment, Object primaryKey) {
        this.interfaceType = ejbInterfaceType;
        this.ejbDeployment = ejbDeployment;
        this.primaryKey = primaryKey;

        // get the interface class
        Class type;
        if (InterfaceType.EJB_HOME == ejbInterfaceType) {
            type = ejbDeployment.getHomeInterface();
        } else if (InterfaceType.EJB_OBJECT == ejbInterfaceType) {
            type = ejbDeployment.getRemoteInterface();
        } else {
            throw new IllegalArgumentException("Only home and remote interfaces are supported in this servant: " + ejbInterfaceType);
        }

        // build the operations index
        this.operations = Util.mapOperationToMethod(type);

        // creat the corba ids array
        typeIds = Util.createCorbaIds(type);

        // create ReadOnlyContext
        Map componentContext = new HashMap(2);
        componentContext.put("ORB", orb);
        componentContext.put("HandleDelegate", new CORBAHandleDelegate());
        try {
            enc = EnterpriseNamingContext.createEnterpriseNamingContext(componentContext);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public InterfaceType getInterfaceType() {
        return interfaceType;
    }

    public EjbDeployment getEjbDeployment() {
        return ejbDeployment;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return typeIds;
    }

    public OutputStream _invoke(String operationName, InputStream _in, ResponseHandler reply) throws SystemException {
        // get the method object
        Method method = (Method) operations.get(operationName);

        org.omg.CORBA_2_3.portable.InputStream in = (org.omg.CORBA_2_3.portable.InputStream) _in;

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Context oldContext = RootContext.getComponentContext();
        try {
            Thread.currentThread().setContextClassLoader(ejbDeployment.getClassLoader());
            RootContext.setComponentContext(enc);

            // read in all of the arguments
            Class[] parameterTypes = method.getParameterTypes();
            Object[] arguments = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class parameterType = parameterTypes[i];
                arguments[i] = Util.readObject(parameterType, in);
            }

            // invoke the method
            Object result = null;
            try {

                if (log.isDebugEnabled()) log.debug("Calling " + method.getName());

                if (method.getDeclaringClass() == javax.ejb.EJBObject.class) {
                    if (method.equals(GETHANDLE)) {
                        result = ejbDeployment.getEjbObject(primaryKey).getHandle();
                    } else if (method.equals(GETPRIMARYKEY)) {
                        result = ejbDeployment.getEjbObject(primaryKey).getPrimaryKey();
                    } else if (method.equals(ISIDENTICAL)) {
                        org.omg.CORBA.Object thisObject = this._this_object();
                        org.omg.CORBA.Object otherObject = (org.omg.CORBA.Object)arguments[0];
                        result = new Boolean(thisObject._is_equivalent(otherObject));
                    } else if (method.equals(GETEJBHOME)) {
                        result = ejbDeployment.getEJBHome();
                    } else if (method.equals(REMOVE)) {
                        try {
                            ejbDeployment.getEjbObject(primaryKey).remove();
                            result = null;
                        } catch (RemoveException e) {
                            return Util.writeUserException(method, reply, e);
                        }
                    } else {
                        throw new UnsupportedOperationException("unknown method: " + method);
                    }
                } else if (method.getDeclaringClass() == javax.ejb.EJBHome.class) {
                   if (method.equals(GETEJBMETADATA)) {
                        result = ejbDeployment.getEJBHome().getEJBMetaData();
                    } else if (method.equals(GETHOMEHANDLE)) {
                        result = ejbDeployment.getEJBHome().getHomeHandle();
                    } else if (method.equals(REMOVE_W_HAND)) {
                        CORBAHandle handle = (CORBAHandle) arguments[0];
                        try {
                            if (ejbDeployment.getComponentType() == BeanType.STATELESS) {
                                if (handle == null) {
                                    throw new RemoveException("Handle is null");
                                }
                                Class remoteInterface = ejbDeployment.getRemoteInterface();


                                try {
                                    EJBObject narrowed = (EJBObject)PortableRemoteObject.narrow(handle.getEJBObject(), remoteInterface);
                                    if (narrowed == null) {
                                        throw new RemoteException("Handle does not hold a " + remoteInterface.getName());
                                    }
                                } catch (ClassCastException e) {
                                    throw new RemoteException("Handle does not hold a " + remoteInterface.getName());
                                }
                            } else {
                                try {
                                    RpcContainer container = (RpcContainer) ejbDeployment.getContainer();
                                    result = container.invoke(ejbDeployment.getDeploymentId(), method, arguments, primaryKey, null);
                                } catch (OpenEJBException e) {
                                    Throwable cause = e.getCause();
                                    if (cause instanceof Exception) {
                                        Exception exception = (Exception) cause;
                                        return Util.writeUserException(method, reply, exception);
                                    }
                                    throw cause;
                                }
                            }
                        } catch (RemoveException e) {

                            return Util.writeUserException(method, reply, e);
                        }
                        result = null;
                    } else if (method.equals(REMOVE_W_KEY)) {
                        try {
                            ejbDeployment.getEJBHome().remove(arguments[0]);
                            result = null;
                        } catch (RemoveException e) {
                            return Util.writeUserException(method, reply, e);
                        }
                    } else {
                        throw new UnsupportedOperationException("unknown method: " + method);
                    }
                } else {
                    try {
                        RpcContainer container = (RpcContainer) ejbDeployment.getContainer();
                        result = container.invoke(ejbDeployment.getDeploymentId(), method, arguments, primaryKey, null);
                        // some methods like create() return a ProxyInfo object.  We need to 
                        // turn this into a real EJB remote reference. 
                        if (result instanceof ProxyInfo) {
                            result = Util.getEJBProxy((ProxyInfo)result); 
                        }
                    } catch (OpenEJBException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof Exception) {
                            Exception exception = (Exception) cause;
                            return Util.writeUserException(method, reply, exception);
                        }
                        throw cause;
                    }
                }
            } catch (TransactionRolledbackException e) {
                log.debug("TransactionRolledbackException", e);
                throw new TRANSACTION_ROLLEDBACK(e.toString());
            } catch (TransactionRequiredException e) {
                log.debug("TransactionRequiredException", e);
                throw new TRANSACTION_REQUIRED(e.toString());
            } catch (InvalidTransactionException e) {
                log.debug("InvalidTransactionException", e);
                throw new INVALID_TRANSACTION(e.toString());
            } catch (NoSuchObjectException e) {
                log.debug("NoSuchObjectException", e);
                throw new OBJECT_NOT_EXIST(e.toString());
            } catch (AccessException e) {
                log.debug("AccessException", e);
                throw new NO_PERMISSION(e.toString());
            } catch (MarshalException e) {
                log.debug("MarshalException", e);
                throw new MARSHAL(e.toString());
            } catch (RemoteException e) {
                log.debug("RemoteException", e);
                e.printStackTrace();
                throw new UnknownException(e);
            } catch (RuntimeException e) {
                log.debug("RuntimeException", e);
                e.printStackTrace();
                RemoteException remoteException = new RemoteException(e.getClass().getName() + " thrown from " + ejbDeployment.getDeploymentId() + ": " + e.getMessage());
                throw new UnknownException(remoteException);
            } catch (Error e) {
                log.debug("Error", e);
                RemoteException remoteException = new RemoteException(e.getClass().getName() + " thrown from " + ejbDeployment.getDeploymentId() + ": " + e.getMessage());
                throw new UnknownException(remoteException);
            } catch (Throwable e) {
                log.warn("Unexpected throwable", e);
                throw new UNKNOWN("Unknown exception type " + e.getClass().getName() + ": " + e.getMessage());
            }

            // creat the output stream
            org.omg.CORBA_2_3.portable.OutputStream out = (org.omg.CORBA_2_3.portable.OutputStream) reply.createReply();

            // write the output value
            Util.writeObject(method.getReturnType(), result, out);

            return out;
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
            RootContext.setComponentContext(oldContext);
        }
    }

    private static Method getMethod(Class c, String method, Class[] params) {
        try {
            return c.getMethod(method, params);
        } catch (NoSuchMethodException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        }
    }
}
