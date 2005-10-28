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

import java.util.List;

import org.omg.PortableInterceptor.ForwardRequest;

import org.apache.geronimo.corba.giop.GIOPOutputStream;


public class ClientInvocation implements Invocation {

    private final InvocationProfileSelector manager;
    private final String operation;
    private final boolean responseExpected;

    /** */
    private final InvocationProfile profile;

    private List requestServiceContextList;

    public ClientInvocation(InvocationProfileSelector manager,
                            String operation,
                            boolean responseExpected,
                            InvocationProfile profile
    )
    {
        this.manager = manager;
        this.operation = operation;
        this.responseExpected = responseExpected;
        this.profile = profile;
    }

    ClientDelegate getDelegate() {
        return manager.getDelegate();
    }


    public GIOPOutputStream startRequest()
            throws ForwardRequest
    {
        return profile.startRequest();
    }

}
