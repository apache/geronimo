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
import java.util.Iterator;

import EDU.oswego.cs.dl.util.concurrent.Mutex;

import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.network.protocol.AbstractProtocol;
import org.apache.geronimo.network.protocol.DownPacket;
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.ProtocolStack;
import org.apache.geronimo.network.protocol.UpPacket;
import org.apache.geronimo.network.protocol.control.commands.MenuItem;
import org.apache.geronimo.system.ClockPool;
import org.apache.geronimo.system.ThreadPool;


/**
 * @version $Revision: 1.1 $ $Date: 2004/03/10 02:14:28 $
 */
class ControlClientProtocolKitchen extends ProtocolStack implements ControlClientListener {

    private ClassLoader classLoader;
    private ThreadPool threadPool;
    private ClockPool clockPool;
    private SelectorManager selectorManager;
    private Mutex sendMutex = new Mutex();  //todo: replace with something that uses no locks


    ControlClientProtocolKitchen() throws InterruptedException {
        push(new Dummy());
        sendMutex.acquire();
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
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

    public void serveUp(Collection menu) throws ControlException {
        System.out.println("serveUp");

        ControlContext context = new ControlContext();
        context.setClassLoader(classLoader);
        context.setThreadPool(threadPool);
        context.setClockPool(clockPool);
        context.setSelectorManager(selectorManager);

        for (Iterator iter = menu.iterator(); iter.hasNext();) {
            MenuItem item = (MenuItem) iter.next();
            Object object = item.execute(context);

            if (object != null && object instanceof Protocol) {
                push((Protocol) object);
            }
        }
        try {
            doStart();
        } catch (ProtocolException e) {
            throw new ControlException(e);
        }

        sendMutex.release();
    }

    public void shutdown() {
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        try {
            if (!sendMutex.attempt(1000 * 1000)) throw new ProtocolException("Send timeout.");
            super.sendDown(packet);
            sendMutex.release();
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    protected class Dummy extends AbstractProtocol {

        public void doStart() throws ProtocolException {
        }

        public void doStop() throws ProtocolException {
        }

        public void sendUp(UpPacket packet) throws ProtocolException {
            getUp().sendUp(packet);
        }

        public void sendDown(DownPacket packet) throws ProtocolException {
            getDown().sendDown(packet);
        }

    }

}
