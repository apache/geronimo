/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @version $Rev$ $Date$
 */
public class Realm implements Serializable {

    private String realmName;
    private Map domains = new HashMap();

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public Map getLoginDomains() {
        return domains;
    }

    public void merge(Realm other) {
        for (Iterator iter = other.domains.keySet().iterator(); iter.hasNext();) {
            LoginDomain domain = (LoginDomain) domains.get(iter.next());
            if (domain != null) {
                domain.merge((LoginDomain) other.domains.get(domain.getDomainName()));
            }
        }
    }
}
