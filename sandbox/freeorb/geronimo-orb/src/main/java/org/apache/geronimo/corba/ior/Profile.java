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

import org.omg.CORBA.OctetSeqHelper;
import org.omg.CORBA.portable.OutputStream;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TAG_MULTIPLE_COMPONENTS;
import org.omg.IOP.TaggedComponent;

import org.apache.geronimo.corba.AbstractORB;


public abstract class Profile extends TaggedValue {

    public Profile() {
    }

    /**
     * write content including tag
     */
    public void write(OutputStream out) {
        out.write_long(tag());
        OctetSeqHelper.write(out, get_encapsulation_bytes());
    }

    protected abstract byte[] get_encapsulation_bytes();

    public static Profile read(AbstractORB orb, int tag, byte[] data) {

        switch (tag) {
            case TAG_INTERNET_IOP.value:
                return IIOPProfile.read(orb, data);

            case TAG_MULTIPLE_COMPONENTS.value:
                return MultiComponentProfile.read(orb, data);

            default:
                return new UnknownProfile(tag, data);
        }
    }


    abstract int getComponentCount();

    public abstract int getTag(int idx);

    public abstract TaggedComponent getTaggedComponent(int idx);

    public abstract Component getComponent(int idx);

}
