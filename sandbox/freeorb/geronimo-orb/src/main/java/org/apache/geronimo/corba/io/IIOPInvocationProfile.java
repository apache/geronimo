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

import org.apache.geronimo.corba.ClientDelegate;
import org.apache.geronimo.corba.ClientInvocation;
import org.apache.geronimo.corba.InvocationProfile;
import org.apache.geronimo.corba.giop.GIOPInputStream;
import org.apache.geronimo.corba.giop.GIOPMessageTransport;
import org.apache.geronimo.corba.giop.GIOPOutputStream;
import org.apache.geronimo.corba.giop.RequestID;
import org.apache.geronimo.corba.ior.IIOPProfile;
import org.apache.geronimo.corba.ior.InternalServiceContextList;
import org.apache.geronimo.corba.ior.InternalTargetAddress;
import org.apache.geronimo.corba.ior.Profile;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.portable.InputStream;

public class IIOPInvocationProfile implements InvocationProfile {

	private IIOPProfile profile;

	private ClientConnectionFactory endpoint;

	private GIOPMessageTransport mt;

	public IIOPInvocationProfile(IIOPProfile profile,
			ClientConnectionFactory endpoint) {
		this.profile = profile;
		this.endpoint = endpoint;

		ClientConnection conn = endpoint.getConnection();

		try {
			mt = conn.getGIOPMessageTransport();
		} catch (IOException e) {
			e.printStackTrace();
			TRANSIENT tt =  new TRANSIENT();
			tt.initCause(e);
			throw tt;
		}
	}

	public GIOPOutputStream startRequest(ClientInvocation inv) {

		byte[] principal = new byte[0]; // TODO: old-style principal handling
		InternalTargetAddress targetAddress = profile.getTargetAddress();
		GIOPVersion version = profile.getGIOPVersion();

		try {
			return mt.startRequest(version, targetAddress, inv, principal);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MARSHAL ex = new MARSHAL("failed to start request", MinorCodes.REQUEST_START_FAILED,
					CompletionStatus.COMPLETED_NO);
			ex.initCause(e);
			throw ex;
		}
	}

	public InputStreamBase invoke(ClientInvocation invocation,
			ClientDelegate delegate, OutputStreamBase out) {

		GIOPOutputStream gout = (GIOPOutputStream) out;

		if (invocation.isResponseExpected()) {
			mt.registerResponse(invocation.getRequestIDObject());
		}

		// push message
		gout.finishGIOPMessage();

		if (!invocation.isResponseExpected()) {
			return null;
		}

		GIOPInputStream in = mt.waitForResponse(invocation);
		
		return in;
	}

	public void releaseReply(InputStreamBase in) {
		GIOPInputStream gin = (GIOPInputStream) in;
		gin.finishGIOPMessage();		
	}

	public Profile getProfile() {
		return profile;
	}

}
