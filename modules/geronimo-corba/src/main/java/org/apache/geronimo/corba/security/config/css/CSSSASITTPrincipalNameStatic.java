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

import org.omg.CORBA.Any;
import org.omg.CSI.GSS_NT_ExportedNameHelper;
import org.omg.CSI.IdentityToken;
import org.omg.GSSUP.GSSUPMechOID;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;

import org.apache.geronimo.corba.util.Util;


/**
 * @version $Revision: 503493 $ $Date: 2007-02-04 13:47:55 -0800 (Sun, 04 Feb 2007) $
 */
public class CSSSASITTPrincipalNameStatic implements CSSSASIdentityToken {

    private final String oid;
    private final String name;
    private transient IdentityToken token;

    public CSSSASITTPrincipalNameStatic(String name) {

        this(GSSUPMechOID.value.substring(4), name);
    }

    public CSSSASITTPrincipalNameStatic(String oid, String name) {
        this.oid = (oid == null ? GSSUPMechOID.value.substring(4) : oid);
        this.name = name;
    }

    public IdentityToken encodeIdentityToken() {

        if (token == null) {
            Any any = Util.getORB().create_any();
            //TODO consider including a domain in this scoped-username
            GSS_NT_ExportedNameHelper.insert(any, Util.encodeGSSExportName(oid, name));

            byte[] encoding = null;
            try {
                encoding = Util.getCodec().encode_value(any);
            } catch (InvalidTypeForEncoding itfe) {
                throw new IllegalStateException("Unable to encode principal name '" + name + "' " + itfe, itfe);
            }

            token = new IdentityToken();
            token.principal_name(encoding);
        }
        return token;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString("", buf);
        return buf.toString();
    }

    public void toString(String spaces, StringBuffer buf) {
        String moreSpaces = spaces + "  ";
        buf.append(spaces).append("CSSSASITTPrincipalNameStatic: [\n");
        buf.append(moreSpaces).append("oid: ").append(oid).append("\n");
        buf.append(moreSpaces).append("name: ").append(name).append("\n");
        buf.append(spaces).append("]\n");
    }

}
