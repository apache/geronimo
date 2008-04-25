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

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.Map;
import java.lang.reflect.Field;

import org.slf4j.LoggerFactory;

import org.apache.geronimo.kernel.util.ClassLoaderRegistry;

/**
 *
 * @version $Rev$ $Date$
 */
public class ConfigurationClassLoader extends URLClassLoader {
    private final URI id;
    
    public ConfigurationClassLoader(URI id, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        ClassLoaderRegistry.add(this);
        this.id = id;
    }
    
    public URI getID() {
        return id;
    }

    public void destroy() {
        clearSoftCache(ObjectInputStream.class, "subclassAudits");
        clearSoftCache(ObjectOutputStream.class, "subclassAudits");
        clearSoftCache(ObjectStreamClass.class, "localDescs");
        clearSoftCache(ObjectStreamClass.class, "reflectors");
        ClassLoaderRegistry.remove(this);
    }

    public String toString() {
        return "[Configuration ClassLoader id=" + id + "]";
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
                    LoggerFactory.getLogger(ConfigurationClassLoader.class).debug("Unable to clear SoftCache field {} in class {}", fieldName, clazz);
                }
            }
        }

        if (cache != null) {
            synchronized (cache) {
                cache.clear();
            }
        }
    }
    protected void finalize(){
        ClassLoaderRegistry.remove(this);
    }
}
