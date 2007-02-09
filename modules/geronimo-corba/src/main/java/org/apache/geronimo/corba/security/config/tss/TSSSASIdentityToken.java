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

import java.io.Serializable;
import javax.security.auth.Subject;

import org.omg.CSI.IdentityToken;
import org.apache.geronimo.corba.security.SASException;


/**
 * @version $Rev: 503274 $ $Date: 2007-02-03 10:19:18 -0800 (Sat, 03 Feb 2007) $
 */
public abstract class TSSSASIdentityToken implements Serializable {

    public abstract short getType();

    public abstract String getOID();

    public abstract Subject check(IdentityToken identityToken) throws SASException;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TSSSASIdentityToken)) return false;

        final TSSSASIdentityToken token = (TSSSASIdentityToken) o;

        if (getType() != token.getType()) return false;
        if (!getOID().equals(token.getOID())) return false;

        return true;
    }

    public int hashCode() {
        int result = getOID().hashCode();
        result = 29 * result + (int) getType();
        return result;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString("", buf);
        return buf.toString();
    }

    abstract void toString(String spaces, StringBuffer buf);

}
