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

import javax.net.ssl.SSLSession;
import javax.security.auth.Subject;

import org.apache.geronimo.corba.security.SASException;
import org.omg.CORBA.ORB;
import org.omg.CSI.EstablishContext;
import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;


/**
 * @version $Rev: 503274 $ $Date: 2007-02-03 10:19:18 -0800 (Sat, 03 Feb 2007) $
 */
public class TSSConfig implements Serializable {

    private boolean inherit;
    private TSSTransportMechConfig transport_mech;
    private final TSSCompoundSecMechListConfig mechListConfig = new TSSCompoundSecMechListConfig();

    public boolean isInherit() {
        return inherit;
    }

    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }

    public TSSTransportMechConfig getTransport_mech() {
        return transport_mech;
    }

    public void setTransport_mech(TSSTransportMechConfig transport_mech) {
        this.transport_mech = transport_mech;
    }

    public TSSCompoundSecMechListConfig getMechListConfig() {
        return mechListConfig;
    }

    public TaggedComponent generateIOR(ORB orb, Codec codec) throws Exception {
        return mechListConfig.encodeIOR(orb, codec);
    }

    public Subject check(SSLSession session, EstablishContext msg) throws SASException {

        Subject transportSubject = transport_mech.check(session);
        
        Subject mechSubject = mechListConfig.check(msg);
        if (mechSubject != null) return mechSubject;

        return transportSubject;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString("", buf);
        return buf.toString();
    }

    void toString(String spaces, StringBuffer buf) {
        String moreSpaces = spaces + "  ";
        buf.append(spaces).append("TSSConfig: [\n");
        if (transport_mech != null) {
            transport_mech.toString(moreSpaces, buf);
        } else {
            buf.append(moreSpaces).append("null transport_mech\n");
        }
        mechListConfig.toString(moreSpaces, buf);
        buf.append(spaces).append("]\n");
    }
}
