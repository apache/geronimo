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

package org.apache.geronimo.network.protocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.network.SelectionEventListner;
import org.apache.geronimo.network.SelectorManager;


/**
 * @version $Revision: 1.5 $ $Date: 2004/04/25 02:03:37 $
 */
public class DatagramProtocol implements Protocol, SelectionEventListner {

    final static private Log log = LogFactory.getLog(DatagramProtocol.class);

    private Protocol up;

    private URI connectURI;
    private DatagramChannel source;
    private DatagramChannel destination;
    private InetSocketAddress sourceAddress;
    private InetSocketAddress destinationInterface;

    private SelectorManager selectorManager;
    private SelectionKey selectionKey;

    private final int STARTED = 0;
    private final int STOPPED = 1;
    private int state = STOPPED;


    public Protocol getUpProtocol() {
        return up;
    }

    public void setUpProtocol(Protocol up) {
        this.up = up;
    }

    public Protocol getDownProtocol() {
        throw new UnsupportedOperationException("Datagram protocol is at the bottom");
    }

    public void setDownProtocol(Protocol down) {
        throw new UnsupportedOperationException("Datagram protocol is at the bottom");
    }

    public URI getConnectURI() {
        return connectURI;
    }

    public SocketAddress getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(InetSocketAddress sourceAddress) {
        if (state == STARTED) throw new IllegalStateException("Protocol already started");
        this.sourceAddress = sourceAddress;
    }

    public SocketAddress getDestinationInterface() {
        return destinationInterface;
    }

    public void setDestinationInterface(InetSocketAddress destinationInterface) {
        if (state == STARTED) throw new IllegalStateException("Protocol already started");
        this.destinationInterface = destinationInterface;
    }

    public SelectorManager getSelectorManager() {
        return selectorManager;
    }

    public void setSelectorManager(SelectorManager selectorManager) {
        if (state == STARTED) throw new IllegalStateException("Protocol already started");
        this.selectorManager = selectorManager;
    }

    public void clearLinks() {
        up = null;
    }

    public Protocol cloneProtocol() throws CloneNotSupportedException {
        return (Protocol) super.clone();
    }

    public void setup() throws ProtocolException {
        try {
            if (sourceAddress != null) {
                source = DatagramChannel.open();
                source.configureBlocking(true);
                source.socket().bind(sourceAddress);
                source.socket().setReuseAddress(true);
                source.configureBlocking(false);
                selectionKey = selectorManager.register(source, SelectionKey.OP_READ, this);

                connectURI = new URI("async",
                                     null,
                                     sourceAddress.getHostName(),
                                     source.socket().getLocalPort(),
                                     "",
                                     "",
                                     null);

                log.info("Datagram protocol available at: "
                         + sourceAddress.getHostName()
                         + ":"
                         + source.socket().getLocalPort());
                log.info("Datagram protocol clients will send datagrams to: " + connectURI);
            }
            if (destinationInterface != null) {
                destination = DatagramChannel.open();
                destination.configureBlocking(true);
                destination.socket().bind(destinationInterface);
                destination.socket().setReuseAddress(true);
            }
        } catch (SocketException e) {
            state = STOPPED;
            throw new ProtocolException(e);
        } catch (IOException e) {
            state = STOPPED;
            throw new ProtocolException(e);
        } catch (URISyntaxException e) {
            state = STOPPED;
            throw new ProtocolException(e);
        }
        state = STARTED;
    }

    public void drain() throws ProtocolException {
        close();
        state = STOPPED;
    }

    public void teardown() throws ProtocolException {
    }

    public void sendUp(UpPacket packet) throws ProtocolException {
        throw new IllegalAccessError("Method not implemented");
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        if (state == STOPPED) throw new IllegalStateException("Protocol is not started");
        if (destinationInterface == null) throw new IllegalStateException("No detestination address set");
        if (!(packet instanceof DatagramDownPacket)) throw new IllegalArgumentException("DownPacket not instance of DatagramDownPacket");

        DatagramDownPacket datagramPacket = (DatagramDownPacket) packet;
        try {
            Collection patcketBuffers = packet.getBuffers();

            ByteBuffer buffer;
            if (patcketBuffers.size() > 1) {
                int size = 0;
                Iterator iter = patcketBuffers.iterator();
                while (iter.hasNext()) {
                    size += ((ByteBuffer) iter.next()).remaining();
                }

                buffer = ByteBuffer.allocate(size);

                iter = patcketBuffers.iterator();
                while (iter.hasNext()) {
                    buffer.put((ByteBuffer) iter.next());
                }

                buffer.flip();
            } else {
                buffer = (ByteBuffer) patcketBuffers.iterator().next();
            }

            destination.send(buffer, datagramPacket.getAddress());
        } catch (IOException e) {
            state = STOPPED;
            throw new ProtocolException(e);
        }
    }

    ByteBuffer receiveBuffer = ByteBuffer.allocate(65336);

    public synchronized void selectionEvent(SelectionKey selection) {
        boolean tracing = log.isTraceEnabled();

        if (tracing) log.trace("ReadDataAction triggered.");

        try {
            SocketAddress address = source.receive(receiveBuffer);
            if (address == null) return;

            receiveBuffer.flip();

            ByteBuffer packetBuffer = ByteBuffer.allocate(receiveBuffer.remaining());
            packetBuffer.put(receiveBuffer);
            packetBuffer.flip();

            DatagramUpPacket packet = new DatagramUpPacket();
            packet.setBuffer(packetBuffer);
            packet.setAddress(address);

            up.sendUp(packet);

            receiveBuffer.clear();

            selectorManager.addInterestOps(selectionKey, SelectionKey.OP_READ);
        } catch (IOException e) {
            log.debug("Communications error, closing connection: ", e);
            close();
        } catch (ProtocolException e) {
            log.debug("Communications error, closing connection: ", e);
            close();
        }
    }

    synchronized public void close() {
        if (source != null) {
            try {
                selectionKey.cancel();
                source.close();
            } catch (Throwable e) {
            }
            source = null;
        }
        if (destination != null) {
            try {
                destination.close();
            } catch (Throwable e) {
            }
            destination = null;
        }
        state = STOPPED;
    }
}
