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

package org.apache.geronimo.network.protocol.control;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.network.protocol.DownPacket;
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.UpPacket;

import EDU.oswego.cs.dl.util.concurrent.Latch;


/**
 * @version $Revision: 1.6 $ $Date: 2004/04/24 06:29:01 $
 */
public class ControlClientProtocol extends AbstractControlProtocol {

    final static private Log log = LogFactory.getLog(ControlClientProtocol.class);

    private ControlClientListener listener;
    private ClassLoader classLoader;
    private Latch sendLatch = new Latch();  //todo: replace with something that uses no locks
    private Latch shutdownLatch = new Latch();
    private long timeout;

    private final int STARTED = 0;
    private final int STOPPED = 1;
    private int state = STOPPED;

    /**
	 * @see org.apache.geronimo.network.protocol.AbstractProtocol#cloneProtocol()
	 */
	public Protocol cloneProtocol() throws CloneNotSupportedException {
		ControlClientProtocol p = (ControlClientProtocol)super.cloneProtocol();
		p.sendLatch = new Latch();
		p.shutdownLatch = new Latch();
		return p;
	}
    
    public ControlClientListener getListener() {
        return listener;
    }

    public void setListener(ControlClientListener listener) {
        this.listener = listener;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setup() throws ProtocolException {
        log.trace("Starting");
        getDownProtocol().sendDown(new BootRequestDownPacket()); //todo: this is probably dangerous, put in thread pool
        state = STARTED;
    }

    public void drain() throws ProtocolException {
        log.trace("Stopping");
        if (state == STARTED) {
            getDownProtocol().sendDown(new ShutdownRequestDownPacket());
            try {
                shutdownLatch.acquire();
            } catch (InterruptedException e) {
                throw new ProtocolException(e);
            }
            state = STOPPED;
        }
    }

    public void teardown() throws ProtocolException {
    }

    public void sendUp(UpPacket packet) throws ProtocolException {
        UpPacket p = ControlPacketReader.getInstance().read(packet.getBuffer());
        if (p instanceof PassthroughUpPacket) {
            log.trace("PASSTHROUGH");
            getUpProtocol().sendUp(packet);
        } else if (p instanceof BootResponseUpPacket) {
            try {
                log.trace("BOOT RESPONSE");
                listener.serveUp(((BootResponseUpPacket) p).getMenu());
                getDownProtocol().sendDown(new BootSuccessDownPacket());
                log.trace("RELEASING " + sendLatch);
                sendLatch.release();
                log.trace("RELEASED " + sendLatch);
            } catch (ControlException e) {
                throw new ProtocolException(e);
            }
        } else if (p instanceof NoBootUpPacket) {
            log.trace("NO BOOT");
            state = STOPPED;
            listener.shutdown();
        } else if (p instanceof ShutdownRequestUpPacket) {
            log.trace("SHUTDOWN_REQ");
            getDownProtocol().sendDown(new ShutdownAcknowledgeDownPacket());
            state = STOPPED;
            listener.shutdown();
        } else if (p instanceof ShutdownAcknowledgeUpPacket) {
            log.trace("SHUTDOWN_ACK");
            shutdownLatch.release();
            state = STOPPED;
            listener.shutdown();
        }
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        try {
            log.trace("AQUIRING " + sendLatch);
            if (!sendLatch.attempt(timeout)) throw new ProtocolException("Send timeout");
            log.trace("AQUIRED " + sendLatch);

            PassthroughDownPacket passthtough = new PassthroughDownPacket();
            passthtough.setBuffers(packet.getBuffers());

            getDownProtocol().sendDown(passthtough);

        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }
}
