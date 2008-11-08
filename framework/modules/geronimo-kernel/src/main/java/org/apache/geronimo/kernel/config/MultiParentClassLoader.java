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
package org.apache.geronimo.kernel.config;

import java.beans.Introspector;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.kernel.classloader.UnionEnumeration;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ClassLoadingRule;
import org.apache.geronimo.kernel.repository.ClassLoadingRules;
import org.apache.geronimo.kernel.util.ClassLoaderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A MultiParentClassLoader is a simple extension of the URLClassLoader that simply changes the single parent class
 * loader model to support a list of parent class loaders.  Each operation that accesses a parent, has been replaced
 * with a operation that checks each parent in order.  This getParent method of this class will always return null,
 * which may be interpreted by the calling code to mean that this class loader is a direct child of the system class
 * loader.
 *
 * @version $Rev$ $Date$
 */
public class MultiParentClassLoader extends URLClassLoader
{
    private static final Logger log = LoggerFactory.getLogger(MultiParentClassLoader.class);

    private final Artifact id;
    private final ClassLoader[] parents;
    private final ClassLoadingRules classLoadingRules;
    private boolean destroyed = false;

    // I used this pattern as its temporary and with the static final we get compile time 
    // optimizations.
    private final static int classLoaderSearchMode;
    private final static int ORIGINAL_SEARCH = 1;
    private final static int OPTIMIZED_SEARCH = 2;

    private final static boolean LONG_CLASSLOADER_TO_STRING = false;
     
    static {
    	// Extract the classLoaderSearchMode if specified.  If not, default to "safe".
    	String mode = System.getProperty("Xorg.apache.geronimo.kernel.config.MPCLSearchOption");
    	int runtimeMode = OPTIMIZED_SEARCH;  // Default to optimized
    	String runtimeModeMessage = "Original Classloading";
    	if (mode != null) { 
    		if (mode.equals("safe")) {
                runtimeMode = ORIGINAL_SEARCH;
                runtimeModeMessage = "Safe ClassLoading";
    		} else if (mode.equals("optimized"))
    			runtimeMode = OPTIMIZED_SEARCH;
    	}
    	
		classLoaderSearchMode = runtimeMode;
		LoggerFactory.getLogger(MultiParentClassLoader.class).info(
                 "ClassLoading behaviour has changed.  The "+runtimeModeMessage+" mode is in effect.  If you are experiencing a problem\n"+
				 "you can change the behaviour by specifying -DXorg.apache.geronimo.kernel.config.MPCLSearchOption= property.  Specify \n"+
				 "=\"safe\" to revert to the original behaviour.  This is a temporary change until we decide whether or not to make it\n"+
				 "permanent for the 2.0 release");
    }

    /**
     * Creates a named class loader with no parents.
     *
     * @param id   the id of this class loader
     * @param urls the urls from which this class loader will classes and resources
     */
    public MultiParentClassLoader(Artifact id, URL[] urls) {
        super(urls);
        this.id = id;
        
        parents = new ClassLoader[]{ClassLoader.getSystemClassLoader()};
        classLoadingRules = new ClassLoadingRules();
        ClassLoaderRegistry.add(this);
    }


    /**
     * Creates a named class loader as a child of the specified parent.
     *
     * @param id     the id of this class loader
     * @param urls   the urls from which this class loader will classes and resources
     * @param parent the parent of this class loader
     */
    public MultiParentClassLoader(Artifact id, URL[] urls, ClassLoader parent) {
        this(id, urls, new ClassLoader[]{parent});
    }

    public MultiParentClassLoader(Artifact id, URL[] urls, ClassLoader parent, ClassLoadingRules classLoadingRules) {
        this(id, urls, new ClassLoader[]{parent}, classLoadingRules);
    }

    /**
     * Creates a named class loader as a child of the specified parent and using the specified URLStreamHandlerFactory
     * for accessing the urls..
     *
     * @param id      the id of this class loader
     * @param urls    the urls from which this class loader will classes and resources
     * @param parent  the parent of this class loader
     * @param factory the URLStreamHandlerFactory used to access the urls
     */
    public MultiParentClassLoader(Artifact id, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        this(id, urls, new ClassLoader[]{parent}, factory);
    }

    /**
     * Creates a named class loader as a child of the specified parents.
     *
     * @param id      the id of this class loader
     * @param urls    the urls from which this class loader will classes and resources
     * @param parents the parents of this class loader
     */
    public MultiParentClassLoader(Artifact id, URL[] urls, ClassLoader[] parents) {
        super(urls);
        this.id = id;
        this.parents = copyParents(parents);

        classLoadingRules = new ClassLoadingRules();
        ClassLoaderRegistry.add(this);
    }

    public MultiParentClassLoader(Artifact id, URL[] urls, ClassLoader[] parents, ClassLoadingRules classLoadingRules) {
        super(urls);
        this.id = id;
        this.parents = copyParents(parents);
        this.classLoadingRules = classLoadingRules;
        ClassLoaderRegistry.add(this);
    }

    public MultiParentClassLoader(MultiParentClassLoader source) {
        this(source.id, source.getURLs(), deepCopyParents(source.parents), source.classLoadingRules);
    }

    static ClassLoader copy(ClassLoader source) {
        if (source instanceof MultiParentClassLoader) {
            return new MultiParentClassLoader((MultiParentClassLoader) source);
        } else if (source instanceof URLClassLoader) {
            return new URLClassLoader(((URLClassLoader) source).getURLs(), source.getParent());
        } else {
            return new URLClassLoader(new URL[0], source);
        }
    }

    ClassLoader copy() {
        return MultiParentClassLoader.copy(this);
    }

    /**
     * Creates a named class loader as a child of the specified parents and using the specified URLStreamHandlerFactory
     * for accessing the urls..
     *
     * @param id      the id of this class loader
     * @param urls    the urls from which this class loader will classes and resources
     * @param parents the parents of this class loader
     * @param factory the URLStreamHandlerFactory used to access the urls
     */
    public MultiParentClassLoader(Artifact id, URL[] urls, ClassLoader[] parents, URLStreamHandlerFactory factory) {
        super(urls, null, factory);
        this.id = id;
        this.parents = copyParents(parents);
        
        classLoadingRules = new ClassLoadingRules();
        ClassLoaderRegistry.add(this);
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

    private static ClassLoader[] deepCopyParents(ClassLoader[] parents) {
        ClassLoader[] newParentsArray = new ClassLoader[parents.length];
        for (int i = 0; i < parents.length; i++) {
            ClassLoader parent = parents[i];
            if (parent == null) {
                throw new NullPointerException("parent[" + i + "] is null");
            }
            if (parent instanceof MultiParentClassLoader) {
                parent = ((MultiParentClassLoader) parent).copy();
            }
            newParentsArray[i] = parent;
        }
        return newParentsArray;
    }

    /**
     * Gets the id of this class loader.
     *
     * @return the id of this class loader
     */
    public Artifact getId() {
        return id;
    }

    /**
     * Gets the parents of this class loader.
     *
     * @return the parents of this class loader
     */
    public ClassLoader[] getParents() {
        return parents;
    }

    public void addURL(URL url) {
        // todo this needs a security check
        super.addURL(url);
    }

    /**
     * TODO This method should be removed and replaced with the best classLoading option.  Its intent is to 
     * provide a way for folks to switch back to the old classLoader if this fix breaks something.
     */
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    	if (classLoaderSearchMode == ORIGINAL_SEARCH) 
    		return loadSafeClass(name, resolve);
    	else
    		return loadOptimizedClass(name, resolve);
    }
    
    /**
     * This method executes the old class loading behaviour before optimization.
     * 
     * @param name
     * @param resolve
     * @return
     * @throws ClassNotFoundException
     */
    protected synchronized Class<?> loadSafeClass(String name, boolean resolve) throws ClassNotFoundException {
        //
        // Check if class is in the loaded classes cache
        //
        Class cachedClass = findLoadedClass(name);
        if (cachedClass != null) {
            return resolveClass(cachedClass, resolve);
        }

        // This is a reasonable hack.  We can add some classes to the list below.
        // Since we know these classes are in the system class loader let's not waste our
        // time going through the hierarchy.
        //
        // The order is based on profiling the server.  It may not be optimal for all
        // workloads.

        if (name.startsWith("java.") ||
                name.equals("boolean") ||
                name.equals("int") ||
                name.equals("double") ||
                name.equals("long")) {
            Class clazz = ClassLoader.getSystemClassLoader().loadClass(name);
            return resolveClass(clazz, resolve);
        }

        //
        // if we are using inverse class loading, check local urls first
        //
        if (classLoadingRules.isInverseClassLoading() && !isDestroyed() && !isNonOverridableClass(name)) {
            try {
                Class clazz = findClass(name);
                return resolveClass(clazz, resolve);
            } catch (ClassNotFoundException ignored) {
            }
        }

        //
        // Check parent class loaders
        //
        if (!isHiddenClass(name)) {
            for (ClassLoader parent : parents) {
                try {
                    Class clazz = parent.loadClass(name);
                    return resolveClass(clazz, resolve);
                } catch (ClassNotFoundException ignored) {
                    // this parent didn't have the class; try the next one
                }
            }
        }

        //
        // if we are not using inverse class loading, check local urls now
        //
        // don't worry about excluding non-overridable classes here... we
        // have alredy checked he parent and the parent didn't have the
        // class, so we can override now
        if (!isDestroyed()) {
            try {
                Class clazz = findClass(name);
                return resolveClass(clazz, resolve);
            } catch (ClassNotFoundException ignored) {
            }
        }

        throw new ClassNotFoundException(name + " in classloader " + id);
    }    
    
    /**
     * 
     * Optimized classloading.
     * 
     * This method is the normal way to resolve class loads.  This method recursively calls its parents to resolve 
     * classloading requests.  Here is the sequence of operations:
     * 
     *   1. Call findLoadedClass to see if we already have this class loaded.
     *   2. If this class is a java.* or data primitive class, call the SystemClassLoader.
     *   3. If inverse loading and class is not in the non-overridable list, check the local ClassLoader.
     *   4. If the class is not a hidden class, search our parents, recursively.  Keeping track of which parents have already been called.
     *      Since MultiParentClassLoaders can appear more than once we do not search an already searched ClassLoader.
     *   5. Finally, search this ClassLoader.  
     * 
     */
    protected synchronized Class<?> loadOptimizedClass(String name, boolean resolve) throws ClassNotFoundException {

        //
        // Check if class is in the loaded classes cache
        //
        Class cachedClass = findLoadedClass(name);
        if (cachedClass != null) {
            return resolveClass(cachedClass, resolve);
        }

        //
        // If this is a java.* or primitive class, use the primordial ClassLoader...
        //
        // The order is based on profiling the server.  It may not be optimal for all
        // workloads.
        if (name.startsWith("java.") ||
                name.equals("boolean") ||
                name.equals("int") ||
                name.equals("double") ||
                name.equals("long")) {
            try {
        	    return resolveClass(findSystemClass(name), resolve);
            } catch (ClassNotFoundException cnfe) {
                // ignore...just being a good citizen.
            }
        }

        //
        // if we are using inverse class loading, check local urls first
        //
        if (classLoadingRules.isInverseClassLoading() && !isDestroyed() && !isNonOverridableClass(name)) {
            try {
                Class clazz = findClass(name);
                return resolveClass(clazz, resolve);
            } catch (ClassNotFoundException ignored) {
            }
        }
        
        //
        // Check parent class loaders
        //
        if (!isHiddenClass(name)) {
       		try {
       			LinkedList<ClassLoader> visitedClassLoaders = new LinkedList<ClassLoader>();
                Class clazz = checkParents(name, resolve, visitedClassLoaders);
                if (clazz != null) return resolveClass(clazz, resolve);
      		} catch (ClassNotFoundException cnfe) {
        			// ignore
       		}
        }

        //
        // if we are not using inverse class loading, check local urls now
        //
        // don't worry about excluding non-overridable classes here... we
        // have alredy checked he parent and the parent didn't have the
        // class, so we can override now
        if (!isDestroyed()) {
            try {
                Class clazz = findClass(name);
                return resolveClass(clazz, resolve);
            } catch (ClassNotFoundException ignored) {
            }
        }

        throw new ClassNotFoundException(name + " in classloader " + id);
    }
    
    /**
     * This method is an internal hook that allows us to be performant on Class lookups when multiparent
     * classloaders are involved.  We can bypass certain lookups that have already occurred in the initiating
     * classloader.  Also, we track the classLoaders that are visited by adding them to an already vistied list.
     * In this way, we can bypass redundant checks for the same class.
     * 
     * @param name
     * @param visitedClassLoaders
     * @return
     * @throws ClassNotFoundException
     */
    protected synchronized Class<?> loadClassInternal(String name, boolean resolve, List<ClassLoader> visitedClassLoaders) throws ClassNotFoundException, MalformedURLException {
        //
        // Check if class is in the loaded classes cache
        //
        Class cachedClass = findLoadedClass(name);
        if (cachedClass != null) {
            return resolveClass(cachedClass, resolve);
        }

        //
        // Check parent class loaders
        //
        if (!isHiddenClass(name)) {
            try {
        	    Class clazz = checkParents(name, resolve, visitedClassLoaders);
        	    if (clazz != null) return resolveClass(clazz,resolve);
            } catch (ClassNotFoundException cnfe) {
        	    // ignore
            }
        }
        
        //
        // if we are not using inverse class loading, check local urls now
        //
        // don't worry about excluding non-overridable classes here... we
        // have alredy checked he parent and the parent didn't have the
        // class, so we can override now
        if (!isDestroyed()) {
        	Class clazz = findClass(name);
            return resolveClass(clazz, resolve);
        }

        return null;  // Caller is expecting a class.  Null indicates CNFE and will save some time.
    }

    /**
     * In order to optimize the classLoading process and visit a directed set of 
     * classloaders this internal method for Geronimo MultiParentClassLoaders 
     * is used.  Effectively, as each classloader is visited it is passed a linked
     * list of classloaders that have already been visited and can safely be skipped.
     * This method assumes the context of an MPCL and is not for use external to this class.
     * 
     * @param name
     * @param visitedClassLoaders
     * @return
     * @throws ClassNotFoundException
     */
    private synchronized Class<?> checkParents(String name, boolean resolve, List<ClassLoader> visitedClassLoaders) throws ClassNotFoundException {
        for (ClassLoader parent : parents) {
            if (!visitedClassLoaders.contains(parent)) {
                visitedClassLoaders.add(parent);  // Track that we've been here before
                try {
        	        if (parent instanceof MultiParentClassLoader) {
        	        	Class clazz = ((MultiParentClassLoader) parent).loadClassInternal(name, resolve, visitedClassLoaders);
        	        	if (clazz != null) return resolveClass(clazz, resolve);
        	        } else if (parent instanceof ChildrenConfigurationClassLoader) {
                        Class clazz = ((ChildrenConfigurationClassLoader) parent).loadClass(name, visitedClassLoaders);
                        if (clazz != null) return resolveClass(clazz, resolve);
        	        } else {
        	        	return parent.loadClass(name);
        	        }
    	    	} catch (ClassNotFoundException cnfe) {
                    // ignore
                } catch (MalformedURLException me) {
                    log.debug("Failed findClass for {}", name, me);
                }
            }
    	} 
     	// To avoid yet another CNFE we'll simply return null and let the caller handle appropriately.
    	return null;
    }

    private boolean isNonOverridableClass(String name) {
        ClassLoadingRule nonOverrideableRule = classLoadingRules.getNonOverrideableRule();
        return nonOverrideableRule.isFilteredClass(name);
    }

    private boolean isHiddenClass(String name) {
        ClassLoadingRule hiddenRule = classLoadingRules.getHiddenRule();
        return hiddenRule.isFilteredClass(name);
    }

    private Class resolveClass(Class clazz, boolean resolve) {
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    public URL getResource(String name) {
        if (isDestroyed()) {
            return null;
        }

        //
        // if we are using inverse class loading, check local urls first
        //
        if (classLoadingRules.isInverseClassLoading() && !isDestroyed() && !isNonOverridableResource(name)) {
            URL url = findResource(name);
            if (url != null) {
                return url;
            }
        }

        //
        // Check parent class loaders
        //
        if (!isHiddenResource(name)) {
            for (ClassLoader parent : parents) {
                URL url = parent.getResource(name);
                if (url != null) {
                    return url;
                }
            }
        }

        //
        // if we are not using inverse class loading, check local urls now
        //
        // don't worry about excluding non-overridable resources here... we
        // have alredy checked he parent and the parent didn't have the
        // resource, so we can override now
        if (!isDestroyed()) {
            // parents didn't have the resource; attempt to load it from my urls
            return findResource(name);
        }

        return null;
    }

    public Enumeration<URL> findResources(String name) throws IOException {
        if (isDestroyed()) {
            return Collections.enumeration(Collections.EMPTY_SET);
        }

        Set<ClassLoader> knownClassloaders = new HashSet<ClassLoader>();
        List<Enumeration<URL>> enumerations = new ArrayList<Enumeration<URL>>();

        recursiveFind(knownClassloaders, enumerations, name);

        return new UnionEnumeration<URL>(enumerations);
    }

    protected void recursiveFind(Set<ClassLoader> knownClassloaders, List<Enumeration<URL>> enumerations, String name) throws IOException {
        if (isDestroyed() || knownClassloaders.contains(this)) {
            return;
        }
        knownClassloaders.add(this);
        if (classLoadingRules.isInverseClassLoading() && !isNonOverridableResource(name)) {
            enumerations.add(internalfindResources(name));
        }
        if (!isHiddenResource(name)) {
            for (ClassLoader parent : parents) {
                if (parent instanceof MultiParentClassLoader) {
                    ((MultiParentClassLoader) parent).recursiveFind(knownClassloaders, enumerations, name);
                } else {
                    if (!knownClassloaders.contains(parent)) {
                        enumerations.add(parent.getResources(name));
                        knownClassloaders.add(parent);
                    }
                }
            }
        }
        if (!classLoadingRules.isInverseClassLoading()) {
            enumerations.add(internalfindResources(name));
        }
    }

    protected Enumeration<URL> internalfindResources(String name) throws IOException {
        return super.findResources(name);
    }

    private boolean isNonOverridableResource(String name) {
        ClassLoadingRule nonOverrideableRule = classLoadingRules.getNonOverrideableRule();
        return nonOverrideableRule.isFilteredResource(name);
    }

    private boolean isHiddenResource(String name) {
        ClassLoadingRule hiddenRule = classLoadingRules.getHiddenRule();
        return hiddenRule.isFilteredResource(name);
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        if (LONG_CLASSLOADER_TO_STRING) {
            toBuilder(b, "");
        } else {
            b.append("[").append(getClass().getName()).append(" id=").append(id).append("]");
        }
        return b.toString();
    }

    private void toBuilder(StringBuilder b, String indent) {
        b.append(indent).append("[").append(getClass().getName()).append(" id=").append(id).append("]\n");
        b.append(indent).append("urls:\n");
        String newIndent = indent + "  ";
        for (URL url: getURLs()) {
            b.append(newIndent).append(url).append("\n");
        }
        b.append(indent).append("parents:\n");

        for (ClassLoader cl: parents) {
            if (cl instanceof MultiParentClassLoader) {
                ((MultiParentClassLoader)cl).toBuilder(b, newIndent);
            } else {
                b.append(newIndent).append(cl.toString()).append("\n");
            }
        }
    }

    public synchronized boolean isDestroyed() {
        return destroyed;
    }

    public void destroy() {
        synchronized (this) {
            if (destroyed) return;
            destroyed = true;
        }

        clearSoftCache(ObjectInputStream.class, "subclassAudits");
        clearSoftCache(ObjectOutputStream.class, "subclassAudits");
        clearSoftCache(ObjectStreamClass.class, "localDescs");
        clearSoftCache(ObjectStreamClass.class, "reflectors");

        // The beanInfoCache in java.beans.Introspector will hold on to Classes which
        // it has introspected. If we don't flush the cache, we may run out of
        // Permanent Generation space.
        Introspector.flushCaches();

        ClassLoaderRegistry.remove(this);
    }

    private static final Object lock = new Object();
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
                    LoggerFactory.getLogger(MultiParentClassLoader.class).debug("Unable to clear SoftCache field {} in class {}", fieldName, clazz);
                }
            }
        }

        if (cache != null) {
            synchronized (cache) {
                cache.clear();
            }
        }
    }

    protected void finalize() throws Throwable {
        ClassLoaderRegistry.remove(this);
        super.finalize();
    }
    
}
