/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.adapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Vector;

import org.apache.geronimo.interop.rmi.iiop.RemoteInterface;


public class Adapter {
    //
    // Public Accessible Properties
    //
    protected String _bindName;
    protected String _remoteClassName;
    protected String _remoteInterfaceName;
    protected Vector _idVector;
    protected boolean _shared;
    protected ClassLoader _cl;
    protected RemoteInterface _ri;

    //
    // Internal Properrties
    //

    protected Object _sharedObject;
    protected HashMap _objects;
    protected Class _remoteClassClass;
    protected Class _remoteInterfaceClass;

    public Adapter() {
        _objects = new HashMap();
        _idVector = new Vector();
    }

    /*
     * BindName is the name that will be registered with the INS (Inter-operable Name Service)
     */
    public String getBindName() {
        return _bindName;
    }

    public void setBindName(String bindName) {
        _bindName = bindName;
    }

    /*
     * Is this a shared component?  If so this will invoke the getInstance method on
     * the component ...
     */
    public boolean isShared() {
        return _shared;
    }

    public void setShared(boolean shared) {
        _shared = shared;
    }

    /*
     * The classloader that will load any dependancies of the adapter or corba skel interfaces.
     * Its should be set by the ejb container
     */
    public ClassLoader getClassLoader() {
        return _cl;
    }

    public void setClassLoader(ClassLoader cl) {
        _cl = cl;
    }

    /*
     * This is the name of the remote class that implements the remote interface.
     *
     * This is only used if this adapter is going to directly invoke an object.  For the
     * EJB Container, the adapter will pass through the method invocations.
     */
    public String getRemoteClassName() {
        return _remoteClassName;
    }

    public void setRemoteClassName(String rcName) {
        _remoteClassName = rcName;
    }

    /*
     * The remote interface name for the remote object.  This will most likely be the name
     * of the EJB's RemoteInterface and RemoteHomeInterface
     *
     * The stub/skel generator will use this interface name.
     */
    public String getRemoteInterfaceName() {
        return _remoteInterfaceName;
    }

    public void setRemoteInterfaceName(String riName) {
        _remoteInterfaceName = riName;
    }

    /*
     * A list of public IDs that the remote object implements:
     *
     * IDL:....:1.0
     * RMI:....:X:Y
     */
    public Vector getIds() {
        return _idVector;
    }

    public void addId(String id) {
        _idVector.add(id);
    }

    public void removeId(String id) {
        _idVector.remove(id);
    }

    /*
     * Return the skeleton implemention for the remote interface.  This interface has the
     * invoke method to handle the rmi/iiop messages.
     */
    public RemoteInterface getRemoteInterface() {
        if (_ri == null) {
            synchronized (this) {
                String riName = _remoteInterfaceName + "_Skeleton";
                _remoteInterfaceClass = loadClass(riName);

                try {
                    _ri = (RemoteInterface) _remoteInterfaceClass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IllegalAccessException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

        return _ri;
    }

    /*
     * Get an object instance to invoke based on the object key.
     *
     * The objectKey could probably be passed to the EJB container so that the
     * container can directly invoke the ejb object as required.
     */
    public Object getInstance(byte[] objectKey) {
        String key = new String(objectKey);
        return getInstance(key);
    }

    public Object getInstance(String key) {
        Object o = _objects.get(key);

        if (o == null) {
            o = newInstance(key);
        }

        return o;
    }

    public Object newInstance(byte[] objectKey) {
        String key = new String(objectKey);
        return newInstance(key);
    }

    public Object newInstance(String key) {
        Object o = null;

        if (_remoteClassClass == null) {
            synchronized (this) {
                _remoteClassClass = loadClass(_remoteClassName);
            }

            try {
                if (_shared) {
                    synchronized (this) {
                        Method m = _remoteClassClass.getMethod("getInstance", (Class[]) null);
                        o = m.invoke(_remoteClassClass, (Object[]) null);

                        if (o != null) {
                            _objects.put(key, o);
                        }
                    }
                } else {
                    o = _remoteClassClass.newInstance();
                    _objects.put(key, o);
                }
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return o;
    }

    /*
     * Invoke method from the IIOP Message Handler.  The adapter is bound to the INS name service.
     * When an RMI/IIOP message is processed by the server, the message handler will perform a lookup
     * on the name service to get the Adapter, then the invocation will be passed to the adapter
     * The adapter will obtain the object key and then determine which object instance to pass the
     * invocation to.
     */
    public void invoke(java.lang.String methodName, byte[] objectKey, org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {
        RemoteInterface skeleton = getRemoteInterface();
        Object instance = getInstance(objectKey);

        if (instance != null) {
            skeleton.$invoke(methodName, objectKey, instance, input, output);
        } else {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST(new String(objectKey));
        }
    }

    /*
     * Helper function to load a class.  This uses classloader for the adapter.
     */
    protected Class loadClass(String name) {
        Class c = null;

        try {
            c = _cl.loadClass(name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }
}
