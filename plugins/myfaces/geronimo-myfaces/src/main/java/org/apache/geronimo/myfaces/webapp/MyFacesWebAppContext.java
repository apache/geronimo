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

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.discovery.tools.EnvironmentCache;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.myfaces.config.annotation.GeronimoAnnotationProviderFactory;
import org.apache.geronimo.myfaces.config.resource.ConfigurationResource;
import org.apache.geronimo.myfaces.config.resource.GeronimoFacesConfigResourceProviderFactory;
import org.apache.myfaces.spi.AnnotationProviderFactory;
import org.apache.myfaces.spi.FacesConfigResourceProviderFactory;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
@GBean(name = "MyFaces WebApplication Context", j2eeType = "MyFacesWebAppContext")
public class MyFacesWebAppContext implements GBeanLifecycle {

    private static final Map<Bundle, MyFacesWebAppContext> myFacesWebAppContexts = new ConcurrentHashMap<Bundle, MyFacesWebAppContext>();

    private Map<Class<? extends Annotation>, Set<Class<?>>> annotationClassSetMap;

    private Set<URL> metaInfConfigurationResources;

    private Bundle bundle;

    private ClassLoader classLoader;

    public MyFacesWebAppContext(@ParamAttribute(name = "annotationClassSetMap") Map<Class<? extends Annotation>, Set<Class<?>>> annotationClassSetMap,
            @ParamAttribute(name = "metaInfConfigurationResources") Set<ConfigurationResource> metaInfConfigurationResources, @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
            @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader) {
        this.annotationClassSetMap = annotationClassSetMap;
        this.bundle = bundle;
        this.classLoader = classLoader;
        this.metaInfConfigurationResources = new HashSet<URL>();
        try {
            for (ConfigurationResource metaInfConfigurationResource : metaInfConfigurationResources) {
                this.metaInfConfigurationResources.add(metaInfConfigurationResource.getConfigurationResourceURL(bundle));
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotationClassSetMap() {
        return annotationClassSetMap;
    }

    public Set<URL> getMetaInfConfigurationResources() {
        return metaInfConfigurationResources;
    }

    @Override
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }
    }

    @Override
    public void doStart() throws Exception {
        myFacesWebAppContexts.put(bundle, this);
        //Set AnnotationProviderFactory/FacesConfigResourceProviderFactory in the commons-discovery cache
        Map<String, Object> cache = (Map<String, Object>) EnvironmentCache.get(classLoader);
        if (cache == null) {
            cache = new HashMap<String, Object>(EnvironmentCache.smallHashSize);
            EnvironmentCache.put(classLoader, cache);
        }
        cache.put(AnnotationProviderFactory.class.getName(), new GeronimoAnnotationProviderFactory(annotationClassSetMap));
        cache.put(FacesConfigResourceProviderFactory.class.getName(), new GeronimoFacesConfigResourceProviderFactory(metaInfConfigurationResources));
    }

    @Override
    public void doStop() throws Exception {
        myFacesWebAppContexts.remove(bundle);
        //Clear the cache
        Map<String, Object> cache = (Map<String, Object>) EnvironmentCache.get(classLoader);
        if (cache != null) {
            cache.remove(AnnotationProviderFactory.class.getName());
            cache.remove(FacesConfigResourceProviderFactory.class.getName());
        }

    }

    public static MyFacesWebAppContext getMyFacesWebAppContext(Bundle bundle) {
        return myFacesWebAppContexts.get(bundle);
    }

}
