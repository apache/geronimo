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
package org.apache.geronimo.corba.channel.classic;

import java.io.IOException;
import java.net.Socket;

import org.apache.geronimo.corba.channel.InputHandler;
import org.apache.geronimo.corba.channel.RingByteBuffer;
import org.apache.geronimo.corba.channel.SocketTransportBase;


public class SyncClassicTransport extends SocketTransportBase {

    public SyncClassicTransport(ClassicTransportManager manager, Socket sock,
                                InputHandler handler)
    {
        super(manager, handler, sock);
    }

    public Socket socket() {
        return sock;
    }


    protected RingByteBuffer allocateSendBuffer(int bufferSize) {

        return new RingByteBuffer(bufferSize, false) {

            protected void bufferFullHook(String how) throws IOException {
                if (!socket().isOutputShutdown()) {
                    writeTo(sock);
                }
            }

            protected void bufferEmptyHook(String how) {
                // stopSenderThread();
            }

            /**
             * the send buffer was closed(), and we have send everything
             */
            protected void readEOFHook() {
                // do nothing //
                try {
                    sock.shutdownOutput();
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
        return new RingByteBuffer( bufferSize, false) {

            protected void bufferFullHook(String how) {

            }

            protected void bufferEmptyHook(String how) throws IOException {
                if (!isClosedForPut()) {
                    readFrom(sock);
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

}
