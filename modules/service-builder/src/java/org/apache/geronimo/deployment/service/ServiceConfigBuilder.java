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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.xbeans.AttributeType;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xbeans.ReferenceType;
import org.apache.geronimo.deployment.xbeans.ReferencesType;
import org.apache.geronimo.deployment.xbeans.ServiceDocument;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.xmlbeans.XmlException;

/**
 * @version $Rev$ $Date$
 */
public class ServiceConfigBuilder implements ConfigurationBuilder {
    private final URI defaultParentId;
    private final Repository repository;
    private final Kernel kernel;

    public ServiceConfigBuilder(URI defaultParentId, Repository repository) {
        this(defaultParentId, repository, null);
    }

    public ServiceConfigBuilder(URI defaultParentId, Repository repository, Kernel kernel) {
        this.defaultParentId = defaultParentId;
        this.repository = repository;
        this.kernel = kernel;
    }

    public Object getDeploymentPlan(File planFile, JarFile module) throws DeploymentException {
        if (planFile == null) {
            return null;
        }

        // todo tell the difference between an invalid plan and one that's not for me
        try {
            ConfigurationDocument configurationDoc = ConfigurationDocument.Factory.parse(planFile);
            return configurationDoc.getConfiguration();
        } catch (XmlException e) {
            return null;
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    public List buildConfiguration(Object plan, JarFile unused, File outfile) throws IOException, DeploymentException {
        ConfigurationType configType = (ConfigurationType) plan;
        String domain = null;
        String server = null;

        buildConfiguration(configType, domain, server, outfile);

        return Collections.EMPTY_LIST;
    }

    public void buildConfiguration(ConfigurationType configType, String domain, String server, File outfile) throws DeploymentException, IOException {
        URI parentID = null;
        if (configType.isSetParentId()) {
            try {
                parentID = new URI(configType.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + configType.getParentId(), e);
            }
        } else {
            if (configType.isSetDomain()) {
                if (!configType.isSetServer()) {
                    throw new DeploymentException("You must set both domain and server");
                }
                domain = configType.getDomain();
                server = configType.getServer();
            } else {
                parentID = defaultParentId;
            }
        }

        if (domain == null) {
            //get from parent id
            if (kernel == null) {
                throw new DeploymentException("You must supply a kernel or the domain and server names");
            }
        }

        URI configID;
        try {
            configID = new URI(configType.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + configType.getConfigId(), e);
        }

        DeploymentContext context = null;
        try {
            context = new DeploymentContext(outfile, configID, ConfigurationModuleType.SERVICE, parentID, domain, server, kernel);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException(e);
        }

        J2eeContext j2eeContext = new J2eeContextImpl(context.getDomain(), context.getServer(), NameFactory.NULL, NameFactory.J2EE_MODULE, configID.toString(), null, null);
        DependencyType[] includes = configType.getIncludeArray();
        addIncludes(context, includes, repository);
        addDependencies(context, configType.getDependencyArray(), repository);
        ClassLoader cl = context.getClassLoader(repository);
        GbeanType[] gbeans = configType.getGbeanArray();
        addGBeans(gbeans, cl, j2eeContext, context);
        context.close();
    }

    public static void addIncludes(DeploymentContext context, DependencyType[] includes, Repository repository) throws DeploymentException {
        for (int i = 0; i < includes.length; i++) {
            DependencyType include = includes[i];
            URI uri = getDependencyURI(include, repository);
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

    public static void addDependencies(DeploymentContext context, DependencyType[] deps, Repository repository) throws DeploymentException {
        for (int i = 0; i < deps.length; i++) {
            URI dependencyURI = getDependencyURI(deps[i], repository);
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
                    addDependencies(context, dependencyDeps, repository);
                }
            }
        }
    }

    public static void addGBeans(GbeanType[] gbeans, ClassLoader cl, J2eeContext j2eeContext, DeploymentContext context) throws DeploymentException {
        Set result = new HashSet();
        for (int i = 0; i < gbeans.length; i++) {
            GBeanData gBeanData = getGBeanData(gbeans[i], j2eeContext, cl);
            context.addGBean(gBeanData);
            result.add(gBeanData);
        }
    }

    public static GBeanData getGBeanData(GbeanType gbean, J2eeContext j2eeContext, ClassLoader cl) throws DeploymentException {
        GBeanInfo gBeanInfo = GBeanInfo.getGBeanInfo(gbean.getClass1(), cl);
        ObjectName objectName;
        if (gbean.isSetName()) {
            try {
                objectName = ObjectName.getInstance(gbean.getName());
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Invalid ObjectName: " + gbean.getName(), e);
            }
        } else {
            String namePart = gbean.getNamePart();
            try {
                String j2eeType = gBeanInfo.getJ2eeType();
                //todo investigate using the module type from the j2eecontext.
                objectName = NameFactory.getComponentName(null, null, null, null, namePart, j2eeType, j2eeContext);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Invalid ObjectName: " + namePart, e);
            }
        }
        GBeanBuilder builder = new GBeanBuilder(objectName, gBeanInfo, cl);

        // set up attributes
        AttributeType[] attributeArray = gbean.getAttributeArray();
        if (attributeArray != null) {
            for (int j = 0; j < attributeArray.length; j++) {
                builder.setAttribute(attributeArray[j].getName().trim(), attributeArray[j].getType(), attributeArray[j].getStringValue());
            }
        }

        // set up all single pattern references
        ReferenceType[] referenceArray = gbean.getReferenceArray();
        if (referenceArray != null) {
            for (int j = 0; j < referenceArray.length; j++) {
                builder.setReference(referenceArray[j].getName(), referenceArray[j].getStringValue());
            }
        }

        // set up app multi-patterned references
        ReferencesType[] referencesArray = gbean.getReferencesArray();
        if (referencesArray != null) {
            for (int j = 0; j < referencesArray.length; j++) {
                builder.setReference(referencesArray[j].getName(), referencesArray[j].getPatternArray());
            }
        }

        GBeanData gBeanData = builder.getGBeanData();
        return gBeanData;
    }

    private static URI getDependencyURI(DependencyType dep, Repository repository) throws DeploymentException {
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
        if (!repository.hasURI(uri)) {
            throw new DeploymentException(new MissingDependencyException("uri " + uri + " not found in repository"));
        }
        return uri;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ServiceConfigBuilder.class, NameFactory.CONFIG_BUILDER);

        infoFactory.addInterface(ConfigurationBuilder.class);

        infoFactory.addAttribute("defaultParentId", URI.class, true);
        infoFactory.addReference("Repository", Repository.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.setConstructor(new String[]{"defaultParentId", "Repository", "kernel"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
