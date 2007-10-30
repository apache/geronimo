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
package org.apache.geronimo.corba.security.config.css;

import java.io.Serializable;

import org.omg.CSI.AuthorizationElement;
import org.omg.CSI.IdentityToken;

import org.apache.geronimo.corba.security.config.tss.TSSSASMechConfig;
import org.apache.geronimo.corba.security.config.ConfigUtil;


/**
 * @version $Rev: 503274 $ $Date: 2007-02-03 10:19:18 -0800 (Sat, 03 Feb 2007) $
 */
public class CSSSASMechConfig implements Serializable {

    private short supports;
    private short requires;
    private boolean required;
    private CSSSASIdentityToken identityToken;


    public short getSupports() {
        return supports;
    }

    public short getRequires() {
        return requires;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public CSSSASIdentityToken getIdentityToken() {
        return identityToken;
    }

    public void setIdentityToken(CSSSASIdentityToken identityToken) {
        this.identityToken = identityToken;
    }

    public boolean canHandle(TSSSASMechConfig sasMech) {
        if ((supports & sasMech.getRequires()) != sasMech.getRequires()) return false;
        if ((requires & sasMech.getSupports()) != requires) return false;

        // TODO: FILL THIS IN

        return true;
    }

    public AuthorizationElement[] encodeAuthorizationElement() {
        return new AuthorizationElement[0];
    }

    public IdentityToken encodeIdentityToken() {
        return identityToken.encodeIdentityToken();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString("", buf);
        return buf.toString();
    }

    void toString(String spaces, StringBuffer buf) {
        String moreSpaces = spaces + "  ";
        buf.append(spaces).append("CSSSASMechConfig: [\n");
        buf.append(moreSpaces).append("SUPPORTS: ").append(ConfigUtil.flags(supports)).append("\n");
        buf.append(moreSpaces).append("REQUIRES: ").append(ConfigUtil.flags(requires)).append("\n");
        if (identityToken != null) {
            identityToken.toString(moreSpaces, buf);
        }
        buf.append(spaces).append("]\n");
    }

}
