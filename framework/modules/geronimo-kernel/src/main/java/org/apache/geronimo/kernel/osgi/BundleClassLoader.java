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
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

/**
 * @version $Rev$ $Date$
 */
public class BundleClassLoader extends ClassLoader {

    private final Bundle bundle;

    public BundleClassLoader(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = bundle.loadClass(name);
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    @Override
    public String toString() {
        return "[BundleClassLoader] " + bundle;
    }

    @Override
	public URL getResource(String name) {
        return bundle.getResource(name);
	}

	@SuppressWarnings("unchecked")
    @Override
	public Enumeration<URL> getResources(String name) throws IOException {
	    Enumeration<URL> e = (Enumeration<URL>)bundle.getResources(name);
	    if (e == null) {
	        return Collections.enumeration(Collections.EMPTY_LIST);
	    } else {
	        return e;
	    }
	}

    /**
     * Return the bundle instance backing this classloader.
     *
     * @return The bundle used to source the classloader.
     */
    public Bundle getBundle() {
        return bundle;
    }
}
