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

import java.security.Principal;
import java.util.Iterator;
import java.util.Set;
import javax.security.auth.Subject;

import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.PrimaryDomainPrincipal;
import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.omg.CORBA.Any;
import org.omg.CSI.GSS_NT_ExportedNameHelper;
import org.omg.CSI.IdentityToken;
import org.omg.GSSUP.GSSUPMechOID;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.apache.geronimo.corba.util.Util;


/**
 * @version $Revision: 503493 $ $Date: 2007-02-04 13:47:55 -0800 (Sun, 04 Feb 2007) $
 */
public class CSSSASITTPrincipalNameDynamic implements CSSSASIdentityToken {

    private final String oid;
    private final Class principalClass;
    private final String domain;
    private final String realm;

//    public CSSSASITTPrincipalNameDynamic(String domain) {
//        this(GSSUPMechOID.value.substring(4), domain);
//    }

    public CSSSASITTPrincipalNameDynamic(String oid, Class principalClass, String domain, String realm) {
        this.oid = (oid == null ? GSSUPMechOID.value.substring(4) : oid);
        this.principalClass = principalClass;
        this.domain = domain;
        this.realm = realm;
    }

    /**
     * TODO should also use login domains?
     * @return IdentityToken
     */
    public IdentityToken encodeIdentityToken() {

        IdentityToken token = null;
        Subject subject = ContextManager.getNextCaller();
        String principalName = null;
        if (subject == null) {
//            Set principals = Collections.EMPTY_SET;
        } else if (realm != null) {
            Set principals = subject.getPrincipals(RealmPrincipal.class);
            for (Iterator iter = principals.iterator(); iter.hasNext();) {
                RealmPrincipal p = (RealmPrincipal) iter.next();
                if (p.getRealm().equals(realm) && p.getLoginDomain().equals(domain) && p.getPrincipal().getClass().equals(principalClass)) {
                    principalName = p.getPrincipal().getName();
                    if (p instanceof PrimaryRealmPrincipal) break;
                }
            }
        } else if (domain != null) {
            Set principals = subject.getPrincipals(DomainPrincipal.class);
            for (Iterator iter = principals.iterator(); iter.hasNext();) {
                DomainPrincipal p = (DomainPrincipal) iter.next();
                if (p.getDomain().equals(domain) && p.getPrincipal().getClass().equals(principalClass)) {
                    principalName = p.getPrincipal().getName();
                    if (p instanceof PrimaryDomainPrincipal) break;
                }
            }
        } else {
            Set principals = subject.getPrincipals(principalClass);
            if (!principals.isEmpty()) {
                Principal principal = (Principal) principals.iterator().next();
                principalName = principal.getName();

            }
        }

        if (principalName != null) {

            Any any = Util.getORB().create_any();

            //TODO consider including a domain in this scoped-username
            GSS_NT_ExportedNameHelper.insert(any, Util.encodeGSSExportName(oid, principalName));

            byte[] encoding = null;
            try {
                encoding = Util.getCodec().encode_value(any);
            } catch (InvalidTypeForEncoding itfe) {
                throw new IllegalStateException("Unable to encode principal name '" + principalName + "' " + itfe, itfe);
            }

            token = new IdentityToken();
            token.principal_name(encoding);
        } else {
            token = new IdentityToken();
            token.anonymous(true);
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
        buf.append(spaces).append("CSSSASITTPrincipalNameDynamic: [\n");
        buf.append(moreSpaces).append("oid: ").append(oid).append("\n");
        buf.append(moreSpaces).append("principalClass: ").append(principalClass).append("\n");
        buf.append(moreSpaces).append("domain: ").append(domain).append("\n");
        buf.append(moreSpaces).append("realm: ").append(realm).append("\n");
        buf.append(spaces).append("]\n");
    }
    
}
