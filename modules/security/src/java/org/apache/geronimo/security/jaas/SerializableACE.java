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
 * @version $Rev$ $Date$
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
}
