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
import javax.security.auth.login.AppConfigurationEntry;


/**
 * A wrapper for the JAAS login module control flag that is Serializable.
 *
 * @version $Rev$ $Date$
 */
public class LoginModuleControlFlag implements Serializable {

    private static final LoginModuleControlFlag[] values = new LoginModuleControlFlag[4];

    public static final LoginModuleControlFlag REQUIRED = new LoginModuleControlFlag(0, AppConfigurationEntry.LoginModuleControlFlag.REQUIRED);
    public static final LoginModuleControlFlag REQUISITE = new LoginModuleControlFlag(1, AppConfigurationEntry.LoginModuleControlFlag.REQUISITE);
    public static final LoginModuleControlFlag SUFFICIENT = new LoginModuleControlFlag(2, AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT);
    public static final LoginModuleControlFlag OPTIONAL = new LoginModuleControlFlag(3, AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL);

    private final int ordinal;
    private final transient AppConfigurationEntry.LoginModuleControlFlag flag;

    private LoginModuleControlFlag(int ordinal, AppConfigurationEntry.LoginModuleControlFlag flag) {
        this.ordinal = ordinal;
        this.flag = flag;
        values[ordinal] = this;
    }

    public AppConfigurationEntry.LoginModuleControlFlag getFlag() {
        return flag;
    }

    Object readResolve() throws ObjectStreamException {
        return values[ordinal];
    }

    public static LoginModuleControlFlag getInstance(AppConfigurationEntry.LoginModuleControlFlag flag) {
        for(int i = 0; i < values.length; i++) {
            if(values[i].flag == flag) {
                return values[i];
            }
        }
        return null;
    }
}
