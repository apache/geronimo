package org.apache.geronimo.security.jaas;

import org.apache.geronimo.common.GeronimoSecurityException;

import javax.security.auth.spi.LoginModule;
import java.io.Serializable;
import java.util.Map;

/**
 * Describes the configuration of a LoginModule -- its name, class, control
 * flag, options, and the Geronimo extension for whether it should run on
 * the client side or server side.
 *
 * @version $Revision: 1.0$
 */
public class JaasLoginModuleConfiguration implements Serializable {
    private boolean serverSide;
    private LoginModuleControlFlag flag;
    private String loginModuleName;
    private Map options;
    private transient LoginModule loginModule;

    public JaasLoginModuleConfiguration(String loginModuleName, LoginModuleControlFlag flag, Map options, boolean serverSide) {
        this.serverSide = serverSide;
        this.flag = flag;
        this.loginModuleName = loginModuleName;
        this.options = options;
    }

    public LoginModule getLoginModule(ClassLoader loader) throws GeronimoSecurityException {
        if(loginModule == null) {
            try {
                loginModule = (LoginModule) loader.loadClass(loginModuleName).newInstance();
            } catch (Exception e) {
                throw new GeronimoSecurityException("Unable to instantiate login module", e);
            }
        }
        return loginModule;
    }

    public boolean isServerSide() {
        return serverSide;
    }

    public LoginModuleControlFlag getFlag() {
        return flag;
    }

    public Map getOptions() {
        return options;
    }
}
