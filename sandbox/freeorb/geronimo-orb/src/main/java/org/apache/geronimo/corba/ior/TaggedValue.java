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

import java.io.IOException;

import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.portable.OutputStream;

import org.apache.geronimo.corba.io.EncapsulationOutputStream;
import org.apache.geronimo.corba.io.OutputStreamBase;


public abstract class TaggedValue {

    public abstract int tag();

    public void write(OutputStreamBase out) {
        out.write_long(tag());
        write_encapsulated_content(out);
    }

    protected void write_encapsulated_content(OutputStreamBase out) {
        EncapsulationOutputStream eo = new EncapsulationOutputStream(out.__orb());
        write_content(eo);
        try {
            eo.writeTo(out);
        }
        catch (IOException ex) {
            MARSHAL m = new MARSHAL();
            m.initCause(ex);
            throw m;
        }
    }

    protected abstract void write_content(OutputStream eo);

}
