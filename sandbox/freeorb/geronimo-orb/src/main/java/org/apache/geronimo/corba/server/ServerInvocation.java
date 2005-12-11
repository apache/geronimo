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

import org.apache.geronimo.corba.Invocation;
import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.Policies;
import org.apache.geronimo.corba.ior.InternalServiceContextList;

/** reification of server-side invocation */
public class ServerInvocation implements Invocation {

	public String getOperation() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getRequestID() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean responseExpected() {
		// TODO Auto-generated method stub
		return false;
	}

	public short getSyncScope() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isResponseExpected() {
		// TODO Auto-generated method stub
		return false;
	}

	public InternalServiceContextList getResponseServiceContextList(boolean create) {
		// TODO Auto-generated method stub
		return null;
	}

	public InternalServiceContextList getRequestServiceContextList(boolean create) {
		// TODO Auto-generated method stub
		return null;
	}

	public ORB getORB() {
		// TODO Auto-generated method stub
		return null;
	}

	public Policies getPolicies() {
		// TODO Auto-generated method stub
		return null;
	}

}
