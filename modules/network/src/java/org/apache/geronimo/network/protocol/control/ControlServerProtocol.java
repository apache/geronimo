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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.network.protocol.DownPacket;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.UpPacket;
import org.apache.geronimo.system.ClockPool;
import org.apache.geronimo.system.ThreadPool;

import EDU.oswego.cs.dl.util.concurrent.Latch;


/**
 * @version $Revision: 1.5 $ $Date: 2004/04/24 06:29:01 $
 */
public class ControlServerProtocol extends AbstractControlProtocol {

    final static private Log log = LogFactory.getLog(ControlServerProtocol.class);

    private ControlServerListener controlServerListener;
    private BootstrapChef bootstrapChef;
    private ThreadPool threadPool;
    private ClockPool clockPool;
    private SelectorManager selectorManager;
    private Latch sendLatch;  //todo: replace with something that uses no locks
    private long timeout;

    private final int STARTED = 0;
    private final int STOPPED = 1;
    private int state = STOPPED;

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

    public void setup() throws ProtocolException {
        log.trace("Starting");
        sendLatch = new Latch();
        state = STARTED;
    }

    public void drain() throws ProtocolException {
        log.trace("Stopping");
        if (state == STARTED) {
            getDownProtocol().sendDown(new ShutdownRequestDownPacket());
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
        } else if (p instanceof BootRequestUpPacket) {
            log.trace("BOOT REQUEST");
            getDownProtocol().sendDown(constructBootPacket());
        } else if (p instanceof BootSuccessUpPacket) {
            log.trace("BOOT SUCCESS");
            log.trace("RELEASING " + sendLatch);
            sendLatch.release();
            log.trace("RELEASED " + sendLatch);
        } else if (p instanceof ShutdownRequestUpPacket) {
            log.trace("SHUTDOWN_REQ");
            getDownProtocol().sendDown(new ShutdownAcknowledgeDownPacket());
            state = STOPPED;
            controlServerListener.shutdown();
        }
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        try {
            log.trace("AQUIRING " + sendLatch);
            if (!sendLatch.attempt(timeout)) throw new ProtocolException("Send timeout.");
            log.trace("AQUIRED " + sendLatch);

            PassthroughDownPacket passthtough = new PassthroughDownPacket();
            passthtough.setBuffers(packet.getBuffers());

            getDownProtocol().sendDown(passthtough);

        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
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

}
