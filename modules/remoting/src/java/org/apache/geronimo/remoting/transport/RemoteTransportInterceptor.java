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

package org.apache.geronimo.remoting.transport;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import org.apache.geronimo.remoting.InvocationSupport;
import org.apache.geronimo.remoting.InvocationType;
import org.apache.geronimo.remoting.MarshalledObject;
import org.apache.geronimo.remoting.TransportInterceptor;

/**
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:58:03 $
 */
public class RemoteTransportInterceptor implements TransportInterceptor, Externalizable {

    URI remoteURI;
    transient TransportClient transportClient;

    public RemoteTransportInterceptor() {
    }

    public RemoteTransportInterceptor(URI remoteURI) {
        this.remoteURI = remoteURI;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {

        MarshalledObject mo = InvocationSupport.getMarshaledValue(invocation);
        // URI remoteURI = InvocationSupport.getRemoteURI(invocation);
        InvocationType type = InvocationSupport.getInvocationType(invocation);
        if (type == null)
            type = InvocationType.REQUEST;

        Msg msg = transportClient.createMsg();
        msg.pushMarshaledObject(mo);
        if (type == InvocationType.REQUEST) {
            msg = transportClient.sendRequest(remoteURI, msg);
            return new SimpleInvocationResult(true, msg.popMarshaledObject());
        } else {
            transportClient.sendDatagram(remoteURI, msg);
            MarshalledObject rcmo = transportClient.createMarshalledObject();
            rcmo.set(null);
            return new SimpleInvocationResult(true, rcmo);
        }
    }

    /**
     * @see org.apache.geronimo.remoting.TransportInterceptor#createMarshalledObject()
     */
    public MarshalledObject createMarshalledObject() {
        return transportClient.createMarshalledObject();
    }

    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(remoteURI.toString());
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        try {
            remoteURI = new URI(in.readUTF());
            TransportFactory tf = TransportFactory.getTransportFactory(remoteURI);
            transportClient = tf.createClient();
        } catch (URISyntaxException e) {
            throw new StreamCorruptedException(e.getMessage());
        }
    }

    /**
     * @return
     */
    public URI getRemoteURI() {
        return remoteURI;
    }

    /**
     * @param remoteURI
     */
    public void setRemoteURI(URI remoteURI) {
        this.remoteURI = remoteURI;
        TransportFactory tf = TransportFactory.getTransportFactory(remoteURI);
        transportClient = tf.createClient();
    }

}
