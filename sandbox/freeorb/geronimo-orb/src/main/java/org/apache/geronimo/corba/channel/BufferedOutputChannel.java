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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class BufferedOutputChannel extends OutputChannel {

    ByteArrayOutputStream bao = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream(bao);

    public void writeByte(byte b) throws IOException {
        dout.writeByte(b);
    }

    public void writeInt(int i) throws IOException {
        dout.writeInt(i);
    }

    public void writeLong(long l) throws IOException {
        dout.writeLong(l);
    }

    public void skip(int count) throws IOException {
        for (int i = 0; i < count; i++) {
            dout.write(0);
        }
    }

    public OutputChannelMarker mark(MarkHandler handler) {
        // TODO Auto-generated method stub
        return null;
    }

    public void writeTo(OutputChannel ch) throws IOException {
        bao.writeTo(ch);
    }

    public void relinquish() {
        // do nothing //
    }

    public void writeShort(short s) throws IOException {
        dout.writeShort(s);
    }

}
