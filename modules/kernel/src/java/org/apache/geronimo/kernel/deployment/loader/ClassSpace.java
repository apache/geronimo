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
package org.apache.geronimo.kernel.deployment.loader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Comparator;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @jmx:mbean
 *
 * @version $Revision: 1.3 $ $Date: 2003/10/22 02:04:31 $
 */
public class ClassSpace extends URLClassLoader implements ClassSpaceMBean {
    private final static Log log = LogFactory.getLog(ClassSpace.class);

    /**
     * Unique name of this class space.
     */
    private final ObjectName name;

    /**
     * Cache of loaded classesByName... used for debugging purposes
     */
    private final Map classesByName = new HashMap();

    /**
     * Deployments handled by this space
     */
    private final Map urlsByDeployment = new HashMap();

    /**
     * @jmx:managed-constructor
     */
    public ClassSpace(ClassLoader parent, ObjectName name) {
        super(new URL[0], parent);
        this.name = name;
    }

    /**
     * @jmx:managed-attribute
     */
    public ObjectName getName() {
        return name;
    }

    /**
     * @jmx:managed-operation
     */
    public synchronized void addDeployment(ObjectName deployment, List urls) {
        for (Iterator i = urls.iterator(); i.hasNext();) {
            URL url = (URL) i.next();
            addURL(url);
            log.debug("Added url to class-space: name=" + name + " url=" + url);
        }
        urlsByDeployment.put(deployment, Collections.unmodifiableList(new ArrayList(urls)));
    }

    /**
     * Returns a sorted list loaded classes.
     * @jmx:managed-operation
     */
    public synchronized SortedSet listLoadedClassNames() {
        return Collections.unmodifiableSortedSet(new TreeSet(classesByName.keySet()));
    }

    /**
     * @jmx:managed-operation
     */
    public synchronized Map listUrlsByDeployment() {
        return Collections.unmodifiableMap(urlsByDeployment);
    }

    /**
     * @jmx:managed-operation
     */
    public synchronized SortedSet listURLs() {
        TreeSet urls = new TreeSet(new ToStringComparator());
        URL[] urlArray = getURLs();
        for (int i = 0; i < urlArray.length; i++) {
            urls.add(urlArray[i]);

        }
        return Collections.unmodifiableSortedSet(urls);
    }

    /**
     * @jmx:managed-operation
     */
    public synchronized Class loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name, false);
    }

    /**
     * @jmx:managed-operation
     */
    public synchronized Class loadClass(String className, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(className, resolve);
    }

    protected Class findClass(final String className) throws ClassNotFoundException {
        // define the class
        Class clazz = super.findClass(className);

        // add it to our cache
        classesByName.put(clazz.getName(), clazz);

        return clazz;
    }

    private class ToStringComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }
    }
}
