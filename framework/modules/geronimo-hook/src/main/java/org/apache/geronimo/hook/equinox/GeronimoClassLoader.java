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
package org.apache.geronimo.hook.equinox;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.eclipse.osgi.framework.internal.core.AbstractBundle;
import org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader;
import org.eclipse.osgi.service.urlconversion.URLConverter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class GeronimoClassLoader extends URLClassLoader implements BaseClassLoader {

    private static final String PREFIX = "org.apache.xbean.osgi.bundle.util.BundleResourceHelper";
    private static final String SEARCH_WIRED_BUNDLES = PREFIX + ".searchWiredBundles";
    private static final String CONVERT_RESOURCE_URLS = PREFIX + ".convertResourceUrls";

    private final static String META_INF_1 = "META-INF/";
    private final static String META_INF_2 = "/META-INF/";

    private final ClassLoaderHook hook;
    private final ClassLoaderDelegate delegate;
    private final ProtectionDomain domain;
    private final AbstractBundle bundle;
    private final ClasspathManager manager;

    private LinkedHashSet<Bundle> wiredBundles = null;
    private boolean searchWiredBundles = getSearchWiredBundles(true);
    private boolean convertResourceUrls = getConvertResourceUrls(true);

    private URLConverter converter;

    public GeronimoClassLoader(ClassLoaderHook hook,
                               ClassLoader parent,
                               ClassLoaderDelegate delegate,
                               ProtectionDomain domain,
                               BaseData bundledata,
                               String[] classpath,
                               AbstractBundle bundle) {
        super(new URL[] {}, parent);
        this.hook = hook;
        this.delegate = delegate;
        this.domain = domain;
        this.bundle = bundle;

        this.manager = new ClasspathManager(bundledata, classpath, this);
    }

    @Override
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = delegate.findClass(name);
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    @Override
    public URL getResource(String name) {
        URL resource = delegate.findResource(name);
        if (resource == null && isMetaInfResource(name)) {
            LinkedHashSet<Bundle> wiredBundles = getWiredBundles();
            Iterator<Bundle> iterator = wiredBundles.iterator();
            while (iterator.hasNext() && resource == null) {
                resource = iterator.next().getResource(name);
            }
        }
        if (resource != null && converter != null) {
            return convert(resource);
        } else {
            return resource;
        }
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL> e = delegate.findResources(name);
        if (isMetaInfResource(name)) {
            List<URL> allResources = getList();
            addToList(allResources, e);
            LinkedHashSet<Bundle> wiredBundles = getWiredBundles();
            for (Bundle wiredBundle : wiredBundles) {
                Enumeration<URL> resources = wiredBundle.getResources(name);
                addToList(allResources, resources);
            }
            return Collections.enumeration(allResources);
        } else if (e == null) {
            return Collections.enumeration(Collections.<URL>emptyList());
        } else {
            List<URL> allResources = getList();
            addToList(allResources, e);
            return Collections.enumeration(allResources);
        }
    }

    public void setSearchWiredBundles(boolean search) {
        searchWiredBundles = search;
    }

    public boolean getSearchWiredBundles() {
        return searchWiredBundles;
    }

    private synchronized LinkedHashSet<Bundle> getWiredBundles() {
        if (wiredBundles == null) {
            wiredBundles = BundleUtils.getWiredBundles(bundle);
        }
        return wiredBundles;
    }

    private boolean isMetaInfResource(String name) {
        return searchWiredBundles && name != null && (name.startsWith(META_INF_1) || name.startsWith(META_INF_2));
    }

    private List<URL> getList() {
        if (converter == null) {
            return new ArrayList<URL>();
        } else {
            return new ArrayList<URL>() {
                public boolean add(URL u) {
                    return super.add(convert(u));
                }
            };
        }
    }

    private void addToList(List<URL> list, Enumeration<URL> enumeration) {
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                list.add(enumeration.nextElement());
            }
        }
    }

    private URL convert(URL url) {
        try {
            URL convertedURL = converter.resolve(url);
            return convertedURL;
        } catch (IOException e) {
            e.printStackTrace();
            return url;
        }
    }

    public void initialize() {
        manager.initialize();
        if (convertResourceUrls) {
            BundleContext context = bundle.getBundleContext();
            if (context != null) {
                ServiceReference urlReference = context.getServiceReference(URLConverter.class.getName());
                if (urlReference != null) {
                    converter = (URLConverter) context.getService(urlReference);
                }
            }
        }
    }

    public void close() {
        manager.close();
    }

    // other BaseClassLoader methods

    public ProtectionDomain getDomain() {
        return domain;
    }

    @Override
    protected String findLibrary(String libname) {
        // let the manager find the library for us
        return manager.findLibrary(libname);
    }

    public ClasspathEntry createClassPathEntry(BundleFile bundlefile, ProtectionDomain cpDomain) {
        return new ClasspathEntry(bundlefile, DefaultClassLoader.createProtectionDomain(bundlefile, cpDomain));
    }

    public Class defineClass(String name, byte[] classbytes, ClasspathEntry classpathEntry, BundleEntry entry) {
        return defineClass(name, classbytes, 0, classbytes.length, classpathEntry.getDomain());
    }

    public Class publicFindLoaded(String classname) {
        return findLoadedClass(classname);
    }

    public Object publicGetPackage(String pkgname) {
        return getPackage(pkgname);
    }

    public Object publicDefinePackage(String name, String specTitle, String specVersion, String specVendor, String implTitle, String implVersion, String implVendor, URL sealBase) {
        return definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
    }

    public URL findLocalResource(String resource) {
        return manager.findLocalResource(resource);
    }

    public Enumeration findLocalResources(String resource) {
        return manager.findLocalResources(resource);
    }

    public Class findLocalClass(String classname) throws ClassNotFoundException {
        return manager.findLocalClass(classname);
    }

    public void attachFragment(BundleData sourcedata, ProtectionDomain sourcedomain, String[] sourceclasspath) {
        manager.attachFragment(sourcedata, sourcedomain, sourceclasspath);
    }

    public ClassLoaderDelegate getDelegate() {
        return delegate;
    }

    public List<URL> findEntries(String path, String filePattern, int options) {
        return manager.findEntries(path, filePattern, options);
    }

    public Collection<String> listResources(String path, String filePattern, int options) {
        return delegate.listResources(path, filePattern, options);
    }

    public Collection<String> listLocalResources(String path, String filePattern, int options) {
        return manager.listLocalResources(path, filePattern, options);
    }

    public ClasspathManager getClasspathManager() {
        return manager;
    }

    public Bundle getBundle() {
        return manager.getBaseData().getBundle();
    }

    private static boolean getSearchWiredBundles(boolean defaultValue) {
        String value = System.getProperty(SEARCH_WIRED_BUNDLES);
        return (value == null) ? defaultValue : Boolean.parseBoolean(value);
    }

    private static boolean getConvertResourceUrls(boolean defaultValue) {
        String value = System.getProperty(CONVERT_RESOURCE_URLS);
        return (value == null) ? defaultValue : Boolean.parseBoolean(value);
    }
}
