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

package org.apache.geronimo.network.protocol;

import java.util.ArrayList;
import java.util.Collection;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.network.protocol.control.BootstrapCook;
import org.apache.geronimo.network.protocol.control.ControlContext;
import org.apache.geronimo.network.protocol.control.commands.CreateInstanceMenuItem;
import org.apache.geronimo.network.protocol.control.commands.SetReferenceMenuItem;
import org.apache.geronimo.pool.ThreadPool;


/**
 * @version $Revision: 1.6 $ $Date: 2004/07/11 21:45:37 $
 */
public class BufferProtocol extends AbstractProtocol implements BootstrapCook {

    final static private Log log = LogFactory.getLog(BufferProtocol.class);

    private ThreadPool threadPool;  //todo: do a selector-type architecture
    private LinkedQueue upQueue = new LinkedQueue();
    private LinkedQueue downQueue = new LinkedQueue();
    private boolean running = false;
    private ProtocolException error = null;

    public ThreadPool getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public void setup() throws ProtocolException {
        log.trace("Starting");
        running = true;
        try {
            threadPool.execute(new Runnable() {
                public void run() {
                    try {
                        while (running) {
                            UpPacket packet = (UpPacket) upQueue.poll(500);
                            if (packet != null) getUpProtocol().sendUp(packet);
                        }
                    } catch (InterruptedException e) {
                    } catch (ProtocolException e) {
                        running = false;
                        error = e;
                    }
                }
            });
            threadPool.execute(new Runnable() {
                public void run() {
                    try {
                        while (running) {
                            DownPacket packet = (DownPacket) downQueue.poll(500);
                            if (packet != null) getDownProtocol().sendDown(packet);
                        }
                    } catch (InterruptedException e) {
                    } catch (ProtocolException e) {
                        running = false;
                        error = e;
                    }
                }
            });
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    public void drain() throws ProtocolException {
        log.trace("Stopping");
        running = false;
    }

    public void teardown() throws ProtocolException {
    }

    public void sendUp(UpPacket packet) throws ProtocolException {
        if (error != null) throw error;
        try {
            upQueue.put(packet);
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        if (error != null) throw error;
        try {
            downQueue.put(packet);
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    public Collection cook(ControlContext context) {
        ArrayList list = new ArrayList(2);

        CreateInstanceMenuItem create = new CreateInstanceMenuItem();
        create.setClassName("org.apache.geronimo.network.protocol.BufferProtocol");
        create.setInstanceId(context.assignId(this));
        list.add(create);

        SetReferenceMenuItem set = new SetReferenceMenuItem();
        set.setInstanceId(context.assignId(this));
        set.setReferenceName("ThreadPool");
        set.setReferenceId(context.assignId(threadPool));
        list.add(set);

        return list;
    }

}
