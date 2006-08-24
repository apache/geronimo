package org.apache.geronimo.deployment;

import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.util.NestedJarFile;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.net.URL;

/**
 * @version
 */
public class DefaultDeployableModule implements DeployableModule {

    private String uri;
    private File root;
    private JarFile jarFile;

    public DefaultDeployableModule(File root, JarFile jarFile) {
        this.uri = uri;
        this.root = root;
        this.jarFile = jarFile;
    }

    public String getURI() {
        return uri;
    }

    public File getRoot() {
        return root;
    }

    public File[] getModuleContextResources() {
        return null;
    }

    public File[] getClassesFolders() {
        return null;
    }

    public boolean isArchived() {
        return root.isFile();
    }

    public DeployableModule[] getModules() {
        return null;
    }

    public URL resolve(String path) throws IOException {
        return DeploymentUtil.createJarURL(jarFile, path);
    }

    public DeployableModule resolveModule(String uri) throws IOException {
        JarFile nestedJar = new NestedJarFile(jarFile, uri);
        return DeployableModuleFactory.createDeployableModule(nestedJar);
    }

    public JarFile getJarFile() {
        return jarFile;
    }

    public void cleanup() {
        DeploymentUtil.close(jarFile);
    }

}
