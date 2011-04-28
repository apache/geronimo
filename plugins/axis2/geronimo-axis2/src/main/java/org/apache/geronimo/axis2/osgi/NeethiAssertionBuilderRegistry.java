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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
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
public class NeethiAssertionBuilderRegistry implements BundleListener, GBeanLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(NeethiAssertionBuilderRegistry.class);

    private BundleContext bundleContext;

    public NeethiAssertionBuilderRegistry(@ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) {
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
        if (bundleState == Bundle.RESOLVED) {
            registerAssertionBuilder(bundle);
        }
    }

    @Override
    public void doStart() throws Exception {
        for (Bundle bundle : bundleContext.getBundles()) {
            if (BundleUtils.isResolved(bundle)) {
                registerAssertionBuilder(bundle);
            }
        }
        bundleContext.addBundleListener(this);

    }

    @Override
    public void doStop() throws Exception {
        bundleContext.removeBundleListener(this);
    }

    protected void registerAssertionBuilder(Bundle bundle) {
        URL url = bundle.getEntry("META-INF/services/" + AssertionBuilder.class.getName());
        if (url == null) {
            return;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            String currLine = null;
            while ((currLine = reader.readLine()) != null) {
                int commentIndex = currLine.indexOf("#");
                if (commentIndex != -1) {
                    currLine = currLine.substring(0, commentIndex);
                }
                currLine = currLine.trim();
                if (currLine.length() > 0) {
                    try {
                        Class<?> cls = bundle.loadClass(currLine);
                        Object instance = cls.newInstance();
                        if (instance instanceof AssertionBuilder) {
                            AssertionBuilder assertionBuilder = (AssertionBuilder) instance;
                            for (QName supportedQName : assertionBuilder.getKnownElements()) {
                                PolicyEngine.registerBuilder(supportedQName, assertionBuilder);
                            }
                        } else {
                            logger.warn(currLine + " in the META-INF/services/ org.apache.neethi.builders.AssertionBuilder from bundle " + bundle.getSymbolicName()
                                    + " is not of type AssertionBuilder, it will be ignored");
                        }
                    } catch (Exception e) {
                        logger.warn(currLine + " in the META-INF/services/ org.apache.neethi.builders.AssertionBuilder from bundle " + bundle.getSymbolicName()
                                + " could not be registered to PolicyEngine, it will be ignored", e);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            IOUtils.close(reader);
        }
    }
}
