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

import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.security.jgss.spi.GSSNameSpi;


/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public final class GSSUPUserName implements GSSNameSpi {
    private final byte[] name;

    GSSUPUserName(byte[] name) {
        this.name = name;
    }

    public boolean isAnonymousName() {
        return false;
    }

    public byte[] export() {
        byte[] result = new byte[name.length];

        System.arraycopy(name, 0, result, 0, name.length);

        return result;
    }

    public Provider getProvider() {
        return GSSUPMechanismFactory.PROVIDER;
    }

    public Oid getMechanism() {
        return GSSUPMechanismFactory.MECHANISM_OID;
    }

    public Oid getStringNameType() {
        return GSSName.NT_USER_NAME;
    }

    public boolean equals(GSSNameSpi obj) {
        if (obj == null) return false;
        if (!(obj instanceof GSSUPUserName)) return false;

        GSSUPUserName test = (GSSUPUserName) obj;
        for (int i = 0; i < name.length; i++) {
            if (name[i] != test.name[i]) return false;
        }
        return true;
    }

    public String toString() {
        return new String(name);
    }
}
