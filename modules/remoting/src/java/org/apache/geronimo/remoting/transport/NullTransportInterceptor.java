/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.remoting.transport;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.remoting.MarshalledObject;
import org.apache.geronimo.remoting.TransportInterceptor;

/**
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:19 $
 */
public class NullTransportInterceptor implements TransportInterceptor {

    private Interceptor next;

    public NullTransportInterceptor(Interceptor next) {
        this.next = next;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        return next.invoke(invocation);
    }

    /**
     * @see org.apache.geronimo.remoting.TransportInterceptor#createMarshalledObject()
     */
    public MarshalledObject createMarshalledObject() {
        return new BytesMarshalledObject();
    }

}
