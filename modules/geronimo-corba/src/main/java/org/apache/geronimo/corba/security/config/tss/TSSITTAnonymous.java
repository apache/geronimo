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

import javax.security.auth.Subject;

import org.omg.CSI.ITTAnonymous;
import org.omg.CSI.IdentityToken;
import org.apache.geronimo.corba.security.SASException;


/**
 * @version $Rev: 503274 $ $Date: 2007-02-03 10:19:18 -0800 (Sat, 03 Feb 2007) $
 */
public class TSSITTAnonymous extends TSSSASIdentityToken {

    public static final String OID = "";

    public TSSITTAnonymous() {
    }

    public short getType() {
        return ITTAnonymous.value;
    }

    public String getOID() {
        return OID;
    }

    public Subject check(IdentityToken identityToken) throws SASException {
        //TODO this is wrong.  The anonymous identity token should force the default subject/principal since
        //TODO the presence of an identity token is an identity assertion.
        return null;
    }

    public void toString(String spaces, StringBuffer buf) {
        buf.append(spaces).append("TSSITTAnonymous\n");
    }

}
