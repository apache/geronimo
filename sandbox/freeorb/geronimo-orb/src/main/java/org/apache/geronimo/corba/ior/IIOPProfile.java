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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.omg.CORBA.portable.OutputStream;
import org.omg.IIOP.ProfileBody_1_0;
import org.omg.IIOP.ProfileBody_1_0Helper;
import org.omg.IIOP.Version;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedComponent;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.io.EncapsulationInputStream;
import org.apache.geronimo.corba.io.EncapsulationOutputStream;


public class IIOPProfile extends Profile {

    byte[] bytes;

    ProfileBody_1_0 body;
    TaggedComponent[] tagged_components;
    Component[] components;
    private final AbstractORB orb;
    private IIOPTransportSpec saddr;

    IIOPProfile(AbstractORB orb) {
        super();
        this.orb = orb;
    }

    public int tag() {
        return TAG_INTERNET_IOP.value;
    }

    public Version getVersion() {
        return body.iiop_version;
    }

    public byte[] getObjectKey() {
        return body.object_key;
    }

    public int getComponentCount() {
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


    public static Profile read(AbstractORB orb, byte[] data) {

        EncapsulationInputStream ein = new EncapsulationInputStream(orb, data);
        IIOPProfile result = new IIOPProfile(orb);
        result.body = ProfileBody_1_0Helper.read(ein);
        if (result.body.iiop_version.major == 1
            && result.body.iiop_version.minor >= 1)
        {
            result.tagged_components = TaggedComponentSeqHelper.read(ein);
        }

        result.bytes = data;
        return result;
    }

    public IIOPTransportSpec getInetTransport() throws UnknownHostException {
        if (saddr == null) {
            saddr = new IIOPTransportSpec(getVersion(), getAddress(), getPort());
        }
        return saddr;
    }

    public InetAddress getAddress() throws UnknownHostException {
        return orb.getAddress(body.host);
    }

    public String getHost() {
        return body.host;
    }

    public int getPort() {
        return (body.port & 0xffff);
    }

    protected void write_content(OutputStream eo) {
        ProfileBody_1_0Helper.write(eo, body);
        if (body.iiop_version.major == 1 && body.iiop_version.minor > 0) {
            TaggedComponentSeqHelper.write(eo, tagged_components);
        }
    }

    protected byte[] get_encapsulation_bytes() {
        // TODO Auto-generated method stub
        return null;
    }

    protected void write_content(EncapsulationOutputStream eo) {
        // TODO Auto-generated method stub

    }


}
