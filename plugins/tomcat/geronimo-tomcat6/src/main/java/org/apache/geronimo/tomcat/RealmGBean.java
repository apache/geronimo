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
package org.apache.geronimo.tomcat;

import java.util.Map;
import java.lang.reflect.Constructor;

import org.apache.catalina.Realm;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.security.jaas.ConfigurationFactory;

public class RealmGBean extends BaseGBean implements GBeanLifecycle, ObjectRetriever {
    
    private final Realm realm;

    public RealmGBean(String className, Map initParams, ConfigurationFactory configurationFactory, ClassLoader classLoader) throws Exception {
        super();
        
        if (className == null) throw new NullPointerException("No realm class supplied");

        Class<?> clazz = Class.forName(className, true, classLoader);
        Realm realm;
        try {
            Constructor c = clazz.getConstructor(ConfigurationFactory.class);
            if (configurationFactory == null) {
                throw new NullPointerException("ConfigurationFactory must be supplied");
            }
            realm = (Realm) c.newInstance(configurationFactory);
        } catch (NoSuchMethodException e) {
            realm = (Realm) clazz.newInstance();
        }

        setParameters(realm, initParams);
        this.realm = realm;
    }

    public Object getInternalObject() {
        return realm;
    }

    public void doFail() {
    }

    public void doStart() throws Exception {
    }

    public void doStop() throws Exception {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("TomcatRealm", RealmGBean.class);
        infoFactory.addAttribute("className", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addReference("ConfigurationFactory", ConfigurationFactory.class);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);


        infoFactory.setConstructor(new String[] { "className", "initParams", "ConfigurationFactory", "classLoader" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
