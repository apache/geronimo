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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.FileUtils;
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
    private int state = 2;

    public MockBundle(ClassLoader classLoader, String location, long id) {
        this.classLoader = classLoader;
        if (location != null && location.endsWith("/")) {
            this.location = location.substring(0, location.length() - 1);
        } else {
            this.location = location;
        }
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void start(int i) throws BundleException {
        state = 32;
    }

    public void start() throws BundleException {
        state = 32;
    }

    public void stop(int i) throws BundleException {
        state = 2;
    }

    public void stop() throws BundleException {
        state = 2;
    }

    public void update() throws BundleException {
    }

    public void update(InputStream inputStream) throws BundleException {
    }

    public void uninstall() throws BundleException {
    }

    public Dictionary getHeaders() {
        return new Hashtable();
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
        return location;
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
        if (s.startsWith("/")) {
            s = s.substring(1);
        }
        try {
            return new URL(location + "/" + s);
        } catch (MalformedURLException e) {
            try {
                return new File(location + File.separator + s).toURI().toURL();
            } catch (MalformedURLException e1) {
               throw new RuntimeException(e1);
            }
        }
    }


    public long getLastModified() {
        return 0;
    }

    public Enumeration findEntries(String path, String pattern, boolean b) {
        File base = getLocationFile();
        if (base == null) {
            return null;
        }
        String filePattern = path;
        if (!filePattern.endsWith("/")) {
            filePattern += "/";
        }
        if (pattern != null) {
            filePattern += pattern;
        }
        Set<URL> entries;
        try {
            entries = FileUtils.search(base, filePattern);
        } catch (MalformedURLException e) {
            entries = Collections.emptySet();
        }
        return Collections.enumeration(entries);
    }

    private File getLocationFile() {
        if (location == null) {
            return null;
        }
        File file = null;
        if (location.startsWith("file:")) {
            try {
                file = new File( (new URI(location)).getPath() );
            } catch (URISyntaxException e) {
                // ignore
            }
        } else {
            file = new File(location);
        }
        return file;
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

    @Override
    public <A> A adapt(Class<A> aClass) {
        return null;
    }

    @Override
    public File getDataFile(String s) {
        return null;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public int compareTo(Bundle bundle) {
        return 0;
    }
}
