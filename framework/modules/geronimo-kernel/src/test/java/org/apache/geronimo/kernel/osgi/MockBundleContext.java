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
import java.util.Collection;
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
import org.apache.geronimo.kernel.mock.MockConfigurationManager;
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


    private ConfigurationManager configurationManager = new MockConfigurationManager();
    private final Bundle bundle;
    private final ClassLoader classLoader;
    private final Map<Artifact, ConfigurationData> configurationDatas;
    private final Map<String, Artifact> locations;
    private final Map<Long, Bundle> bundles = new HashMap<Long, Bundle>();
    private final Map<String, ServiceReference> serviceReferences = new HashMap<String, ServiceReference>();

    private long counter = 0;

    public MockBundleContext(Bundle bundle) {
        this.bundle = bundle;
        this.classLoader = null;
        configurationDatas = new HashMap<Artifact, ConfigurationData>();
        locations = new HashMap<String, Artifact>();
        bundles.put(bundle.getBundleId(), bundle);
    }

    public MockBundleContext(ClassLoader classLoader, String location, Map<Artifact, ConfigurationData> configurationDatas, Map<String, Artifact> locationToArtifact) {
        this.bundle = new MockBundle(classLoader, location, counter);
        bundles.put(counter++, this.bundle);
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
        MockBundle bundle = new MockBundle(classLoader, location, counter);
        bundles.put(counter++, bundle);
        //activate it.
        Artifact configId = getArtifactByLocation(location);
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

    @Override
    public <S> ServiceRegistration<S> registerService(Class<S> sClass, S s, Dictionary<String, ?> stringDictionary) {
        return null;
    }

    public ServiceRegistration registerService(String[] strings, Object o, Dictionary dictionary) {
        return null;
    }

    public ServiceRegistration registerService(String s, Object o, Dictionary dictionary) {
        ServiceReference sr = new MockServiceReference(o);
        serviceReferences.put(s, sr);
        return new MockServiceRegistration(s, sr);
    }

    public ServiceReference[] getServiceReferences(String s, String s1) throws InvalidSyntaxException {
        return new ServiceReference[0];
    }

    public ServiceReference[] getAllServiceReferences(String s, String s1) throws InvalidSyntaxException {
        return new ServiceReference[0];
    }

    public ServiceReference getServiceReference(String s) {
        return serviceReferences.get(s);
    }

    @Override
    public <S> ServiceReference<S> getServiceReference(Class<S> sClass) {
        return null;
    }

    @Override
    public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> sClass, String s) throws InvalidSyntaxException {
        return null;
    }

    public Object getService(ServiceReference serviceReference) {
        return ((MockServiceReference)serviceReference).service;
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

    @Override
    public Bundle getBundle(String s) {
        return null;
    }

    private Artifact getArtifactByLocation(String location) {
        Artifact configId = locations.get(location);
        if (configId == null) {
            configId = locations.get(null);
        }
        if (configId == null) {
            try {
                configId = Artifact.create(location);
            } catch (Exception e) {
                configId = new Artifact("org.apache.geronimo", "testwebapp" + System.currentTimeMillis(), "1.0", "car");
            }
        }
        return configId;
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

    private class MockServiceReference implements ServiceReference {

        private final Object service;

        private MockServiceReference(Object service) {
            this.service = service;
        }

        @Override
        public Object getProperty(String s) {
            return null;
        }

        @Override
        public String[] getPropertyKeys() {
            return new String[0];
        }

        @Override
        public Bundle getBundle() {
            return bundle;
        }

        @Override
        public Bundle[] getUsingBundles() {
            return new Bundle[0];
        }

        @Override
        public boolean isAssignableTo(Bundle bundle, String s) {
            return false;
        }

        @Override
        public int compareTo(Object o) {
            return 0;
        }
    }

    private class MockServiceRegistration implements ServiceRegistration {

        private final String s;
        private final ServiceReference sr;

        private MockServiceRegistration(String s, ServiceReference sr) {
            this.s = s;
            this.sr = sr;
        }

        @Override
        public ServiceReference getReference() {
            return sr;
        }

        @Override
        public void setProperties(Dictionary dictionary) {
        }

        @Override
        public void unregister() {
            serviceReferences.remove(s);
        }
    }

}
