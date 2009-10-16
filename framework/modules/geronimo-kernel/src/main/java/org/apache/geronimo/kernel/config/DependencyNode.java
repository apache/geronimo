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


package org.apache.geronimo.kernel.config;

import java.util.LinkedHashSet;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * Tracks dependency info for a plugin/configuration
 *
 * @version $Rev$ $Date$
 */
public class DependencyNode {

    private final Artifact id;
    private final LinkedHashSet<Artifact> classParents;
    private final LinkedHashSet<Artifact> serviceParents;

    public DependencyNode(Artifact id, LinkedHashSet<Artifact> classParents, LinkedHashSet<Artifact> serviceParents) {
        this.id = id;
        this.classParents = classParents;
        this.serviceParents = serviceParents;
    }

    public Artifact getId() {
        return id;
    }

    public LinkedHashSet<Artifact> getClassParents() {
        return classParents;
    }

    public LinkedHashSet<Artifact> getServiceParents() {
        return serviceParents;
    }

    public LinkedHashSet<Artifact> getParents() {
        LinkedHashSet<Artifact> all = new LinkedHashSet<Artifact>(classParents);
        all.addAll(serviceParents);
        return all;
    }
}
