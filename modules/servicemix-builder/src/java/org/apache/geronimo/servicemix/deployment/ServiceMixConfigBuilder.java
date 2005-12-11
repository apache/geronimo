/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.servicemix.deployment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.servicemix.ServiceMixDeployment;

/**
 * @version $Rev$ $Date$
 */
public class ServiceMixConfigBuilder implements ConfigurationBuilder {
    private static final Log log = LogFactory.getLog(ServiceMixConfigBuilder.class);

    private final List defaultParentId;
    private final Repository repository;
    private final Kernel kernel;
    private String deploymentDependencies;

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ServiceMixConfigBuilder.class, NameFactory.CONFIG_BUILDER);
        infoFactory.addInterface(ConfigurationBuilder.class);
        infoFactory.addAttribute("defaultParentId", List.class, true);
        infoFactory.addAttribute("deploymentDependencies", String.class, true);
        infoFactory.addReference("Repository", Repository.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.setConstructor(new String[]{"defaultParentId", "Repository", "kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public ServiceMixConfigBuilder(List defaultParentId, Repository repository) {
        this(defaultParentId, repository, null);
    }

    public ServiceMixConfigBuilder(List defaultParentId, Repository repository, Kernel kernel) {
        this.defaultParentId = defaultParentId;
        this.repository = repository;
        this.kernel = kernel;
    }

    public Object getDeploymentPlan(File planFile, JarFile module) throws DeploymentException {
        log.debug("Checking for ServiceMix deployment.");
        if (module == null) {
            return null;
        }
        try {
            DeploymentUtil.readAll(DeploymentUtil.createJarURL(module, "META-INF/jbi.xml"));
        } catch (Exception e) {
            log.debug("Not a ServiceMix deployment: no jbi.xml found.");
            //no jbi.xml, not for us
            return null;
        }

        try {
            Properties properties = new Properties();
            InputStream is = DeploymentUtil.createJarURL(module, "META-INF/jbi-geronimo.properties").openStream();
            try {
                properties.load(is);
            } finally {
                is.close();
            }
            return properties;
        } catch (Exception e) {
            throw new DeploymentException("Could not load META-INF/jbi-geronimo.properties: " + e, e);
        }
    }

    public URI getConfigurationID(Object plan, JarFile module) throws IOException, DeploymentException {
        Properties properties = (Properties) plan;
        try {
            return new URI(properties.getProperty("configID"));
        } catch (URISyntaxException e1) {
            throw new DeploymentException("Invalid configuration URI", e1);
        }
    }

    public ConfigurationData buildConfiguration(Object plan, JarFile module, File outfile) throws IOException, DeploymentException {
        log.debug("Installing ServiceMix deployment.");
        Properties properties = (Properties) plan;

        List parentID = new ArrayList(defaultParentId);
        URI configID;
        try {
            configID = new URI(properties.getProperty("configID"));
        } catch (URISyntaxException e1) {
            throw new DeploymentException("Invalid configuration URI", e1);
        }

        DeploymentContext context = null;
        context = new DeploymentContext(outfile, configID, ConfigurationModuleType.SERVICE, parentID, null, null, kernel);

        // Copy over all files.
        for (Enumeration e = module.entries(); e.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            context.addFile(URI.create(entry.getName()), module, entry);
        }

        // Create the JBI deployment managed object
        try {
            Properties props = new Properties();
            props.put("jbiType", "Deployment");
            props.put("name", configID.toString());
            ObjectName name = ObjectName.getInstance(context.getDomain(), props);
            GBeanData gbeanData = new GBeanData(name, ServiceMixDeployment.GBEAN_INFO);

            context.addGBean(gbeanData);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Invalid gbean name: " + e, e);
        }

        if (deploymentDependencies != null) {
            String[] strings = deploymentDependencies.split("\\,");
            for (int i = 0; i < strings.length; i++) {
                strings[i] = strings[i].trim();
                if (strings[i].length() > 0) {
                    try {
                        context.addDependency(new URI(strings[i]));
                    } catch (URISyntaxException e) {
                        throw new DeploymentException("Invalid dependency URI: " + strings[i], e);
                    }
                }
            }
        }

        context.close();
        ConfigurationData configurationData = context.getConfigurationData();
        try {
            configurationData.addClassPathLocation(new URI("."));
        } catch (URISyntaxException e) {
            throw new DeploymentException("Could not set classpath: " + e, e);
        }
        return configurationData;
    }

    public String getDeploymentDependencies() {
        return deploymentDependencies;
    }

    public void setDeploymentDependencies(String deploymentDependencies) {
        this.deploymentDependencies = deploymentDependencies;
    }

}
