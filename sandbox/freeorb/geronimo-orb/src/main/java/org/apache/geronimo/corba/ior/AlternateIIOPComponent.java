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
import java.net.UnknownHostException;

import org.omg.CORBA.portable.OutputStream;
import org.omg.IIOP.Version;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.io.EncapsulationInputStream;


public class AlternateIIOPComponent extends Component {

    private IIOPTransportSpec saddr;
    private final AbstractORB orb;
    private final String host;
    private final int port;
    private InetAddress addr;

    public AlternateIIOPComponent(AbstractORB orb, String host, int port) {
        this.orb = orb;
        this.host = host;
        this.port = port;
    }

    public int tag() {
        return TAG_ALTERNATE_IIOP_ADDRESS.value;
    }

    protected void write_content(OutputStream eo) {
        eo.write_string(host);
        eo.write_short((short) port);
    }

    public IIOPTransportSpec getInetTransport() throws UnknownHostException {
        if (saddr == null) {
            saddr = new IIOPTransportSpec(getVersion(), getAddress(), getPort());
        }
        return saddr;
    }

    private Version getVersion() {
        return orb.getIIOPVersion();
    }

    private InetAddress getAddress() throws UnknownHostException {
        if (addr == null) {
            addr = orb.getAddress(getHost());
        }
        return addr;
    }

    private String getHost() {
        return host;
    }

    private int getPort() {
        return port;
    }

    public static Component read(AbstractORB orb, byte[] data) {
        EncapsulationInputStream in = new EncapsulationInputStream(orb, data);
        String host = in.read_string();
        int port = in.read_short() & 0xffff;
        return new AlternateIIOPComponent(orb, host, port);
    }

}
