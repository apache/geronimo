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
package org.apache.geronimo.corba.csi.gssup;

public class GSSUPPolicyValueHelper {

    private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB
            .init()
            .create_struct_tc(
                    GSSUPPolicyValueHelper.id(),
                    "GSSUPPolicyValue",
                    new org.omg.CORBA.StructMember[]{
                            new org.omg.CORBA.StructMember(
                                    "mode",
                                    org.omg.CORBA.ORB
                                            .init()
                                            .create_enum_tc(
                                            org.omg.Security.RequiresSupportsHelper
                                                    .id(),
                                            "RequiresSupports",
                                            new String[]{
                                                    "SecRequires",
                                                    "SecSupports"}),
                                    null),
                            new org.omg.CORBA.StructMember("domain",
                                                           org.omg.CORBA.ORB.init()
                                                                   .create_string_tc(0), null)});

    public GSSUPPolicyValueHelper() {
    }

    public static void insert(org.omg.CORBA.Any any, GSSUPPolicyValue s) {
        any.type(type());
        write(any.create_output_stream(), s);
    }

    public static GSSUPPolicyValue extract(org.omg.CORBA.Any any) {
        return read(any.create_input_stream());
    }

    public static org.omg.CORBA.TypeCode type() {
        return _type;
    }

    public String get_id() {
        return id();
    }

    public org.omg.CORBA.TypeCode get_type() {
        return type();
    }

    public void write_Object(org.omg.CORBA.portable.OutputStream out,
                             java.lang.Object obj)
    {
        throw new RuntimeException(" not implemented");
    }

    public java.lang.Object read_Object(org.omg.CORBA.portable.InputStream in) {
        throw new RuntimeException(" not implemented");
    }

    public static String id() {
        return "IDL:com/trifork/eas/api/csi/GSSUPPolicyValue:1.0";
    }

    public static GSSUPPolicyValue read(
            org.omg.CORBA.portable.InputStream in)
    {
        GSSUPPolicyValue result = new GSSUPPolicyValue();
        result.mode = org.omg.Security.RequiresSupportsHelper.read(in);
        result.domain = in.read_string();
        return result;
    }

    public static void write(org.omg.CORBA.portable.OutputStream out,
                             GSSUPPolicyValue s)
    {
        org.omg.Security.RequiresSupportsHelper.write(out, s.mode);
        out.write_string(s.domain);
    }
}
