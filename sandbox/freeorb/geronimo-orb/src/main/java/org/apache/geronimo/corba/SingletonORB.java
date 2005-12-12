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

import org.apache.geronimo.corba.dii.EnvironmentImpl;
import org.apache.geronimo.corba.dii.ExceptionListImpl;
import org.apache.geronimo.corba.dii.NamedValueImpl;


public class SingletonORB extends AbstractORB {

    /**
     * Public no-arg constructor
     */
    public SingletonORB() {
    }

    private void illegalSingletonOperation() {
        throw new org.omg.CORBA.NO_IMPLEMENT(
                "illegal operation on singleton ORB");
    }

    public String[] list_initial_services() {
        illegalSingletonOperation();
        return null;
    }

    public org.omg.CORBA.Object resolve_initial_references(String name)
            throws org.omg.CORBA.ORBPackage.InvalidName
    {
        illegalSingletonOperation();
        return null;
    }

    public void register_initial_reference(String name, org.omg.CORBA.Object obj)
            throws org.omg.CORBA.ORBPackage.InvalidName
    {
        illegalSingletonOperation();
    }

    public String object_to_string(org.omg.CORBA.Object object) {
        illegalSingletonOperation();
        return null;
    }

    public org.omg.CORBA.Object string_to_object(String str) {
        illegalSingletonOperation();
        return null;
    }

    public org.omg.CORBA.NVList create_list(int count) {
        illegalSingletonOperation();
        return null;
    }

    public org.omg.CORBA.NVList create_operation_list(org.omg.CORBA.Object oper) {
        illegalSingletonOperation();
        return null;
    }

    public org.omg.CORBA.NamedValue create_named_value(String name,
                                                       org.omg.CORBA.Any value, int flags)
    {
        return new NamedValueImpl(name, value, flags);
    }

    public org.omg.CORBA.ExceptionList create_exception_list() {
        return new ExceptionListImpl();
    }

    public org.omg.CORBA.ContextList create_context_list() {
        illegalSingletonOperation();
        return null;
    }

    public org.omg.CORBA.Context get_default_context() {
        illegalSingletonOperation();
        return null;
    }

    public org.omg.CORBA.Environment create_environment() {
        return new EnvironmentImpl();
    }

    public void send_multiple_requests_oneway(org.omg.CORBA.Request[] requests) {
        illegalSingletonOperation();
    }

    public void send_multiple_requests_deferred(org.omg.CORBA.Request[] requests) {
        illegalSingletonOperation();
    }

    public boolean poll_next_response() {
        illegalSingletonOperation();
        return false;
    }

    public org.omg.CORBA.Request get_next_response()
            throws org.omg.CORBA.WrongTransaction
    {
        illegalSingletonOperation();
        return null;
    }


    public boolean work_pending() {
        illegalSingletonOperation();
        return false;
    }

    public void perform_work() {
        illegalSingletonOperation();
    }

    public void run() {
        illegalSingletonOperation();
    }

    public void shutdown(boolean wait_for_completion) {
        illegalSingletonOperation();
    }

    public void destroy() {
        illegalSingletonOperation();
    }

    public org.omg.CORBA.Any create_any() {
        return new AnyImpl(this);
    }

    public org.omg.CORBA.portable.OutputStream create_output_stream() {
        illegalSingletonOperation();
        return null;
    }

    public void connect(org.omg.CORBA.Object obj) {
        illegalSingletonOperation();
    }

    public void disconnect(org.omg.CORBA.Object obj) {
        illegalSingletonOperation();
    }

    public org.omg.CORBA.Policy create_policy(int policy_type,
                                              org.omg.CORBA.Any val) throws org.omg.CORBA.PolicyError
    {
        illegalSingletonOperation();
        return null;
    }

    protected void set_parameters(String[] args, java.util.Properties props) {
        illegalSingletonOperation();
    }

    protected void set_parameters(java.applet.Applet app,
                                  java.util.Properties props)
    {
        illegalSingletonOperation();
    }

}
