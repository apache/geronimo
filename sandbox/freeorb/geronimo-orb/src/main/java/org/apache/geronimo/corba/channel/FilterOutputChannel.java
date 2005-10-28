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


public class FilterOutputChannel extends OutputChannel {

    final protected OutputChannel ch;

    protected FilterOutputChannel(OutputChannel ch) {
        this.ch = ch;
    }

    public void writeByte(byte b) throws IOException {
        ch.writeByte(b);
    }

    public void writeInt(int i) throws IOException {
        ch.writeInt(i);
    }

    public void writeShort(short s) throws IOException {
        ch.writeShort(s);
    }

    public void writeLong(long l) throws IOException {
        ch.writeLong(l);
    }

    public void skip(int count) throws IOException {
        ch.skip(count);
    }

    public OutputChannelMarker mark(MarkHandler handler) {
        return ch.mark(handler);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        ch.write(b, off, len);
    }

    public void relinquish() {
        ch.relinquish();
    }

}
