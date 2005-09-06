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

package org.apache.geronimo.security.remoting.jmx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.activeio.Packet;
import org.activeio.RequestChannel;
import org.activeio.Service;
import org.activeio.SyncChannel;
import org.activeio.adapter.AsyncChannelToClientRequestChannel;
import org.activeio.adapter.PacketToInputStream;
import org.activeio.filter.PacketAggregatingSyncChannel;
import org.activeio.net.SocketMetadata;
import org.activeio.net.SocketSyncChannelFactory;
import org.activeio.packet.ByteArrayPacket;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.kernel.ObjectInputStreamExt;

/**
 * @version $Rev: 71492 $ $Date: 2004-11-14 21:31:50 -0800 (Sun, 14 Nov 2004) $
 */
public class RequestChannelInterceptor implements Interceptor {

    private final ClassLoader cl;
    private final URI target;

    public RequestChannelInterceptor(URI target, ClassLoader cl) {
        this.target = target;
        this.cl = cl;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {

        ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
        try {

            RequestChannel channel = createRequestChannel(target);
            Packet response;
            try { 
                channel.start();
                Packet request = serialize(invocation);
                response = channel.request(request, Service.WAIT_FOREVER_TIMEOUT);
            } finally {
                channel.dispose();                
            }
            
            Object obj;
            try {            
                obj =  deserialize(response, cl);
            } catch ( ClassNotFoundException e ) { 
                // Weird.
                Thread.currentThread().setContextClassLoader(RequestChannelInterceptor.class.getClassLoader());
                response.clear();
                obj =  deserialize(response, cl);
            }

            // Are we demarshalling a thrown exception.
            if (obj instanceof RequestChannelInterceptorInvoker.ThrowableWrapper) {
                throw ((RequestChannelInterceptorInvoker.ThrowableWrapper) obj).exception;
            }
            return (InvocationResult)obj;
            
        } finally {
            Thread.currentThread().setContextClassLoader(originalLoader);
        }
    }
    
    private static RequestChannel createRequestChannel(URI target) throws IOException, URISyntaxException {
        SocketSyncChannelFactory factory = new SocketSyncChannelFactory();
        SyncChannel channel = factory.openSyncChannel(target);
        SocketMetadata socket = (SocketMetadata) channel.narrow(SocketMetadata.class);
        socket.setTcpNoDelay(true);
        return new AsyncChannelToClientRequestChannel(
	               new PacketAggregatingSyncChannel(
                       channel));        
    }

    /**
     * @param response
     * @param cl
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    static public Object deserialize(Packet response, ClassLoader cl) throws IOException, ClassNotFoundException {
        ObjectInputStreamExt is = new ObjectInputStreamExt(new PacketToInputStream(response), cl);
        Object rc = is.readObject();
        is.close();
        return rc;
    }

    static public Packet serialize(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        return new ByteArrayPacket(baos.toByteArray());
    }
}
