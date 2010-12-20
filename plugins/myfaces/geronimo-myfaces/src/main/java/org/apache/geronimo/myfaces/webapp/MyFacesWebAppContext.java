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

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.myfaces.config.resource.ConfigurationResource;
import org.apache.myfaces.config.element.FacesConfigData;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
@GBean(name = "MyFaces WebApplication Context", j2eeType = "MyFacesWebAppContext")
public class MyFacesWebAppContext implements GBeanLifecycle {

    private static final Map<Bundle, MyFacesWebAppContext> MYFACES_WEBAPP_CONTEXTS = new ConcurrentHashMap<Bundle, MyFacesWebAppContext>();

    private FacesConfigData facesConfigData;

    private Bundle bundle;

    private ClassLoader classLoader;

    private List<URL> faceletConfigResources;

    public MyFacesWebAppContext(@ParamAttribute(name = "facesConfigData") FacesConfigData facesConfigData,
                                                                @ParamAttribute(name = "faceletConfigResources") Set<ConfigurationResource> faceletConfigResources,
                                                                @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
                                                                @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader) {
        this.bundle = bundle;
        this.facesConfigData = facesConfigData;
        this.classLoader = classLoader;
        this.faceletConfigResources = new ArrayList<URL>(faceletConfigResources.size());
        try {
            for (ConfigurationResource faceletConfigResource : faceletConfigResources) {
                this.faceletConfigResources.add(faceletConfigResource.getConfigurationResourceURL(bundle));
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
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

    @Override
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }
    }

    @Override
    public void doStart() throws Exception {
        MYFACES_WEBAPP_CONTEXTS.put(bundle, this);
    }

    @Override
    public void doStop() throws Exception {
        MYFACES_WEBAPP_CONTEXTS.remove(bundle);
    }

    public static MyFacesWebAppContext getMyFacesWebAppContext(Bundle bundle) {
        return MYFACES_WEBAPP_CONTEXTS.get(bundle);
    }
}
