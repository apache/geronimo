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

import org.apache.geronimo.interop.rmi.iiop.ObjectInputStream;
import org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream;
import org.apache.geronimo.interop.rmi.iiop.ObjectRef;
import org.openejb.EJBContainer;
import org.openejb.client.ProxyFactory;
import org.openejb.proxy.ProxyInfo;
import org.openejb.proxy.EJBProxyFactory;

import java.util.HashMap;
import java.util.Vector;

public class RemoteAdapter extends Adapter
{
    private EJBContainer        ejbContainer;
    private ProxyInfo           proxyInfo;
    private EJBProxyFactory     proxyFactory;

    public RemoteAdapter( EJBContainer container ) {
        this.ejbContainer = container;
        proxyInfo = ejbContainer.getProxyInfo();
        //proxyFactory = ejbContainer.getProxyFactory();
    }

    public Object getAdapterID()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getBindNames()
    {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void start()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void stop()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ObjectRef getObjectRef()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getServant() {
        // Stateless
        return ejbContainer.getEJBObject( null );
    }

    public EJBContainer getEJBContainer()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getEJBHome()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void invoke(String methodName, byte[] objectKey, ObjectInputStream input, ObjectOutputStream output)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
