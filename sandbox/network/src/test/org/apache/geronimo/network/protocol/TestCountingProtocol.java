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

import EDU.oswego.cs.dl.util.concurrent.CountDown;


/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class TestCountingProtocol extends AbstractProtocol {
    private CountDown completed;

    public TestCountingProtocol() {
    }

    public TestCountingProtocol(CountDown completed) {
        this.completed = completed;
    }

    public void setup() throws ProtocolException {
    }

    public void drain() throws ProtocolException {
    }

    public void teardown() throws ProtocolException {
    }

    public void sendUp(UpPacket packet) throws ProtocolException {
        completed.release();
        if (getUpProtocol() != null) getUpProtocol().sendUp(packet);
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
        getDownProtocol().sendDown(packet);
    }

    public void flush() throws ProtocolException {
    }
}
