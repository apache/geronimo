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

/**
 * @version $Rev$ $Date$
 */
public interface ConfigRegistry {

    /**
     * Get the parsed FacesConfig instances from the dependency tree of the specified bundle and its parents
     * @param bundle
     * @return
     */
    List<FacesConfig> getDependentFacesConfigs(Long bundleId);

    /**
     * Get the list of tag-lib.xml URLs in the specified bundle and its parents
     * @param bundleId
     * @return
     */
    List<URL> getDependentFaceletsConfigResources(Long bundleId);

    /**
     * Get all the bundle Id which have shipped faces-config.xml files in their META-INF directories
     * @return
     */
    Set<Long> getFacesConfigsBundleIds();

    /**
     * Get the list of parsed faces-config.xml in the specified bundle
     * @param bundleId
     * @return
     */
    List<FacesConfig> getFacesConfigs(Long bundleId);

    /**
     * Get the list of faces-config.xml URLs in the specified bundle
     * @param bundleId
     * @return
     */
    List<URL> getFacesConfigURLs(Long bundleId);

    /**
     * Get all the bundle Id which have shipped tag-lib.xml files in their META-INF directories
     * @return
     */
    Set<Long> getFaceletsConfigResourcesBundleIds();

    /**
     * Get the list of tag-lib.xml files in the specified bundle
     * @param bundleId
     * @return
     */
    List<URL> getFaceletsConfigResources(Long bundleId);
}
