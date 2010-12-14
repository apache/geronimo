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

package org.apache.geronimo.myfaces.info;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.myfaces.config.element.Behavior;
import org.apache.myfaces.config.element.ClientBehaviorRenderer;
import org.apache.myfaces.config.element.Converter;
import org.apache.myfaces.config.element.FacesConfigData;
import org.apache.myfaces.config.element.LocaleConfig;
import org.apache.myfaces.config.element.ManagedBean;
import org.apache.myfaces.config.element.NamedEvent;
import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.config.element.Renderer;
import org.apache.myfaces.config.element.ResourceBundle;
import org.apache.myfaces.config.element.SystemEventListener;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoFacesConfigData extends FacesConfigData {

    private List<String> applicationFactories = new ArrayList<String>();

    private List<String> exceptionHandlerFactories = new ArrayList<String>();

    private List<String> externalContextFactories = new ArrayList<String>();

    private List<String> facesContextFactories = new ArrayList<String>();

    private List<String> lifecycleFactories = new ArrayList<String>();

    private List<String> viewDeclarationLanguageFactories = new ArrayList<String>();

    private List<String> partialViewContextFactories = new ArrayList<String>();

    private List<String> renderKitFactories = new ArrayList<String>();

    private List<String> tagHandlerDelegateFactories = new ArrayList<String>();

    private List<String> visitContextFactories = new ArrayList<String>();

    private String defaultRenderKitId;

    private String messageBundle;

    private String partialTraversal;

    private String facesVersion;

    private LocaleConfig localeConfig;

    private Map<String, String> components = new HashMap<String, String>();

    private Map<String, String> converterByClass = new HashMap<String, String>();

    private Map<String, String> converterById = new HashMap<String, String>();

    private Map<String, String> validators = new HashMap<String, String>();

    private List<Behavior> behaviors = new ArrayList<Behavior>();

    private Map<String, Converter> converterConfigurationByClassName = new HashMap<String, Converter>();

    private Map<String, org.apache.myfaces.config.impl.digester.elements.RenderKit> renderKits = new LinkedHashMap<String, org.apache.myfaces.config.impl.digester.elements.RenderKit>();

    private List<String> actionListeners = new ArrayList<String>();

    private List<String> elResolvers = new ArrayList<String>();

    private List<String> lifecyclePhaseListeners = new ArrayList<String>();

    private List<String> navigationHandlers = new ArrayList<String>();

    private List<String> propertyResolver = new ArrayList<String>();

    private List<String> resourceHandlers = new ArrayList<String>();

    private List<String> stateManagers = new ArrayList<String>();

    private List<String> variableResolver = new ArrayList<String>();

    private List<String> viewHandlers = new ArrayList<String>();

    private List<String> defaultValidatorIds = new ArrayList<String>();

    private List<ManagedBean> managedBeans = new ArrayList<ManagedBean>();

    private List<NavigationRule> navigationRules = new ArrayList<NavigationRule>();

    private List<ResourceBundle> resourceBundles = new ArrayList<ResourceBundle>();

    private List<SystemEventListener> systemEventListeners = new ArrayList<SystemEventListener>();

    private List<NamedEvent> namedEvents = new ArrayList<NamedEvent>();

    public GeronimoFacesConfigData(FacesConfigData facesConfigData) {
        applicationFactories.addAll(facesConfigData.getApplicationFactoryIterator());

        exceptionHandlerFactories.addAll(facesConfigData.getExceptionHandlerFactoryIterator());

        externalContextFactories.addAll(facesConfigData.getExternalContextFactoryIterator());

        facesContextFactories.addAll(facesConfigData.getFacesContextFactoryIterator());

        lifecycleFactories.addAll(facesConfigData.getLifecycleFactoryIterator());

        viewDeclarationLanguageFactories.addAll(facesConfigData.getViewDeclarationLanguageFactoryIterator());

        partialViewContextFactories.addAll(facesConfigData.getPartialViewContextFactoryIterator());

        renderKitFactories.addAll(facesConfigData.getRenderKitFactoryIterator());

        tagHandlerDelegateFactories.addAll(facesConfigData.getTagHandlerDelegateFactoryIterator());

        visitContextFactories.addAll(facesConfigData.getVisitContextFactoryIterator());

        defaultRenderKitId = facesConfigData.getDefaultRenderKitId();

        messageBundle = facesConfigData.getMessageBundle();

        partialTraversal = facesConfigData.getPartialTraversal();

        facesVersion = facesConfigData.getFacesVersion();

        if (facesConfigData.getDefaultLocale() != null || facesConfigData.getSupportedLocalesIterator().size() > 0) {
            org.apache.myfaces.config.impl.digester.elements.LocaleConfig destLocaleConfig = new org.apache.myfaces.config.impl.digester.elements.LocaleConfig();
            destLocaleConfig.setDefaultLocale(facesConfigData.getDefaultLocale());
            destLocaleConfig.getSupportedLocales().addAll(facesConfigData.getSupportedLocalesIterator());
            this.localeConfig = destLocaleConfig;
        }

        for (String componentType : facesConfigData.getComponentTypes()) {
            components.put(componentType, facesConfigData.getComponentClass(componentType));
        }

        for (String converterClass : facesConfigData.getConverterClasses()) {
            converterByClass.put(converterClass, facesConfigData.getConverterClassByClass(converterClass));
        }

        for (String converterId : facesConfigData.getConverterIds()) {
            converterById.put(converterId, facesConfigData.getConverterClassById(converterId));
        }

        for (String validatorId : facesConfigData.getValidatorIds()) {
            validators.put(validatorId, facesConfigData.getValidatorClass(validatorId));
        }

        behaviors.addAll(facesConfigData.getBehaviors());

        for (String converterClassName : facesConfigData.getConverterConfigurationByClassName()) {
            converterConfigurationByClassName.put(converterClassName, facesConfigData.getConverterConfiguration(converterClassName));
        }

        for (String renderKitId : facesConfigData.getRenderKitIds()) {
            org.apache.myfaces.config.impl.digester.elements.RenderKit renderKit = new org.apache.myfaces.config.impl.digester.elements.RenderKit();
            renderKit.setId(renderKitId);
            renderKit.getClientBehaviorRenderers().addAll(facesConfigData.getClientBehaviorRenderers(renderKitId));
            renderKit.getRenderer().addAll(facesConfigData.getRenderers(renderKitId));
            renderKit.getRenderKitClasses().addAll(facesConfigData.getRenderKitClasses(renderKitId));
            renderKits.put(renderKitId, renderKit);
        }

        actionListeners.addAll(facesConfigData.getActionListenerIterator());

        elResolvers.addAll(facesConfigData.getElResolvers());

        lifecyclePhaseListeners.addAll(facesConfigData.getLifecyclePhaseListeners());

        navigationHandlers.addAll(facesConfigData.getNavigationHandlerIterator());

        propertyResolver.addAll(facesConfigData.getPropertyResolverIterator());

        resourceHandlers.addAll(facesConfigData.getResourceHandlerIterator());

        stateManagers.addAll(facesConfigData.getStateManagerIterator());

        variableResolver.addAll(facesConfigData.getVariableResolverIterator());

        viewHandlers.addAll(facesConfigData.getViewHandlerIterator());

        defaultValidatorIds.addAll(facesConfigData.getDefaultValidatorIds());

        managedBeans.addAll(facesConfigData.getManagedBeans());

        navigationRules.addAll(facesConfigData.getNavigationRules());

        resourceBundles.addAll(facesConfigData.getResourceBundles());

        systemEventListeners.addAll(facesConfigData.getSystemEventListeners());

        namedEvents.addAll(facesConfigData.getNamedEvents());
    }

    @Override
    public Collection<String> getActionListenerIterator() {
        return new ArrayList<String>(actionListeners);
    }

    @Override
    public String getDefaultRenderKitId() {
        return defaultRenderKitId;
    }

    @Override
    public String getMessageBundle() {
        return messageBundle;
    }

    @Override
    public Collection<String> getNavigationHandlerIterator() {
        return new ArrayList<String>(navigationHandlers);
    }

    @Override
    public String getPartialTraversal() {
        return partialTraversal;
    }

    @Override
    public Collection<String> getResourceHandlerIterator() {
        return new ArrayList<String>(resourceHandlers);
    }

    @Override
    public Collection<String> getViewHandlerIterator() {
        return new ArrayList<String>(viewHandlers);
    }

    @Override
    public Collection<String> getStateManagerIterator() {
        return new ArrayList<String>(stateManagers);
    }

    @Override
    public Collection<String> getPropertyResolverIterator() {
        return new ArrayList<String>(propertyResolver);
    }

    @Override
    public Collection<String> getVariableResolverIterator() {

        return new ArrayList<String>(variableResolver);
    }

    @Override
    public String getDefaultLocale() {
        if (localeConfig != null) {
            return localeConfig.getDefaultLocale();
        }
        return null;
    }

    @Override
    public Collection<String> getSupportedLocalesIterator() {
        List<String> locale;
        if (localeConfig != null) {
            locale = localeConfig.getSupportedLocales();
        } else {
            locale = Collections.emptyList();
        }

        return locale;
    }

    @Override
    public Collection<String> getComponentTypes() {
        return components.keySet();
    }

    @Override
    public String getComponentClass(String componentType) {
        return components.get(componentType);
    }

    @Override
    public Collection<String> getConverterIds() {
        return converterById.keySet();
    }

    @Override
    public Collection<String> getConverterClasses() {
        return converterByClass.keySet();
    }

    @Override
    public Collection<String> getConverterConfigurationByClassName() {
        return converterConfigurationByClassName.keySet();
    }

    @Override
    public Converter getConverterConfiguration(String converterClassName) {
        return converterConfigurationByClassName.get(converterClassName);
    }

    @Override
    public String getConverterClassById(String converterId) {
        return converterById.get(converterId);
    }

    @Override
    public String getConverterClassByClass(String className) {
        return converterByClass.get(className);
    }

    @Override
    public Collection<String> getDefaultValidatorIds() {
        return defaultValidatorIds;
    }

    @Override
    public Collection<String> getValidatorIds() {
        return validators.keySet();
    }

    @Override
    public String getValidatorClass(String validatorId) {
        return validators.get(validatorId);
    }

    @Override
    public Collection<ManagedBean> getManagedBeans() {
        return managedBeans;
    }

    @Override
    public Collection<NavigationRule> getNavigationRules() {
        return navigationRules;
    }

    @Override
    public Collection<String> getRenderKitIds() {
        return renderKits.keySet();
    }

    @Override
    public Collection<String> getRenderKitClasses(String renderKitId) {
        return renderKits.get(renderKitId).getRenderKitClasses();
    }

    @Override
    public Collection<ClientBehaviorRenderer> getClientBehaviorRenderers(String renderKitId) {
        return renderKits.get(renderKitId).getClientBehaviorRenderers();
    }

    @Override
    public Collection<Renderer> getRenderers(String renderKitId) {
        return renderKits.get(renderKitId).getRenderer();
    }

    @Override
    public Collection<String> getLifecyclePhaseListeners() {
        return lifecyclePhaseListeners;
    }

    @Override
    public Collection<ResourceBundle> getResourceBundles() {
        return resourceBundles;
    }

    @Override
    public Collection<String> getElResolvers() {
        return elResolvers;
    }

    @Override
    public Collection<SystemEventListener> getSystemEventListeners() {
        return systemEventListeners;
    }

    @Override
    public Collection<Behavior> getBehaviors() {
        return behaviors;
    }

    @Override
    public String getFacesVersion() {
        return facesVersion;
    }

    @Override
    public Collection<NamedEvent> getNamedEvents() {
        return namedEvents;
    }

    @Override
    public Collection<String> getApplicationFactoryIterator() {
        return applicationFactories;
    }

    @Override
    public Collection<String> getExceptionHandlerFactoryIterator() {
        return exceptionHandlerFactories;
    }

    @Override
    public Collection<String> getExternalContextFactoryIterator() {
        return externalContextFactories;
    }

    @Override
    public Collection<String> getFacesContextFactoryIterator() {
        return facesContextFactories;
    }

    @Override
    public Collection<String> getLifecycleFactoryIterator() {
        return lifecycleFactories;
    }

    @Override
    public Collection<String> getViewDeclarationLanguageFactoryIterator() {
        return viewDeclarationLanguageFactories;
    }

    @Override
    public Collection<String> getPartialViewContextFactoryIterator() {
        return partialViewContextFactories;
    }

    @Override
    public Collection<String> getRenderKitFactoryIterator() {
        return renderKitFactories;
    }

    @Override
    public Collection<String> getTagHandlerDelegateFactoryIterator() {
        return tagHandlerDelegateFactories;
    }

    @Override
    public Collection<String> getVisitContextFactoryIterator() {
        return visitContextFactories;
    }

}
