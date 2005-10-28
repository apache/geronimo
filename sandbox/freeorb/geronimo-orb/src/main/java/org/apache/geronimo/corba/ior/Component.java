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

import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;
import org.omg.IOP.TAG_CODE_SETS;

import org.apache.geronimo.corba.AbstractORB;


public abstract class Component extends TaggedValue {


    public static Component read(AbstractORB orb, int tag, byte[] data) {

        switch (tag) {
            case TAG_CODE_SETS.value:
                return CodeSetsComponent.read(orb, data);

            case org.omg.IOP.TAG_ORB_TYPE.value:
                return ORBTypeComponent.read(orb, data);

            case TAG_ALTERNATE_IIOP_ADDRESS.value:
                return AlternateIIOPComponent.read(orb, data);

            case TAG_CSI_SEC_MECH_LIST.value:
                return SecurityMechanismListComponent.read(orb, data);

            default:
                return new UnknownComponent(tag, data);
        }
    }


}
