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
package org.apache.geronimo.corba.io;

import java.io.IOException;

import org.apache.geronimo.corba.AbstractORB;


public class FilterInputStream extends InputStreamBase {

    private final InputStreamBase base;

    FilterInputStream(InputStreamBase base) {
        this.base = base;
    }

    protected AbstractORB __orb() {
        return base.__orb();
    }

    public void close() throws IOException {
        base.close();
    }

    public int __stream_position() {
        return base.__stream_position();
    }

    protected GIOPVersion getGIOPVersion() {
        return base.getGIOPVersion();
    }

    public byte read_octet() {
        return base.read_octet();
    }

    public short read_short() {
        return base.read_short();
    }

    public int read_long() {
        return base.read_long();
    }

    public long read_longlong() {
        return base.read_longlong();
    }


}
