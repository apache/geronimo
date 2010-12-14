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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.context.ExternalContext;

import org.apache.myfaces.config.element.FacesConfig;
import org.apache.myfaces.spi.FacesConfigurationProvider;
import org.apache.myfaces.spi.ServiceProviderFinderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Geronimo only uses this in the deployment process, all the parsing, sorting and merging work will be done.
 * @version $Rev$ $Date$
 */
public class GeronimoFacesConfigurationProvider extends FacesConfigurationProvider {

    private static final Logger logger = LoggerFactory.getLogger(GeronimoFacesConfigurationProvider.class);

    private static final Set<String> FACTORY_NAMES = new HashSet<String>();
    {
        FACTORY_NAMES.add(FactoryFinder.APPLICATION_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.EXCEPTION_HANDLER_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.EXTERNAL_CONTEXT_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.FACES_CONTEXT_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.LIFECYCLE_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.RENDER_KIT_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.TAG_HANDLER_DELEGATE_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.VISIT_CONTEXT_FACTORY);
        FACTORY_NAMES.add(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY);
    }

    private FacesConfig annotationsFacesConfig;

    private List<FacesConfig> classloaderFacesConfigs;

    private List<FacesConfig> contextSpecifiedFacesConfigs;

    private FacesConfig webAppFacesConfig;

    private FacesConfig standardFacesConfig;

    public GeronimoFacesConfigurationProvider(FacesConfig standardFacesConfig, FacesConfig webAppFacesConfig, FacesConfig annotationsFacesConfig, List<FacesConfig> classloaderFacesConfigs,
            List<FacesConfig> contextSpecifiedFacesConfigs) {
        this.annotationsFacesConfig = annotationsFacesConfig;
        this.classloaderFacesConfigs = classloaderFacesConfigs;
        this.contextSpecifiedFacesConfigs = contextSpecifiedFacesConfigs;
        this.webAppFacesConfig = webAppFacesConfig;
        this.standardFacesConfig = standardFacesConfig;
    }

    @Override
    public FacesConfig getAnnotationsFacesConfig(ExternalContext ectx, boolean metadataComplete) {
        return annotationsFacesConfig;
    }

    @Override
    public List<FacesConfig> getClassloaderFacesConfig(ExternalContext externalContext) {
        return classloaderFacesConfigs;
    }

    @Override
    public List<FacesConfig> getContextSpecifiedFacesConfig(ExternalContext externalContext) {
        return contextSpecifiedFacesConfigs;
    }

    @Override
    public FacesConfig getStandardFacesConfig(ExternalContext externalContext) {
        return standardFacesConfig;
    }

    @Override
    public FacesConfig getWebAppFacesConfig(ExternalContext externalContext) {
        return webAppFacesConfig;
    }

    @Override
    public FacesConfig getMetaInfServicesFacesConfig(ExternalContext externalContext) {
        try {
            org.apache.myfaces.config.impl.digester.elements.FacesConfig facesConfig = new org.apache.myfaces.config.impl.digester.elements.FacesConfig();
            org.apache.myfaces.config.impl.digester.elements.Factory factory = new org.apache.myfaces.config.impl.digester.elements.Factory();

            facesConfig.addFactory(factory);

            for (String factoryName : FACTORY_NAMES) {
                List<String> classList = ServiceProviderFinderFactory.getServiceProviderFinder(externalContext).getServiceProviderList(factoryName);

                for (String className : classList) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found " + factoryName + " factory implementation: " + className);
                    }

                    if (factoryName.equals(FactoryFinder.APPLICATION_FACTORY)) {
                        factory.addApplicationFactory(className);
                    } else if (factoryName.equals(FactoryFinder.EXTERNAL_CONTEXT_FACTORY)) {
                        factory.addExternalContextFactory(className);
                    } else if (factoryName.equals(FactoryFinder.FACES_CONTEXT_FACTORY)) {
                        factory.addFacesContextFactory(className);
                    } else if (factoryName.equals(FactoryFinder.LIFECYCLE_FACTORY)) {
                        factory.addLifecycleFactory(className);
                    } else if (factoryName.equals(FactoryFinder.RENDER_KIT_FACTORY)) {
                        factory.addRenderkitFactory(className);
                    } else if (factoryName.equals(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY)) {
                        factory.addPartialViewContextFactory(className);
                    } else if (factoryName.equals(FactoryFinder.VISIT_CONTEXT_FACTORY)) {
                        factory.addVisitContextFactory(className);
                    } else {
                        throw new IllegalStateException("Unexpected factory name " + factoryName);
                    }
                }
            }
            return facesConfig;
        } catch (Throwable e) {
            throw new FacesException(e);
        }
    }

}
