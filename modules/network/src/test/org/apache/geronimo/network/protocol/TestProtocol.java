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

import javax.security.auth.Subject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.network.protocol.control.BootstrapCook;
import org.apache.geronimo.network.protocol.control.ControlContext;
import org.apache.geronimo.network.protocol.control.commands.CreateInstanceMenuItem;
import org.apache.geronimo.network.protocol.control.commands.SetAttributeMenuItem;
import org.apache.geronimo.network.protocol.control.commands.SetReferenceMenuItem;
import org.apache.geronimo.pool.ClockPool;
import org.apache.geronimo.pool.ThreadPool;


/**
 * @version $Revision: 1.4 $ $Date: 2004/07/08 05:13:28 $
 */
public class TestProtocol extends AbstractProtocol implements BootstrapCook {

    final static private Log log = LogFactory.getLog(TestProtocol.class);

    private String value;
    private ThreadPool threadPool;
    private ClockPool clockPool;
    private SelectorManager selectorManager;

    public TestProtocol() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public void setup() throws ProtocolException {
    }

    public void drain() throws ProtocolException {
    }

    public void teardown() throws ProtocolException {
    }

    public void sendUp(UpPacket packet) throws ProtocolException {
        log.trace("sendUp");

        ByteBuffer buffer = packet.getBuffer();
        byte[] b = new byte[buffer.remaining()];
        buffer.get(b);
        for (int i = buffer.position(); i < buffer.limit(); i++) {
            if (b[i] != (byte) 0x0b) throw new ProtocolException("bb");
        }
        Subject subject = MetadataSupport.getSubject(packet);
        if (subject != null) log.trace("Subject passed: " + subject);

        if (getUpProtocol() != null) getUpProtocol().sendUp(packet);
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        log.trace("sendDown");
        getDownProtocol().sendDown(packet);
    }

    public Collection cook(ControlContext context) {
        ArrayList items = new ArrayList(2);

        CreateInstanceMenuItem create = new CreateInstanceMenuItem();
        create.setClassName(TestProtocol.class.getName());
        create.setInstanceId(context.assignId(this));
        items.add(create);

        SetAttributeMenuItem set = new SetAttributeMenuItem();
        set.setInstanceId(context.assignId(this));
        set.setAttributeName("Value");
        set.setAttributeValue(value);
        items.add(set);

        SetReferenceMenuItem ref = new SetReferenceMenuItem();
        ref.setInstanceId(context.assignId(this));
        ref.setReferenceName("ThreadPool");
        ref.setReferenceId(context.assignId(threadPool));
        items.add(ref);

        ref = new SetReferenceMenuItem();
        ref.setInstanceId(context.assignId(this));
        ref.setReferenceName("ClockPool");
        ref.setReferenceId(context.assignId(clockPool));
        items.add(ref);

        ref = new SetReferenceMenuItem();
        ref.setInstanceId(context.assignId(this));
        ref.setReferenceName("SelectorManager");
        ref.setReferenceId(context.assignId(selectorManager));
        items.add(ref);


        return items;
    }

}
