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

package org.apache.geronimo.farm.config;

import java.util.Collection;

import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicClusterInfo implements ClusterInfo {
    private final String name;
    private final Collection<NodeInfo> nodes;

    public BasicClusterInfo(@ParamAttribute(name=GBEAN_ATTR_CLUSTER_NAME) String name,
            @ParamReference(name=GBEAN_REF_NODE_INFOS) Collection<NodeInfo> nodes) {
        if (null == name) {
            throw new IllegalArgumentException("name is required");
        } else if (null == nodes) {
            throw new IllegalArgumentException("nodes is required");
        }
        this.name = name;
        this.nodes = nodes;
    }

    public String getName() {
        return name;
    }

    public Collection<NodeInfo> getNodeInfos() {
        return nodes;
    }

    public static final String GBEAN_ATTR_CLUSTER_NAME = "name";
    public static final String GBEAN_REF_NODE_INFOS = "NodeInfos";
}
