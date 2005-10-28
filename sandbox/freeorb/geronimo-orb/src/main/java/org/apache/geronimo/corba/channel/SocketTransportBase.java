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
package org.apache.geronimo.corba.channel;

import java.io.IOException;
import java.net.Socket;

import EDU.oswego.cs.dl.util.concurrent.Mutex;
import EDU.oswego.cs.dl.util.concurrent.Semaphore;
import EDU.oswego.cs.dl.util.concurrent.Sync;

import org.apache.geronimo.corba.channel.nio.ParticipationExecutor;


public abstract class SocketTransportBase extends Transport {

    static protected final int RCV_BUFFER_SIZE = getIntProperty("org.freeorb.rcv_buffer_size", 64 * 1024);

    static protected final int SND_BUFFER_SIZE = getIntProperty("org.freeorb.snd_buffer_size", 64 * 1024);

    protected InputHandler handler;

    protected Thread inputWorker;

    protected Sync inputWorkerLock = new Mutex();

    protected RingByteBuffer receiveBuffer;

    protected RingByteBuffer sendBuffer;

    protected Semaphore outputWorkerLock = new Semaphore(1);

    protected Thread outputWorker;

    protected TransportManager manager;

    private ParticipationExecutor executor;

    protected Socket sock;

    protected SocketTransportBase(TransportManager manager, InputHandler handler, Socket sock) {
        this.manager = manager;
        this.handler = handler;
        this.executor = new ParticipationExecutor(manager.getExecutor());
        this.sock = sock;

        this.receiveBuffer = allocateReceiveBuffer(RCV_BUFFER_SIZE);
        this.sendBuffer = allocateSendBuffer(SND_BUFFER_SIZE);
    }


    protected abstract RingByteBuffer allocateSendBuffer(int bufferSize);

    protected abstract RingByteBuffer allocateReceiveBuffer(int bufferSize);


    private static int getIntProperty(String string, int defaultValue) {
        try {
            return Integer.parseInt(System.getProperty(string, ""));
        }
        catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public void releaseOutputChannel() {
        if (outputWorker == Thread.currentThread()) {

            try {
                sendBuffer.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            outputWorker = null;
            outputWorkerLock.release();
        }
    }

    /**
     * wait for the output channel to become available
     */
    public OutputChannel getOutputChannel() {

        do {
            try {
                outputWorkerLock.acquire();
            }
            catch (InterruptedException e) {
                continue;
            }
        }
        while (false);

        assertEquals(outputWorker, null);

        outputWorker = Thread.currentThread();
        return sendBuffer.getOutputChannel();
    }

    public InputChannel getInputChannel() {
        LOOP:
        do {
            try {
                inputWorkerLock.acquire();
            }
            catch (InterruptedException e) {
                continue LOOP;
            }
        }
        while (false);

        try {

            if (inputWorker == null) {
                inputWorker = Thread.currentThread();

            } else if (inputWorker != Thread.currentThread()) {
                throw new IllegalStateException(
                        "only the designated input worker can do that");
            }

        }
        finally {
            inputWorkerLock.release();
        }

        return receiveBuffer.getInputChannel();
    }

    /**
     * this runnable is started when input is available
     */
    protected final Runnable processInput = new Runnable() {
        public void run() {

            assertEquals(inputWorker, null);

            inputWorker = Thread.currentThread();
            try {
                inputWorkerLock.release();
                handler.inputAvailable(SocketTransportBase.this);
            }
            catch (Error e) {
                e.printStackTrace();
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }
            finally {
                releaseOutputChannel();
                unsetInputWorker();
            }
        }
    };

    /**
     * to be called when something is added to the input buffer
     */
    protected void processAvailableInput() throws InterruptedException {
        inputWorkerLock.acquire();

        // is there someone processing input?
        // if not, then we need to start a new
        // input processor

        if (inputWorker == null && !receiveBuffer.isEmpty()
            && handler != null)
        {
            executor.execute(processInput);
        } else {
            inputWorkerLock.release();
        }
    }


    public void releaseInputChannel() {
        unsetInputWorker();
    }

    void unsetInputWorker() {

        Thread.interrupted();

        do {
            try {
                inputWorkerLock.acquire();
            }
            catch (InterruptedException e) {
                continue;
            }
        }
        while (false);

        if (inputWorker == Thread.currentThread()) {
            inputWorker = null;
            if (!receiveBuffer.isEmpty() && handler != null) {
                // we're done with this request, but there
                // is a new request (partially) available

                do {
                    try {
                        executor.execute(processInput);
                    }
                    catch (InterruptedException e) {
                        continue;
                    }
                }
                while (false);
            } else {
                // we're done with this request and there is
                // no more input
                inputWorkerLock.release();
            }
        } else {
            // response was given to another thread via signalResponse
            inputWorkerLock.release();
        }

    }

    void registerResponse(Object key) {

    }

    public Object waitForResponse(Object key) {

        do {
            try {
                inputWorkerLock.acquire();
            }
            catch (InterruptedException e) {
                continue;
            }
        }
        while (false);

        if (inputWorker == Thread.currentThread()) {
            inputWorker = null;
        }
        inputWorkerLock.release();

        Object value = executor.participate(key);

        inputWorker = Thread.currentThread();
        inputWorkerLock.release(); // {22}

        return value;
    }

    public void signalResponse(Object key, Object value) {
        assertEquals(inputWorker, Thread.currentThread());

        // this lock is released at {22}, when the
        // relevant participant reaquires control
        do {
            try {
                inputWorkerLock.acquire();
            }
            catch (InterruptedException e) {
                continue;
            }
        }
        while (false);

        inputWorker = null;
        executor.release(key, value);
    }


    public void setInputHandler(InputHandler handler) {
        this.handler = handler;
    }


    private void assertEquals(Object o1, Object o2) {
        if (o1 != o2) {
            throw new IllegalStateException("assertion failed");
        }
    }


    public void close() throws IOException {
        sock.close();
    }


}
