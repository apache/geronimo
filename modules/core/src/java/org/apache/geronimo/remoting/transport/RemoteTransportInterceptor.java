/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.4 $ $Date: 2003/11/07 16:41:10 $
 */
public class RemoteTransportInterceptor implements TransportInterceptor, Externalizable {

    URI remoteURI;
    transient TransportClient transportClient;

    public RemoteTransportInterceptor() {
    }

    public RemoteTransportInterceptor(URI remoteURI) {
        this.remoteURI = remoteURI;
    }

    /**
     * @see org.apache.geronimo.core.service.AbstractInterceptor#invoke(org.apache.geronimo.core.service.Invocation)
     */
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
            return new SimpleInvocationResult(msg.popMarshaledObject());
        } else {
            transportClient.sendDatagram(remoteURI, msg);
            MarshalledObject rcmo = transportClient.createMarshalledObject();
            rcmo.set(null);
            return new SimpleInvocationResult(rcmo);
        }
    }

    /**
     * @see org.apache.geronimo.core.service.Interceptor#getNext()
     */
    public Interceptor getNext() {
        return null;
    }

    /**
     * @see org.apache.geronimo.core.service.Interceptor#setNext(org.apache.geronimo.core.service.Interceptor)
     */
    public void setNext(Interceptor interceptor) throws IllegalStateException {
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
