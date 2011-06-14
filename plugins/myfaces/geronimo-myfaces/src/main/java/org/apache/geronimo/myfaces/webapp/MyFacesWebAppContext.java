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

package org.apache.geronimo.myfaces.webapp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.myfaces.config.resource.ConfigurationResource;
import org.apache.geronimo.myfaces.config.resource.osgi.api.ConfigRegistry;
import org.apache.myfaces.config.annotation.LifecycleProvider;
import org.apache.myfaces.config.element.FacesConfigData;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(name = "MyFaces WebApplication Context", j2eeType = "MyFacesWebAppContext")
public class MyFacesWebAppContext implements GBeanLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(MyFacesWebAppContext.class);

    private static final Map<String, MyFacesWebAppContext> MYFACES_WEBAPP_CONTEXTS = new ConcurrentHashMap<String, MyFacesWebAppContext>();

    private FacesConfigData facesConfigData;

    private Bundle bundle;

    private ClassLoader classLoader;

    private List<URL> faceletConfigResources;

    private AbstractName abName;

    private LifecycleProvider lifecycleProvider;

    public MyFacesWebAppContext(@ParamAttribute(name = "facesConfigData") FacesConfigData facesConfigData,
            @ParamAttribute(name = "faceletConfigResources") Set<ConfigurationResource> faceletConfigResources,
            @ParamReference(name = "LifecycleProvider") LifecycleProvider lifecycleProvider,
            @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
            @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
            @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abName) {
        this.bundle = BundleUtils.unwrapBundle(bundle);
        this.facesConfigData = facesConfigData;
        this.classLoader = classLoader;
        this.abName = abName;
        this.lifecycleProvider = lifecycleProvider;

        ServiceReference serviceReference = null;
        this.faceletConfigResources = new ArrayList<URL>(faceletConfigResources.size());
        try {
            for (ConfigurationResource faceletConfigResource : faceletConfigResources) {
                this.faceletConfigResources.add(faceletConfigResource.getConfigurationResourceURL(bundle));
            }
            serviceReference = bundle.getBundleContext().getServiceReference(ConfigRegistry.class.getName());
            if (serviceReference != null) {
                ConfigRegistry configRegistry = (ConfigRegistry) bundle.getBundleContext().getService(serviceReference);
                List<URL> dependentFaceletsConfigResources = configRegistry.getDependentFaceletsConfigResources(bundle.getBundleId());
                if (dependentFaceletsConfigResources != null) {
                    this.faceletConfigResources.addAll(dependentFaceletsConfigResources);
                }
            } else {
                logger.warn("Fail to find ConfigRegistry service, those *.taglib.xml from dependent bundles will not be registered in current web application " + bundle.getSymbolicName());
            }

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        } finally {
            if (serviceReference != null) {
                bundle.getBundleContext().ungetService(serviceReference);
            }
        }
    }

    public FacesConfigData getFacesConfigData() {
        return facesConfigData;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public List<URL> getRuntimeFaceletConfigResources() {
        return faceletConfigResources;
    }

    public LifecycleProvider getLifecycleProvider() {
        return lifecycleProvider;
    }

    @Override
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }
    }

    public String getWebModuleName(){
        return abName.getNameProperty(NameFactory.WEB_MODULE);
    }

    @Override
    public void doStart() throws Exception {
        MYFACES_WEBAPP_CONTEXTS.put(getWebModuleName(), this);
    }

    @Override
    public void doStop() throws Exception {
        MYFACES_WEBAPP_CONTEXTS.remove(getWebModuleName());
    }

    public static MyFacesWebAppContext getMyFacesWebAppContext(String webModuleName) {
        return MYFACES_WEBAPP_CONTEXTS.get(webModuleName);
    }
}
