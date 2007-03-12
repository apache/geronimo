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


package org.apache.geronimo.myfaces;

import org.apache.myfaces.config.annotation.ApplicationIndexedLifecycleProviderFactory;
import org.apache.myfaces.config.annotation.LifecycleProviderFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev:$ $Date:$
 */
public class LifecycleProviderFactoryGBean {

    private final ApplicationIndexedLifecycleProviderFactory factory;

    public LifecycleProviderFactoryGBean() {
        factory = new ApplicationIndexedLifecycleProviderFactory();
        LifecycleProviderFactory.setInstance(factory);
    }


    public ApplicationIndexedLifecycleProviderFactory getLifecycleProviderFactory() {
        return factory;
    }
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(LifecycleProviderFactoryGBean.class, NameFactory.GERONIMO_SERVICE);

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
