package org.apache.geronimo.deployment.mavenplugin;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.DeploymentStatus;

public class DeploymentClient {
    private static final DeploymentFactoryManager FACTORY_MANAGER = DeploymentFactoryManager.getInstance();

    private URL provider;

    public URL getProvider() {
        return provider;
    }

    public void setProvider(URL provider) {
        this.provider = provider;
    }

    public void doIt() throws IOException {
        registerProvider(provider, null);
    }

    public static void registerProvider(URL provider, ClassLoader parent) throws IOException {
        if (parent == null) {
            parent = Thread.currentThread().getContextClassLoader();
        }
        if (parent == null) {
            parent = DeploymentClient.class.getClassLoader();
        }

        // read manifest entry from provider
        URL url = new URL("jar:" + provider.toString() + "!/");
        JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
        Attributes attrs = jarConnection.getMainAttributes();
        String factoryNames = attrs.getValue("J2EE-DeploymentFactory-Implementation-Class");
        if (factoryNames == null) {
            throw new IOException("No DeploymentFactory found in jar");
        }

        // register listed DeploymentFactories
        ClassLoader cl = new URLClassLoader(new URL[]{provider}, parent);
        for (StringTokenizer tokenizer = new StringTokenizer(factoryNames); tokenizer.hasMoreTokens();) {
            String className = tokenizer.nextToken();
            try {
                DeploymentFactory factory = (DeploymentFactory) cl.loadClass(className).newInstance();
                FACTORY_MANAGER.registerDeploymentFactory(factory);
            } catch (Exception e) {
                throw (IOException) new IOException("Unable to instantiate DeploymentFactory: " + className).initCause(e);
            }
        }
    }

    public static void waitFor(final ProgressObject progress) throws InterruptedException {
        ProgressListener listener = new ProgressListener() {
            public void handleProgressEvent(ProgressEvent event) {
                DeploymentStatus status = event.getDeploymentStatus();
                if (status.getMessage() != null) {
                    System.out.println(status.getMessage());
                }
                if (!status.isRunning()) {
                    synchronized (progress) {
                        progress.notify();
                    }
                }
            }
        };
        progress.addProgressListener(listener);
        synchronized (progress) {
            while (progress.getDeploymentStatus().isRunning()) {
                progress.wait();
            }
        }
    }
}
