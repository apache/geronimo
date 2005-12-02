/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package org.apache.geronimo.corba.server;


public class ServantObject
    extends org.omg.CORBA.portable.ServantObject
{

    private POA poa;
    private boolean deactivated;
    private byte[] oid;
    public org.omg.PortableServer.Servant original_servant; 
    boolean mediated;

    ServantObject (POA poa, byte[] oid, Object servant)
    {
        this.poa = poa;
        this.deactivated = false;
        this.oid = oid;
        this.original_servant = (org.omg.PortableServer.Servant) servant;
        this.servant = servant;

        if (servant instanceof org.omg.PortableServer.ServantManagerOperations)
            mediated = false;
        else
            mediated = true;
    }
    
    public boolean isDeactivated ()
    {
        return deactivated;
    }

    public void setDeactivated (boolean value)
    {
        deactivated = value;
    }

    /**
     * Returns true on a successful preinvoke.  
     */
    public ServantObject preinvoke (String operation)
    {
        POAManager manager = (POAManager)poa.the_POAManager ();

        // assert that the POAManager has been activated; this may cause a 
        // TRANSIENT exception if that is not the case.
        manager.__checkActive ();

        // This checks to make sure that the poa is not in the process
        // of being destroyed before incrementing the ref count.
        if (poa.__incrementRequestCount () == false)
            return null;

        // Now do the real thing...
        poa.__preinvoke (operation, // name of operation
                         oid,       // object id
                         original_servant, // the servant
                         null);     // the cookie

        return this;
    }

    public void postinvoke ()
    {
        poa.__postinvoke ();
        poa.__decrementRequestCount ();
    }
}


