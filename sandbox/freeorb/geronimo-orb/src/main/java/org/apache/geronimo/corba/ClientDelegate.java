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
package org.apache.geronimo.corba;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Request;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA_2_3.portable.Delegate;
import org.omg.IOP.IOR;

import org.apache.geronimo.corba.ior.InternalIOR;


public class ClientDelegate extends Delegate {

    private final ORB orb;
    private InternalIOR ior;

    private InvocationProfileSelector profileManager;

    public ClientDelegate(InternalIOR ior) {
        this.ior = ior;
        this.orb = (ORB) ior.orb;
    }

    ClientDelegate(ORB orb, IOR ior) {
        this(new InternalIOR(orb, ior));
    }

    //
    //
    //

    public boolean is_local(org.omg.CORBA.Object self) {
        // TODO: implement
        return false;
    }

    public OutputStream request(org.omg.CORBA.Object self, String operation,
                                boolean responseExpected)
    {


        while (true) {

            InvocationProfileSelector manager = getProfileSelector();

            try {

                // process client interceptor (pre-marshal) and write
                // RequestHeader to output stream.

                OutputStream result = manager.setupRequest(operation,
                                                           responseExpected);

                return result;

            }
            catch (org.omg.PortableInterceptor.ForwardRequest ex) {

                setIOR(InternalIOR.extract(ex.forward));

                continue;

            }

        }
    }

    private void setIOR(InternalIOR ior) {
        this.ior = ior;
        this.profileManager = null;
    }


    private InvocationProfileSelector getProfileSelector() {
        if (this.profileManager == null) {
            this.profileManager = orb.createInvocationProfileSelector(this);
        }
        return profileManager;
    }

    public InputStream invoke(org.omg.CORBA.Object self, OutputStream output)
            throws ApplicationException, RemarshalException
    {
        // TODO: implement
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void releaseReply(org.omg.CORBA.Object self, InputStream input) {
        // TODO: implement
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public Object get_interface_def(Object self) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object duplicate(Object obj) {
        // TODO Auto-generated method stub
        return null;
    }

    public void release(Object obj) {
        // TODO Auto-generated method stub

    }

    public boolean is_a(Object obj, String repository_id) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean non_existent(Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean is_equivalent(Object obj, Object other) {
        // TODO Auto-generated method stub
        return false;
    }

    public int hash(Object obj, int max) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Request request(Object obj, String operation) {
        // TODO Auto-generated method stub
        return null;
    }

    public Request create_request(Object obj, Context ctx, String operation,
                                  NVList arg_list, NamedValue result)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Request create_request(Object obj, Context ctx, String operation,
                                  NVList arg_list, NamedValue result, ExceptionList exclist,
                                  ContextList ctxlist)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public InternalIOR getInternalIOR() {
        return ior;
    }

    public ORB getORB() {
        return orb;
    }

    public InternalIOR getIOR() {
        return ior;
    }

}
