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

import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.channel.Transport;
import org.apache.geronimo.corba.channel.TransportManager;
import org.apache.geronimo.corba.giop.GIOPMessageTransport;
import org.apache.geronimo.corba.ior.IIOPTransportSpec;


public class TCPClientConnectionFactory implements ClientConnectionFactory {

    private final IIOPTransportSpec spec;
	private ClientConnection connection;
	private final ORB orb;
	private TransportManager transportmanger;

    public TCPClientConnectionFactory(ORB orb, IIOPTransportSpec spec, TransportManager manager) {
        this.orb = orb;
		this.spec = spec;
		this.transportmanger = manager;
    }

    public ClientConnection getConnection() {
    		
	    	if (connection == null) {
	    		connection = new DefaultClientConnection(this, transportmanger);
	    	}
	    	
	    	return connection;
	    	
    }

    public IIOPTransportSpec getTransportSpec() {
        return spec;
    }

	public ORB getORB() {
		return orb;
	}


}
