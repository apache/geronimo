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

package org.apache.geronimo.remoting;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.remoting.transport.NullTransportInterceptor;

/**
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:59:19 $
 */
public class IntraVMRoutingInterceptor implements Interceptor, Externalizable {

    Long deMarshalingInterceptorID;
    boolean allwaysMarshall=false;
    transient Interceptor next;

    public IntraVMRoutingInterceptor(Interceptor next, Long deMarshalingInterceptorID, boolean allwaysMarshall) {
        this.next = next;
        this.deMarshalingInterceptorID = deMarshalingInterceptorID;
        this.allwaysMarshall = allwaysMarshall;
    }

    public IntraVMRoutingInterceptor() {
    }

    public IntraVMRoutingInterceptor(Long deMarshalingInterceptorID, boolean allwaysMarshall) {
        this.deMarshalingInterceptorID = deMarshalingInterceptorID;
        this.allwaysMarshall = allwaysMarshall;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        return next.invoke(invocation);
    }

    /**
     *
     */
    synchronized private void resolveNext() {
        // Get the demarshaling interceptor to find out the classloader scope that the target
        // app is in.
        DeMarshalingInterceptor deMarshalingInterceptor =
            (DeMarshalingInterceptor) InterceptorRegistry.instance.lookup(deMarshalingInterceptorID);
        
        if( deMarshalingInterceptor==null ) {
            // Forget it.. we will not be able to route locally.
            return;
        }
        ClassLoader parent = deMarshalingInterceptor.getClassloader();
        ClassLoader child = Thread.currentThread().getContextClassLoader();

        // Did we deserialize with the same app classloader that
        // the target belongs to??
        if (InvocationSupport.isAncestor(parent, child) && !allwaysMarshall) {
            // Then we can avoid demarshalling/marshalling
            next = deMarshalingInterceptor.getNext();
        } else {
            next = new MarshalingInterceptor(new NullTransportInterceptor(deMarshalingInterceptor));
        }
    }

    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(deMarshalingInterceptorID.longValue());
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        deMarshalingInterceptorID = new Long(in.readLong());
        resolveNext();
    }

    /**
     * @return
     */
    public Long getDeMarshalingInterceptorID() {
        return deMarshalingInterceptorID;
    }

    /**
     * @param deMarshalingInterceptorID
     */
    public void setDeMarshalingInterceptorID(Long deMarshalingInterceptorID) {
        this.deMarshalingInterceptorID = deMarshalingInterceptorID;
    }

    public boolean getAlwaysMarshall() {
        return allwaysMarshall;
    }

    public void setAlwaysMarshall(boolean value) {
        allwaysMarshall=value;
    }
}
