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
package org.apache.geronimo.corba;

import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.portable.OutputStream;

import org.apache.geronimo.corba.giop.GIOPOutputStream;
import org.apache.geronimo.corba.io.OutputStreamBase;


public class InvocationProfileSelector {

    private ClientDelegate delegate;

    private InvocationProfile[] profiles;

    private int currentProfile;

    private final ORB orb;

    public InvocationProfileSelector(ORB orb, ClientDelegate delegate) {
        this.orb = orb;
        this.delegate = delegate;
        this.profiles = getProfileList();
    }

    private InvocationProfile[] getProfileList() {
        return getORB().getInvocationProfiles(delegate.getIOR());
    }

    private ORB getORB() {
        return orb;
    }

    public OutputStreamBase setupRequest(String operation, boolean responseExpected, Policies policies)
            throws LocationForwardException
    {
        while (true) {
            ClientInvocation invocation = createClientInvcation(operation,
                                                                responseExpected, policies);

            try {

                OutputStreamBase out = invocation.startRequest();
                
                out.setClientInvocation(invocation);
                
                return out;

            }
            catch (RuntimeException ex) {

                currentProfile++;

                if (currentProfile >= profiles.length) {
                    throw ex;
                }

			}
        }

    }

    private ClientInvocation createClientInvcation(String operation,
                                                   boolean responseExpected,
                                                   Policies policies)
    {

        if (currentProfile >= profiles.length) {
            throw new NO_RESOURCES();
        }

        InvocationProfile profile = profiles[currentProfile];

        return new ClientInvocation(orb, this, operation, responseExpected, profile, policies);
    }

    public ClientDelegate getDelegate() {
        return delegate;
    }

	public void reset() {
		currentProfile = 0;
	}

}
