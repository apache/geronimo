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

import org.apache.geronimo.remoting.transport.TransportException;

/**
 * An AsynchChannel allows you to transport bytes of data
 * back and forth between a client and a server in a async 
 * manner.
 * 
 * This interace abstraction is here so that it can be implemented 
 * using both the Blocking and Non-blocking IO APIs.
 * 
 * @version $Rev$ $Date$
 */
public interface Channel {

    /**
     * opens a connection to another server.
     * 
     * @param uri
     * @param localURI
     * @param listner
     * @throws IOException
     * @throws ConnectionFailedException
     */
    public void open(URI uri, URI backConnectURI, ChannelListner listner) throws TransportException;

    /**
     * starts an accepted connection.
     * 
     * @param listner
     * @throws IOException
     */
    public void open(ChannelListner listner) throws TransportException;

    public void close() throws TransportException;

    /**
     * Sends an asynch packet of data down the channel.  It does not 
     * wait wait for a response if possible.
     */
    public void send(AsyncMsg data) throws TransportException;

}
