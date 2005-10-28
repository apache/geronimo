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
import java.nio.channels.SocketChannel;

import org.apache.geronimo.corba.channel.InputChannel;
import org.apache.geronimo.corba.channel.InputHandler;
import org.apache.geronimo.corba.channel.OutputChannel;
import org.apache.geronimo.corba.channel.RingByteBuffer;
import org.apache.geronimo.corba.channel.Transport;


public class SyncNIOTransport extends Transport {

    private final SyncNIOTransportManager manager;

    private final SocketChannel chan;

    private final InputHandler handler;

    private ParticipationExecutor executor;

    private RingByteBuffer receiveBuffer;

    private RingByteBuffer sendBuffer;

    static final int RCV_BUFFER_SIZE = getIntProperty(
            "org.freeorb.rcv_buffer_size", 64 * 1024);

    static final int SND_BUFFER_SIZE = getIntProperty(
            "org.freeorb.snd_buffer_size", 64 * 1024);

    private static int getIntProperty(String string, int defaultValue) {
        try {
            return Integer.parseInt(System.getProperty(string, ""));
        }
        catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public SyncNIOTransport(SyncNIOTransportManager manager,
                            final SocketChannel chan, InputHandler handler)
    {
        this.manager = manager;
        this.chan = chan;
        this.handler = handler;

        this.executor = new ParticipationExecutor(manager.getExecutor());

        receiveBuffer = new RingByteBuffer(RCV_BUFFER_SIZE, true) {

            public String getName() {
                return "receive buffer for " + chan.toString();
            }

            protected void bufferFullHook(String how) {
                // do nothing //
            }

            protected void bufferEmptyHook(String how) throws IOException {
                if (!isClosedForPut()) {
                    fillReceiveBuffer();
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

        sendBuffer = new RingByteBuffer("send" + chan.socket(), SND_BUFFER_SIZE) {

            protected void bufferFullHook(String how) throws IOException {
                if (!chan.socket().isOutputShutdown()) {
                    flushSendBuffer();
                }
            }

            protected void bufferEmptyHook(String how) {
                // what do we care? //
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
                    e.printStackTrace();
                }
            }

        };


        try {
            executor.execute(inputListener);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Runnable inputListener = new Runnable() {

        public void run() {

            while (true) {

                while (receiveBuffer.isEmpty()) {
                    try {
                        fillReceiveBuffer();
                    }
                    catch (IOException e) {
                        System.out.println("loop reached EOF");
                        return;
                    }

                    if (receiveBuffer.isClosedForPut()) {
                        System.out.println("END OF INPUT");
                        return;
                    }
                }

                handler.inputAvailable(SyncNIOTransport.this);
            }

        }

    };


    public OutputChannel getOutputChannel() {
        return sendBuffer.getOutputChannel();
    }

    public InputChannel getInputChannel() {
        return receiveBuffer.getInputChannel();
    }

    public void close() throws IOException {
        chan.close();
    }

    public void releaseInputChannel() {
        // TODO Auto-generated method stub

    }

}
