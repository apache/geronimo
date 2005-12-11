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

import org.omg.CORBA.portable.OutputStream;
import org.omg.IOP.TAG_MULTIPLE_COMPONENTS;
import org.omg.IOP.TaggedComponent;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.io.EncapsulationInputStream;


public class MultiComponentProfile extends Profile {

    private TaggedComponent[] tagged_components;

    private Component[] components;

    private final AbstractORB orb;

    private byte[] data;

    public MultiComponentProfile(AbstractORB orb, byte[] data) {
        this.orb = orb;
        this.data = data;
    }


    public static Profile read(AbstractORB orb, byte[] data) {

        EncapsulationInputStream ein2 = new EncapsulationInputStream(orb, data);
        MultiComponentProfile result = new MultiComponentProfile(orb, data);
        result.tagged_components = TaggedComponentSeqHelper.read(ein2);
        return result;
    }

    public int tag() {
        return TAG_MULTIPLE_COMPONENTS.value;
    }

    int getComponentCount() {
        if (tagged_components == null) {
            return 0;
        } else {
            return tagged_components.length;
        }
    }

    public int getTag(int idx) {
        return tagged_components[idx].tag;
    }

    public TaggedComponent getTaggedComponent(int idx) {
        return tagged_components[idx];
    }

    public Component getComponent(int idx) {

        if (components == null) {
            components = new Component[getComponentCount()];
        }

        if (components[idx] == null) {
            components[idx] = Component.read(orb, tagged_components[idx].tag,
                                             tagged_components[idx].component_data);
        }

        return components[idx];
    }

    protected void write_content(OutputStream eo) {
        TaggedComponentSeqHelper.write(eo, tagged_components);
    }


    protected byte[] get_encapsulation_bytes() {
        return data;
    }

}
