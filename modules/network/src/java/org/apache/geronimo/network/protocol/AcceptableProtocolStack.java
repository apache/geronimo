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

import java.nio.channels.SocketChannel;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @version $Revision: 1.4 $ $Date: 2004/08/01 13:03:37 $
 */
public class AcceptableProtocolStack extends Stack implements AcceptableProtocol {

    final static private Log log = LogFactory.getLog(AcceptableProtocolStack.class);

    private Protocol top;
    private AcceptableProtocol bottom;

    public Object push(Object object) {
        if (empty()) {
            top = (Protocol) object;
            bottom = (AcceptableProtocol) object;
        } else {
            top = (Protocol) object;
            Protocol down = (Protocol) super.peek();
            top.setUpProtocol(down.getUpProtocol());
            top.setDownProtocol(down);
            down.setUpProtocol(top);
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
        top.setUpProtocol(result.getUpProtocol());

        result.setUpProtocol(null);
        result.setDownProtocol(null);

        return result;
    }

    public Protocol getUpProtocol() {
        return top.getUpProtocol();
    }

    public void setUpProtocol(Protocol up) {
        top.setUpProtocol(up);
    }

    public Protocol getDownProtocol() {
        return bottom.getDownProtocol();
    }

    public void setDownProtocol(Protocol down) {
        bottom.setDownProtocol(down);
    }

    public void clearLinks() {
        top.setUpProtocol(null);
        bottom.setDownProtocol(null);
    }

    public Protocol cloneProtocol() throws CloneNotSupportedException {
        AcceptableProtocolStack stack = new AcceptableProtocolStack();

        for (int i = 0; i < size(); i++) {
            Protocol protocol = (Protocol) ((Protocol) get(i)).cloneProtocol();
            protocol.clearLinks();
            stack.push(protocol);
        }

        return stack;
    }

    public void setup() throws ProtocolException {
        log.trace("Starting");
        for (int i = 0; i < this.size(); i++) {
            Protocol protocol = (Protocol) this.get(i);
            protocol.setup();
        }
    }

    public void drain() throws ProtocolException {
        log.trace("Stopping");
        for (int i = this.size() - 1; 0 <= i; i--) {
            Protocol protocol = (Protocol) this.get(i);
            protocol.drain();
        }
    }

    public void teardown() throws ProtocolException {
    }

    public void sendUp(UpPacket packet) throws ProtocolException {
        bottom.sendUp(packet);
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        top.sendDown(packet);
    }

    public void flush() throws ProtocolException {
        top.flush();
    }

    public void accept(SocketChannel socketChannel) {
        bottom.accept(socketChannel);
    }

    public boolean isDone() {
        return bottom.isDone();
    }

    public long getCreated() {
        return bottom.getCreated();
    }

    public long getLastUsed() {
        return bottom.getLastUsed();
    }
}
