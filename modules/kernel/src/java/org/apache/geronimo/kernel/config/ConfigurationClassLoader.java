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

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.Map;
import java.lang.reflect.Field;

import org.apache.commons.logging.LogFactory;

/**
 *
 * @version $Rev$ $Date$
 */
public class ConfigurationClassLoader extends URLClassLoader {
    private final URI id;
    
    public ConfigurationClassLoader(URI id, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.id = id;
    }
    
    public URI getID() {
        return id;
    }

    public void destroy() {
        LogFactory.release(this);
        clearSoftCache(ObjectInputStream.class, "subclassAudits");
        clearSoftCache(ObjectOutputStream.class, "subclassAudits");
        clearSoftCache(ObjectStreamClass.class, "localDescs");
        clearSoftCache(ObjectStreamClass.class, "reflectors");
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
