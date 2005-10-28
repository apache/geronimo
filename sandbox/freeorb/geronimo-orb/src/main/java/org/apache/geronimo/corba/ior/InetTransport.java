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


public class InetTransport {

    InetSocketAddress addr;
    private CompoundSecurityMechanism securityMechanism;

    InetTransport(InetSocketAddress addr) {
        this.addr = addr;
    }

    public InetTransport(InetSocketAddress addr2, CompoundSecurityMechanism mechanism) {
        this(addr2);
        securityMechanism = mechanism;
    }

    public InetTransport(InetAddress address, int port) {
        this(new InetSocketAddress(address, port));
    }

    public InetAddress getAddress() {
        return addr.getAddress();
    }

    public String protocol() {
        return "tcp";
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
