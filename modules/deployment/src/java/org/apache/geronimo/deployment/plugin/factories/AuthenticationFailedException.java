package org.apache.geronimo.deployment.plugin.factories;

import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;

/**
 * A Geronimo-specific DeploymentManagerCreationException indicating that
 * there was a login failure.
 * 
 * @version $Revision 1.0 $
 */
public class AuthenticationFailedException extends DeploymentManagerCreationException {
    public AuthenticationFailedException(String s) {
        super(s);
    }
}
