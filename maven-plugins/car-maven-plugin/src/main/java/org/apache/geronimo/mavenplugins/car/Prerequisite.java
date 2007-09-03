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


package org.apache.geronimo.mavenplugins.car;

import org.apache.geronimo.system.plugin.model.PrerequisiteType;

/**
 * @version $Rev$ $Date$
 */
public class Prerequisite {

    /**
     * @parameter
     */
    private ModuleId id;

    /**
     * @parameter
     */
    private String resourceType;

    /**
     * @parameter
     */
    private String description;


    public ModuleId getId() {
        return id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getDescription() {
        return description;
    }

    PrerequisiteType toPrerequisiteType() {
        PrerequisiteType prereq = new PrerequisiteType();
        prereq.setId(id.toArtifactType());
        prereq.setResourceType(resourceType);
        prereq.setDescription(description);
        return prereq;
    }

}
