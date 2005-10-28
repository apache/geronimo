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


package org.apache.geronimo.corba.dii;

import org.omg.CORBA.ARG_IN;
import org.omg.CORBA.ARG_INOUT;
import org.omg.CORBA.ARG_OUT;
import org.omg.CORBA.Any;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.RepositoryID;


public class RequestImpl
        extends org.omg.CORBA.Request
{

    org.omg.CORBA.portable.ObjectImpl target;
    String operation;
    NVListImpl arguments;
    NamedValueImpl response;
    ORB orb;
    org.omg.CORBA.Context ctx;
    ExceptionListImpl exceptions;
    EnvironmentImpl env;
    org.omg.CORBA.ContextList contexts;

    public RequestImpl(ORB orb,
                       org.omg.CORBA.portable.ObjectImpl target,
                       String operation)
    {
        this(orb,
             target,
             operation,
             null,
             (NVListImpl) orb.create_list(3),
             new NamedValueImpl("",
                                orb.create_any(),
                                ARG_OUT.value));
    }

    public RequestImpl(ORB orb,
                       org.omg.CORBA.portable.ObjectImpl target,
                       String operation,
                       org.omg.CORBA.Context ctx,
                       NVListImpl arg_list,
                       NamedValueImpl result)
    {
        this.orb = orb;
        this.target = target;
        this.operation = operation;
        this.arguments = arg_list;
        this.ctx = ctx;
        this.response = result;
        this.exceptions = (ExceptionListImpl) orb.create_exception_list();
        this.env = (EnvironmentImpl) orb.create_environment();
    }

    public RequestImpl(ORB orb,
                       org.omg.CORBA.portable.ObjectImpl target,
                       String operation,
                       org.omg.CORBA.Context ctx,
                       NVListImpl arg_list,
                       NamedValueImpl result,
                       ExceptionListImpl exceptions,
                       org.omg.CORBA.ContextList contexts)
    {
        this.orb = orb;
        this.target = target;
        this.operation = operation;
        this.arguments = arg_list;
        this.ctx = ctx;
        this.response = result;
        this.exceptions = exceptions;
        this.env = (EnvironmentImpl) orb.create_environment();
        this.contexts = contexts;
    }

    public org.omg.CORBA.Object target() {
        return target;
    }

    public java.lang.String operation() {
        return operation;
    }

    public NVList arguments() {
        return arguments;
    }

    void arguments(NVList arg_list) {
        if (arg_list instanceof NVListImpl) {
            this.arguments = (NVListImpl) arg_list;
        } else {
            throw new org.omg.CORBA.BAD_PARAM("foreign NVList");
        }
    }

    public NamedValue result() {
        if (response == null) {
            throw new org.omg.CORBA.NO_RESPONSE();
        }

        return response;
    }


    public org.omg.CORBA.Environment env() {
        return env;
    }

    public org.omg.CORBA.ExceptionList exceptions() {
        return exceptions;
    }

    public org.omg.CORBA.ContextList contexts() {
        return contexts;
    }

    public org.omg.CORBA.Context ctx() {
        return ctx;
    }

    public void ctx(org.omg.CORBA.Context ctx) {
        this.ctx = ctx;
    }

    public org.omg.CORBA.Any add_in_arg() {
        NamedValue nv = arguments.add(ARG_IN.value);
        return nv.value();
    }

    public org.omg.CORBA.Any add_named_in_arg(java.lang.String name) {
        NamedValue nv = arguments.add_item(name, ARG_IN.value);
        return nv.value();
    }

    public org.omg.CORBA.Any add_inout_arg() {
        NamedValue nv = arguments.add(ARG_INOUT.value);
        return nv.value();
    }

    public org.omg.CORBA.Any add_named_inout_arg(java.lang.String name) {
        NamedValue nv = arguments.add_item(name, ARG_INOUT.value);
        return nv.value();
    }

    public org.omg.CORBA.Any add_out_arg() {
        NamedValue nv = arguments.add(ARG_OUT.value);
        return nv.value();
    }

    public org.omg.CORBA.Any add_named_out_arg(java.lang.String name) {
        NamedValue nv = arguments.add_item(name, ARG_OUT.value);
        return nv.value();
    }

    public void set_return_type(org.omg.CORBA.TypeCode tc) {
        return_value().type(tc);
    }

    public org.omg.CORBA.Any return_value() {
        return result().value();
    }

    public void invoke() {
        invoke(true);
    }

    public void send_oneway() {
        invoke(false);
    }

    private void invoke(boolean responseExpected) {
        REMARSHAL:
        do {
            OutputStream out = (OutputStream)
                    target._request(operation, responseExpected);

            for (int i = 0; i < arguments.count(); i++) {
                NamedValue nv = null;

                try {
                    nv = arguments.item(i);
                }
                catch (org.omg.CORBA.Bounds ex) {
                    throw new org.omg.CORBA.INTERNAL("concurrent modification");
                }

                switch (nv.flags()) {
                    case ARG_IN.value:
                    case ARG_INOUT.value:
                        Any value = nv.value();
                        value.write_value(out);
                }
            }

            InputStream in = null;
            try {
                in = (InputStream) target._invoke(out);

                if (!responseExpected) {
                    return;
                }

                org.omg.CORBA.Any return_value = return_value();
                TypeCode tc = return_value.type();
                if (return_value != null && tc != null) {
                    // create return value
                    return_value.read_value(in, tc);
                }

                for (int i = 0; i < arguments.count(); i++) {
                    NamedValue nv = null;

                    try {
                        nv = arguments.item(i);
                    }
                    catch (org.omg.CORBA.Bounds ex) {
                        throw new org.omg.CORBA.INTERNAL("concurrent modification");
                    }

                    switch (nv.flags()) {
                        case ARG_OUT.value:
                        case ARG_INOUT.value:
                            org.omg.CORBA.Any value = nv.value();
                            value.read_value(in, value.type());
                    }
                }

            }
            catch (org.omg.CORBA.portable.RemarshalException _exception) {
                continue REMARSHAL;

            }
            catch (org.omg.CORBA.portable.UnknownException uex) {

                Throwable e = uex.originalEx;
                String exname = e.getClass().getName();

                for (int i = 0; i < exceptions.count(); i++) {
                    TypeCode ex_tc = null;
                    try {
                        ex_tc = exceptions.item(i);
                    }
                    catch (org.omg.CORBA.Bounds ex) {
                        throw new org.omg.CORBA.INTERNAL("concurrent modification");
                    }

                    String expected;
                    try {
                        expected = RepositoryID.idToClassName(ex_tc.id());
                    }
                    catch (org.omg.CORBA.TypeCodePackage.BadKind ex) {
                        throw new org.omg.CORBA.BAD_PARAM("bad exception type");
                    }

                    if (exname.equals(expected)) {
                        env.exception((Exception) e);
                        return;
                    }
                }

                env.exception(uex);

            }
            catch (org.omg.CORBA.portable.ApplicationException aex) {

                InputStream ex_in = (InputStream) aex.getInputStream();
                String ex_id = aex.getId();

                for (int i = 0; i < exceptions.count(); i++) {
                    TypeCode ex_tc = null;
                    try {
                        ex_tc = exceptions.item(i);
                    }
                    catch (org.omg.CORBA.Bounds ex) {
                        throw new org.omg.CORBA.INTERNAL("concurrent modification");
                    }

                    boolean match = false;
                    try {
                        match = ex_id.equals(ex_tc.id());
                    }
                    catch (org.omg.CORBA.TypeCodePackage.BadKind ex) {
                        throw new org.omg.CORBA.BAD_PARAM("bad exception type");
                    }

                    if (match) {
                        Any ex_val = orb.create_any();
                        ex_val.read_value(ex_in, ex_tc);
                        env.exception(new org.omg.CORBA.UnknownUserException(ex_val));
                        return;
                    }
                }

                env.exception(new org.omg.CORBA.UnknownUserException());

            }
            finally {
                target._releaseReply(in);
            }
        }
        while (false);
    }

    public void send_deferred() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean poll_response() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void get_response()
            throws org.omg.CORBA.WrongTransaction
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


}
