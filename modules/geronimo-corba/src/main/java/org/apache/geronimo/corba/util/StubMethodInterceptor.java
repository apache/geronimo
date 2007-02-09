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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import javax.ejb.EJBObject;
import javax.rmi.CORBA.Util;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

/**
 * @version $Revision: 502310 $ $Date: 2007-02-01 10:34:57 -0800 (Thu, 01 Feb 2007) $
 */
public class StubMethodInterceptor implements MethodInterceptor {
    private static final Method ISIDENTICAL;

    static {
        try {
            ISIDENTICAL = EJBObject.class.getMethod("isIdentical", new Class[]{EJBObject.class});
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Class type;
    private final Map operations;

    public StubMethodInterceptor(Class type) {
        this.type = type;
        this.operations = Collections.unmodifiableMap(org.apache.geronimo.corba.util.Util.mapMethodToOperation(type));
    }

    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        ClientContextHolderStub stub = (ClientContextHolderStub) proxy;

        // handle is identical in stub to avoid unnecessary round trip
        if (method.equals(ISIDENTICAL)) {
            org.omg.CORBA.Object otherObject = (org.omg.CORBA.Object) args[0];
            return new Boolean(stub._is_equivalent(otherObject));
        }

        // get the operation name object
        String operationName = (String) operations.get(method);
        if (operationName == null) {
            throw new IllegalStateException("Unknown method: " + method);
        }

        while (true) {
            // if this is a stub to a remote object we invoke over the wire
            if (!Util.isLocal(stub)) {

                InputStream in = null;
                try {
                    // create the request output stream
                    OutputStream out = (OutputStream) stub._request(operationName, true);

                    // write the arguments
                    Class[] parameterTypes = method.getParameterTypes();
                    for (int i = 0; i < parameterTypes.length; i++) {
                        Class parameterType = parameterTypes[i];
                        org.apache.geronimo.corba.util.Util.writeObject(parameterType, args[i], out);
                    }

                    // send the invocation
                    in = (InputStream) stub._invoke(out);

                    // read the result
                    Object result = org.apache.geronimo.corba.util.Util.readObject(method.getReturnType(), in);
                    return result;
                } catch (RemarshalException exception) {
                    continue;
                } catch (ApplicationException exception) {
                    org.apache.geronimo.corba.util.Util.throwException(method, (InputStream) exception.getInputStream());
                } catch (SystemException e) {
                    throw Util.mapSystemException(e);
                } finally {
                    stub._releaseReply(in);
                }
            } else {
                // get the servant
                ServantObject servantObject = stub._servant_preinvoke(operationName, type);
                if (servantObject == null) {
                    continue;
                }

                try {
                    // copy the arguments
                    Object[] argsCopy = Util.copyObjects(args, stub._orb());

                    // invoke the servant
                    Object result = null;
                    try {
                        result = method.invoke(servantObject.servant, argsCopy);
                    } catch (InvocationTargetException e) {
                        if (e.getCause() != null) {
                            throw e.getCause();
                        }
                        throw e;
                    }

                    // copy the result
                    result = Util.copyObject(result, stub._orb());

                    return result;
                } catch (Throwable throwable) {
                    // copy the exception
                    Throwable throwableCopy = (Throwable) Util.copyObject(throwable, stub._orb());

                    // if it is one of my exception rethrow it
                    Class[] exceptionTypes = method.getExceptionTypes();
                    for (int i = 0; i < exceptionTypes.length; i++) {
                        Class exceptionType = exceptionTypes[i];
                        if (exceptionType.isInstance(throwableCopy)) {
                            throw throwableCopy;
                        }
                    }

                    throw Util.wrapException(throwableCopy);
                } finally {
                    stub._servant_postinvoke(servantObject);
                }
            }
        }
    }
}
