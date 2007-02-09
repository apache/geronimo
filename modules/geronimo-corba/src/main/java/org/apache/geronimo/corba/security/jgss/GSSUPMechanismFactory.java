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
package org.apache.geronimo.corba.security.jgss;

import java.security.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.security.jgss.spi.GSSContextSpi;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.jgss.spi.MechanismFactory;


/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public final class GSSUPMechanismFactory implements MechanismFactory {

    private final static Log log = LogFactory.getLog(GSSUPMechanismFactory.class);

    final static Oid MECHANISM_OID;
    private final static Oid[] NAME_TYPES = new Oid[]{GSSName.NT_HOSTBASED_SERVICE, GSSName.NT_USER_NAME};
    final static Provider PROVIDER;

    public Provider getProvider() {
        return PROVIDER;
    }

    public Oid getMechanismOid() {
        return MECHANISM_OID;
    }

    public Oid[] getNameTypes() {
        return NAME_TYPES;
    }

    public GSSContextSpi getMechanismContext(byte[] exportedContext) throws GSSException {
        return GSSUPContext.importGSSUPContext(exportedContext);
    }

    public GSSContextSpi getMechanismContext(GSSCredentialSpi myAcceptorCred) throws GSSException {
        return new GSSUPContext(myAcceptorCred);
    }

    public GSSCredentialSpi getCredentialElement(GSSNameSpi name, int initLifetime, int acceptLifetime, int usage) {
        if (name == null) {
            if (usage == GSSCredential.INITIATE_ONLY || usage == GSSCredential.INITIATE_AND_ACCEPT) {
                name = new GSSUPAnonUserName();
            } else {
                name = new GSSUPAnonServerName();
            }
        }
        return new GSSUPCredential(name, initLifetime, acceptLifetime, usage);
    }

    public GSSNameSpi getNameElement(byte[] name, Oid nameType) throws GSSException {
        if (nameType.equals(GSSName.NT_HOSTBASED_SERVICE)) {
            return new GSSUPServerName(name);
        } else if (nameType.equals(GSSName.NT_USER_NAME)) {
            return new GSSUPUserName(name);
        }
        throw new GSSException(GSSException.BAD_NAMETYPE, -1, nameType.toString() + " is an unsupported nametype");
    }

    public GSSContextSpi getMechanismContext(GSSNameSpi peer, GSSCredentialSpi myInitiatorCred, int lifetime) {
        return new GSSUPContext(peer, myInitiatorCred, lifetime);
    }

    public GSSNameSpi getNameElement(String nameStr, Oid nameType) throws GSSException {
        return getNameElement(nameStr.getBytes(), nameType);
    }

    static {
        Oid tempOID = null;
        try {
            tempOID = new Oid("2.23.130.1.1.1");
        } catch (GSSException e) {
            log.fatal("Unable to initialize mechanisms OID: " + e);
        }
        MECHANISM_OID = tempOID;

        PROVIDER = new GSSUPProvider();
    }

}
