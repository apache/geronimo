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

import java.util.Collection;

import EDU.oswego.cs.dl.util.concurrent.Latch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.network.protocol.DownPacket;
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.UpPacket;
import org.apache.geronimo.pool.ClockPool;
import org.apache.geronimo.pool.ThreadPool;


/**
 * @version $Rev$ $Date$
 */
public class ControlServerProtocol extends AbstractControlProtocol {

    final static private Log log = LogFactory.getLog(ControlServerProtocol.class);

    private ControlServerListener controlServerListener;
    private BootstrapChef bootstrapChef;
    private ThreadPool threadPool;
    private ClockPool clockPool;
    private SelectorManager selectorManager;
    private long timeout;

    public ControlServerListener getControlServerListener() {
        return controlServerListener;
    }

    public void setControlServerListener(ControlServerListener controlServerListener) {
        this.controlServerListener = controlServerListener;
    }

    public BootstrapChef getBootstrapChef() {
        return bootstrapChef;
    }

    public void setBootstrapChef(BootstrapChef bootstrapChef) {
        this.bootstrapChef = bootstrapChef;
    }

    public ThreadPool getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public ClockPool getClockPool() {
        return clockPool;
    }

    public void setClockPool(ClockPool clockPool) {
        this.clockPool = clockPool;
    }

    public SelectorManager getSelectorManager() {
        return selectorManager;
    }

    public void setSelectorManager(SelectorManager selectorManager) {
        this.selectorManager = selectorManager;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Protocol cloneProtocol() throws CloneNotSupportedException {
        ControlServerProtocol result = (ControlServerProtocol) super.clone();
        result.START.setParent(result);
        result.RUN.setParent(result);
        return result;
    }

    public void setup() throws ProtocolException {
        log.trace("Starting");
    }

    public void drain() throws ProtocolException {
        log.trace("Stopping");

        if (state == RUN) {
            getDownProtocol().sendDown(new ShutdownRequestDownPacket());
        }
    }

    public void teardown() throws ProtocolException {
    }

    public void sendUp(UpPacket packet) throws ProtocolException {
        state.sendUp(packet);
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        state.sendDown(packet);
    }

    public void flush() throws ProtocolException {
        getDownProtocol().flush();
    }

    protected DownPacket constructBootPacket() {

        ControlContext context = new ControlContext();
        context.setThreadPool(threadPool);
        context.setClockPool(clockPool);
        context.setSelectorManager(selectorManager);

        Collection menu = bootstrapChef.createMenu(context);

        if (menu == null)
            return new NoBootDownPacket();
        else {
            BootResponseDownPacket packet = new BootResponseDownPacket();
            packet.setMenu(menu);
            return packet;
        }
    }

    private final State START = new State(this) {
        Latch startupLatch = new Latch();

        public void sendUp(UpPacket packet) throws ProtocolException {
            UpPacket p = ControlPacketReader.getInstance().read(packet.getBuffer());
            if (p instanceof BootRequestUpPacket) {
                log.trace("BOOT REQUEST");
                getDownProtocol().sendDown(constructBootPacket());
            } else if (p instanceof BootSuccessUpPacket) {
                log.trace("BOOT SUCCESS");
                log.trace("RELEASING " + startupLatch);
                ((ControlServerProtocol)getParent()). state = RUN;
                startupLatch.release();
                log.trace("RELEASED " + startupLatch);
            }
        }

        public void sendDown(DownPacket packet) throws ProtocolException {
            try {
                log.trace("AQUIRING " + startupLatch);
                if (!startupLatch.attempt(timeout)) throw new ProtocolException("Send timeout");
                log.trace("AQUIRED " + startupLatch);

                PassthroughDownPacket passthtough = new PassthroughDownPacket();
                passthtough.setBuffers(packet.getBuffers());

                getDownProtocol().sendDown(passthtough);
            } catch (InterruptedException e) {
                throw new ProtocolException(e);
            }
        }
    };

    private final State RUN = new State(this) {

        public void sendUp(UpPacket packet) throws ProtocolException {
            UpPacket p = ControlPacketReader.getInstance().read(packet.getBuffer());
            if (p instanceof PassthroughUpPacket) {
                log.trace("PASSTHROUGH");
                getUpProtocol().sendUp(packet);
            } else if (p instanceof ShutdownRequestUpPacket) {
                log.trace("SHUTDOWN_REQ");
                ((ControlServerProtocol)getParent()).state = START;
                controlServerListener.shutdown();
            }
        }

        public void sendDown(DownPacket packet) throws ProtocolException {
            PassthroughDownPacket passthtough = new PassthroughDownPacket();
            passthtough.setBuffers(packet.getBuffers());

            getDownProtocol().sendDown(passthtough);
        }
    };

    private volatile State state = START;
}
