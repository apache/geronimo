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

import org.omg.IIOP.Version;


public class SecureInetTransport extends IIOPTransportSpec {

    private final short target_requires;
    private final short target_supports;

    SecureInetTransport(Version version, InetSocketAddress addr, short target_requires, short target_supports, CompoundSecurityMechanism mechanism) {
        super(version, addr, mechanism);
        this.target_requires = target_requires;
        this.target_supports = target_supports;
    }

}
