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
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This is the class loader used internally by the default class space.
 * This is just a URLClassLoader with extra intrumentation.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/10/27 20:58:38 $
 */
class ClassSpaceLoader extends URLClassLoader {
    /**
     * Cache of loaded classesByName... used for debugging purposes
     */
    private final Map classesByName = new HashMap();

    /**
     * Unique name of this class loader
     */
    private String name;

    /**
     * Constructs a ClassSpaceLoader wich is a child of the specified parent.
     * @param parent parent of this class loader
     */
    public ClassSpaceLoader(ClassLoader parent, String name) {
        super(new URL[0], parent);
        this.name = name;
    }

    public String toString() {
        return "[ClassSpaceLoader: name=" + name + "]";
    }

    /**
     * Adds the specifiec URLs to this class loader.
     * @param urls the URLs to add
     */
    void addURLs(List urls) {
        for (Iterator iterator = urls.iterator(); iterator.hasNext();) {
            URL url = (URL) iterator.next();
            addURL(url);
        }
    }

    SortedSet listLoadedClassNames() {
        synchronized(classesByName) {
            return Collections.unmodifiableSortedSet(new TreeSet(classesByName.keySet()));
        }
    }

    protected Class findClass(final String className) throws ClassNotFoundException {
        // define the class
        Class clazz = super.findClass(className);

        // add it to our cache
        synchronized(classesByName) {
            classesByName.put(clazz.getName(), clazz);
        }

        return clazz;
    }
}
