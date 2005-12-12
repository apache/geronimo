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
package org.apache.geronimo.corba.ior;

import org.apache.geronimo.corba.io.OutputStreamBase;


public class UnknownComponent extends Component {

    private final int tag;

    private final byte[] data;

    public UnknownComponent(int tag, byte[] data) {
        this.tag = tag;
        this.data = data;
    }

    public int tag() {
        return tag;
    }

    protected void write_encapsulated_content(OutputStreamBase out) {
        out.write_octet_array(data, 0, data.length);
    }

    protected void write_content(org.omg.CORBA.portable.OutputStream eo) {
    }

}
