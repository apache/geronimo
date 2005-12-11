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

import org.apache.geronimo.corba.LocationForwardException;
import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.ServerManager;
import org.apache.geronimo.corba.ior.InternalIOR;
import org.omg.CORBA.Policy;

public class DefaultServerManager implements ServerManager {

	private final ORB orb;

	public DefaultServerManager(ORB orb) {
		this.orb = orb;
	}

	public ServantObject getServantObject(InternalIOR ior, Policy[] policies)
			throws LocationForwardException {
		// TODO Auto-generated method stub
		return null;
	}

}
