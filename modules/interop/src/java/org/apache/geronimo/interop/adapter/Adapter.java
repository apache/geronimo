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

import org.apache.geronimo.interop.rmi.iiop.ObjectRef;
import org.apache.geronimo.interop.rmi.iiop.RemoteInterface;
import org.openejb.EJBContainer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class Adapter
{
    public abstract Object getAdapterID();
    public abstract String[] getBindNames();

    public abstract void start();
    public abstract void stop();

    public abstract ObjectRef getObjectRef();

    public abstract Object getServant();
    public abstract EJBContainer getEJBContainer();
    public abstract Object getEJBHome();

    public abstract void invoke(java.lang.String methodName, byte[] objectKey, org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output);


    protected Class loadClass( String className, ClassLoader cl )
    {
        Class rc = null;
        try {
            rc = cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  
        }
        return rc;
    }

}
