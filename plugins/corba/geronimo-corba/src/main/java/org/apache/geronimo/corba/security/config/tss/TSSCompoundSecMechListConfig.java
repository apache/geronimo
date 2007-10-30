/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.security.config.tss;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import javax.security.auth.Subject;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CSI.EstablishContext;
import org.omg.CSIIOP.CompoundSecMech;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;

import org.apache.geronimo.corba.security.SASException;


/**
 * @version $Rev: 503274 $ $Date: 2007-02-03 10:19:18 -0800 (Sat, 03 Feb 2007) $
 */
public class TSSCompoundSecMechListConfig implements Serializable {

    private boolean stateful;
    private final ArrayList mechs = new ArrayList();

    public boolean isStateful() {
        return stateful;
    }

    public void setStateful(boolean stateful) {
        this.stateful = stateful;
    }

    public void add(TSSCompoundSecMechConfig mech) {
        mechs.add(mech);
    }

    public TSSCompoundSecMechConfig mechAt(int i) {
        return (TSSCompoundSecMechConfig) mechs.get(i);
    }

    public int size() {
        return mechs.size();
    }

    public TaggedComponent encodeIOR(ORB orb, Codec codec) throws Exception {
        CompoundSecMechList csml = new CompoundSecMechList();

        csml.stateful = stateful;
        csml.mechanism_list = new CompoundSecMech[mechs.size()];

        for (int i = 0; i < mechs.size(); i++) {
            csml.mechanism_list[i] = ((TSSCompoundSecMechConfig) mechs.get(i)).encodeIOR(orb, codec);
        }

        Any any = orb.create_any();
        CompoundSecMechListHelper.insert(any, csml);

        return new TaggedComponent(TAG_CSI_SEC_MECH_LIST.value, codec.encode_value(any));
    }

    public static TSSCompoundSecMechListConfig decodeIOR(Codec codec, TaggedComponent taggedComponent) throws Exception {
        TSSCompoundSecMechListConfig result = new TSSCompoundSecMechListConfig();

        Any any = codec.decode_value(taggedComponent.component_data, CompoundSecMechListHelper.type());
        CompoundSecMechList csml = CompoundSecMechListHelper.extract(any);

        result.setStateful(csml.stateful);

        for (int i = 0; i < csml.mechanism_list.length; i++) {
            result.add(TSSCompoundSecMechConfig.decodeIOR(codec, csml.mechanism_list[i]));
        }

        return result;
    }

    public Subject check(EstablishContext msg) throws SASException {
        Subject result = null;

        for (int i = 0; i < mechs.size(); i++) {
            result = ((TSSCompoundSecMechConfig) mechs.get(i)).check(msg);
            if (result != null) break;
        }

        return result;
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString("", buf);
        return buf.toString();
    }

    void toString(String spaces, StringBuffer buf) {
        buf.append(spaces).append("TSSCompoundSecMechListConfig: [\n");
        for (Iterator availMechs = mechs.iterator(); availMechs.hasNext();) {
            TSSCompoundSecMechConfig aConfig = (TSSCompoundSecMechConfig) availMechs.next();
            aConfig.toString(spaces + "  ", buf);
            buf.append("\n");
        }
        buf.append(spaces).append("]\n");
    }

}
