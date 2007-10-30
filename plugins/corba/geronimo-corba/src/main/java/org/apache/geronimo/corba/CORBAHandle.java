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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.spi.HandleDelegate;
import javax.naming.Context;
import javax.naming.InitialContext;

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
 * @version $Revision: 494431 $ $Date: 2007-01-09 07:18:14 -0800 (Tue, 09 Jan 2007) $
 */
public class CORBAHandle implements Handle, Serializable {

    private static final long serialVersionUID = -3390719015323727224L;

    // the actual EJBObject instance
    private EJBObject ejbObject;
    private Object primaryKey;

    public CORBAHandle(EJBObject ejb, Object primaryKey) {
        this.ejbObject = ejb;
        this.primaryKey = primaryKey;
    }

    public EJBObject getEJBObject() throws RemoteException {
        return ejbObject;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        HandleDelegate handleDelegate = getHandleDelegate();
        handleDelegate.writeEJBObject(getEJBObject(), out);
        out.writeObject(primaryKey);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        HandleDelegate handleDelegate = getHandleDelegate();
        ejbObject = handleDelegate.readEJBObject(in);
        primaryKey = in.readObject();
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
