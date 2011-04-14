/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.jaxws.builder;

import java.util.Map;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.jaxws.PortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WARWebServiceFinder implements WebServiceFinder<WebModule> {

    private static final Logger LOG = LoggerFactory.getLogger(WARWebServiceFinder.class);

    private static final WebServiceFinder webServiceFinder = getWebServiceFinder();

    private static WebServiceFinder getWebServiceFinder() {
        boolean useSimpleFinder =
            Boolean.getBoolean("org.apache.geronimo.jaxws.builder.useSimpleFinder");

        WebServiceFinder webServiceFinder;

        if (useSimpleFinder) {
            webServiceFinder = new SimpleWARWebServiceFinder();
        } else {
            webServiceFinder = new AdvancedWARWebServiceFinder();
        }

        return webServiceFinder;
    }

    @Override
    public Map<String, PortInfo> discoverWebServices(WebModule module, Map<String, String> correctedPortLocations) throws DeploymentException {
        return webServiceFinder.discoverWebServices(module, correctedPortLocations);
    }
}
