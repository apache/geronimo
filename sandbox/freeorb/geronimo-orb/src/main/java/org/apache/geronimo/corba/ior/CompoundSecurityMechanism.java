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

import org.omg.CSIIOP.CompoundSecMech;
import org.omg.CSIIOP.TAG_TLS_SEC_TRANS;
import org.omg.IIOP.Version;
import org.omg.SSLIOP.TAG_SSL_SEC_TRANS;

import org.apache.geronimo.corba.AbstractORB;


public class CompoundSecurityMechanism {

    private final CompoundSecMech mech;

    private final AbstractORB orb;

    private IIOPTransportSpec[] transport;

    public CompoundSecurityMechanism(AbstractORB orb, CompoundSecMech mech) {
        this.orb = orb;
        this.mech = mech;
    }

    public int getTransportTag() {
        return mech.transport_mech.tag;
    }

    public IIOPTransportSpec[] getTransports(InetAddress profileAddress, Version version) {
        if (transport == null) {
            switch (getTransportTag()) {
                case TAG_TLS_SEC_TRANS.value: {
                    transport = TLSSecureTransport.read(orb,
                                                        mech.transport_mech.component_data, this);
                    break;
                }
                case TAG_SSL_SEC_TRANS.value: {
                    if (profileAddress != null) {
                        transport = new IIOPTransportSpec[]{SSLSecureTransport.read(
                                orb, profileAddress, version,
                                mech.transport_mech.component_data, this)};
                        break;
                    }
                }
                default:
                    transport = new IIOPTransportSpec[0];
            }
        }

        return transport;
    }

}
