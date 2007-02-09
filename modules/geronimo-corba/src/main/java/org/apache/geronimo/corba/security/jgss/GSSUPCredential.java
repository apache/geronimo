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

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Provider;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;

import org.apache.geronimo.security.jaas.UsernamePasswordCredential;


/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class GSSUPCredential implements GSSCredentialSpi {

    private GSSNameSpi name;
    private int initLifetime;
    private int acceptLifetime;
    private int usage;
    private UsernamePasswordCredential credential;

    GSSUPCredential(GSSNameSpi name, int initLifetime, int acceptLifetime, int usage) {
        this.name = name;
        this.initLifetime = initLifetime;
        this.acceptLifetime = acceptLifetime;
        this.usage = usage;

        if (isInitiatorCredential()) {
            AccessControlContext acc = AccessController.getContext();
            credential = (UsernamePasswordCredential) AccessController.doPrivileged(new SubjectComber(acc, name.toString()));
        }
    }

    public UsernamePasswordCredential getCredential() {
        return credential;
    }

    public int getAcceptLifetime() throws GSSException {
        return acceptLifetime;
    }

    public int getInitLifetime() throws GSSException {
        return initLifetime;
    }

    public void dispose() throws GSSException {
        credential = null;
    }

    public boolean isAcceptorCredential() {
        return usage == GSSCredential.ACCEPT_ONLY || usage == GSSCredential.INITIATE_AND_ACCEPT;
    }

    public boolean isInitiatorCredential() {
        return usage == GSSCredential.INITIATE_ONLY || usage == GSSCredential.INITIATE_AND_ACCEPT;
    }

    public Provider getProvider() {
        return GSSUPMechanismFactory.PROVIDER;
    }

    public Oid getMechanism() {
        return GSSUPMechanismFactory.MECHANISM_OID;
    }

    public GSSNameSpi getName() throws GSSException {
        return name;
    }
}
