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
package org.apache.geronimo.interop.rmi.iiop;


public class CorbaObject implements org.omg.CORBA.Object {
    // -----------------------------------------------------------------------
    // unimplemented public methods (only required for full CORBA ORB)
    // -----------------------------------------------------------------------

    public boolean _is_a(String id) {
        return false;
    }

    public org.omg.CORBA.Request _create_request(org.omg.CORBA.Context p1, String p2, org.omg.CORBA.NVList p3, org.omg.CORBA.NamedValue p4) {
        throw new org.omg.CORBA.BAD_OPERATION("_create_request");
    }

    public org.omg.CORBA.Request _create_request(org.omg.CORBA.Context p1, String p2, org.omg.CORBA.NVList p3, org.omg.CORBA.NamedValue p4, org.omg.CORBA.ExceptionList p5, org.omg.CORBA.ContextList p6) {
        throw new org.omg.CORBA.BAD_OPERATION("_create_request");
    }

    public org.omg.CORBA.Object _duplicate() {
        throw new org.omg.CORBA.BAD_OPERATION("_duplicate");
    }

    public org.omg.CORBA.DomainManager[] _get_domain_managers() {
        throw new org.omg.CORBA.BAD_OPERATION("_get_domain_manager");
    }

    public org.omg.CORBA.Object _get_interface_def() {
        throw new org.omg.CORBA.BAD_OPERATION("_get_interface_def");
    }

    public org.omg.CORBA.Policy _get_policy(int p1) {
        throw new org.omg.CORBA.BAD_OPERATION("_get_policy");
    }

    public int _hash(int p1) {
        throw new org.omg.CORBA.BAD_OPERATION("_hash");
    }

    public boolean _is_equivalent(org.omg.CORBA.Object p1) {
        throw new org.omg.CORBA.BAD_OPERATION("_is_equivalent");
    }

    public boolean _non_existent() {
        throw new org.omg.CORBA.BAD_OPERATION("_non_existent");
    }

    public void _release() {
        throw new org.omg.CORBA.BAD_OPERATION("_release");
    }

    public org.omg.CORBA.Request _request(String p1) {
        throw new org.omg.CORBA.BAD_OPERATION("_request");
    }

    public org.omg.CORBA.Object _set_policy_override(org.omg.CORBA.Policy[] p1, org.omg.CORBA.SetOverrideType p2) {
        throw new org.omg.CORBA.BAD_OPERATION("_set_policy_override");
    }
}
