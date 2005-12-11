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
package org.apache.geronimo.corba.io;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import EDU.oswego.cs.dl.util.concurrent.SyncMap;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.CSIIOP.TAG_TLS_SEC_TRANS;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.SSLIOP.TAG_SSL_SEC_TRANS;

import org.apache.geronimo.corba.ConnectionManager;
import org.apache.geronimo.corba.InvocationProfile;
import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.channel.TransportManager;
import org.apache.geronimo.corba.channel.nio.AsyncNIOTransportManager;
import org.apache.geronimo.corba.ior.AlternateIIOPComponent;
import org.apache.geronimo.corba.ior.CompoundSecurityMechanism;
import org.apache.geronimo.corba.ior.IIOPProfile;
import org.apache.geronimo.corba.ior.IIOPTransportSpec;
import org.apache.geronimo.corba.ior.InternalIOR;
import org.apache.geronimo.corba.ior.SecurityMechanismListComponent;


public class DefaultConnectionManager implements ConnectionManager {

    private final ORB orb;

    SyncMap connectionFactories = new SyncMap(new HashMap(),
                                              new WriterPreferenceReadWriteLock());

    TransportManager tcpTransportManager;
    TransportManager sslTransportManager;
    TransportManager tlsTransportManager;
    
    
    public DefaultConnectionManager(ORB orb) throws IOException {
        this.orb = orb;
        
        tcpTransportManager = new AsyncNIOTransportManager(orb.getExecutor());
        try {
			tcpTransportManager.start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }

    public InvocationProfile[] getInvocationProfiles(InternalIOR ior) {

        List profs = new ArrayList();

        for (int i = 0; i < ior.getProfileCount(); i++) {
            if (ior.profileTag(i) == TAG_INTERNET_IOP.value) {
                getInvocationProfilesForOneProfile(profs, (IIOPProfile) ior
                        .getProfile(i));
            }

        }

        InvocationProfile[] result = new InvocationProfile[profs.size()];
        profs.toArray(result);
        return result;

    }

    private void getInvocationProfilesForOneProfile(List profs,
                                                    IIOPProfile profile)
    {

        boolean add_default_profile = true;

        for (int i = 0; i < profile.getComponentCount(); i++) {
            switch (profile.getTag(i)) {
                case TAG_ALTERNATE_IIOP_ADDRESS.value:
                    AlternateIIOPComponent aiiopc = (AlternateIIOPComponent) profile
                            .getComponent(i);
                    try {
                        addProfile(profs, profile, aiiopc.getInetTransport());
                    }
                    catch (UnknownHostException e) {
                        // ignore -- we don't know this host //
                    }
                    break;

                case TAG_CSI_SEC_MECH_LIST.value:
                    SecurityMechanismListComponent secmeclist = (SecurityMechanismListComponent) profile
                            .getComponent(i);

                    for (int j = 0; j < secmeclist.getMechanismCount(); j++) {

                        CompoundSecurityMechanism mech = secmeclist
                                .getSecurityMechanism(j);

                        IIOPTransportSpec pt = null;
                        switch (mech.getTransportTag()) {
                            case TAG_SSL_SEC_TRANS.value:
                            case TAG_TLS_SEC_TRANS.value:
                                try {
                                    pt = profile.getInetTransport();
                                }
                                catch (UnknownHostException e) {
                                    // ignore //
                                }
                                IIOPTransportSpec[] transports = mech.getTransports(pt
                                        .getAddress(), profile.getVersion());
                                for (int k = 0; k < transports.length; k++) {
                                    addProfile(profs, profile, transports[k]);
                                }

                                if (pt != null && pt.getPort() != 0) {
                                    IIOPTransportSpec it = new IIOPTransportSpec(
                                            profile.getVersion(),
                                            pt.getSocketAddress(), mech);
                                    addProfile(profs, profile, it);
                                    add_default_profile = false;
                                }

                        }
                    }
            }

        }

        if (add_default_profile && profile.getPort() != 0) {
            try {
                addProfile(profs, profile, profile.getInetTransport());
            }
            catch (UnknownHostException e) {
                // ignore -- we don't know this host //
            }

        }

    }

    private void addProfile(List profs, IIOPProfile profile,
                            IIOPTransportSpec transport)
    {

        ClientConnectionFactory endpoint = getClientEndpoint(transport);

        InvocationProfile ip = new IIOPInvocationProfile(profile, endpoint);

        profs.add(ip);

    }

    private ClientConnectionFactory getClientEndpoint(
            IIOPTransportSpec transportSpec)
    {
        ClientConnectionFactory ccf = (ClientConnectionFactory) connectionFactories
                .get(transportSpec);

        if (ccf == null) {
            String protocol = transportSpec.protocol();
            if (IIOPTransportSpec.PROTO_TCP.equals(protocol)) {
                ccf = new TCPClientConnectionFactory(orb, transportSpec, tcpTransportManager);
            } else if (IIOPTransportSpec.PROTO_SSL.equals(protocol)) {
                ccf = new SSLClientConnectionFactory(orb, transportSpec, sslTransportManager);
            } else if (IIOPTransportSpec.PROTO_TLS.equals(protocol)) {
                ccf = new TLSClientConnectionFactory(orb, transportSpec, tlsTransportManager);
            }
        }

        return ccf;
    }

}
