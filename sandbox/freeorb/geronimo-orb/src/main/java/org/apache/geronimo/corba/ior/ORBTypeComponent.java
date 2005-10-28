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


import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.IOP.TAG_ORB_TYPE;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.io.EncapsulationInputStream;


public class ORBTypeComponent extends Component {

    private final int orbtype;

    public ORBTypeComponent(int orbtype) {
        this.orbtype = orbtype;
    }

    public int tag() {
        return TAG_ORB_TYPE.value;
    }

    public static Component read(AbstractORB orb, byte[] data) {
        InputStream is = new EncapsulationInputStream(orb, data);
        return new ORBTypeComponent(is.read_long());
    }

    protected void write_content(OutputStream eo) {
        eo.write_long(orbtype);
    }

}
