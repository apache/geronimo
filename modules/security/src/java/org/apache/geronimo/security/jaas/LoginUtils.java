package org.apache.geronimo.security.jaas;

import javax.security.auth.login.LoginException;

/**
 * 
 * 
 * @version $Revision 1.0 $
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
