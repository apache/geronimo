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
package org.apache.geronimo.kernel.classloader;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.MalformedURLException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public class JarFileUrlStreamHandler extends URLStreamHandler {
    public static URL createUrl(JarFile jarFile, JarEntry jarEntry) throws MalformedURLException {
        return createUrl(jarFile, jarEntry, new File(jarFile.getName()).toURL());
    }

    public static URL createUrl(JarFile jarFile, JarEntry jarEntry, URL codeSource) throws MalformedURLException {
        JarFileUrlStreamHandler handler = new JarFileUrlStreamHandler(jarFile, jarEntry);
        URL url = new URL("jar", "", -1, codeSource + "!/" + jarEntry.getName(), handler);
        handler.setExpectedUrl(url);
        return url;
    }

    private URL expectedUrl;
    private final JarFile jarFile;
    private final JarEntry jarEntry;

    public JarFileUrlStreamHandler(JarFile jarFile, JarEntry jarEntry) {
        if (jarFile == null) throw new NullPointerException("jarFile is null");
        if (jarEntry == null) throw new NullPointerException("jarEntry is null");

        this.jarFile = jarFile;
        this.jarEntry = jarEntry;
    }

    public void setExpectedUrl(URL expectedUrl) {
        if (expectedUrl == null) throw new NullPointerException("expectedUrl is null");
        this.expectedUrl = expectedUrl;
    }

    public URLConnection openConnection(URL url) throws MalformedURLException {
        if (expectedUrl == null) throw new IllegalStateException("expectedUrl was not set");

        // alternatively we could return a connection using the normal jar url connection
        if (!expectedUrl.equals(url)) throw new IllegalArgumentException("Expected url [" + expectedUrl + "], but was [" + url + "]");

        return new JarFileUrlConnection(url, jarFile, jarEntry);
    }
}
