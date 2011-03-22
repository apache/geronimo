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

package org.apache.geronimo.axis2.osgi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.ModuleBuilder;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Version;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.modules.Module;
import org.apache.axis2.osgi.deployment.Registry;
import org.apache.axis2.util.Utils;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean
@OsgiService
public class Axis2ModuleRegistry implements BundleListener, GBeanLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(Axis2ModuleRegistry.class);

    private BundleContext bundleContext;

    private final Map<Bundle, List<URL>> bundleModuleXmlURLsMap = new ConcurrentHashMap<Bundle, List<URL>>();

    public Axis2ModuleRegistry(@ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }

    }

    @Override
    public void bundleChanged(BundleEvent event) {
        Bundle bundle = event.getBundle();
        int bundleState = bundle.getState();
        if (bundleState == Bundle.ACTIVE) {
            addModuleXmlBundle(bundle);
        } else if (bundleState == Bundle.STOPPING) {
            bundleModuleXmlURLsMap.remove(bundle);
        }
    }

    @Override
    public void doStart() throws Exception {
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getState() == Bundle.ACTIVE) {
                addModuleXmlBundle(bundle);
            }
        }
        bundleContext.addBundleListener(this);
    }

    @Override
    public void doStop() throws Exception {
        bundleContext.removeBundleListener(this);
        bundleModuleXmlURLsMap.clear();
    }

    public Map<Bundle, List<URL>> getBundleModuleXmlURLsMap() {
        return new HashMap<Bundle, List<URL>>(bundleModuleXmlURLsMap);
    }

    private void addModuleXmlBundle(Bundle bundle) {
        Enumeration<URL> enumeration = bundle.findEntries("META-INF", "*module.xml", false);
        if (enumeration == null) {
            return;
        }
        List<URL> moduleXmls = new ArrayList<URL>();
        while (enumeration.hasMoreElements()) {
            moduleXmls.add(enumeration.nextElement());
        }
        bundleModuleXmlURLsMap.put(bundle, moduleXmls);
    }

    public void configureModules(ConfigurationContext configurationContext) {
        for (Map.Entry<Bundle, List<URL>> entry : bundleModuleXmlURLsMap.entrySet()) {
            Bundle bundle = entry.getKey();
            for (URL url : entry.getValue()) {
                try {
                    AxisModule axismodule = new AxisModule();
                    ClassLoader loader = new org.apache.axis2.osgi.deployment.BundleClassLoader(bundle, Registry.class.getClassLoader());
                    axismodule.setModuleClassLoader(loader);
                    AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
                    ModuleBuilder builder = new ModuleBuilder(url.openStream(), axismodule, axisConfig);
                    Dictionary headers = bundle.getHeaders();
                    String bundleSymbolicName = (String) headers.get("Bundle-SymbolicName");
                    if (bundleSymbolicName != null && bundleSymbolicName.length() != 0) {
                        axismodule.setName(bundleSymbolicName);
                    }
                    String bundleVersion = (String) headers.get("Bundle-Version");
                    if (bundleVersion != null && bundleVersion.length() != 0) {
                        /*
                            Bundle version is defined as
                            version ::=
                                major( '.' minor ( '.' micro ( '.' qualifier )? )? )?
                                major ::= number
                                minor ::= number
                                micro ::= number
                                qualifier ::= ( alphanum | ’_’ | '-' )+
                         */
                        String[] versionSplit = bundleVersion.split("\\.");
                        int[] components = new int[Math.min(versionSplit.length, 3)];
                        for (int i = 0; i < components.length; i++) {
                            components[i] = Integer.parseInt(versionSplit[i]);
                        }
                        axismodule.setVersion(new Version(components, versionSplit.length > 3 ? versionSplit[3] : null));
                    }
                    builder.populateModule();
                    axismodule.setParent(axisConfig);
                    AxisModule module = axisConfig.getModule(axismodule.getName());
                    if (module == null) {
                        DeploymentEngine.addNewModule(axismodule, axisConfig);
                        //initialze the module if the module contains Module interface.
                        Module moduleObj = axismodule.getModule();
                        if (moduleObj != null) {
                            moduleObj.init(configurationContext, axismodule);
                        }
                    }
                    // set in default map if necessary
                    Utils.calculateDefaultModuleVersion(axisConfig.getModules(), axisConfig);
                } catch (IOException e) {
                    logger.error("Error while reading module.xml", e);
                }
            }
        }
    }

}
