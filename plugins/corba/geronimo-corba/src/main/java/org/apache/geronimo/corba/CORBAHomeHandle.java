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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.HomeHandle;
import javax.ejb.spi.HandleDelegate;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.ORB;


/**
 * EJB v2.1 spec, section 19.5.5.1
 * <p/>
 * The <code>javax.ejb.spi.HandleDelegate</code> service provider interface
 * defines methods that enable portable implementations of <code>Handle</code>
 * and <code>HomeHandle</code> that are instantiated in a different vendor’s
 * container to serialize and deserialize EJBObject and EJBHome references.
 * The <code>HandleDelegate</code> interface is not used by enterprise beans
 * or J2EE application components directly.
 *
 * @version $Revision: 474745 $ $Date: 2006-11-14 03:30:38 -0800 (Tue, 14 Nov 2006) $
 */
public class CORBAHomeHandle implements HomeHandle, Serializable {

    private static final long serialVersionUID = -5537884768260058215L;

    private String ior;

    public CORBAHomeHandle(String ior) {
        this.ior = ior;
    }

    public EJBHome getEJBHome() throws RemoteException {

        try {
            return (EJBHome) PortableRemoteObject.narrow(getOrb().string_to_object(ior), EJBHome.class);
        } catch (Exception e) {
            throw new RemoteException("Unable to convert IOR into home", e);
        }

    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        HandleDelegate handleDelegate = getHandleDelegate();
        handleDelegate.writeEJBHome(getEJBHome(), out);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        HandleDelegate handleDelegate = getHandleDelegate();
        EJBHome home = handleDelegate.readEJBHome(in);

        try {
            ior = getOrb().object_to_string((org.omg.CORBA.Object) home);
        } catch (Exception e) {
            throw new RemoteException("Unable to convert object to IOR", e);
        }
    }

    private static ORB getOrb() {
        try {
            Context context = new InitialContext();
            ORB orb = (ORB) context.lookup("java:comp/ORB");
            return orb;
        } catch (Throwable e) {
            throw (org.omg.CORBA.MARSHAL)new org.omg.CORBA.MARSHAL("Could not find ORB in jndi at java:comp/ORB", 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES).initCause(e);
        }
    }

    private static HandleDelegate getHandleDelegate() {
        try {
            Context context = new InitialContext();
            HandleDelegate handleDelegate = (HandleDelegate) context.lookup("java:comp/HandleDelegate");
            return handleDelegate;
        } catch (Throwable e) {
            throw (org.omg.CORBA.MARSHAL)new org.omg.CORBA.MARSHAL("Could not find handle delegate in jndi at java:comp/HandleDelegate", 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES).initCause(e);
        }
    }
}
