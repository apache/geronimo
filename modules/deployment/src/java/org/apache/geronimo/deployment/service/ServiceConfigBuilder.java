/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xbeans.ServiceDocument;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class ServiceConfigBuilder implements ConfigurationBuilder {
    private final Repository repository;
    private final Kernel kernel;

    public ServiceConfigBuilder(Repository repository) {
        this(repository, null);
    }

    public ServiceConfigBuilder(Repository repository, Kernel kernel) {
        this.repository = repository;
        this.kernel = kernel;
    }

    public SchemaTypeLoader[] getTypeLoaders() {
        return new SchemaTypeLoader[]{XmlBeans.getContextTypeLoader()};
    }

    public boolean canConfigure(XmlObject plan) {
        return plan instanceof ConfigurationDocument;
    }

    public XmlObject getDeploymentPlan(URL module) {
        return null;
    }

    public void buildConfiguration(File outfile, Manifest manifest, File module, XmlObject plan) throws IOException, DeploymentException {
        buildConfiguration(outfile, manifest, (InputStream) null, plan);
    }

    public void buildConfiguration(File outfile, Manifest manifest, InputStream ignored, XmlObject plan) throws IOException, DeploymentException {
        FileOutputStream fos = new FileOutputStream(outfile);
        try {
            JarOutputStream os = new JarOutputStream(new BufferedOutputStream(fos), manifest);

            // if this is an executable jar add the startup jar finder file
            if (manifest.getMainAttributes().containsKey(Attributes.Name.MAIN_CLASS)) {
                os.putNextEntry(new ZipEntry("META-INF/startup-jar"));
                os.closeEntry();
            }

            buildConfiguration(os, plan);

        } finally {
            fos.close();
        }

    }

    public void buildConfiguration(JarOutputStream os, XmlObject plan) throws DeploymentException, IOException {
        ConfigurationType configType = ((ConfigurationDocument) plan).getConfiguration();
        URI configID;
        try {
            configID = new URI(configType.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + configType.getConfigId(), e);
        }
        URI parentID;
        if (configType.isSetParentId()) {
            try {
                parentID = new URI(configType.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + configType.getParentId(), e);
            }
        } else {
            parentID = null;
        }

        DeploymentContext context = null;
        try {
            context = new DeploymentContext(os, configID, ConfigurationModuleType.SERVICE, parentID, kernel);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException(e);
        }
        addIncludes(context, configType);
        addDependencies(context, configType.getDependencyArray());
        ClassLoader cl = context.getClassLoader(repository);
        GbeanType[] gbeans = configType.getGbeanArray();
        for (int i = 0; i < gbeans.length; i++) {
            GBeanHelper.addGbean(new ServiceGBeanAdapter(gbeans[i]), cl, context);

        }
        context.close();
        os.flush();
    }

    private void addIncludes(DeploymentContext context, ConfigurationType configType) throws DeploymentException {
        DependencyType[] includes = configType.getIncludeArray();
        for (int i = 0; i < includes.length; i++) {
            DependencyType include = includes[i];
            URI uri = getDependencyURI(include);
            String name = uri.toString();
            int idx = name.lastIndexOf('/');
            if (idx != -1) {
                name = name.substring(idx + 1);
            }
            URI path;
            try {
                path = new URI(name);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Unable to generate path for include: " + uri, e);
            }
            try {
                URL url = repository.getURL(uri);
                context.addInclude(path, url);
            } catch (IOException e) {
                throw new DeploymentException("Unable to add include: " + uri, e);
            }
        }
    }

    private void addDependencies(DeploymentContext context, DependencyType[] deps) throws DeploymentException {
        for (int i = 0; i < deps.length; i++) {
            URI dependencyURI = getDependencyURI(deps[i]);
            context.addDependency(dependencyURI);

            URL url;
            try {
                url = repository.getURL(dependencyURI);
            } catch (MalformedURLException e) {
                throw new DeploymentException("Unable to get URL for dependency " + dependencyURI, e);
            }
            ClassLoader depCL = new URLClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
            InputStream is = depCL.getResourceAsStream("META-INF/geronimo-service.xml");
            if (is != null) {
                // it has a geronimo-service.xml file
                ServiceDocument serviceDoc = null;
                try {
                    serviceDoc = ServiceDocument.Factory.parse(is);
                } catch (org.apache.xmlbeans.XmlException e) {
                    throw new DeploymentException("Invalid geronimo-service.xml file in " + url, e);
                } catch (IOException e) {
                    throw new DeploymentException("Unable to parse geronimo-service.xml file in " + url, e);
                }
                DependencyType[] dependencyDeps = serviceDoc.getService().getDependencyArray();
                if (dependencyDeps != null) {
                    addDependencies(context, dependencyDeps);
                }
            }
        }
    }

    private URI getDependencyURI(DependencyType dep) throws DeploymentException {
        URI uri;
        if (dep.isSetUri()) {
            try {
                uri = new URI(dep.getUri());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid dependency URI " + dep.getUri(), e);
            }
        } else {
            // @todo support more than just jars
            String id = dep.getGroupId() + "/jars/" + dep.getArtifactId() + '-' + dep.getVersion() + ".jar";
            try {
                uri = new URI(id);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Unable to construct URI for groupId=" + dep.getGroupId() + ", artifactId=" + dep.getArtifactId() + ", version=" + dep.getVersion(), e);
            }
        }
        return uri;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ServiceConfigBuilder.class);

        infoFactory.addInterface(ConfigurationBuilder.class);

        infoFactory.addReference("Repository", Repository.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.setConstructor(new String[]{"Repository", "kernel"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
