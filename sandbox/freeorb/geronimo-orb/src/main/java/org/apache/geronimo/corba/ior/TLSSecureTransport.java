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

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CSIIOP.TLS_SEC_TRANS;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.io.EncapsulationInputStream;


public class TLSSecureTransport extends SecureInetTransport {

    public TLSSecureTransport(AbstractORB orb, TLS_SEC_TRANS trans, int idx, CompoundSecurityMechanism mechanism)
            throws UnknownHostException
    {
        super(orb.getIIOPVersion(), new InetSocketAddress(orb
                .getAddress(trans.addresses[idx].host_name),
                                                          trans.addresses[idx].port & 0xffff), trans.target_requires,
                                                                                               trans.target_supports, mechanism);

    }

    public static IIOPTransportSpec[] read(AbstractORB orb, byte[] component_data, CompoundSecurityMechanism mechanism) {
        InputStream in = new EncapsulationInputStream(orb, component_data);
        org.omg.CSIIOP.TLS_SEC_TRANS trans = org.omg.CSIIOP.TLS_SEC_TRANSHelper
                .read(in);

        List list = new ArrayList();
        for (int i = 0; i < trans.addresses.length; i++) {
            try {
                list.add(new TLSSecureTransport(orb, trans, i, mechanism));
            }
            catch (UnknownHostException e) {
                // ignore - we don't know the targe thost //
            }
        }

        TLSSecureTransport[] result = new TLSSecureTransport[list.size()];
        list.toArray(result);
        return result;
    }

    public String protocol() {
        return PROTO_TLS;
    }

}
