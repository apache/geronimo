/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.security.jaas;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;


/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:09 $
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
