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

package org.apache.geronimo.corba.interceptor;

import org.apache.geronimo.corba.Invocation;
import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.Policies;
import org.apache.geronimo.corba.ior.InternalIOR;
import org.apache.geronimo.corba.ior.InternalServiceContext;
import org.apache.geronimo.corba.ior.InternalServiceContextList;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.omg.PortableInterceptor.RequestInfo;

class RequestInfoImpl
    extends LocalObject
    implements RequestInfo
{
    
    short status;
    InternalIOR forward_ior;
    Exception received_ex;
    String received_ex_id;
	protected Invocation inv;

    void __setStatus (short status) {
        this.status = status;
    }

    void __setForwardIOR (InternalIOR forward) {

        __setStatus (LOCATION_FORWARD.value);

        forward_ior = forward;
    }

    void __setReceivedEx (Exception ex, String id) {
        received_ex = ex;
        received_ex_id = id;
    }

    RequestInfoImpl (Invocation inv)
    {
    		this.inv = inv;
    }


    public int request_id()
    {
    		return inv.getRequestID();
    }

    public java.lang.String operation()
    {
    		return inv.getOperation();
    }

    public org.omg.Dynamic.Parameter[] arguments()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ();
    }

    public org.omg.CORBA.TypeCode[] exceptions()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ();
    }

    public java.lang.String[] contexts()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ();
    }

    public java.lang.String[] operation_context()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ();
    }

    public org.omg.CORBA.Any result()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ();
    }

    public boolean response_expected()
    {
        return inv.isResponseExpected();
    }

    public short sync_scope()
    {
    		return inv.getSyncScope();
    }

    public short reply_status()
    {
        return status;
    }

    public org.omg.CORBA.Object forward_reference()
    {
        if (status != LOCATION_FORWARD.value) {

            throw new org.omg.CORBA.BAD_INV_ORDER ();
        }

        if (forward_ior == null)
            inv.getORB().fatal ("no forward ior");

        return inv.getORB().createObject (forward_ior);
    }

    public org.omg.CORBA.Any get_slot(int id) 
        throws org.omg.PortableInterceptor.InvalidSlot
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ();
    }

    public org.omg.IOP.ServiceContext get_request_service_context(int id)
    {
		InternalServiceContext sc = inv.getRequestServiceContextList(false).getContextWithID(id);
		if (sc != null) {
			return sc.asServiceContext();
		}

        throw new org.omg.CORBA.BAD_PARAM ("no service context id: "+id);
    }

    public org.omg.IOP.ServiceContext get_reply_service_context(int id)

    {
		InternalServiceContext sc = inv.getResponseServiceContextList(false).getContextWithID(id);
		if (sc != null) {
			return sc.asServiceContext();
		}

        throw new org.omg.CORBA.BAD_PARAM ("no service context id: "+id);
    }
    
    public ORB getORB() {
    		return inv.getORB();
    }
}
