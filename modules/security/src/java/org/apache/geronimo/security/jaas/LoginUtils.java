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

import javax.security.auth.login.LoginException;

/**
 * Helper class the computes the login result across a number of separate
 * login modules.
 * 
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class LoginUtils {
    public static boolean computeLogin(LoginModuleConfiguration[] modules) throws LoginException {
        Boolean success = null;
        Boolean backup = null;
        // see http://java.sun.com/j2se/1.4.2/docs/api/javax/security/auth/login/Configuration.html
        for(int i = 0; i < modules.length; i++) {
            LoginModuleConfiguration module = modules[i];
            boolean result = module.getModule().login();
            if(module.getControlFlag() == LoginModuleControlFlag.REQUIRED) {
                if(success == null || success.booleanValue()) {
                    success = result ? Boolean.TRUE : Boolean.FALSE;
                }
            } else if(module.getControlFlag() == LoginModuleControlFlag.REQUISITE) {
                if(!result) {
                    return false;
                } else if(success == null) {
                   success = Boolean.TRUE;
                }
            } else if(module.getControlFlag() == LoginModuleControlFlag.SUFFICIENT) {
                if(result && (success == null || success.booleanValue())) {
                    return true;
                }
            } else if(module.getControlFlag() == LoginModuleControlFlag.OPTIONAL) {
                if(backup == null || backup.booleanValue()) {
                    backup = result ? Boolean.TRUE : Boolean.FALSE;
                }
            }
        }
        // all required and requisite modules succeeded, or at least one required module failed
        if(success != null) {
            return success.booleanValue();
        }
        // no required or requisite modules, no sufficient modules succeeded, fall back to optional modules
        if(backup != null) {
            return backup.booleanValue();
        }
        // perhaps only a sufficient module, and it failed
        return false;
    }
}
