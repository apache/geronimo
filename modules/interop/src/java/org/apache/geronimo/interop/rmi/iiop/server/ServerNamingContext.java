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
package org.apache.geronimo.interop.rmi.iiop.server;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.geronimo.interop.CosNaming.BindingIteratorHolder;
import org.apache.geronimo.interop.CosNaming.BindingListHolder;
import org.apache.geronimo.interop.CosNaming.NameComponent;
import org.apache.geronimo.interop.CosNaming.NamingContext;
import org.apache.geronimo.interop.CosNaming.NamingContextExtPackage.InvalidAddress;
import org.apache.geronimo.interop.CosNaming.NamingContextPackage.AlreadyBound;
import org.apache.geronimo.interop.CosNaming.NamingContextPackage.CannotProceed;
import org.apache.geronimo.interop.CosNaming.NamingContextPackage.InvalidName;
import org.apache.geronimo.interop.CosNaming.NamingContextPackage.NotFound;
import org.apache.geronimo.interop.CosNaming.NamingContextPackage.NotFoundReason;
import org.apache.geronimo.interop.adapter.Adapter;
import org.apache.geronimo.interop.naming.NameService;
import org.apache.geronimo.interop.naming.NameServiceLog;
import org.apache.geronimo.interop.rmi.iiop.RemoteInterface;


public class ServerNamingContext implements org.apache.geronimo.interop.rmi.iiop.NameServiceOperations {
    
    protected static ServerNamingContext _snc = null;

    public static ServerNamingContext getInstance() {
        if (_snc == null) {
            synchronized (ServerNamingContext.class) {
                _snc = new ServerNamingContext();
                _snc.init();
            }
        }

        return _snc;
    }

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    private NameService _nameService;

    private volatile int _cycle;

    // -----------------------------------------------------------------------
    // public methods from interface NamingContextExtOperations
    // -----------------------------------------------------------------------

    public org.omg.CORBA.Object resolve_str(String name) throws NotFound, CannotProceed, InvalidName {
        return lookup(name, null);
    }


    public String to_string(NameComponent[] n) throws InvalidName {
        throw NoImplement();
    }

    public NameComponent[] to_name(String sn) throws InvalidName {
        throw NoImplement();
    }

    public String to_url(String addr, String sn) throws InvalidAddress, InvalidName {
        throw NoImplement();
    }

    // -----------------------------------------------------------------------
    // public methods from interface NamingContextOperations
    // -----------------------------------------------------------------------

    public org.omg.CORBA.Object resolve(NameComponent[] name) throws NotFound, CannotProceed, InvalidName {
        return lookup(toString(name), name);
    }

    public void bind(NameComponent[] n, org.omg.CORBA.Object obj) throws NotFound, CannotProceed, InvalidName, AlreadyBound {
        throw NoImplement();
    }

    public void bind_context(org.apache.geronimo.interop.CosNaming.NameComponent[] n, org.apache.geronimo.interop.CosNaming.NamingContext nc) throws NotFound, CannotProceed, InvalidName, AlreadyBound {
        throw NoImplement();
    }

    public void rebind(org.apache.geronimo.interop.CosNaming.NameComponent[] n, org.omg.CORBA.Object obj) throws NotFound, CannotProceed, InvalidName {
        throw NoImplement();
    }

    public void rebind_context(org.apache.geronimo.interop.CosNaming.NameComponent[] n, org.apache.geronimo.interop.CosNaming.NamingContext nc) throws NotFound, CannotProceed, InvalidName {
        throw NoImplement();
    }

    public void unbind(NameComponent[] n) throws NotFound, CannotProceed, InvalidName {
        throw NoImplement();
    }

    public void list(int how_many, BindingListHolder bl, BindingIteratorHolder bi) {
        throw NoImplement();
    }

    public NamingContext new_context() {
        throw NoImplement();
    }

    public NamingContext bind_new_context(NameComponent[] n) throws NotFound, AlreadyBound, CannotProceed, InvalidName {
        throw NoImplement();
    }

    protected org.omg.CORBA.NO_IMPLEMENT NoImplement() {
        return new org.omg.CORBA.NO_IMPLEMENT();
    }

    // -----------------------------------------------------------------------
    // public methods from interface NameServiceOperations (Sybase proprietary)
    // -----------------------------------------------------------------------

    public String resolve_host(String host) {
        System.out.println("ServerNamingContext.resolve_host(): TODO host = " + host);

        //String resolvedHost = ClusterPartition.getInstance(host).resolveHost();
        //return "cycle=" + Math.max(1, ++_cycle) + ";" + resolvedHost;
        
        // Cycle prefix for round-robin load balancing.
        // Any weighted balancing is applied by client.

        return host;
    }

    // -----------------------------------------------------------------------
    // protected methods
    // -----------------------------------------------------------------------

    protected void init() {
        _nameService = NameService.getInstance();
    }

    protected org.omg.CORBA.Object lookup(String nameString, NameComponent[] name) throws NotFound {
        try {
            Object object = _nameService.lookup(nameString);

            /*
            if (object instanceof RemoteInterface)
            {
                RemoteInterface remote = (RemoteInterface)object;
                return remote.$getObjectRef();
            }
            else
            {
                NameServiceLog.getInstance().warnObjectHasNoRemoteInterface(nameString, object.getClass().getName());
                throw new NotFound(NotFoundReason.not_object, name);
            }
            */

            if (object instanceof Adapter) {
                Adapter a = (Adapter) object;
                RemoteInterface remote = a.getRemoteInterface();
                return remote.$getObjectRef();
            } else {
                NameServiceLog.getInstance().warnObjectHasNoRemoteInterface(nameString, object.getClass().getName());
                throw new NotFound(NotFoundReason.not_object, name);
            }
        } catch (NameNotFoundException notFound) {
            // Assume warning message has already been logged.
            throw new NotFound(NotFoundReason.missing_node, name);
        } catch (NamingException ex) {
            NameServiceLog.getInstance().warnNameNotFound(nameString, ex);
            throw new NotFound(NotFoundReason.missing_node, name);
        }
    }

    protected String toString(NameComponent[] name) {
        int n = name.length;
        if (n == 1) {
            return name[0].id;
        } else {
            StringBuffer nameBuffer = new StringBuffer();
            for (int i = 0; i < n; i++) {
                if (i > 0) {
                    nameBuffer.append('/');
                }
                nameBuffer.append(name[i].id);
                if (name[i].kind.length() > 0) {
                    nameBuffer.append(",kind=");
                    nameBuffer.append(name[i].kind);
                }
            }
            return nameBuffer.toString();
        }
    }
}
