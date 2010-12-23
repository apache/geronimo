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


package org.apache.geronimo.myfaces.config.resource.osgi.api;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.myfaces.config.element.FacesConfig;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:$ $Date:$
 */
public interface ConfigRegistry {

    /**
     * Get the URL list of founded faces-config.xml for all the started bundles
     * @return
     */
    Set<URL> getRegisteredConfigUrls();

    /**
     * Get the parsed FacesConfig instances from the dependency tree of the specified bundle
     * @param bundle
     * @return
     */
    List<FacesConfig> getDependentFacesConfigs(Bundle bundle);
}
