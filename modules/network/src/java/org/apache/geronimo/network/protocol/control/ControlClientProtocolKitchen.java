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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.network.protocol.AbstractProtocol;
import org.apache.geronimo.network.protocol.DownPacket;
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.ProtocolStack;
import org.apache.geronimo.network.protocol.UpPacket;
import org.apache.geronimo.network.protocol.control.commands.MenuItem;
import org.apache.geronimo.pool.ClockPool;
import org.apache.geronimo.pool.ThreadPool;

import EDU.oswego.cs.dl.util.concurrent.Latch;


/**
 * @version $Revision: 1.6 $ $Date: 2004/08/01 13:03:50 $
 */
class ControlClientProtocolKitchen extends ProtocolStack implements ControlClientListener {

	final private static Log log = LogFactory.getLog(ControlClientProtocolKitchen.class);
	
    private ClassLoader classLoader;
    private ThreadPool threadPool;
    private ClockPool clockPool;
    private SelectorManager selectorManager;
    private Latch sendLatch = new Latch();

    ControlClientProtocolKitchen() throws InterruptedException {
        push(new Dummy());
    }
    
    /**
	 * @see org.apache.geronimo.network.protocol.ProtocolStack#cloneProtocol()
	 */
	public Protocol cloneProtocol() throws CloneNotSupportedException {
		ControlClientProtocolKitchen p = (ControlClientProtocolKitchen) super.cloneProtocol();
		p.sendLatch = new Latch();
		return p;
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
    	log.trace("serveUp");

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
            setup();
        } catch (ProtocolException e) {
            throw new ControlException(e);
        }
        
    	log.trace("RELEASING send Latch: "+sendLatch);
        sendLatch.release();
    }

    public void shutdown() {
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        try {
        	log.trace("AQUIRING send Latch: "+sendLatch);
            if (!sendLatch.attempt(1000 * 1000)) throw new ProtocolException("Send timeout.");
        	log.trace("AQUIRED send Latch: "+sendLatch);
            super.sendDown(packet);
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    protected class Dummy extends AbstractProtocol {

        public void setup() throws ProtocolException {
        }

        public void drain() throws ProtocolException {
        }

        public void teardown() throws ProtocolException {
        }

        public void sendUp(UpPacket packet) throws ProtocolException {
            getUpProtocol().sendUp(packet);
        }

        public void sendDown(DownPacket packet) throws ProtocolException {
            getDownProtocol().sendDown(packet);
        }

        public void flush() throws ProtocolException {
            getDownProtocol().flush();
        }
    }

}
