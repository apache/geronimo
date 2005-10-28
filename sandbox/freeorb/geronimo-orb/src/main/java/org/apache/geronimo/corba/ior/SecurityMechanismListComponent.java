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
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.io.EncapsulationInputStream;


public class SecurityMechanismListComponent extends Component {

    private final AbstractORB orb;
    private final CompoundSecMechList mech_list;
    private CompoundSecurityMechanism[] mechs;

    public SecurityMechanismListComponent(AbstractORB orb2, CompoundSecMechList mech_list) {
        this.orb = orb2;
        this.mech_list = mech_list;
    }

    public int tag() {
        return TAG_CSI_SEC_MECH_LIST.value;
    }

    protected void write_content(OutputStream out) {
        CompoundSecMechListHelper.write(out, mech_list);
    }

    public int getMechanismCount() {
        return mech_list.mechanism_list.length;
    }

    public int getTransportMechanismTag(int j) {
        return mech_list.mechanism_list[j].transport_mech.tag;
    }

    public static Component read(AbstractORB orb, byte[] data) {
        EncapsulationInputStream ein = new EncapsulationInputStream(orb, data);
        CompoundSecMechList mech_list = CompoundSecMechListHelper.read(ein);
        return new SecurityMechanismListComponent(orb, mech_list);
    }

    public CompoundSecurityMechanism getSecurityMechanism(int j) {
        if (mechs == null) {
            mechs = new CompoundSecurityMechanism[mech_list.mechanism_list.length];
        }

        if (mechs[j] == null) {
            mechs[j] = new CompoundSecurityMechanism(orb, mech_list.mechanism_list[j]);
        }

        return mechs[j];
    }

}
