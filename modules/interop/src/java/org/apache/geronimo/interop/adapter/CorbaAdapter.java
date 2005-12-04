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

import org.apache.geronimo.interop.rmi.iiop.RemoteInterface;
import org.apache.geronimo.interop.rmi.iiop.ObjectRef;
import org.apache.geronimo.interop.naming.NameService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CorbaAdapter extends Adapter {

    private final Log log = LogFactory.getLog(CorbaAdapter.class);

    private ClassLoader classLoader;

    private NameService nameService = NameService.getInstance();

    private String bindNames[];
    private String ids[];
    private String remoteClassName;
    private String remoteInterfaceClassName;

    private Class remoteClassClass;
    private Class remoteInterfaceClass;
    private RemoteInterface remoteInterfaceObject;
    private Object remoteClassObject;

    public CorbaAdapter(String[] bindNames, String[] ids, String remoteClassName,
                        String remoteInterfaceName, ClassLoader classLoader) {
        this.bindNames = bindNames;
        this.ids = ids;
        this.remoteClassName = remoteClassName;
        this.remoteInterfaceClassName = remoteInterfaceName;
        this.classLoader = classLoader;

        this.remoteInterfaceClassName += "_Skeleton";

        loadRemoteInterface();
        loadRemoteObject();
    }

    public Object getAdapterID() {
        return "CorbaAdapter";
    }

    /*
     * BindName is the name that will be registered with the INS (Inter-operable Name Service)
     * These are the names from the EJBContainer.
     */
    public String[] getBindNames() {
        return bindNames;
    }

    /*
     * The classloader that will load any dependancies of the adapter or corba skel interfaces.
     * Its should be set by the ejb container
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /*
     * The classloader that will load any dependancies of the adapter or corba skel interfaces.
     * Its should be set by the ejb container
     */
    public void setClassLoader(ClassLoader cl) {
        this.classLoader = cl;
    }

    /*
     * Invoke method from the IIOP Message Handler.  The adapter is bound to the INS name service.
     * When an RMI/IIOP message is processed by the server, the message handler will perform a lookup
     * on the name service to get the HomeAdapter, then the invocation will be passed to the adapter
     * The adapter will obtain the object key and then determine which object instance to pass the
     * invocation to.
     */
    public void invoke(java.lang.String methodName, byte[] objectKey, org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input, org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output) {
        if (remoteInterfaceObject != null) {
            remoteInterfaceObject.invoke(methodName, objectKey, this, input, output);
        } else {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST(new String(objectKey));
        }
    }

    public void start() {
        log.debug("Starting CorbaAdapter: ");
        nameService.bindAdapter(this);
    }

    public void stop() {
        log.debug("Stopping CorbaAdapter: ");
        nameService.unbindAdapter(this);
    }

    public ObjectRef getObjectRef() {
        return remoteInterfaceObject.getObjectRef();
        //org.apache.geronimo.interop.rmi.iiop.ObjectRef or = new ObjectRef();
        //or.$setID("RMI:org.apache.geronimo.interop.CosNaming.NamingContext:0000000000000000");
        //or.$setObjectKey("org.apache.geronimo.interop.CosNaming.NamingContext");
        //return or;
    }

    protected void loadRemoteInterface() {
        remoteInterfaceClass = loadClass(remoteInterfaceClassName, classLoader);

        if (remoteInterfaceClass != null) {
            remoteInterfaceObject = null;
            try {
                remoteInterfaceObject = (RemoteInterface) remoteInterfaceClass.newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    protected void loadRemoteObject() {
        remoteClassClass = loadClass(remoteClassName, classLoader);

        if (remoteClassClass != null) {
            remoteClassObject = null;
            try {
                remoteClassObject = remoteClassClass.newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    public Object getServant() {
        return remoteClassObject;
    }

    public Object getEJBContainer() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getEJBHome() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
