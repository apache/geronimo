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

import javax.net.ssl.SSLSession;
import javax.security.auth.Subject;

import org.omg.CORBA.ORB;
import org.omg.CSIIOP.TAG_NULL_TAG;
import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;

import org.apache.geronimo.corba.security.SASException;


/**
 * At the moment, this config class can only handle a single address.
 *
 * @version $Rev: 503274 $ $Date: 2007-02-03 10:19:18 -0800 (Sat, 03 Feb 2007) $
 */
public class TSSNULLTransportConfig extends TSSTransportMechConfig {

    public short getSupports() {
        return 0;
    }

    public short getRequires() {
        return 0;
    }

    public TaggedComponent encodeIOR(ORB orb, Codec codec) {
        TaggedComponent result = new TaggedComponent();

        result.tag = TAG_NULL_TAG.value;
        result.component_data = new byte[0];

        return result;
    }

    /**
     * Returns null subject, since the transport layer can not establish the subject.
     * @param session
     * @return
     * @throws SASException
     */
    public Subject check(SSLSession session) throws SASException {
        return null;
    }

    public void toString(String spaces, StringBuffer buf) {
        buf.append(spaces).append("TSSNULLTransportConfig\n");
    }

}
