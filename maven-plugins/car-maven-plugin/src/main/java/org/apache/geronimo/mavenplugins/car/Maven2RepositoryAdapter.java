/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.car;

import java.io.File;

import org.apache.geronimo.system.repository.Maven2Repository;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * Helps adapt Geronimo repositories to Maven repositories for packaging building.
 *
 * @version $Rev$ $Date$
 */
public class Maven2RepositoryAdapter
    extends Maven2Repository
{
    private ArtifactLookup lookup;

    public Maven2RepositoryAdapter(final ArtifactLookup lookup) {
        super(lookup.getBasedir());

        this.lookup = lookup;
    }

    public File getLocation(final Artifact artifact) {
        assert artifact != null;

        return lookup.getLocation(artifact);
    }

    //
    // ArtifactLookup
    //

    public static interface ArtifactLookup
    {
        File getLocation(Artifact artifact);

        File getBasedir();
    }

    //
    // GBean
    //

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(Maven2RepositoryAdapter.class, "Repository");
        infoFactory.addAttribute("lookup", ArtifactLookup.class, true);
        infoFactory.setConstructor(new String[]{ "lookup" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
