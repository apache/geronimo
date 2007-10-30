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
import java.util.List;
import javax.net.ssl.SSLSession;
import javax.security.auth.Subject;

import org.omg.CORBA.ORB;
import org.omg.CSIIOP.TAG_NULL_TAG;
import org.omg.CSIIOP.TAG_SECIOP_SEC_TRANS;
import org.omg.CSIIOP.TAG_TLS_SEC_TRANS;
import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;

import org.apache.geronimo.corba.security.SASException;


/**
 * @version $Rev: 503274 $ $Date: 2007-02-03 10:19:18 -0800 (Sat, 03 Feb 2007) $
 */
public abstract class TSSTransportMechConfig implements Serializable {

    private boolean trustEveryone;
    private boolean trustNoone = true;
    private final List entities = new ArrayList();

    public boolean isTrustEveryone() {
        return trustEveryone;
    }

    public void setTrustEveryone(boolean trustEveryone) {
        this.trustEveryone = trustEveryone;
    }

    public boolean isTrustNoone() {
        return trustNoone;
    }

    public void setTrustNoone(boolean trustNoone) {
        this.trustNoone = trustNoone;
    }

    public List getEntities() {
        return entities;
    }

    public abstract short getSupports();

    public abstract short getRequires();

    public abstract TaggedComponent encodeIOR(ORB orb, Codec codec) throws Exception;

    public static TSSTransportMechConfig decodeIOR(Codec codec, TaggedComponent tc) throws Exception {
        TSSTransportMechConfig result = null;

        if (tc.tag == TAG_NULL_TAG.value) {
            result = new TSSNULLTransportConfig();
        } else if (tc.tag == TAG_TLS_SEC_TRANS.value) {
            result = new TSSSSLTransportConfig(tc, codec);
        } else if (tc.tag == TAG_SECIOP_SEC_TRANS.value) {
            result = new TSSSECIOPTransportConfig(tc, codec);
        }

        return result;
    }

    public abstract Subject check(SSLSession session) throws SASException;

    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString("", buf);
        return buf.toString();
    }

    abstract void toString(String spaces, StringBuffer buf);

}
