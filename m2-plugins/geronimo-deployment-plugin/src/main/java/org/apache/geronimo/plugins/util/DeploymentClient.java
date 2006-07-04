/**
 *
 * Copyright 2004-2006 The Apache Software Foundation
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

package org.apache.geronimo.plugins.util;

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

/**
 * @version $Rev:$ $Date:$
 */
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
