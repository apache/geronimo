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
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.ProtocolStack;
import org.apache.geronimo.pool.ClockPool;
import org.apache.geronimo.pool.ThreadPool;


/**
 * @version $Rev$ $Date$
 */
public class ControlClientProtocolStack extends ProtocolStack implements ControlClientListener {

    final static private Log log = LogFactory.getLog(ControlServerProtocolStack.class);

    private ControlClientProtocolKitchen kitchen;
    private ClassLoader classLoader;
    private ThreadPool threadPool;
    private ClockPool clockPool;
    private SelectorManager selectorManager;

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

    public Object push(Object object) {
        if (object instanceof ControlClientProtocol) {
            ((ControlClientProtocol) object).setListener(this);
        }
        return super.push(object);
    }

    public Object pop() {
        Protocol result = (Protocol) super.pop();

        if (result instanceof ControlClientProtocol) {
            ((ControlClientProtocol) result).setListener(null);
        }
        return result;
    }

    public void setup() throws ProtocolException {
        try {
            kitchen = new ControlClientProtocolKitchen();
            kitchen.setClassLoader(classLoader);
            kitchen.setThreadPool(threadPool);
            kitchen.setClockPool(clockPool);
            kitchen.setSelectorManager(selectorManager);

            push(kitchen);

            super.setup();
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    public void drain() throws ProtocolException {
        pop();

        super.drain();
    }

    public void serveUp(Collection menu) throws ControlException {
        kitchen.serveUp(menu);
    }

    public void shutdown() {
        log.trace("Shutdown");
        try {
            drain();
        } catch (ProtocolException e) {
        }
    }
}
