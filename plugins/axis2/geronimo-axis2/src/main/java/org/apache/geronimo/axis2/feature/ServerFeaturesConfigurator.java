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

package org.apache.geronimo.axis2.feature;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.geronimo.jaxws.feature.AddressingFeatureInfo;
import org.apache.geronimo.jaxws.feature.MTOMFeatureInfo;
import org.apache.geronimo.jaxws.feature.RespectBindingFeatureInfo;
import org.apache.geronimo.jaxws.feature.WebServiceFeatureInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class ServerFeaturesConfigurator implements WebServiceFeatureConfigurator<WebServiceFeatureInfo> {

    private static final Logger logger = LoggerFactory.getLogger(ServerFeaturesConfigurator.class);

    private Map<String, WebServiceFeatureConfigurator> classNameConfiguratorMap = new ConcurrentHashMap<String, WebServiceFeatureConfigurator>();

    public ServerFeaturesConfigurator() {
        classNameConfiguratorMap.put(AddressingFeatureInfo.class.getName(), new AddressingFeatureConfigurator());
        classNameConfiguratorMap.put(MTOMFeatureInfo.class.getName(), new MTOMFeatureConfigurator());
        classNameConfiguratorMap.put(RespectBindingFeatureInfo.class.getName(), new RespectBindingFeatureConfigurator());
    }

    @Override
    public void configure(EndpointDescription endpointDescription, WebServiceFeatureInfo webServiceFeatureInfo) {
        WebServiceFeatureConfigurator configurator = classNameConfiguratorMap.get(webServiceFeatureInfo.getClass().getName());
        if (configurator == null) {
            logger.warn("No web service configurator supports the target webServiceFeatureInfo" + webServiceFeatureInfo);
            return;
        }
        configurator.configure(endpointDescription, webServiceFeatureInfo);
    }

    public void registerWebServiceFeatureConfigurator(String className, WebServiceFeatureConfigurator configurator) {
        classNameConfiguratorMap.put(className, configurator);
    }

    public WebServiceFeatureConfigurator unregisterWebServiceFeatureConfigurator(String className) {
        return classNameConfiguratorMap.remove(className);
    }
}
