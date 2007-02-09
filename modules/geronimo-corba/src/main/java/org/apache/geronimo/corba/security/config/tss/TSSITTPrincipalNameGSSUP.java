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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import javax.security.auth.Subject;

import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.PrimaryDomainPrincipal;
import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.omg.CORBA.Any;
import org.omg.CSI.GSS_NT_ExportedNameHelper;
import org.omg.CSI.ITTPrincipalName;
import org.omg.CSI.IdentityToken;
import org.omg.GSSUP.GSSUPMechOID;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.apache.geronimo.corba.security.SASException;
import org.apache.geronimo.corba.util.Util;


/**
 * @version $Rev: 503493 $ $Date: 2007-02-04 13:47:55 -0800 (Sun, 04 Feb 2007) $
 */
public class TSSITTPrincipalNameGSSUP extends TSSSASIdentityToken {

    public static final String OID = GSSUPMechOID.value.substring(4);
    private final Class principalClass;
    private transient Constructor constructor;
    private final String realmName;
    private final String domainName;

    public TSSITTPrincipalNameGSSUP(Class principalClass, String realmName, String domainName) throws NoSuchMethodException {
        this.principalClass = principalClass;
        this.realmName = realmName;
        this.domainName = domainName;
        getConstructor();
    }

    private void getConstructor() throws NoSuchMethodException {
        if (constructor == null && principalClass != null) {
            constructor = principalClass.getConstructor(new Class[]{String.class});
        }
    }

    public short getType() {
        return ITTPrincipalName.value;
    }

    public String getOID() {
        return OID;
    }

    public Subject check(IdentityToken identityToken) throws SASException {
        assert principalClass != null;
        byte[] principalNameToken = identityToken.principal_name();
        Any any = null;
        try {
            any = Util.getCodec().decode_value(principalNameToken, GSS_NT_ExportedNameHelper.type());
        } catch (FormatMismatch formatMismatch) {
            throw new SASException(1, formatMismatch);
        } catch (TypeMismatch typeMismatch) {
            throw new SASException(1, typeMismatch);
        }
        byte[] principalNameBytes = GSS_NT_ExportedNameHelper.extract(any);
        String principalName = Util.decodeGSSExportName(principalNameBytes);
        principalName = Util.extractUserNameFromScopedName(principalName);
        Principal basePrincipal = null;
        try {
            getConstructor();
            basePrincipal = (Principal) constructor.newInstance(new Object[]{principalName});
        } catch (InstantiationException e) {
            throw new SASException(1, e);
        } catch (IllegalAccessException e) {
            throw new SASException(1, e);
        } catch (InvocationTargetException e) {
            throw new SASException(1, e);
        } catch (NoSuchMethodException e) {
            throw new SASException(1, e);
        }

        Subject subject = new Subject();
        subject.getPrincipals().add(basePrincipal);
        if (realmName != null && domainName != null) {
            subject.getPrincipals().add(new RealmPrincipal(realmName, domainName, basePrincipal));
            subject.getPrincipals().add(new PrimaryRealmPrincipal(realmName, domainName, basePrincipal));
        }
        if (domainName != null) {
            subject.getPrincipals().add(new DomainPrincipal(domainName, basePrincipal));
            subject.getPrincipals().add(new PrimaryDomainPrincipal(domainName, basePrincipal));
        }

        return subject;
    }

    public void toString(String spaces, StringBuffer buf) {
        String moreSpaces = spaces + "  ";
        buf.append(spaces).append("TSSITTPrincipalNameGSSUP: [\n");
        buf.append(moreSpaces).append("principalClass: ").append(principalClass).append("\n");
        buf.append(moreSpaces).append("domain: ").append(domainName).append("\n");
        buf.append(moreSpaces).append("realm: ").append(realmName).append("\n");
        buf.append(spaces).append("]\n");
    }

}
