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
package org.apache.geronimo.corba.ior;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.omg.CORBA.portable.InputStream;
import org.omg.IIOP.Version;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.io.EncapsulationInputStream;


public class SSLSecureTransport extends SecureInetTransport {

    SSLSecureTransport(Version version, InetSocketAddress addr, short target_requires,
                       short target_supports, CompoundSecurityMechanism mechanism)
    {
        super(version, addr, target_requires, target_supports, mechanism);
        // TODO Auto-generated constructor stub
    }

    public static SecureInetTransport read(AbstractORB orb, InetAddress profile,
                                           Version version, byte[] component_data, CompoundSecurityMechanism mechanism)
    {

        InputStream is = new EncapsulationInputStream(orb, component_data);
        org.omg.SSLIOP.SSL ssl = org.omg.SSLIOP.SSLHelper.read(is);

        InetSocketAddress addr = new InetSocketAddress(profile, ssl.port & 0xffff);
        SSLSecureTransport trans = new SSLSecureTransport(version, addr,
                                                          ssl.target_requires, ssl.target_supports, mechanism);

        return trans;

    }

    public String protocol() {
        return PROTO_SSL;
    }

}
