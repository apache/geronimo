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

/**
 *
 * @version $Rev: 156292 $ $Date: 2005-03-06 13:48:02 +1100 (Sun, 06 Mar 2005) $
 */
public class ConfigurationClassLoader extends URLClassLoader {
    private final URI id;
    private URL[] urls;
    
    public ConfigurationClassLoader(URI id, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.id = id;
    }
    
    public URI getID() {
        return id;
    }

    public URL[] getClassLoaderServerURLs() {
        return urls;
    }
    
    public void setClassLoaderServerURLs(URL[] urls) {
        this.urls = urls;
    }
    
    public String toString() {
        return "[Configuration ClassLoader id=" + id + "]";
    }
}
