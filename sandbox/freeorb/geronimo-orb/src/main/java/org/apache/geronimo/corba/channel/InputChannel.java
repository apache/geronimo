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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;


public abstract class InputChannel extends InputStream {

    public abstract byte readByte() throws IOException;

    public abstract short readShort() throws IOException;

    public abstract int readInt() throws IOException;

    public abstract long readLong() throws IOException;

    public abstract void skip(int count) throws IOException;

    public int read() throws IOException {
        try {
            return readByte();
        }
        catch (EOFException ex) {
            return -1;
        }
    }

    public abstract boolean isClosed();

    public abstract void relinquish();

    public abstract void setOrder(ByteOrder order);

	public abstract ByteOrder getOrder();

}
