/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.kernel.classspace;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;

/**
 * This is the default implementation of ClassSpace used in Geronimo.
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/27 20:58:38 $
 */
public class DefaultClassSpace implements ClassSpace, GeronimoMBeanTarget {
    private static final SortedSet EMPTY_SORTED_SET = Collections.unmodifiableSortedSet(new TreeSet());

    /**
     * Is this class space running?
     */
    private boolean running = false;

    /**
     * Current parent of this class space.
     * @todo remove when we have a endpoints
     */
    private ObjectName currentParent;

    /**
     * Parent of this class space.
     * @todo make an endpoint
     */
    private ObjectName parent;

    /**
     * Runtime context of the mbean in the server.
     */
    private GeronimoMBeanContext context;

    /**
     * Deployments currently handled by this class space.
     * This may contain more deployments then the urlsByDeployment because
     * URLs can not be removed until the class space restarts.
     */
    private LinkedHashMap currentURLsByDeployment;

    /**
     * Deployments that should be handled by this space.  This may
     * contain less deployments then the currentURLsByDeployment because
     * URLs can not be remove until the class space restarts
     */
    private final LinkedHashMap urlsByDeployment = new LinkedHashMap();

    /**
     * The class loader used by this instance
     */
    private ClassSpaceLoader classLoader;

    public synchronized ObjectName getParent() {
        return parent;
    }

    public synchronized void setParent(ObjectName parent) {
        this.parent = parent;
    }

    public ObjectName getCurrentParent() {
        if (running) {
            return currentParent;
        } else {
            return parent;
        }
    }

    public synchronized ClassLoader getClassLoader() {
        return classLoader;
    }

    public synchronized void addDeployment(ObjectName deployment, List urls) {
        if(urlsByDeployment.containsKey(deployment)) {
            throw new IllegalArgumentException("Deployment already exists in this class space");
        }

        urlsByDeployment.put(deployment, Collections.unmodifiableList(new ArrayList(urls)));
        if (running) {
            classLoader.addURLs(urls);
            currentURLsByDeployment.put(deployment, Collections.unmodifiableList(new ArrayList(urls)));
        }
    }

    public synchronized void dropDeployment(ObjectName deployment) {
        urlsByDeployment.remove(deployment);
    }

    /**
     * Gets a list of all classes defined and loaded by this class space.
     * @return a sorted list of all loaded classes
     */
    public synchronized SortedSet getLoadedClassNames() {
        if (running) {
            return classLoader.listLoadedClassNames();
        }
        return EMPTY_SORTED_SET;
    }

    /**
     * Gets a map of URLs by deployment.
     * @return map of URLs by deployment
     */
    public synchronized Map getURLsByDeployment() {
        return Collections.unmodifiableMap(urlsByDeployment);
    }

    /**
     * Gets a map of the current URLs by deployment.
     * @return map of URLs by deployment
     */
    public synchronized Map getCurrentURLsByDeployment() {
        if (running) {
            return Collections.unmodifiableMap(currentURLsByDeployment);
        }
        return Collections.unmodifiableMap(urlsByDeployment);
    }

    /**
     * Gets all URLs used by this class space.
     * @return all of URLs referenced by this class space in search order
     */
    public synchronized List getURLs() {
        LinkedList urls = new LinkedList();
        for (Iterator iterator = urlsByDeployment.values().iterator(); iterator.hasNext();) {
            urls.addAll((List) iterator.next());
        }
        return Collections.unmodifiableList(urls);
    }

    /**
     * Gets all URLs used by this class space.
     * @return sorted set of URLs referenced by this class space in search order
     */
    public synchronized List getCurrentURLs() {
        LinkedList urls = new LinkedList();
        if (running) {
            URL[] urlArray = classLoader.getURLs();
            for (int i = 0; i < urlArray.length; i++) {
                urls.add(urlArray[i]);
            }
        } else {
            for (Iterator iterator = urlsByDeployment.values().iterator(); iterator.hasNext();) {
                urls.addAll((List) iterator.next());
            }
        }
        return Collections.unmodifiableList(urls);
    }

    /**
     * Loads and defines the specified class.
     * @param name name of class to load
     * @return the class
     * @throws ClassNotFoundException if the class could not be located
     */
    public synchronized Class loadClass(final String name) throws ClassNotFoundException {
        if (!running) {
            throw new IllegalStateException("Classes may only be loaded while running or stopping");
        }
        return classLoader.loadClass(name);
    }

    /**
     * Gets a URL for the specifed resource.
     * @param name name of resouce to locate
     * @return a url to the resource or null if the resouce could not be found
     */
    public synchronized URL findResource(final String name) {
        if (!running) {
            throw new IllegalStateException("Resources may only be loaded while running or stopping");
        }
        return classLoader.findResource(name);
    }

    public void setMBeanContext(GeronimoMBeanContext context) {
        this.context = context;
    }

    public boolean canStart() {
        return true;
    }

    public synchronized void doStart() {
        ClassLoader parentClassLoader = null;
        if (parent != null) {
            parentClassLoader = ClassSpaceUtil.getClassLoader(context.getServer(), parent);
        } else {
            parentClassLoader = ClassLoader.getSystemClassLoader();
        }
        classLoader = new ClassSpaceLoader(parentClassLoader, context.getObjectName().toString());
        for (Iterator iterator = urlsByDeployment.values().iterator(); iterator.hasNext();) {
            classLoader.addURLs((List) iterator.next());
        }
        currentParent = parent;
        currentURLsByDeployment = new LinkedHashMap(urlsByDeployment);
        running = true;
    }

    public boolean canStop() {
        return true;
    }

    public synchronized void doStop() {
        classLoader = null;
        currentParent = null;
        currentURLsByDeployment = null;
        running = false;
    }

    public synchronized void doFail() {
        classLoader = null;
        currentParent = null;
        currentURLsByDeployment = null;
        running = false;
    }
}