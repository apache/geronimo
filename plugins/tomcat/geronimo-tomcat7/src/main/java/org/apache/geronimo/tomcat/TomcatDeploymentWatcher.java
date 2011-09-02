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

package org.apache.geronimo.tomcat;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.kernel.config.DeploymentWatcher;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.FileUtils;

/**
 * @version $Rev$ $Date$
 */
@GBean
@OsgiService
public class TomcatDeploymentWatcher implements DeploymentWatcher {

    private Map<AbstractName, File> abstractNameTempDirectoryMap = new ConcurrentHashMap<AbstractName, File>();

    @Override
    public void deployed(Artifact artifact) {
    }

    @Override
    public void undeployed(Artifact artifact) {
        for (Iterator<Map.Entry<AbstractName, File>> it = abstractNameTempDirectoryMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<AbstractName, File> entry = it.next();
            if (entry.getKey().getArtifact().equals(artifact)) {
                FileUtils.recursiveDelete(entry.getValue());
                it.remove();
            }
        }
    }

    public void deleteOnUndeployed(AbstractName abName, File tempDirectory) {
        abstractNameTempDirectoryMap.put(abName, tempDirectory);
    }
}
