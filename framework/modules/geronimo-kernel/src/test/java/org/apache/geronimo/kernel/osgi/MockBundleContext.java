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


package org.apache.geronimo.kernel.osgi;

import java.io.InputStream;
import java.io.File;
import java.util.Dictionary;
import java.util.Map;
import java.util.HashMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Filter;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;

/**
 * @version $Rev$ $Date$
 */
public class MockBundleContext implements BundleContext {
    protected static final Naming naming = new Jsr77Naming();


    private ConfigurationManager configurationManager;
    private final Bundle bundle;
    private final ClassLoader classLoader;
    private final Map<Artifact, ConfigurationData> configurationDatas;
    private final Map<String, Artifact> locations;
    private final Map<Long, Bundle> bundles = new HashMap<Long, Bundle>();

    private long counter = 0;

    public MockBundleContext(Bundle bundle) {
        this.bundle = bundle;
        this.classLoader = null;
        configurationDatas = new HashMap<Artifact, ConfigurationData>();
        locations = new HashMap<String, Artifact>();
    }

    public MockBundleContext(ClassLoader classLoader, String location, Map<Artifact, ConfigurationData> configurationDatas, Map<String, Artifact> locationToArtifact) {
        this.bundle = new MockBundle(classLoader, location, counter++);
        bundles.put(counter, this.bundle);
        this.classLoader = classLoader;
        ((MockBundle)bundle).setBundleContext(this);
        this.configurationDatas = configurationDatas == null? new HashMap<Artifact, ConfigurationData>(): configurationDatas;
        this.locations = locationToArtifact == null? new HashMap<String, Artifact>(): locationToArtifact;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public String getProperty(String s) {
        return null;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public Bundle installBundle(String location) throws BundleException {
        if (location.startsWith("reference:")) {
            location = location.substring("reference:".length());
        }
        if (location.startsWith("file:")) {
            location = location.substring("file:".length());
        }
        while (location.startsWith("/")) {
            location = location.substring(1);
        }
        MockBundle bundle = new MockBundle(classLoader, location, counter++);
        bundles.put(counter, bundle);
        //activate it.
        Artifact configId = locations.get(location);
        if (configId == null) {
            configId = Artifact.create(location);
        }
        ConfigurationData configurationData = configurationDatas.get(configId);
        if (configurationData == null) {
            configurationData = new ConfigurationData(configId, naming);
        }
        BundleContext bundleContext = new WrappingBundleContext(bundle);
        bundle.setBundleContext(bundleContext);
        configurationData.setBundleContext(bundleContext);
        try {
            configurationManager.loadConfiguration(configurationData);
        } catch (NoSuchConfigException e) {
            throw (BundleException)new BundleException("").initCause(e);
        } catch (LifecycleException e) {
            throw (BundleException)new BundleException("").initCause(e);
        }
        return bundle;
    }

    public Bundle installBundle(String s, InputStream inputStream) throws BundleException {
        return installBundle(s);
    }

    public Bundle getBundle(long l) {
        return bundles.get(l);
    }

    public Bundle[] getBundles() {
        return bundles.values().toArray(new Bundle[bundles.size()]);
    }

    public void addServiceListener(ServiceListener serviceListener, String s) throws InvalidSyntaxException {
    }

    public void addServiceListener(ServiceListener serviceListener) {
    }

    public void removeServiceListener(ServiceListener serviceListener) {
    }

    public void addBundleListener(BundleListener bundleListener) {
    }

    public void removeBundleListener(BundleListener bundleListener) {
    }

    public void addFrameworkListener(FrameworkListener frameworkListener) {
    }

    public void removeFrameworkListener(FrameworkListener frameworkListener) {
    }

    public ServiceRegistration registerService(String[] strings, Object o, Dictionary dictionary) {
        return null;
    }

    public ServiceRegistration registerService(String s, Object o, Dictionary dictionary) {
        return null;
    }

    public ServiceReference[] getServiceReferences(String s, String s1) throws InvalidSyntaxException {
        return new ServiceReference[0];
    }

    public ServiceReference[] getAllServiceReferences(String s, String s1) throws InvalidSyntaxException {
        return new ServiceReference[0];
    }

    public ServiceReference getServiceReference(String s) {
        return null;
    }

    public Object getService(ServiceReference serviceReference) {
        return null;
    }

    public boolean ungetService(ServiceReference serviceReference) {
        return false;
    }

    public File getDataFile(String s) {
        return null;
    }

    public Filter createFilter(String s) throws InvalidSyntaxException {
        return null;
    }

    private class WrappingBundleContext extends MockBundleContext {

        private WrappingBundleContext(Bundle bundle) {
            super(bundle);
        }

        @Override
        public Bundle installBundle(String location) throws BundleException {
            return MockBundleContext.this.installBundle(location);
        }

    }

}
