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

import org.apache.geronimo.corba.ClientDelegate;
import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.PlainObject;
import org.apache.geronimo.corba.server.POA;
import org.omg.CORBA.Object;

public final class URLManager {
	private final ORB orb;

	public URLManager(ORB orb) {
		this.orb = orb;
	}

	public Object createObject(InternalIOR ior) {
		orb.__checkDestroy();
		if (ior == null)
			return null;
		ClientDelegate delegate = new ClientDelegate(ior);
		PlainObject result = new PlainObject(delegate);
		return result;
	}

	public org.omg.CORBA.Object createObject(org.omg.IOP.IOR ior) {
		orb.__checkDestroy();

		if (ior == null)
			return null;

		if (ior.type_id.length() == 0 && ior.profiles.length == 0)
			return null;

		// construct delegate
		ClientDelegate delegate = new ClientDelegate(orb, ior);

		// construct a PlainObject
		PlainObject result = new PlainObject(delegate);

		return result;
	}

	public org.omg.CORBA.Object createObject(POA poa, byte[] oid,
			String repository_id) {
		orb.__checkDestroy();

		if (repository_id.length() == 0)
			return null;

		ClientDelegate delegate = new ClientDelegate(orb, poa, oid,
				repository_id, orb.getPolicies());

		PlainObject result = new PlainObject(delegate);

		return result;
	}

	public org.omg.CORBA.Object createObject(String url) {
		URLParser handler = new URLParser(orb, url);
		return createObject(handler.getIOR());
	}
}
