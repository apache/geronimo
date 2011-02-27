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

package org.apache.geronimo.openejb.deployment;

import static org.apache.openejb.util.URLs.toFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ClientModule;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.DeploymentModule;
import org.apache.openejb.config.DeploymentsResolver;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.RequireDescriptors;
import org.apache.openejb.config.UnknownModuleTypeException;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.Options;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.UrlSet;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoDeploymentLoader extends DeploymentLoader {

    private String ddDir;

    public GeronimoDeploymentLoader(String ddDir, Set<Class<? extends DeploymentModule>> loadingRequiredModuleTypes) {
        super(ddDir, loadingRequiredModuleTypes);
        this.ddDir = ddDir;
    }

    protected void addWebModule(AppModule appModule, URL warUrl, ClassLoader parentClassLoader, String contextRoot, String moduleName) throws OpenEJBException {
        // create and add the WebModule
        String warPath = URLs.toFilePath(warUrl);
        WebModule webModule = createWebModule(appModule.getJarLocation(), warPath, parentClassLoader, contextRoot, moduleName);

        // get urls in web application
        List<URL> urls = new ArrayList<URL>();
        ClassLoader webClassLoader = webModule.getClassLoader();

        // get include/exclude properties from context-param
        Options contextParams = new Options(new Properties());

        String include = contextParams.get(CLASSPATH_INCLUDE, "");
        String exclude = contextParams.get(CLASSPATH_EXCLUDE, "");
        Set<RequireDescriptors> requireDescriptors = contextParams.getAll(CLASSPATH_REQUIRE_DESCRIPTOR, RequireDescriptors.CLIENT);
        boolean filterDescriptors = contextParams.get(CLASSPATH_FILTER_DESCRIPTORS, false);
        boolean filterSystemApps = contextParams.get(CLASSPATH_FILTER_SYSTEMAPPS, true);

        contextParams.getProperties().put(webModule.getModuleId(), warPath);
        FileUtils base = new FileUtils(webModule.getModuleId(), webModule.getModuleId(), contextParams.getProperties());
        DeploymentsResolver.loadFromClasspath(base, urls, webClassLoader, include, exclude, requireDescriptors, filterDescriptors, filterSystemApps, ddDir);

        // we need to exclude previously deployed modules
        // using a Set instead of a list would be easier ...
        UrlSet urlSet = new UrlSet(urls);
        urlSet = urlSet.exclude(new UrlSet(appModule.getAdditionalLibraries())); // there should not be modules in /lib
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            try {
                urlSet = urlSet.exclude(new File(ejbModule.getJarLocation()));
            } catch (MalformedURLException ignore) {
            }
        }
        for (ClientModule clientModule : appModule.getClientModules()) {
            try {
                urlSet = urlSet.exclude(new File(clientModule.getJarLocation()));
            } catch (MalformedURLException ignore) {
            }
        }
        // for persistence.xml, there is already a check in addPersistenceUnit to remove duplicates
        urls = urlSet.getUrls();

        // save the filtered list so that it can be used elsewhere
        webModule.setUrls(urls);

        // Check each URL to determine if it is an EJB jar
        for (URL url : urls) {
            try {
                Class<?> moduleType = discoverModuleType(url, webClassLoader, true);

                File file = toFile(url);
                String absolutePath = file.getAbsolutePath();

                if (EjbModule.class.isAssignableFrom(moduleType)) {
                    logger.info("Found ejb module " + moduleType.getSimpleName() + " in war " + contextRoot);

                    if (url.getProtocol().equals("file") && url.toString().endsWith("WEB-INF/classes/")) {
                        //EJB found in /WEB-INF/classes, define the war as EJB module
                        absolutePath = warPath;
                        url = warUrl;
                    }

                    EjbModule ejbModule = createEjbModule(url, absolutePath, webClassLoader, getModuleName());
                    appModule.getEjbModules().add(ejbModule);

                }

            } catch (IOException e) {
                logger.warning("Unable to determine the module type of " + url.toExternalForm() + ": Exception: " + e.getMessage(), e);
            } catch (UnknownModuleTypeException ignore) {
            }
        }
    }
}
