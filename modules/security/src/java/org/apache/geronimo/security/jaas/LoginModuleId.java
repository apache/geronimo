/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.security.jaas;

import java.io.Serializable;


/**
 * @version $Revision: 1.1 $ $Date: 2004/02/17 04:30:29 $
 */
public class LoginModuleId implements Serializable {
    private final Long loginModuleId;
    private final byte[] hash;
    private transient int hashCode;
    private transient String name;

    public LoginModuleId(Long loginModuleId, byte[] hash) {
        this.loginModuleId = loginModuleId;
        this.hash = hash;
    }

    public Long getLoginModuleId() {
        return loginModuleId;
    }

    public byte[] getHash() {
        return hash;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof LoginModuleId)) return false;

        LoginModuleId another = (LoginModuleId) obj;
        if (!another.loginModuleId.equals(loginModuleId)) return false;
        for (int i = 0; i < hash.length; i++) {
            if (another.hash[i] != hash[i]) return false;
        }
        return true;
    }

    public String toString() {
        if (name == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append('[');
            buffer.append(loginModuleId);
            buffer.append(":0x");
            for (int i = 0; i < hash.length; i++) {
                buffer.append(HEXCHAR[(hash[i]>>>4)&0x0F]);
                buffer.append(HEXCHAR[(hash[i]    )&0x0F]);
            }
            buffer.append(']');
            name = buffer.toString();
        }
        return name;
    }

    /**
     * Returns a hashcode for this LoginModuleId.
     *
     * @return a hashcode for this LoginModuleId.
     */
    public int hashCode() {
        if (hashCode == 0) {
            for (int i = 0; i < hash.length; i++) {
                hashCode ^= hash[i];
            }
            hashCode ^= loginModuleId.hashCode();
        }
        return hashCode;
    }

    private static final char[] HEXCHAR = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
}
