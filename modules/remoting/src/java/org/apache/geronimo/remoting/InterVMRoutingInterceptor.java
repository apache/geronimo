/**
 *
 * Copyright 2004 The Apache Software Foundation
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

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:02 $
 */
public class InterVMRoutingInterceptor implements Interceptor, Externalizable {

    String targetVMID = getLocalVMID();
    transient Interceptor next;
    TransportInterceptor transportInterceptor;
    Interceptor localInterceptor;

    public InterVMRoutingInterceptor() {
    }

    public InterVMRoutingInterceptor(Interceptor transportInterceptor, Interceptor localInterceptor) {
        this.transportInterceptor = (TransportInterceptor) transportInterceptor;
        this.localInterceptor = localInterceptor;
        next = localInterceptor;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        return next.invoke(invocation);
    }

    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(targetVMID);
        out.writeObject(transportInterceptor);
        out.writeObject(localInterceptor);
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        targetVMID = in.readUTF();
        transportInterceptor = (TransportInterceptor) in.readObject();
        localInterceptor = (Interceptor) in.readObject();

        if (getLocalVMID().equals(targetVMID)) {
            next = localInterceptor;
        } else {
            // We have to marshall first..
            next = new MarshalingInterceptor(transportInterceptor);
        }
    }
    
    final static String localVMID = "VM:"+System.currentTimeMillis(); 
    public static String getLocalVMID() {
        return localVMID;
    }

    /**
     * @return
     */
    public Interceptor getLocalInterceptor() {
        return localInterceptor;
    }

    /**
     * @param localInterceptor
     */
    public void setLocalInterceptor(Interceptor localInterceptor) {
        this.localInterceptor = localInterceptor;
    }

    /**
     * @return
     */
    public TransportInterceptor getTransportInterceptor() {
        return transportInterceptor;
    }

    /**
     * @param remotingInterceptor
     */
    public void setTransportInterceptor(TransportInterceptor remotingInterceptor) {
        this.transportInterceptor = remotingInterceptor;
    }

    /**
     * @return
     */
    public String getTargetVMID() {
        return targetVMID;
    }

    /**
     * @param targetVMID
     */
    public void setTargetVMID(String targetVMID) {
        this.targetVMID = targetVMID;
    }

}
