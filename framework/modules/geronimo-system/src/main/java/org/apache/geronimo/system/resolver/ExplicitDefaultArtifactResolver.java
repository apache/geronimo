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

package org.apache.geronimo.system.resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class ExplicitDefaultArtifactResolver extends DefaultArtifactResolver implements LocalAliasedArtifactResolver {
    private static final String COMMENT = "#You can use this file to indicate that you want to substitute one module for another.\n" +
            "#format is oldartifactid=newartifactId e.g.\n" +
            "#org.apache.geronimo.configs/transaction//car=org.apache.geronimo.configs/transaction-jta11/1.2-SNAPSHOT/car\n" +
            "#versions can be ommitted on the left side but not the right.\n" +
            "#This can also specify explicit versions in the same format.";

    private final String artifactAliasesFile;
    private final ServerInfo serverInfo;

    public ExplicitDefaultArtifactResolver(String versionMapLocation,
                                           ArtifactManager artifactManager,
                                           Collection<? extends ListableRepository> repositories,
                                           ServerInfo serverInfo ) throws IOException {
        this(versionMapLocation, artifactManager, repositories, null, serverInfo);
    }

    public ExplicitDefaultArtifactResolver(String versionMapLocation,
            ArtifactManager artifactManager,
            Collection<? extends ListableRepository> repositories,
            Map<String, String> additionalAliases,
            ServerInfo serverInfo ) throws IOException {
        super(artifactManager, repositories, buildExplicitResolution(versionMapLocation, additionalAliases, serverInfo));
        this.artifactAliasesFile = versionMapLocation;
        this.serverInfo = serverInfo;
    }


    public String getArtifactAliasesFile() {
        return artifactAliasesFile;
    }

    private static Map<Artifact, Artifact> buildExplicitResolution(String versionMapLocation, Map<String, String> additionalAliases, ServerInfo serverInfo) throws IOException {
        if (versionMapLocation == null) {
            return null;
        }
        Properties properties = new Properties();
        File location = serverInfo == null? new File(versionMapLocation): serverInfo.resolveServer(versionMapLocation);
        if (location.exists()) {
            FileInputStream in = new FileInputStream(location);
            try {
                properties.load(in);
            } finally {
                in.close();
            }
        }
        if (additionalAliases != null) {
            properties.putAll(additionalAliases);
        }
        return propertiesToArtifactMap(properties);
    }

    private static Map<Artifact, Artifact> propertiesToArtifactMap(Properties properties) {
        Map<Artifact, Artifact> explicitResolution = new HashMap<Artifact, Artifact>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String resolvedString = (String) entry.getValue();
            Artifact source = Artifact.createPartial(key.trim());
            Artifact resolved = Artifact.create(resolvedString.trim());
            explicitResolution.put(source, resolved);
        }
        return explicitResolution;
    }

    private static void saveExplicitResolution(Map<Artifact, Artifact> artifactMap, String versionMapLocation, ServerInfo serverInfo) throws IOException {
        if (versionMapLocation == null) {
            return;
        }
        File location = serverInfo == null? new File(versionMapLocation): serverInfo.resolveServer(versionMapLocation);
        if (!location.exists()) {
            File parent = location.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IOException("Could not create directory for artifact aliases at " + parent);
            }
        }
        FileOutputStream in = new FileOutputStream(location);
        Properties properties = artifactMapToProperties(artifactMap);
        try {
            properties.store(in, COMMENT);
        } finally {
            in.close();
        }
    }

    private static Properties artifactMapToProperties(Map<Artifact, Artifact> artifactMap) {
        Properties properties = new Properties();
        for (Map.Entry<Artifact, Artifact> entry: artifactMap.entrySet()) {
            properties.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        return properties;
    }

    /**
     * Add some more artifact aliases.  The plugin installer calls this
     * TODO when a plugin is uninstalled, remove the aliases?
     * @param properties Properties object containing the new aliases
     * @throws IOException if the modified aliases map cannot be saved.                                            
     */
    public synchronized void addAliases(Properties properties) throws IOException {
        Map<Artifact, Artifact> explicitResolutions = propertiesToArtifactMap(properties);
        getExplicitResolution().putAll(explicitResolutions);
        saveExplicitResolution(getExplicitResolution(), artifactAliasesFile, serverInfo);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ExplicitDefaultArtifactResolver.class, "ArtifactResolver");
        infoFactory.addAttribute("versionMapLocation", String.class, true, true);
        infoFactory.addAttribute("additionalAliases", Map.class, true, true);
        infoFactory.addReference("ArtifactManager", ArtifactManager.class, "ArtifactManager");
        infoFactory.addReference("Repositories", ListableRepository.class, "Repository");
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addInterface(ArtifactResolver.class);

        infoFactory.setConstructor(new String[]{
                "versionMapLocation",
                "ArtifactManager",
                "Repositories",
                "additionalAliases",
                "ServerInfo"
        });


        GBEAN_INFO = infoFactory.getBeanInfo();

    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
