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

package org.apache.geronimo.messaging.io;

import java.io.IOException;
import java.io.InputStream;

import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

/**
 *
 * @version $Rev$ $Date$
 */
public class MockStreamManager
    implements StreamManager
{

    public Object register(InputStream anIn) {
        return null;
    }

    public InputStream retrieve(Object anId) throws IOException {
        return null;
    }

    public byte[] retrieveLocalNext(Object anID) throws IOException {
        return null;
    }

    public Object getID() {
        return null;
    }

    public MsgOutInterceptor getMsgConsumerOut() {
        return null;
    }

    public void setMsgProducerOut(MsgOutInterceptor aMsgOut) {
    }

    public void start() {
    }

    public void stop() {
    }

}
