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

package org.apache.geronimo.remoting.transport.async;

import java.net.URI;

import org.apache.geronimo.remoting.transport.TransportClient;
import org.apache.geronimo.remoting.transport.TransportServer;
import org.apache.geronimo.remoting.transport.async.bio.BlockingChannel;
import org.apache.geronimo.remoting.transport.async.bio.BlockingServer;
import org.apache.geronimo.remoting.transport.async.nio.NonBlockingChannel;
import org.apache.geronimo.remoting.transport.async.nio.NonBlockingServer;

/**
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:20 $
 */
public class TransportFactory extends org.apache.geronimo.remoting.transport.TransportFactory {

    /** 
     * Should we used Blocking IO instead of Non-blocking IO.  We default
     *  to using Non-blocking IO
     */
    static public final boolean USE_BLOCKING_IO =
        new Boolean(System.getProperty("org.apache.geronimo.remoting.transport.async.use_blocking_io", "true"))
            .booleanValue();

    static final public TransportFactory instance = new TransportFactory();
    public TransportFactory() {
    }

    /**
     * @see org.apache.geronimo.remoting.transport.TransportFactory#handles(java.net.URI)
     */
    protected boolean handles(URI uri) {
        return "async".equals(uri.getScheme());
    }

    /**
     * @see org.apache.geronimo.remoting.transport.TransportFactory#createClient()
     */
    public TransportClient createClient() {
        return new AsyncClient();
    }

    /**
     * @see org.apache.geronimo.remoting.transport.TransportFactory#createSever()
     */
    public TransportServer createSever() {
        if (USE_BLOCKING_IO)
            return new BlockingServer();
        return new NonBlockingServer();
    }

    /**
     * Factory method to create AsynchChannel instances.
     */
    public Channel createAsynchChannel() {
        if (USE_BLOCKING_IO)
            return new BlockingChannel();
        return new NonBlockingChannel();
    }

    /**
     * @see org.apache.geronimo.remoting.transport.TransportFactory#doUnexport(java.lang.Object)
     */
    public boolean doUnexport(Object object) {
        return Registry.instance.unexportObject(object);
    }

}
