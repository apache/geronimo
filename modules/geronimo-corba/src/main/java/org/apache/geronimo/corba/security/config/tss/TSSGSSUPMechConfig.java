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

import java.io.UnsupportedEncodingException;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.omg.CORBA.ORB;
import org.omg.CSI.EstablishContext;
import org.omg.CSIIOP.AS_ContextSec;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.GSSUP.GSSUPMechOID;
import org.omg.GSSUP.InitialContextToken;
import org.omg.IOP.Codec;

import org.apache.geronimo.security.jaas.UsernamePasswordCallback;
import org.apache.geronimo.security.ContextManager;

import org.apache.geronimo.corba.security.SASException;
import org.apache.geronimo.corba.util.Util;


/**
 * @version $Rev: 503493 $ $Date: 2007-02-04 13:47:55 -0800 (Sun, 04 Feb 2007) $
 */
public class TSSGSSUPMechConfig extends TSSASMechConfig {

    private String targetName;
    private boolean required;

    public TSSGSSUPMechConfig() {
    }

    public TSSGSSUPMechConfig(AS_ContextSec context) {
        targetName = Util.decodeGSSExportName(context.target_name);
        required = (context.target_requires == EstablishTrustInClient.value);
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public short getSupports() {
        return EstablishTrustInClient.value;
    }

    public short getRequires() {
        return (required ? EstablishTrustInClient.value : 0);
    }

    public AS_ContextSec encodeIOR(ORB orb, Codec codec) throws Exception {
        AS_ContextSec result = new AS_ContextSec();

        result.target_supports = EstablishTrustInClient.value;
        result.target_requires = (required ? EstablishTrustInClient.value : 0);
        result.client_authentication_mech = Util.encodeOID(GSSUPMechOID.value);
        result.target_name = Util.encodeGSSExportName(GSSUPMechOID.value, targetName);

        return result;
    }

    public Subject check(EstablishContext msg) throws SASException {
        Subject result = null;

        try {
            if (msg.client_authentication_token != null && msg.client_authentication_token.length > 0) {
                InitialContextToken token = new InitialContextToken();

                if (!Util.decodeGSSUPToken(Util.getCodec(), msg.client_authentication_token, token))
                    throw new SASException(2);

                if (token.target_name == null) return null;

                String tokenTargetName = (token.target_name == null ? targetName : new String(token.target_name, "UTF8"));

                if (!targetName.equals(tokenTargetName)) throw new SASException(2);
                String userName = Util.extractUserNameFromScopedName(token.username);

                LoginContext context = ContextManager.login(tokenTargetName,
                        new UsernamePasswordCallback(userName,
                                new String(token.password, "UTF8").toCharArray()));
                result = context.getSubject();
            }
        } catch (UnsupportedEncodingException e) {
            throw new SASException(1, e);
        } catch (LoginException e) {
            throw new SASException(1, e);
        }


        return result;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString("", buf);
        return buf.toString();
    }

    public void toString(String spaces, StringBuffer buf) {
        String moreSpaces = spaces + "  ";
        buf.append(spaces).append("TSSGSSUPMechConfig: [\n");
        buf.append(moreSpaces).append("targetName:   ").append(targetName).append("\n");
        buf.append(moreSpaces).append("required  :   ").append(required).append("\n");
        buf.append(spaces).append("]\n");
    }

}
