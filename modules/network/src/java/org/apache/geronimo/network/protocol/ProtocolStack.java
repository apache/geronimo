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

import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @version $Revision: 1.2 $ $Date: 2004/03/10 09:59:13 $
 */
public class ProtocolStack extends Stack implements Protocol {

    final static private Log log = LogFactory.getLog(ProtocolStack.class);

    private Protocol top;
    private Protocol bottom;

    public Object push(Object object) {
        if (empty()) {
            top = (Protocol) object;
            bottom = (Protocol) object;
        } else {
            top = (Protocol) object;
            Protocol down = (Protocol) super.peek();
            top.setUp(down.getUp());
            top.setDown(down);
            down.setUp(top);
        }
        return super.push(object);
    }

    public Object pop() {
        if (size() == 1) {
            top = null;
            bottom = null;
        } else {
        }

        Protocol result = (Protocol) super.pop();

        top = (Protocol) super.peek();
        top.setUp(result.getUp());

        result.setUp(null);
        result.setDown(null);

        return result;
    }

    public Protocol getUp() {
        return top.getUp();
    }

    public void setUp(Protocol up) {
        top.setUp(up);
    }

    public Protocol getDown() {
        return bottom.getDown();
    }

    public void setDown(Protocol down) {
        bottom.setDown(down);
    }

    public void clearLinks() {
        top.setUp(null);
        bottom.setDown(null);
    }

    public Protocol cloneProtocol() throws CloneNotSupportedException {
        ProtocolStack stack = new ProtocolStack();

        for (int i = 0; i < size(); i++) {
            Protocol protocol = (Protocol) ((Protocol) get(i)).cloneProtocol();
            protocol.clearLinks();
            stack.push(protocol);
        }

        return stack;
    }

    public void doStart() throws ProtocolException {
        log.trace("Starting");
        for (int i = 0; i < this.size(); i++) {
            Protocol protocol = (Protocol) this.get(i);
            protocol.doStart();
        }
    }

    public void doStop() throws ProtocolException {
        log.trace("Stopping");
        for (int i = this.size() - 1; 0 <= i; i--) {
            Protocol protocol = (Protocol) this.get(i);
            protocol.doStop();
        }
    }

    public void sendUp(UpPacket packet) throws ProtocolException {
        bottom.sendUp(packet);
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        top.sendDown(packet);
    }
}
