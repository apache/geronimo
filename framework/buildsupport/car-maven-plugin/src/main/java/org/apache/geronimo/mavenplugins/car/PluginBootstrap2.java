/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.mavenplugins.car;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.jar.JarOutputStream;

import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.service.GBeanBuilder;
import org.apache.geronimo.deployment.xbeans.ModuleDocument;
import org.apache.geronimo.deployment.xbeans.ModuleType;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.NullConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.configuration.ExecutableConfigurationUtil;
import org.apache.geronimo.system.configuration.DependencyManager;
import org.apache.geronimo.system.repository.Maven2Repository;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public class PluginBootstrap2 {
    private File localRepo;
    private File plan;
    private File buildDir;
    private File carFile;
    private boolean expanded;
    private BundleContext bundleContext;

    public void setLocalRepo(File localRepo) {
        this.localRepo = localRepo;
    }

    public void setPlan(File plan) {
        this.plan = plan;
    }

    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
    }

    public void setCarFile(File carFile) {
        this.carFile = carFile;
    }

    public void setExpanded(final boolean expanded) {
        this.expanded = expanded;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void bootstrap() throws Exception {
        System.out.println("Packaging module configuration: " + plan);

//        ModuleType config = ModuleDocument.Factory.parse(plan).getModule();

        Maven2Repository repository = new Maven2Repository(localRepo);
        new DependencyManager(bundleContext, Collections.<Repository>singleton(repository));

        GBeanBuilder gBeanBuilder = new GBeanBuilder(null, null);
        ServiceConfigBuilder builder = new ServiceConfigBuilder(null, Collections.<Repository>singleton(repository), Collections.<NamespaceDrivenBuilder>singleton(gBeanBuilder), new Jsr77Naming(), bundleContext);
        ConfigurationStore targetConfigurationStore = new NullConfigurationStore() {
            public File createNewConfigurationDir(Artifact configId) throws ConfigurationAlreadyExistsException {
                return buildDir;
            }
        };

        ArtifactManager artifactManager = new DefaultArtifactManager();
        ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, repository);

        Object config = builder.getDeploymentPlan(plan, null, new ModuleIDBuilder());

        DeploymentContext context = builder.buildConfiguration(
                false,
                builder.getConfigurationID(config, null, new ModuleIDBuilder()),
                config,
                null,
                Collections.singleton(targetConfigurationStore),
                artifactResolver,
                targetConfigurationStore);

        ConfigurationData configurationData = context.getConfigurationData();

        try {
            writeConfiguration(configurationData);
        }
        finally {
            context.close();
        }
    }

    private void writeConfiguration(final ConfigurationData configurationData) throws IOException {
        if (expanded) {
            ExecutableConfigurationUtil.writeConfiguration(configurationData, carFile);
        } else {
            JarOutputStream out = null;
            try {
                out = new JarOutputStream(new FileOutputStream(carFile));
                ExecutableConfigurationUtil.writeConfiguration(configurationData, out);
                out.flush();
            }
            finally {
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (IOException ignored) {
                        // ignored
                    }
                }
            }
        }
    }
}
