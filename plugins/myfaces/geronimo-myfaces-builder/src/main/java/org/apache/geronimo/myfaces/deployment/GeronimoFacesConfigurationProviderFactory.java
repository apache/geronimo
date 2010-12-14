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

package org.apache.geronimo.myfaces.deployment;

import java.util.List;

import javax.faces.context.ExternalContext;

import org.apache.myfaces.config.element.FacesConfig;
import org.apache.myfaces.spi.FacesConfigurationProvider;
import org.apache.myfaces.spi.FacesConfigurationProviderFactory;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoFacesConfigurationProviderFactory extends FacesConfigurationProviderFactory {

    private FacesConfigurationProvider facesConfigurationProvider;

    public GeronimoFacesConfigurationProviderFactory(FacesConfig standardFacesConfig, FacesConfig webAppFacesConfig, FacesConfig annotationsFacesConfig, List<FacesConfig> classloaderFacesConfigs,
            List<FacesConfig> contextSpecifiedFacesConfigs) {
        facesConfigurationProvider = new GeronimoFacesConfigurationProvider(standardFacesConfig, webAppFacesConfig, annotationsFacesConfig, classloaderFacesConfigs, contextSpecifiedFacesConfigs);
    }

    @Override
    public FacesConfigurationProvider getFacesConfigurationProvider(ExternalContext arg0) {
        return facesConfigurationProvider;
    }

}
