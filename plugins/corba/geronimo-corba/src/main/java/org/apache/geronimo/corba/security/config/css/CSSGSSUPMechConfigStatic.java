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

import org.apache.geronimo.corba.security.config.tss.TSSASMechConfig;
import org.apache.geronimo.corba.security.config.tss.TSSGSSUPMechConfig;
import org.apache.geronimo.corba.util.Util;


/**
 * @version $Revision: 503493 $ $Date: 2007-02-04 13:47:55 -0800 (Sun, 04 Feb 2007) $
 */
public class CSSGSSUPMechConfigStatic implements CSSASMechConfig {

    private final String username;
    private final String password;
    private final String domain;
    private transient byte[] encoding;

    public CSSGSSUPMechConfigStatic(String username, String password, String domain) {
        this.username = username;
        this.password = password;
        this.domain = domain;
    }

    public short getSupports() {
        return 0;
    }

    public short getRequires() {
        return 0;
    }

    public boolean canHandle(TSSASMechConfig asMech) {
        if (asMech instanceof TSSGSSUPMechConfig) return true;
        if (asMech.getRequires() == 0) return true;

        return false;
    }

    public byte[] encode() {
        if (encoding == null) {
            String scopedUserName = Util.buildScopedUserName(username, domain);
            encoding = Util.encodeGSSUPToken(Util.getORB(), Util.getCodec(), scopedUserName, password, domain);

            if (encoding == null) encoding = new byte[0];
        }
        return encoding;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString("", buf);
        return buf.toString();
    }

    public void toString(String spaces, StringBuffer buf) {
        String moreSpaces = spaces + "  ";
        buf.append(spaces).append("CSSGSSUPMechConfigStatic: [\n");
        buf.append(moreSpaces).append("username: ").append(username).append("\n");
        buf.append(moreSpaces).append("password: ").append(password).append("\n");
        buf.append(moreSpaces).append("domain:   ").append(domain).append("\n");
        buf.append(spaces).append("]\n");
    }

}
