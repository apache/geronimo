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
package org.apache.geronimo.security.jaas;

import java.io.Externalizable;
import java.io.Serializable;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;

import org.apache.geronimo.security.jaas.server.JaasLoginModuleConfiguration;


/**
 * Helper class the computes the login result across a number of separate
 * login modules.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class LoginUtils {
    public static void copyPrincipals(Subject to, Subject from) {
        to.getPrincipals().addAll(from.getPrincipals());
    }

    public static Map getSerializableCopy(Map from) {
        Map to = new HashMap();
        for (Iterator it = from.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            Object value = from.get(key);
            if (value instanceof Serializable || value instanceof Externalizable || value instanceof Remote) {
                to.put(key, value);
            }
        }
        return to;
    }

    public static Set getSerializableCopy(Set from) {
        Set to = new HashSet();
        for (Iterator it = from.iterator(); it.hasNext();) {
            Object value = it.next();
            if (value instanceof Serializable || value instanceof Externalizable || value instanceof Remote) {
                to.add(value);
            }
        }
        return to;
    }

    /**
     * Strips out stuff that isn't serializable so this can be safely passed to
     * a remote server.
     */
    public static JaasLoginModuleConfiguration getSerializableCopy(JaasLoginModuleConfiguration config) {
        return new JaasLoginModuleConfiguration(config.getLoginModuleClassName(),
                                                config.getFlag(),
                                                LoginUtils.getSerializableCopy(config.getOptions()),
                                                config.isServerSide(),
                                                config.getLoginDomainName(),
                                                config.isWrapPrincipals());
    }
}
