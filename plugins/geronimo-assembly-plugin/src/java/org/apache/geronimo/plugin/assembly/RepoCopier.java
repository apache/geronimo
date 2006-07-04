/**
 *
 * Copyright 2006 The Apache Software Foundation
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

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.system.repository.Maven1Repository;
import org.apache.geronimo.system.repository.Maven2Repository;

import java.io.File;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class RepoCopier {

    private File sourceRepositoryFile;
    private File targetRepositoryFile;
    private String groupId;
    private String artifactId;
    private String version;
    private String type;

    public File getSourceRepositoryFile() {
        return sourceRepositoryFile;
    }

    public void setSourceRepositoryFile(File sourceRepositoryFile) {
        this.sourceRepositoryFile = sourceRepositoryFile;
    }

    public File getTargetRepositoryFile() {
        return targetRepositoryFile;
    }

    public void setTargetRepositoryFile(File targetRepositoryFile) {
        this.targetRepositoryFile = targetRepositoryFile;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void execute() throws MissingDependencyException, IOException {
        Artifact configId = new Artifact(groupId, artifactId, version, type);
        Repository sourceRepository = new Maven1Repository(sourceRepositoryFile);
        WriteableRepository targetRepository = new Maven2Repository(targetRepositoryFile);
        if (!sourceRepository.contains(configId)) {
            throw new MissingDependencyException("source repository at " + sourceRepositoryFile + " does not contain artifact " + configId);
        }
        if (!targetRepository.contains(configId)) {
            File sourceFile = sourceRepository.getLocation(configId);
            targetRepository.copyToRepository(sourceFile, configId, BaseConfigInstaller.LOG_COPY_START);
        }
    }
}
