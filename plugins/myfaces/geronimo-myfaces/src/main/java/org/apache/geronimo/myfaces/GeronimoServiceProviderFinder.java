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

package org.apache.geronimo.myfaces;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.myfaces.spi.ServiceProviderFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service provider finder is used to find those SPI factory classes, currently, we use a static fixed map to hold the default provider
 * classes, might need to provider methods for registering new providers.
 * @version $Rev$ $Date$
 */
public class GeronimoServiceProviderFinder extends ServiceProviderFinder {

    private static final Logger logger = LoggerFactory.getLogger(GeronimoServiceProviderFinder.class);

    private Map<String, List<String>> spiProvidersMap = new HashMap<String, List<String>>();

    private static final String META_INF_SERVICE_PREFIX = "META-INF/services";

    private ClassLoader classLoader;

    public GeronimoServiceProviderFinder(Map<String, List<String>> spiProvidersMap, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.spiProvidersMap = spiProvidersMap;
    }

    @Override
    public List<String> getServiceProviderList(String spiClass) {
        List<String> providers = spiProvidersMap.get(spiClass);
        return providers == null ? searchByClassLoader(spiClass) : providers;
    }

    private List<String> searchByClassLoader(String spiClass) {
        Enumeration<URL> en;
        try {
            en = classLoader.getResources(META_INF_SERVICE_PREFIX + "/" + spiClass);
        } catch (IOException e) {
            logger.warn("Fail to scan META-INF/services/" + spiClass, e);
            return Collections.<String> emptyList();
        }
        List<String> spiProviderClasses = new ArrayList<String>();
        while (en.hasMoreElements()) {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(en.nextElement().openStream(), "UTF-8"));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        spiProviderClasses.add(line);
                    }
                }
            } catch (IOException e) {
                logger.warn("Fail to scan META-INF/services/" + spiClass, e);
            } finally {
                IOUtils.close(bufferedReader);
            }
        }
        return spiProviderClasses;
    }
}
