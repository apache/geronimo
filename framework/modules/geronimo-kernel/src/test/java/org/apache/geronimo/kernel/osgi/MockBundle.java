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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Artifact;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * @version $Rev$ $Date$
 */
public class MockBundle implements Bundle {

    private final ClassLoader classLoader;
    private final String location;
    private final long id;
    private BundleContext bundleContext;

    public MockBundle(ClassLoader classLoader, String location, long id) {
        this.classLoader = classLoader;
        this.location = location;
        this.id = id;
    }

    public int getState() {
        return 0;
    }

    public void start(int i) throws BundleException {
    }

    public void start() throws BundleException {
    }

    public void stop(int i) throws BundleException {
    }

    public void stop() throws BundleException {
    }

    public void update() throws BundleException {
    }

    public void update(InputStream inputStream) throws BundleException {
    }

    public void uninstall() throws BundleException {
    }

    public Dictionary getHeaders() {
        return null;
    }

    public long getBundleId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public ServiceReference[] getRegisteredServices() {
        return new ServiceReference[0];
    }

    public ServiceReference[] getServicesInUse() {
        return new ServiceReference[0];
    }

    public boolean hasPermission(Object o) {
        return true;
    }

    public URL getResource(String s) {
        return classLoader.getResource(s);
    }

    public Dictionary getHeaders(String s) {
        return null;
    }

    public String getSymbolicName() {
        return null;
    }

    public Class loadClass(String s) throws ClassNotFoundException {
        return classLoader.loadClass(s);
    }

    public Enumeration getResources(String s) throws IOException {
        return classLoader.getResources(s);
    }

    public Enumeration getEntryPaths(String s) {
        return new Vector<String>().elements();
    }

    public URL getEntry(String s) {
        return null;
    }

    public long getLastModified() {
        return 0;
    }

    public Enumeration findEntries(String s, String s1, boolean b) {
        return null;
    }

    public BundleContext getBundleContext() {
        // if no bundle context was provided, just give an empty Mock one
        if (bundleContext == null) {
            bundleContext = new MockBundleContext(classLoader, location, new HashMap<Artifact, ConfigurationData>(), null);
        }
        return bundleContext;
    }

    public Map getSignerCertificates(int signersType) {
        return null;
    }

    public Version getVersion() {
        return null;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
