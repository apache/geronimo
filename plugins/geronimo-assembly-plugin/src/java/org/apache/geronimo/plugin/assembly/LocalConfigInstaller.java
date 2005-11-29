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

package org.apache.geronimo.plugin.assembly;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.configuration.LocalConfigStore;
import org.apache.geronimo.system.repository.FileSystemRepository;

/**
 * JellyBean that installs configuration artifacts into a LocalConfigurationStore,  It also copies all
 * configuration dependencies into the repository
 *
 * @version $Rev: 156292 $ $Date: 2005-03-05 18:48:02 -0800 (Sat, 05 Mar 2005) $
 */
public class LocalConfigInstaller extends BaseConfigInstaller {

    public void execute() throws Exception {
        final LocalConfigStore store = new LocalConfigStore(new File(targetRoot, targetConfigStore));
        store.doStart();
        InstallAdapter installAdapter = new InstallAdapter() {

            public List install(Repository sourceRepo, String artifactPath) throws IOException, InvalidConfigException {
                URL artifact = sourceRepo.getURL(URI.create(artifactPath));
                GBeanData config = store.install2(artifact);
                List dependencies = (List) config.getAttribute("dependencies");
                return dependencies;
            }
        };
        Repository sourceRepo = new InnerRepository();
        URI rootURI = targetRoot.toURI().resolve(targetRepository);
        FileSystemRepository targetRepo = new FileSystemRepository(rootURI, null);
        targetRepo.doStart();

        try {
            try {
                execute(installAdapter, sourceRepo, targetRepo);
            } finally {
                store.doStop();
            }
        } finally {
            targetRepo.doStop();
        }

    }

}
