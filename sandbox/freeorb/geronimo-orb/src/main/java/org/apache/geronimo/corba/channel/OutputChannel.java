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
import java.io.OutputStream;

/** for now, this is always big endian writing */
public abstract class OutputChannel extends OutputStream {

    public abstract void writeByte(byte b) throws IOException;

    public abstract void writeShort(short s) throws IOException;

    public abstract void writeInt(int i) throws IOException;

    public abstract void writeLong(long l) throws IOException;

    public abstract void skip(int count) throws IOException;

    public abstract OutputChannelMarker mark(MarkHandler handler);

    public void write(int b) throws IOException {
        writeByte((byte) b);
    }

    public abstract void relinquish();

}
