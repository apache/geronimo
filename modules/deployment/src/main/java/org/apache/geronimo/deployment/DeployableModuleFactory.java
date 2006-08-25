package org.apache.geronimo.deployment;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

/**
 * @version
 */
public class DeployableModuleFactory {

    public static DeployableModule createDeployableModule(JarFile moduleFile) {
       return new DefaultDeployableModule(null, moduleFile);
    }

    public static DeployableModule createDeployableModule(File moduleFile, String deployableModuleImplClass) throws DeploymentException {
        if (deployableModuleImplClass == null) {
            try {
                JarFile jar = DeploymentUtil.createJarFile(moduleFile);
                return new DefaultDeployableModule(moduleFile, jar);
            } catch (IOException e) {
                throw new DeploymentException("Cound not open module file: " + moduleFile.getAbsolutePath(), e);
            }
        } else {
            //TODO 
        }
        return null;
    }
}