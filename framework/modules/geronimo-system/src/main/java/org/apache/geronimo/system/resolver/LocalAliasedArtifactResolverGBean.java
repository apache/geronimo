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


package org.apache.geronimo.system.resolver;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.wrapper.AbstractServiceWrapper;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.MultipleMatchesException;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:$ $Date:$
 */

@GBean(j2eeType = "ArtifactResolver")
public class LocalAliasedArtifactResolverGBean extends AbstractServiceWrapper<LocalAliasedArtifactResolver> implements LocalAliasedArtifactResolver{
    public LocalAliasedArtifactResolverGBean(@ParamSpecial(type = SpecialAttributeType.bundle)final Bundle bundle) {
        super(bundle, LocalAliasedArtifactResolver.class);
    }

    @Override
    public String getArtifactAliasesFile() {
        return get().getArtifactAliasesFile();
    }

    @Override
    public void addAliases(Map<String, String> properties) throws IOException {
        get().addAliases(properties);
    }

    @Override
    public Artifact generateArtifact(Artifact source, String defaultType) {
        return get().generateArtifact(source, defaultType);
    }

    @Override
    public Artifact resolveInClassLoader(Artifact source) throws MissingDependencyException {
        return get().resolveInClassLoader(source);
    }

    @Override
    public Artifact resolveInClassLoader(Artifact source, Collection<Configuration> parentConfigurations) throws MissingDependencyException {
        return get().resolveInClassLoader(source, parentConfigurations);
    }

    @Override
    public LinkedHashSet<Artifact> resolveInClassLoader(Collection<Artifact> sources) throws MissingDependencyException {
        return get().resolveInClassLoader(sources);
    }

    @Override
    public LinkedHashSet<Artifact> resolveInClassLoader(Collection<Artifact> sources, Collection<Configuration> parentConfigurations) throws MissingDependencyException {
        return get().resolveInClassLoader(sources, parentConfigurations);
    }

    @Override
    public Artifact queryArtifact(Artifact artifact) throws MultipleMatchesException {
        return get().queryArtifact(artifact);
    }

    @Override
    public Artifact[] queryArtifacts(Artifact artifact) {
        return get().queryArtifacts(artifact);
    }
}
