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
package org.apache.geronimo.security.deploy;

import javax.management.ObjectName;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * @version $Rev: $ $Date: $
 */
public class AutoMapAssistant implements Serializable {

    private String securityRealm;
    private Set classOverrides = new HashSet();

    public String getSecurityRealm() {
        return securityRealm;
    }

    public void setSecurityRealm(String securityRealm) {
        this.securityRealm = securityRealm;
    }

    public Set getClassOverrides() {
        return classOverrides;
    }
}
