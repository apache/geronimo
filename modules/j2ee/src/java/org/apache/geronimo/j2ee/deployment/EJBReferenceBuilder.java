package org.apache.geronimo.j2ee.deployment;

import org.apache.geronimo.deployment.DeploymentException;

import javax.naming.Reference;

/**
 */
public interface EJBReferenceBuilder {

    Reference createEJBLocalReference(String objectName, boolean isSession, String localHome, String local) throws DeploymentException;

    Reference createEJBRemoteReference(String objectName, boolean isSession, String home, String remote) throws DeploymentException;

}
