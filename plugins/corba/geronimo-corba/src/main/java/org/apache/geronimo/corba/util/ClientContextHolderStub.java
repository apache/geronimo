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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.CORBA.Stub;
import javax.ejb.EJBHome;

import org.omg.CORBA.ORB;
import org.apache.geronimo.corba.CORBAEJBMemento;

/**
 * @version $Revision: 502310 $ $Date: 2007-02-01 10:34:57 -0800 (Thu, 01 Feb 2007) $
 */
public abstract class ClientContextHolderStub extends Stub {

    protected ClientContextHolderStub() {
    }

    public final Object writeReplace() {
        String ior = getOrb().object_to_string(this);
        return new CORBAEJBMemento(ior, this instanceof EJBHome);
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


