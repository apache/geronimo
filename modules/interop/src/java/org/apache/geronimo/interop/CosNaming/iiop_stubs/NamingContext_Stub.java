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
package org.apache.geronimo.interop.CosNaming.iiop_stubs;

import org.apache.geronimo.interop.rmi.iiop.ObjectRef;


public class NamingContext_Stub
        extends ObjectRef
        implements org.apache.geronimo.interop.CosNaming.NamingContext {
    // 
    // Fields
    // 
    public java.lang.String[] _ids = {"org.apache.geronimo.interop.CosNaming.NamingContext", "RMI:org.apache.geronimo.interop.CosNaming.NamingContext:0000000000000000"};
    private static final org.apache.geronimo.interop.rmi.iiop.ValueType vt$0 = org.apache.geronimo.interop.rmi.iiop.ValueType.getInstance(java.lang.String.class);
    private static final org.apache.geronimo.interop.rmi.iiop.ValueType vt$1 = org.apache.geronimo.interop.rmi.iiop.ValueType.getInstance(org.apache.geronimo.interop.CosNaming.BindingListHolder.class);
    private static final org.apache.geronimo.interop.rmi.iiop.ValueType vt$2 = org.apache.geronimo.interop.rmi.iiop.ValueType.getInstance(org.apache.geronimo.interop.CosNaming.BindingIteratorHolder.class);
    private static final org.apache.geronimo.interop.rmi.iiop.ValueType vt$3 = org.apache.geronimo.interop.rmi.iiop.ValueType.getInstance(org.apache.geronimo.interop.CosNaming.NameComponent[].class);
    private static final org.apache.geronimo.interop.rmi.iiop.ValueType vt$4 = org.apache.geronimo.interop.rmi.iiop.ValueType.getInstance(org.omg.CORBA.Object.class);
    private static final org.apache.geronimo.interop.rmi.iiop.ValueType vt$5 = org.apache.geronimo.interop.rmi.iiop.ValueType.getInstance(org.apache.geronimo.interop.CosNaming.NamingContext.class);

    //
    // Constructors
    // 
    public NamingContext_Stub() {
        super();
    }
    
    // 
    // Methods
    // 
    
    public boolean _is_a(java.lang.String id) {
        java.lang.Object $key = $getRequestKey();
        int $retry;

        for ($retry = 0
                ; ; $retry++
                ) {

            try {
                org.apache.geronimo.interop.rmi.iiop.client.Connection $conn;
                org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $out;
                java.lang.String $et;
                boolean $rc;
                org.apache.geronimo.interop.rmi.iiop.ObjectInputStream $in;
                $conn = this.$connect();
                $out = $conn.getSimpleOutputStream();
                $out.writeObject(vt$0, id);
                $conn.invoke(this, "_is_a", $key, $retry);
                $in = $conn.getSimpleInputStream();
                $conn.forget($key);
                $conn.close();
                $et = $conn.getExceptionType();

                if ($et != null) {
                    throw org.apache.geronimo.interop.rmi.iiop.SystemExceptionFactory.getException($conn.getException());
                }
                $rc = $in.readBoolean();
                return $rc;
            } catch (org.apache.geronimo.interop.rmi.iiop.client.RetryInvokeException $ex) {

                if ($retry == 3) {
                    throw $ex.getRuntimeException();
                }
            }
        }
    }

    public void list(int p0, org.apache.geronimo.interop.CosNaming.BindingListHolder p1, org.apache.geronimo.interop.CosNaming.BindingIteratorHolder p2) {
        java.lang.Object $key = $getRequestKey();
        int $retry;

        for ($retry = 0
                ; ; $retry++
                ) {

            try {
                org.apache.geronimo.interop.rmi.iiop.client.Connection $conn;
                org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $out;
                java.lang.String $et;
                $conn = this.$connect();
                $out = $conn.getSimpleOutputStream();
                $out.writeInt(p0);
                $out.writeObject(vt$1, p1);
                $out.writeObject(vt$2, p2);
                $conn.invoke(this, "list", $key, $retry);
                $conn.forget($key);
                $conn.close();
                $et = $conn.getExceptionType();

                if ($et != null) {
                    throw org.apache.geronimo.interop.rmi.iiop.SystemExceptionFactory.getException($conn.getException());
                }
            } catch (org.apache.geronimo.interop.rmi.iiop.client.RetryInvokeException $ex) {

                if ($retry == 3) {
                    throw $ex.getRuntimeException();
                }
            }
        }
    }

    public org.omg.CORBA.Object resolve(org.apache.geronimo.interop.CosNaming.NameComponent[] p0) throws org.apache.geronimo.interop.CosNaming.NamingContextPackage.NotFound, org.apache.geronimo.interop.CosNaming.NamingContextPackage.CannotProceed, org.apache.geronimo.interop.CosNaming.NamingContextPackage.InvalidName {
        java.lang.Object $key = $getRequestKey();
        int $retry;

        for ($retry = 0
                ; ; $retry++
                ) {

            try {
                org.apache.geronimo.interop.rmi.iiop.client.Connection $conn;
                org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $out;
                java.lang.String $et;
                org.omg.CORBA.Object $rc;
                org.apache.geronimo.interop.rmi.iiop.ObjectInputStream $in;
                $conn = this.$connect();
                $out = $conn.getSimpleOutputStream();
                $out.writeObject(vt$3, p0);
                $conn.invoke(this, "resolve", $key, $retry);
                $in = $conn.getSimpleInputStream();
                $conn.forget($key);
                $conn.close();
                $et = $conn.getExceptionType();

                if ($et != null) {
                    throw org.apache.geronimo.interop.rmi.iiop.SystemExceptionFactory.getException($conn.getException());
                }
                $rc = (org.omg.CORBA.Object) $in.readObject(vt$4);
                return $rc;
            } catch (org.apache.geronimo.interop.rmi.iiop.client.RetryInvokeException $ex) {

                if ($retry == 3) {
                    throw $ex.getRuntimeException();
                }
            }
        }
    }

    public void bind(org.apache.geronimo.interop.CosNaming.NameComponent[] p0, org.omg.CORBA.Object p1) throws org.apache.geronimo.interop.CosNaming.NamingContextPackage.NotFound, org.apache.geronimo.interop.CosNaming.NamingContextPackage.CannotProceed, org.apache.geronimo.interop.CosNaming.NamingContextPackage.InvalidName, org.apache.geronimo.interop.CosNaming.NamingContextPackage.AlreadyBound {
        java.lang.Object $key = $getRequestKey();
        int $retry;

        for ($retry = 0
                ; ; $retry++
                ) {

            try {
                org.apache.geronimo.interop.rmi.iiop.client.Connection $conn;
                org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $out;
                java.lang.String $et;
                $conn = this.$connect();
                $out = $conn.getSimpleOutputStream();
                $out.writeObject(vt$3, p0);
                $out.writeObject(vt$4, p1);
                $conn.invoke(this, "bind", $key, $retry);
                $conn.forget($key);
                $conn.close();
                $et = $conn.getExceptionType();

                if ($et != null) {
                    throw org.apache.geronimo.interop.rmi.iiop.SystemExceptionFactory.getException($conn.getException());
                }
            } catch (org.apache.geronimo.interop.rmi.iiop.client.RetryInvokeException $ex) {

                if ($retry == 3) {
                    throw $ex.getRuntimeException();
                }
            }
        }
    }

    public void bind_context(org.apache.geronimo.interop.CosNaming.NameComponent[] p0, org.apache.geronimo.interop.CosNaming.NamingContext p1) throws org.apache.geronimo.interop.CosNaming.NamingContextPackage.NotFound, org.apache.geronimo.interop.CosNaming.NamingContextPackage.CannotProceed, org.apache.geronimo.interop.CosNaming.NamingContextPackage.InvalidName, org.apache.geronimo.interop.CosNaming.NamingContextPackage.AlreadyBound {
        java.lang.Object $key = $getRequestKey();
        int $retry;

        for ($retry = 0
                ; ; $retry++
                ) {

            try {
                org.apache.geronimo.interop.rmi.iiop.client.Connection $conn;
                org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $out;
                java.lang.String $et;
                $conn = this.$connect();
                $out = $conn.getSimpleOutputStream();
                $out.writeObject(vt$3, p0);
                $out.writeObject(vt$5, p1);
                $conn.invoke(this, "bind_context", $key, $retry);
                $conn.forget($key);
                $conn.close();
                $et = $conn.getExceptionType();

                if ($et != null) {
                    throw org.apache.geronimo.interop.rmi.iiop.SystemExceptionFactory.getException($conn.getException());
                }
            } catch (org.apache.geronimo.interop.rmi.iiop.client.RetryInvokeException $ex) {

                if ($retry == 3) {
                    throw $ex.getRuntimeException();
                }
            }
        }
    }

    public void rebind(org.apache.geronimo.interop.CosNaming.NameComponent[] p0, org.omg.CORBA.Object p1) throws org.apache.geronimo.interop.CosNaming.NamingContextPackage.NotFound, org.apache.geronimo.interop.CosNaming.NamingContextPackage.CannotProceed, org.apache.geronimo.interop.CosNaming.NamingContextPackage.InvalidName {
        java.lang.Object $key = $getRequestKey();
        int $retry;

        for ($retry = 0
                ; ; $retry++
                ) {

            try {
                org.apache.geronimo.interop.rmi.iiop.client.Connection $conn;
                org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $out;
                java.lang.String $et;
                $conn = this.$connect();
                $out = $conn.getSimpleOutputStream();
                $out.writeObject(vt$3, p0);
                $out.writeObject(vt$4, p1);
                $conn.invoke(this, "rebind", $key, $retry);
                $conn.forget($key);
                $conn.close();
                $et = $conn.getExceptionType();

                if ($et != null) {
                    throw org.apache.geronimo.interop.rmi.iiop.SystemExceptionFactory.getException($conn.getException());
                }
            } catch (org.apache.geronimo.interop.rmi.iiop.client.RetryInvokeException $ex) {

                if ($retry == 3) {
                    throw $ex.getRuntimeException();
                }
            }
        }
    }

    public void rebind_context(org.apache.geronimo.interop.CosNaming.NameComponent[] p0, org.apache.geronimo.interop.CosNaming.NamingContext p1) throws org.apache.geronimo.interop.CosNaming.NamingContextPackage.NotFound, org.apache.geronimo.interop.CosNaming.NamingContextPackage.CannotProceed, org.apache.geronimo.interop.CosNaming.NamingContextPackage.InvalidName {
        java.lang.Object $key = $getRequestKey();
        int $retry;

        for ($retry = 0
                ; ; $retry++
                ) {

            try {
                org.apache.geronimo.interop.rmi.iiop.client.Connection $conn;
                org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $out;
                java.lang.String $et;
                $conn = this.$connect();
                $out = $conn.getSimpleOutputStream();
                $out.writeObject(vt$3, p0);
                $out.writeObject(vt$5, p1);
                $conn.invoke(this, "rebind_context", $key, $retry);
                $conn.forget($key);
                $conn.close();
                $et = $conn.getExceptionType();

                if ($et != null) {
                    throw org.apache.geronimo.interop.rmi.iiop.SystemExceptionFactory.getException($conn.getException());
                }
            } catch (org.apache.geronimo.interop.rmi.iiop.client.RetryInvokeException $ex) {

                if ($retry == 3) {
                    throw $ex.getRuntimeException();
                }
            }
        }
    }

    public void unbind(org.apache.geronimo.interop.CosNaming.NameComponent[] p0) throws org.apache.geronimo.interop.CosNaming.NamingContextPackage.NotFound, org.apache.geronimo.interop.CosNaming.NamingContextPackage.CannotProceed, org.apache.geronimo.interop.CosNaming.NamingContextPackage.InvalidName {
        java.lang.Object $key = $getRequestKey();
        int $retry;

        for ($retry = 0
                ; ; $retry++
                ) {

            try {
                org.apache.geronimo.interop.rmi.iiop.client.Connection $conn;
                org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $out;
                java.lang.String $et;
                $conn = this.$connect();
                $out = $conn.getSimpleOutputStream();
                $out.writeObject(vt$3, p0);
                $conn.invoke(this, "unbind", $key, $retry);
                $conn.forget($key);
                $conn.close();
                $et = $conn.getExceptionType();

                if ($et != null) {
                    throw org.apache.geronimo.interop.rmi.iiop.SystemExceptionFactory.getException($conn.getException());
                }
            } catch (org.apache.geronimo.interop.rmi.iiop.client.RetryInvokeException $ex) {

                if ($retry == 3) {
                    throw $ex.getRuntimeException();
                }
            }
        }
    }

    public org.apache.geronimo.interop.CosNaming.NamingContext new_context() {
        java.lang.Object $key = $getRequestKey();
        int $retry;

        for ($retry = 0
                ; ; $retry++
                ) {

            try {
                org.apache.geronimo.interop.rmi.iiop.client.Connection $conn;
                org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $out;
                java.lang.String $et;
                org.apache.geronimo.interop.CosNaming.NamingContext $rc;
                org.apache.geronimo.interop.rmi.iiop.ObjectInputStream $in;
                $conn = this.$connect();
                $out = $conn.getSimpleOutputStream();
                $conn.invoke(this, "new_context", $key, $retry);
                $in = $conn.getSimpleInputStream();
                $conn.forget($key);
                $conn.close();
                $et = $conn.getExceptionType();

                if ($et != null) {
                    throw org.apache.geronimo.interop.rmi.iiop.SystemExceptionFactory.getException($conn.getException());
                }
                $rc = (org.apache.geronimo.interop.CosNaming.NamingContext) $in.readObject(vt$5);
                return $rc;
            } catch (org.apache.geronimo.interop.rmi.iiop.client.RetryInvokeException $ex) {

                if ($retry == 3) {
                    throw $ex.getRuntimeException();
                }
            }
        }
    }

    public org.apache.geronimo.interop.CosNaming.NamingContext bind_new_context(org.apache.geronimo.interop.CosNaming.NameComponent[] p0) throws org.apache.geronimo.interop.CosNaming.NamingContextPackage.NotFound, org.apache.geronimo.interop.CosNaming.NamingContextPackage.AlreadyBound, org.apache.geronimo.interop.CosNaming.NamingContextPackage.CannotProceed, org.apache.geronimo.interop.CosNaming.NamingContextPackage.InvalidName {
        java.lang.Object $key = $getRequestKey();
        int $retry;

        for ($retry = 0
                ; ; $retry++
                ) {

            try {
                org.apache.geronimo.interop.rmi.iiop.client.Connection $conn;
                org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream $out;
                java.lang.String $et;
                org.apache.geronimo.interop.CosNaming.NamingContext $rc;
                org.apache.geronimo.interop.rmi.iiop.ObjectInputStream $in;
                $conn = this.$connect();
                $out = $conn.getSimpleOutputStream();
                $out.writeObject(vt$3, p0);
                $conn.invoke(this, "bind_new_context", $key, $retry);
                $in = $conn.getSimpleInputStream();
                $conn.forget($key);
                $conn.close();
                $et = $conn.getExceptionType();

                if ($et != null) {
                    throw org.apache.geronimo.interop.rmi.iiop.SystemExceptionFactory.getException($conn.getException());
                }
                $rc = (org.apache.geronimo.interop.CosNaming.NamingContext) $in.readObject(vt$5);
                return $rc;
            } catch (org.apache.geronimo.interop.rmi.iiop.client.RetryInvokeException $ex) {

                if ($retry == 3) {
                    throw $ex.getRuntimeException();
                }
            }
        }
    }
}
