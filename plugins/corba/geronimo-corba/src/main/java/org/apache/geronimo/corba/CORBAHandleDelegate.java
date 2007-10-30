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
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.spi.HandleDelegate;
import javax.rmi.PortableRemoteObject;


/**
 * See ejb spec 2.1, 19.5.5.1
 *
 * @version $Revision: 465108 $ $Date: 2006-10-17 17:23:40 -0700 (Tue, 17 Oct 2006) $
 */
public class CORBAHandleDelegate implements HandleDelegate {

    /**
     * Called by home handles to deserialize stubs in any app server, including ones by other vendors.
     * The spec seems to imply that a simple cast of in.readObject() should work but in certain
     * orbs this does not seem to work and in.readObject returns a generic remote stub that needs
     * to be narrowed.  Although we think this is likely an orb bug this code with narrow will
     * work in both circumstances.
     * @param in
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public EJBHome readEJBHome(ObjectInputStream in) throws ClassNotFoundException, IOException {
        Object o = in.readObject();
        EJBHome home = (EJBHome) PortableRemoteObject.narrow(o, EJBHome.class);
        return home;
    }

    /**
     * Called by handles to deserialize stubs in any app server.  See comment to readEJBHome.
     * @param in
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public EJBObject readEJBObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        Object o = in.readObject();
        EJBObject object = (EJBObject) PortableRemoteObject.narrow(o, EJBObject.class);
        return object;
    }

    public void writeEJBHome(EJBHome ejbHome, ObjectOutputStream out) throws IOException {
        out.writeObject(ejbHome);
    }

    public void writeEJBObject(EJBObject ejbObject, ObjectOutputStream out) throws IOException {
        out.writeObject(ejbObject);
    }

}
