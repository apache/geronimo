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
package org.apache.geronimo.corba.server;

import org.omg.CORBA.LocalObject;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAManagerPackage.State;

/** A poa manager represents a set of servers. 
 * 
 * Each server is a logical "endpoint", so a single poa manager can manage
 * both a plain and an ssl server socket. */

public class POAManager extends LocalObject implements
		org.omg.PortableServer.POAManager {

	public void activate() throws AdapterInactive {
		// TODO Auto-generated method stub

	}

	public void hold_requests(boolean arg0) throws AdapterInactive {
		// TODO Auto-generated method stub

	}

	public void discard_requests(boolean arg0) throws AdapterInactive {
		// TODO Auto-generated method stub

	}

	public void deactivate(boolean arg0, boolean arg1) throws AdapterInactive {
		// TODO Auto-generated method stub

	}

	public State get_state() {
		// TODO Auto-generated method stub
		return null;
	}

	public void __checkActive() throws org.omg.CORBA.TRANSIENT {
		// TODO Auto-generated method stub
		
	}

}
