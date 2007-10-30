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

import java.io.Serializable;
import java.io.ObjectStreamException;
import java.io.InvalidObjectException;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.rmi.PortableRemoteObject;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.omg.CORBA.ORB;

/**
 * @version $Revision: 474745 $ $Date: 2006-11-14 03:30:38 -0800 (Tue, 14 Nov 2006) $
 */
public class CORBAEJBMemento implements Serializable {
    private final String ior;
    private final boolean home;

    public CORBAEJBMemento(String ior, boolean home) {
        this.ior = ior;
        this.home = home;
    }

    private Object readResolve() throws ObjectStreamException {
        Class type;
        if (home) {
            type = EJBHome.class;
        } else {
            type = EJBObject.class;
        }

        try {
            return PortableRemoteObject.narrow(getOrb().string_to_object(ior), type);
        } catch (Exception e) {
            throw (InvalidObjectException) new InvalidObjectException("Unable to convert IOR into object").initCause(e);
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
}
