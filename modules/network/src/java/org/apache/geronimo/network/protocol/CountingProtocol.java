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

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.network.protocol.control.BootstrapCook;
import org.apache.geronimo.network.protocol.control.ControlContext;
import org.apache.geronimo.network.protocol.control.commands.CreateInstanceMenuItem;


/**
 * @version $Revision: 1.3 $ $Date: 2004/03/17 03:11:59 $
 */
public class CountingProtocol extends AbstractProtocol implements BootstrapCook {

    final static private Log log = LogFactory.getLog(SocketProtocol.class);

    private volatile long upCount;
    private volatile long downCount;
    private boolean tracing;

    public long getUpCount() {
        return upCount;
    }

    public long getDownCount() {
        return downCount;
    }

    public void setup() throws ProtocolException {
        tracing = log.isTraceEnabled();

        if (tracing) log.trace("Starting");
    }

    public void drain() throws ProtocolException {
        if (tracing) log.trace("Stopping");
    }

    public void teardown() throws ProtocolException {
    }

    public void sendUp(UpPacket packet) throws ProtocolException {
        if (tracing) log.trace("sendUp " + upCount);

        upCount++;
        getUpProtocol().sendUp(packet);
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        if (tracing) log.trace("sendDown " + downCount);

        downCount++;
        getDownProtocol().sendDown(packet);
    }

    public Collection cook(ControlContext context) {
        CreateInstanceMenuItem item = new CreateInstanceMenuItem();
        item.setClassName(CountingProtocol.class.getName());
        item.setInstanceId(context.assignId(this));

        return Collections.singletonList(item);
    }

}
