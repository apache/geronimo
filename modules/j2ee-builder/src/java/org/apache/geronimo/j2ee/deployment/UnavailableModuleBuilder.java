package org.apache.geronimo.j2ee.deployment;

import org.apache.geronimo.common.DeploymentException;

import java.io.File;
import java.util.jar.JarFile;
import java.net.URL;
import java.net.URI;

/**
 */
public class UnavailableModuleBuilder implements ModuleBuilder {
    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        throw new DeploymentException("Module type unavailable");
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId, Object moduleContextInfo) throws DeploymentException {
        throw new DeploymentException("Module type unavailable");
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module) throws DeploymentException {
        throw new DeploymentException("Module type unavailable");
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        throw new DeploymentException("Module type unavailable");
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        throw new DeploymentException("Module type unavailable");
    }

    public String getSchemaNamespace() {
        return null;
    }
}
