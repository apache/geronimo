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

import org.omg.IIOP.Version;

import org.apache.geronimo.corba.io.GIOPVersion;


public class IIOPTransportSpec extends GIOPTransportSpec {

    public static final String PROTO_TCP = "tcp";
    public static final String PROTO_SSL = "ssl";
    public static final String PROTO_TLS = "tls";

    InetSocketAddress addr;
    private CompoundSecurityMechanism securityMechanism;
    private final Version version;

    IIOPTransportSpec(org.omg.IIOP.Version version, InetSocketAddress addr) {
        this.version = version;
        this.addr = addr;
    }

    public IIOPTransportSpec(org.omg.IIOP.Version version, InetSocketAddress addr2, CompoundSecurityMechanism mechanism) {
        this(version, addr2);
        securityMechanism = mechanism;
    }

    public IIOPTransportSpec(org.omg.IIOP.Version version, InetAddress address, int port) {
        this(version, new InetSocketAddress(address, port));
    }

    public InetAddress getAddress() {
        return addr.getAddress();
    }

    public boolean equals(Object other) {
        if (other instanceof IIOPTransportSpec) {
            IIOPTransportSpec io = (IIOPTransportSpec) other;

            if (!getAddress().equals(io.getAddress())) {
                return false;
            }
            if (!protocol().equals(io.protocol())) {
                return false;
            }
            if (getIIOPVersion().major != io.getIIOPVersion().major) {
                return false;
            }
            if (getIIOPVersion().minor != io.getIIOPVersion().minor) {
                return false;
            }

            return true;
        }

        return false;
    }

    public int hashCode() {
        return getAddress().hashCode() + protocol().hashCode() + getIIOPVersion().minor;
    }

    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.get(getIIOPVersion());
    }

    private Version getIIOPVersion() {
        return version;
    }

    public String protocol() {
        return PROTO_TCP;
    }

    public void setSecurityMechanism(CompoundSecurityMechanism mech) {
        this.securityMechanism = mech;
    }

    public CompoundSecurityMechanism getSecurityMechanism() {
        return securityMechanism;
    }

    public int getPort() {
        return addr.getPort();
    }

    public InetSocketAddress getSocketAddress() {
        return addr;
    }

}
