/**
*
* Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.tomcat;

import org.apache.catalina.Engine;
import org.apache.catalina.Realm;
import org.apache.catalina.core.StandardEngine;
import org.apache.geronimo.tomcat.realm.TomcatJAASRealm;

public class TomcatEngine extends StandardEngine implements Engine{

    /**
     * 
     */
    private static final long serialVersionUID = 3834312825844611385L;

    public Realm getRealm() {
        if (realm != null)
            return realm;
        
        if (parent != null){
            Realm configured = parent.getRealm();
            if (configured != null)
                return configured;
        }
        
        //No realms found up the chain, so lets create a default JAAS Realm
        TomcatJAASRealm defaultRealm = new TomcatJAASRealm();
        defaultRealm.setUserClassNames("org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
        defaultRealm.setRoleClassNames("org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
        this.setRealm(defaultRealm);
        return defaultRealm;
    }
}
