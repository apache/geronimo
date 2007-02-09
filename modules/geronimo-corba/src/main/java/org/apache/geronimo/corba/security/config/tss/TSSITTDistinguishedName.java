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
import javax.security.auth.x500.X500Principal;

import org.omg.CORBA.Any;
import org.omg.CSI.ITTDistinguishedName;
import org.omg.CSI.IdentityToken;
import org.omg.CSI.X501DistinguishedNameHelper;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.TypeMismatch;

import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.PrimaryDomainPrincipal;
import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.RealmPrincipal;

import org.apache.geronimo.corba.security.SASException;
import org.apache.geronimo.corba.util.Util;


/**
 * @version $Rev: 503274 $ $Date: 2007-02-03 10:19:18 -0800 (Sat, 03 Feb 2007) $
 */
public class TSSITTDistinguishedName extends TSSSASIdentityToken {

    public static final String OID = "";
    private final String realmName;
    private final String domainName;

    public TSSITTDistinguishedName(String realmName, String domainName) {
        this.realmName = realmName;
        this.domainName = domainName;
    }

    public short getType() {
        return ITTDistinguishedName.value;
    }

    public String getOID() {
        return OID;
    }

    public Subject check(IdentityToken identityToken) throws SASException {
        byte[] distinguishedNameToken = identityToken.dn();
        Any any = null;
        try {
            any = Util.getCodec().decode_value(distinguishedNameToken, X501DistinguishedNameHelper.type());
        } catch (FormatMismatch formatMismatch) {
            throw new SASException(1, formatMismatch);
        } catch (TypeMismatch typeMismatch) {
            throw new SASException(1, typeMismatch);
        }

        byte[] principalNameBytes = X501DistinguishedNameHelper.extract(any);
        Subject subject = new Subject();
        X500Principal x500Principal = new X500Principal(principalNameBytes);
        subject.getPrincipals().add(x500Principal);

        if (realmName != null && domainName != null) {
            subject.getPrincipals().add(new RealmPrincipal(realmName, domainName, x500Principal));
            subject.getPrincipals().add(new PrimaryRealmPrincipal(realmName, domainName, x500Principal));
        }
        if (domainName != null) {
            subject.getPrincipals().add(new DomainPrincipal(domainName, x500Principal));
            subject.getPrincipals().add(new PrimaryDomainPrincipal(domainName, x500Principal));
        }

        return subject;
    }

    public void toString(String spaces, StringBuffer buf) {
        String moreSpaces = spaces + "  ";
        buf.append(spaces).append("TSSITTDistinguishedName: [\n");
        buf.append(moreSpaces).append("domain: ").append(domainName).append("\n");
        buf.append(moreSpaces).append("realm: ").append(realmName).append("\n");
        buf.append(spaces).append("]\n");
    }

}
