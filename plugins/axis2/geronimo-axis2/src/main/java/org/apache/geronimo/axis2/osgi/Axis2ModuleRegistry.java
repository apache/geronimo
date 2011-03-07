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

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * @version $Rev$ $Date$
 */
@GBean
@OsgiService
public class Axis2ModuleRegistry implements BundleListener, GBeanLifecycle {

    private BundleContext bundleContext;

    private final Map<Bundle, List<URL>> bundleModuleXmlURLsMap = new HashMap<Bundle, List<URL>>();

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

}
