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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;


/**
 * @version $Revision: 1.1 $ $Date: 2004/02/17 00:05:39 $
 */
public class SerializableACE implements Serializable {

    private String loginModuleName;
    private LoginModuleControlFlag controlFlag;
    private Map options;

    SerializableACE(String loginModuleName, LoginModuleControlFlag controlFlag, Map options) {
        this.loginModuleName = loginModuleName;
        this.controlFlag = controlFlag;
        this.options = options;
    }

    String getLoginModuleName() {
        return loginModuleName;
    }

    LoginModuleControlFlag getControlFlag() {
        return controlFlag;
    }

    Map getOptions() {
        return options;
    }

    public static final class LoginModuleControlFlag implements Serializable {

        // Be careful here.  If you change the ordinals, this class must be changed on evey client.
        private static int MAX_ORDINAL = 4;
        private static final LoginModuleControlFlag[] values = new LoginModuleControlFlag[MAX_ORDINAL + 1];
        public static final LoginModuleControlFlag REQUIRED = new LoginModuleControlFlag("REQUIRED", 0);
        public static final LoginModuleControlFlag REQUISITE = new LoginModuleControlFlag("REQUISITE", 1);
        public static final LoginModuleControlFlag SUFFICIENT = new LoginModuleControlFlag("SUFFICIENT", 2);
        public static final LoginModuleControlFlag OPTIONAL = new LoginModuleControlFlag("OPTIONAL", 3);

        private final transient String name;
        private final int ordinal;

        private LoginModuleControlFlag(String name, int ordinal) {
            assert ordinal <= MAX_ORDINAL;
            assert values[ordinal] == null;
            this.name = name;
            this.ordinal = ordinal;
            values[ordinal] = this;
        }

        public String toString() {
            return name;
        }

        Object readResolve() throws ObjectStreamException {
            return values[ordinal];
        }
    }
}
