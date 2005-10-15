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
import java.net.URI;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;

import javax.print.attribute.SupportedValuesAttribute;

import org.apache.commons.logging.LogFactory;

/**
 * A MultiParentClassLoader is a simple extension of the URLClassLoader that simply changes the single parent class
 * loader model to support a list of parent class loaders.  Each operation that accesses a parent, has been replaced
 * with a operation that checks each parent in order.  This getParent method of this class will always return null,
 * which may be interpreted by the calling code to mean that this class loader is a direct child of the system class
 * loader.
 * @version $Rev$ $Date$
 */
public class MultiParentClassLoader extends URLClassLoader {
    private final URI id;
    private final ClassLoader[] parents;
    private final boolean inverseClassLoading;
    private final String[] hiddenClasses;
    private final String[] nonOverridableClasses;

    /**
     * Creates a named class loader with no parents.
     * @param id the id of this class loader
     * @param urls the urls from which this class loader will classes and resources
     */
    public MultiParentClassLoader(URI id, URL[] urls) {
        super(urls);
        this.id = id;
        parents = new ClassLoader[0];
        inverseClassLoading = false;
        hiddenClasses = new String[0];
        nonOverridableClasses = new String[0];
    }

    /**
     * Creates a named class loader as a child of the specified parent.
     * @param id the id of this class loader
     * @param urls the urls from which this class loader will classes and resources
     * @param parent the parent of this class loader
     */
    public MultiParentClassLoader(URI id, URL[] urls, ClassLoader parent) {
        this(id, urls, new ClassLoader[] {parent});
    }

    public MultiParentClassLoader(URI id, URL[] urls, ClassLoader parent, boolean inverseClassLoading, String[] hiddenClasses, String[] nonOverridableClasses) {
        this(id, urls, new ClassLoader[] {parent}, inverseClassLoading, hiddenClasses, nonOverridableClasses);
    }

    /**
     * Creates a named class loader as a child of the specified parent and using the specified URLStreamHandlerFactory
     * for accessing the urls..
     * @param id the id of this class loader
     * @param urls the urls from which this class loader will classes and resources
     * @param parent the parent of this class loader
     * @param factory the URLStreamHandlerFactory used to access the urls
     */
    public MultiParentClassLoader(URI id, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        this(id, urls, new ClassLoader[] {parent}, factory);
    }

    /**
     * Creates a named class loader as a child of the specified parents.
     * @param id the id of this class loader
     * @param urls the urls from which this class loader will classes and resources
     * @param parents the parents of this class loader
     */
    public MultiParentClassLoader(URI id, URL[] urls, ClassLoader[] parents) {
        super(urls);
        this.id = id;
        this.parents = copyParents(parents);
        inverseClassLoading = false;
        hiddenClasses = new String[0];
        nonOverridableClasses = new String[0];
    }

    public MultiParentClassLoader(URI id, URL[] urls, ClassLoader[] parents, boolean inverseClassLoading, String[] hiddenClasses, String[] nonOverridableClasses) {
        super(urls, new FilteringParentCL(hiddenClasses));
        this.id = id;
        this.parents = copyParents(parents);
        this.inverseClassLoading = inverseClassLoading;
        this.hiddenClasses = hiddenClasses;
        this.nonOverridableClasses = nonOverridableClasses;
    }

    /**
     * Creates a named class loader as a child of the specified parents and using the specified URLStreamHandlerFactory
     * for accessing the urls..
     * @param id the id of this class loader
     * @param urls the urls from which this class loader will classes and resources
     * @param parents the parents of this class loader
     * @param factory the URLStreamHandlerFactory used to access the urls
     */
    public MultiParentClassLoader(URI id, URL[] urls, ClassLoader[] parents, URLStreamHandlerFactory factory) {
        super(urls, null, factory);
        this.id = id;
        this.parents = copyParents(parents);
        inverseClassLoading = false;
        hiddenClasses = new String[0];
        nonOverridableClasses = new String[0];
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
     * Gets the id of this class loader.
     * @return the id of this class loader
     */
    public URI getId() {
        return id;
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

        if (null != clazz) {
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }

        if (inverseClassLoading) {
            boolean checkMe = true;
            for (int i = 0; i < nonOverridableClasses.length && checkMe; i++) {
                if (name.startsWith(nonOverridableClasses[i])) {
                    checkMe = false;
                }
            }

            if (checkMe) {
                try {
                    clazz = findClass(name);
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                } catch (ClassNotFoundException ignored) {
                }
            }
        }

        boolean checkParents = true;
        for (int i = 0; i < hiddenClasses.length && checkParents; i++) {
            if (name.startsWith(hiddenClasses[i])) {
                checkParents = false;
            }
        }
        if (checkParents) {
            for (int i = 0; i < parents.length && clazz == null; i++) {
                ClassLoader parent = parents[i];
                try {
                    clazz = parent.loadClass(name);
                } catch (ClassNotFoundException ignored) {
                    // this parent didn't have the class; try the next one
                }
            }
        }
        
        if (null == clazz) {
            return super.loadClass(name, resolve);
        }
        
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    public URL getResource(String name) {
        URL url = null;
        
        if (inverseClassLoading) {
            url = super.getResource(name);
            if (null != url) {
                return url;
            }
        }
        
        for (int i = 0; i < parents.length && url == null; i++) {
            ClassLoader parent = parents[i];
            url = parent.getResource(name);
        }

        if (url == null && false == inverseClassLoading) {
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
        return "[" + getClass().getName() + " id=" + id + "]";
    }

    public void destroy() {
        LogFactory.release(this);
        clearSoftCache(ObjectInputStream.class, "subclassAudits");
        clearSoftCache(ObjectOutputStream.class, "subclassAudits");
        clearSoftCache(ObjectStreamClass.class, "localDescs");
        clearSoftCache(ObjectStreamClass.class, "reflectors");
    }

    private static class FilteringParentCL extends ClassLoader {
        private final String[] hiddenClasses;

        public FilteringParentCL(String[] hiddenClasses) {
            this.hiddenClasses = hiddenClasses;
        }
        
        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            boolean checkParents = true;
            for (int i = 0; i < hiddenClasses.length && checkParents; i++) {
                if (name.startsWith(hiddenClasses[i])) {
                    checkParents = false;
                }
            }
            
            if (checkParents) {
                return super.loadClass(name, resolve);
            }

            throw new ClassNotFoundException(name);
        }
    }
    
    private static Object lock = new Object();
    private static boolean clearSoftCacheFailed = false;
    private static void clearSoftCache(Class clazz, String fieldName) {
        Map cache = null;
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            cache = (Map) f.get(null);
        } catch (Throwable e) {
            synchronized (lock) {
                if (!clearSoftCacheFailed) {
                    clearSoftCacheFailed = true;
                    LogFactory.getLog(ConfigurationClassLoader.class).error("Unable to clear SoftCache field " + fieldName + " in class " + clazz);
                }
            }
        }

        if (cache != null) {
            synchronized (cache) {
                cache.clear();
            }
        }
    }
    
}
