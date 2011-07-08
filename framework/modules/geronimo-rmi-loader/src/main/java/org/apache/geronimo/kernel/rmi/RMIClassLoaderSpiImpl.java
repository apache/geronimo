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

package org.apache.geronimo.kernel.rmi;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.File;

import java.util.StringTokenizer;

import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;

import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link RMIClassLoaderSpi} which provides normilzation
 * of codebase URLs and delegates to the default {@link RMIClassLoaderSpi}.
 *
 * @version $Rev$ $Date$
 */
public class RMIClassLoaderSpiImpl
    extends RMIClassLoaderSpi
{
    private RMIClassLoaderSpi delegate = RMIClassLoader.getDefaultProviderInstance();

    //TODO: Not sure of the best initial size.  Starting with 100 which should be reasonable.
    private ConcurrentHashMap cachedCodebases = new ConcurrentHashMap(100);

    public Class loadClass(String codebase, String name, ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException
    {
        if (codebase != null) {
            codebase = getNormalizedCodebase(codebase);
        }
        
        return delegate.loadClass(codebase, name, defaultLoader);
    }
    
    public Class loadProxyClass(String codebase, String[] interfaces, ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException
    {
        if (codebase != null) {
            codebase = getNormalizedCodebase(codebase);
        }
        
        return delegate.loadProxyClass(codebase, interfaces, defaultLoader);
    }
    
    public ClassLoader getClassLoader(String codebase)
        throws MalformedURLException
    {
        if (codebase != null) {
            codebase = getNormalizedCodebase(codebase);
        }
        
        return delegate.getClassLoader(codebase);
    }
    
    public String getClassAnnotation(Class type) {
        Object obj = type.getClassLoader();
        if (obj instanceof ClassLoaderServerAware) {
            ClassLoaderServerAware classLoader = (ClassLoaderServerAware) obj;
            URL urls[] = classLoader.getClassLoaderServerURLs();
            if (null == urls) {
                return delegate.getClassAnnotation(type);
            }
            StringBuilder codebase = new StringBuilder();
            for (int i = 0; i < urls.length; i++) {
                URL url = normalizeURL(urls[i]);
                if (codebase.length() != 0) {
                    codebase.append(' ');
                }
                codebase.append(url);
            }
            return codebase.toString();
        }
        
        String codebase = delegate.getClassAnnotation(type);
        if (codebase != null) {
            try {
                codebase = getNormalizedCodebase( codebase );
            }
            catch (MalformedURLException ignore) {
            }
        }
        return codebase;
    }

    /**
     * Uses a ConcurrentReaderHashmap to save the contents of previous parses.
     *
     * @param codebase
     * @return
     * @throws MalformedURLException
     */
    private String getNormalizedCodebase(String codebase)
            throws MalformedURLException {
        String cachedCodebase = (String)cachedCodebases.get(codebase);
        if (cachedCodebase != null)
            return cachedCodebase;

        String normalizedCodebase = normalizeCodebase(codebase);
        String oldValue = (String)cachedCodebases.put(codebase, normalizedCodebase);

        // If there was a previous value remove the one we just added to make sure the
        // cache doesn't grow.
        if (oldValue != null) {
            cachedCodebases.remove(codebase);
        }
        return normalizedCodebase;  // We can use the oldValue
    }


    static String normalizeCodebase(String input)
        throws MalformedURLException
    {
        assert input != null;

        StringBuilder codebase = new StringBuilder();
        StringBuilder working = new StringBuilder();
        StringTokenizer stok = new StringTokenizer(input, " \t\n\r\f", true);
        
        while (stok.hasMoreTokens()) {
            String item = stok.nextToken();
            // Optimisation: This optimisation to prevent unnecessary MalformedURLExceptions 
            //   being generated is most helpful on windows where directory names in the path 
            //   often contain spaces.  E.G:
            //     file:/C:/Program Files/Apache Software Foundation/Maven 1.0.2/lib/ant-1.5.3-1.jar
            //
            //   Therefore we won't attempt URL("Files/Apache) or URL(" ") for the path delimiter.
            if ( item.indexOf(':') != -1 )
            {
                try {
                    URL url = new URL(item);
                    // If we got this far then item is a valid url, so commit the current
                    // buffer and start collecting any trailing bits from where we are now
                    updateCodebase(working, codebase);
                } catch (MalformedURLException ignore) {
                    // just keep going & append to the working buffer
                }
            }
            
            // Append the URL or delimiter to the working buffer
            working.append(item);
        }
        
        // Handle trailing elements
        updateCodebase(working, codebase);
        
        // System.out.println("Normalized codebase: " + codebase);
        return codebase.toString();
    }
    
    private static void updateCodebase(final StringBuilder working, final StringBuilder codebase)
        throws MalformedURLException
    {
        if (working.length() != 0) {
            // Normalize the URL
            URL url = normalizeURL(new URL(working.toString()));
            // System.out.println("Created normalized URL: " + url);
            
            // Put spaces back in for URL delims
            if (codebase.length() != 0) {
                codebase.append(" ");
            }
            codebase.append(url);
            
            // Reset the working buffer
            working.setLength(0);
        }
    }
    
    static URL normalizeURL(URL url)
    {
        assert url != null;
        
        if (url.getProtocol().equals("file")) {
            String filename = url.getFile().replace('/', File.separatorChar);
            File file = new File(filename);
            try {
                url = file.toURI().toURL();
            }
            catch (MalformedURLException ignore) {}
        }
        
        return url;
    }
    
    public interface ClassLoaderServerAware {
        public URL[] getClassLoaderServerURLs();
    }
}
