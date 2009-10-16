/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.system.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.security.auth.login.FailedLoginException;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.Maven2Repository;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;

/**
 * @version $Rev$ $Date$
 */
public class LocalSourceRepository extends Maven2Repository implements SourceRepository {

    private final ArtifactResolver artifactResolver;

    public LocalSourceRepository(File base) {
        super(base);
        artifactResolver = new DefaultArtifactResolver(new DefaultArtifactManager(), this);
    }

    public PluginListType getPluginList() {
        try {
            FileInputStream in = new FileInputStream(new File(rootFile, "geronimo-plugins.xml"));
            try {
                return PluginXmlUtil.loadPluginList(in);
            } finally {
                in.close();
            }
        } catch (Exception e) {
            return null;
        }
    }

    public OpenResult open(Artifact artifact, FileWriteMonitor monitor) throws IOException, FailedLoginException {
        if (!artifact.isResolved()) {
            try {
                artifact = artifactResolver.resolveInClassLoader(artifact);
            } catch (MissingDependencyException e) {
                throw (IOException)new IOException("Could not resolve artifact " + artifact + " in repo " + rootFile).initCause(e);
            }
        }
        File location = getLocation(artifact);
        return new LocalOpenResult(artifact, location);
    }

    public String toString() {
        return getClass().getName() + ":" + rootFile;
    }
}
