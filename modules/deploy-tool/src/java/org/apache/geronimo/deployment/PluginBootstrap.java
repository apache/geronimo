/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.deployment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.jar.JarOutputStream;

import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.system.configuration.ExecutableConfigurationUtil;
import org.apache.geronimo.system.repository.Maven1Repository;

/**
 * @version $Rev$ $Date$
 */
public class PluginBootstrap {
    private File localRepo;
    private File plan;
    private File buildDir;
    private File carFile;

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

    public void bootstrap() throws Exception {
        ConfigurationType config = ConfigurationDocument.Factory.parse(plan).getConfiguration();

        Maven1Repository repository = new Maven1Repository(localRepo);
        ServiceConfigBuilder builder = new ServiceConfigBuilder(null, repository);
        ConfigurationData configurationData = builder.buildConfiguration(config, null, buildDir);

        JarOutputStream out = new JarOutputStream(new FileOutputStream(carFile));
        ExecutableConfigurationUtil.writeConfiguration(configurationData, out);
        out.flush();
        out.close();
    }
}
