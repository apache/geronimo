/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.kernel.config;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;

/**
 * A MultiParentClassLoader is a simple extension of the URLClassLoader that simply changes the single parent class
 * loader model to support a list of parent class loaders.  Each operation that accesses a parent, has been replaced
 * with a operation that checks each parent in order.  This getParent method of this class will always return null,
 * which may be interperated by the calling code to mean that this class loader is a direct child of the system class
 * loader.
 * @version $Rev$ $Date$
 */
public class MultiParentClassLoader extends URLClassLoader {
    private final String name;
    private final ClassLoader[] parents;

    /**
     * Creates a named class loader with no parents.
     * @param name the name of this class loader
     * @param urls the urls from which this class loader will classes and resources
     */
    public MultiParentClassLoader(String name, URL[] urls) {
        super(urls);
        this.name = name;
        parents = new ClassLoader[0];
    }

    /**
     * Creates a named class loader as a child of the specified parent.
     * @param name the name of this class loader
     * @param urls the urls from which this class loader will classes and resources
     * @param parent the parent of this class loader
     */
    public MultiParentClassLoader(String name, URL[] urls, ClassLoader parent) {
        this(name, urls, new ClassLoader[] {parent});
    }

    /**
     * Creates a named class loader as a child of the specified parent and using the specified URLStreamHandlerFactory
     * for accessing the urls..
     * @param name the name of this class loader
     * @param urls the urls from which this class loader will classes and resources
     * @param parent the parent of this class loader
     * @param factory the URLStreamHandlerFactory used to access the urls
     */
    public MultiParentClassLoader(String name, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        this(name, urls, new ClassLoader[] {parent}, factory);
    }

    /**
     * Creates a named class loader as a child of the specified parents.
     * @param name the name of this class loader
     * @param urls the urls from which this class loader will classes and resources
     * @param parents the parents of this class loader
     */
    public MultiParentClassLoader(String name, URL[] urls, ClassLoader[] parents) {
        super(urls);
        this.name = name;
        this.parents = copyParents(parents);
    }

    /**
     * Creates a named class loader as a child of the specified parents and using the specified URLStreamHandlerFactory
     * for accessing the urls..
     * @param name the name of this class loader
     * @param urls the urls from which this class loader will classes and resources
     * @param parents the parents of this class loader
     * @param factory the URLStreamHandlerFactory used to access the urls
     */
    public MultiParentClassLoader(String name, URL[] urls, ClassLoader[] parents, URLStreamHandlerFactory factory) {
        super(urls, null, factory);
        this.name = name;
        this.parents = copyParents(parents);
    }

    private static ClassLoader[] copyParents(ClassLoader[] parents) {
        ClassLoader[] newParentsArray = new ClassLoader[parents.length];
        for (int i = 0; i < parents.length; i++) {
            ClassLoader parent = parents[i];
            if (parent == null) {
                throw new NullPointerException("parent[" + i + "] is null");
            }
            newParentsArray[i] = parent;
        }
        return newParentsArray;
    }

    /**
     * Gets the name of this class loader.
     * @return the name of this class loader
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the parents of this class loader.
     * @return the parents of this class loader
     */
    public ClassLoader[] getParents() {
        return parents;
    }

    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = findLoadedClass(name);
        for (int i = 0; i < parents.length && clazz == null; i++) {
            ClassLoader parent = parents[i];
            try {
                clazz = parent.loadClass(name);
            } catch (ClassNotFoundException ignored) {
                // this parent didn't have the class; try the next one
            }
        }

        if (clazz == null) {
            // parents didn't have the class; attempt to load from my urls
            return super.loadClass(name, resolve);
        } else {
            // we found the class; resolve it if requested
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }
    }

    public URL getResource(String name) {
        URL url = null;
        for (int i = 0; i < parents.length && url == null; i++) {
            ClassLoader parent = parents[i];
            url = parent.getResource(name);
        }

        if (url == null) {
            // parents didn't have the resource; attempt to load it from my urls
            return super.getResource(name);
        } else {
            return url;
        }
    }

    public Enumeration findResources(String name) throws IOException {
        List resources = new ArrayList();

        // Add resources from all parents
        for (int i = 0; i < parents.length; i++) {
            ClassLoader parent = parents[i];
            List parentResources = Collections.list(parent.getResources(name));
            resources.addAll(parentResources);
        }

        // Add the resources from my urls
        List myResources = Collections.list(super.findResources(name));
        resources.addAll(myResources);

        // return an enumeration over the list
        return Collections.enumeration(resources);
    }

    public String toString() {
        return "[" + getClass().getName() + " name=" + name + "]";
    }
}
