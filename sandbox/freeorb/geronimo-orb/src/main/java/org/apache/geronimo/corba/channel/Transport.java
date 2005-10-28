/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.channel;

import java.io.IOException;


/**
 * Class Transport is the central abstraction in this I/O framework.
 * <p/>
 * A <code>Transport</code> has two channels that it can make available,
 * an input and an output channel.  These channels are considered tokens
 * or capabilities that can be handed only to one thread at a time.  Channels
 * are explicitly relinquished (by calling Channel.relinquish) in order to
 * return them to the transport, so that other threads may use it.
 */

public abstract class Transport {

    /**
     * Get the OutputChannel for this transport.
     * <p/>
     * This operation may block if some other thread is currently using the
     * output channel, and that other thread has not yet released the channel
     * by calling Channel.relinquish.
     */
    abstract public OutputChannel getOutputChannel();

    abstract public InputChannel getInputChannel();

    abstract public Object waitForResponse(Object key);

    public abstract void signalResponse(Object key, Object userData);

    public abstract void close() throws IOException;

    public abstract void setInputHandler(InputHandler handler);

}
