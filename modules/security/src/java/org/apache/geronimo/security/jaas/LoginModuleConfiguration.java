package org.apache.geronimo.security.jaas;

import javax.security.auth.spi.LoginModule;

/**
 * 
 * 
 * @version $Revision 1.0 $
 */
public class LoginModuleConfiguration {
    private LoginModule module;
    private LoginModuleControlFlag controlFlag;

    public LoginModuleConfiguration(LoginModule module, LoginModuleControlFlag controlFlag) {
        this.module = module;
        this.controlFlag = controlFlag;
    }

    public LoginModule getModule() {
        return module;
    }

    public LoginModuleControlFlag getControlFlag() {
        return controlFlag;
    }
}
