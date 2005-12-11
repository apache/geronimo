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
package org.apache.geronimo.corba.channel.nio;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.geronimo.corba.channel.InputHandler;
import org.apache.geronimo.corba.channel.RingByteBuffer;
import org.apache.geronimo.corba.channel.SocketTransportBase;


public class AsyncNIOSocketTransport extends SocketTransportBase implements
                                                                 SelectionListener
{

    public final SocketChannel chan;

    AsyncNIOSocketTransport(AsyncNIOTransportManager manager,
                            final SocketChannel chan, InputHandler handler)
    {

        super(manager, handler, chan.socket());

        this.chan = chan;
    }

    protected RingByteBuffer allocateSendBuffer(int bufferSize) {

        return new RingByteBuffer(bufferSize, true) {

            public String getName() {
                return "send buffer for " + sock.toString();
            }

            protected void bufferFullHook(String how) {
                if (!chan.socket().isOutputShutdown()) {
                    addInterest(SelectionKey.OP_WRITE, "output buffer full : "
                                                       + how);
                }
            }

            protected void bufferEmptyHook(String how) {
                removeInterest(SelectionKey.OP_WRITE, "send buffer empty : "
                                                      + how);
            }

            /**
             * the send buffer was closed(), and we have send everything
             */
            protected void readEOFHook() {
                // do nothing //
                try {
                    chan.socket().shutdownOutput();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            protected void relinquishInput() {
                throw new InternalError();
            }

            protected void relinquishOutput() {
                releaseOutputChannel();
            }

        };
    }


    protected RingByteBuffer allocateReceiveBuffer(int bufferSize) {
        return new RingByteBuffer(bufferSize, true) {

            public String getName() {
                return "receive buffer for " + sock.toString();
            }

            protected void bufferFullHook(String how) {
                removeInterest(SelectionKey.OP_READ, "receive buffer full : "
                                                     + how);
            }

            protected void bufferEmptyHook(String how) {
                if (!isClosedForPut()) {
                    addInterest(SelectionKey.OP_READ, "input buffer empty : "
                                                      + how);
                }
            }

            protected void readEOFHook() {
                // the client just read the EOF marker //
            }

            protected void relinquishInput() {
                releaseInputChannel();
            }

            protected void relinquishOutput() {
                throw new InternalError();
            }

        };
    }


    AsyncNIOTransportManager getNIOManager() {
        return (AsyncNIOTransportManager) manager;
    }

    protected void removeInterest(int interest, String why) {
        getNIOManager().removeInterest(this, interest, why);
    }

    protected void addInterest(int interest, String why) {
        getNIOManager().addInterest(this, interest, why);
    }

    public void canAccept() {
    }

    public void canConnect() {
        try {
            chan.finishConnect();
            removeInterest(SelectionKey.OP_CONNECT, "connected");
            addInterest(SelectionKey.OP_READ, "can connect");
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void canRead() {

        try {
            if (receiveBuffer.readFrom(chan)) {

                if (receiveBuffer.isClosedForPut()) {
                    removeInterest(SelectionKey.OP_READ, "reached eof");
                }

                processAvailableInput();
            }

        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void canWrite() {

        try {
            sendBuffer.writeTo(chan);

            if (sendBuffer.isClosed() || sendBuffer.isEmpty()) {
                removeInterest(SelectionKey.OP_WRITE, "output closed/empty");
            }

        }
        catch (IOException e) {
            removeInterest(SelectionKey.OP_WRITE, "write failed");
        }
    }

    public void channelClosed(ClosedChannelException e) {
    }

    public SocketChannel channel() {
        return chan;
    }

    public void close() throws IOException {
        channel().close();
    }

}
