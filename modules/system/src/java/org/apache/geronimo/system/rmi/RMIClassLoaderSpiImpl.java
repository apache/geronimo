/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.system.rmi;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.File;

import java.util.StringTokenizer;

import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;

import org.apache.geronimo.kernel.config.ConfigurationClassLoader;

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
    
    public Class loadClass(String codebase, String name, ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException
    {
        if (codebase != null) {
            codebase = normalizeCodebase(codebase);
        }
        
        return delegate.loadClass(codebase, name, defaultLoader);
    }
    
    public Class loadProxyClass(String codebase, String[] interfaces, ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException
    {
        if (codebase != null) {
            codebase = normalizeCodebase(codebase);
        }
        
        return delegate.loadProxyClass(codebase, interfaces, defaultLoader);
    }
    
    public ClassLoader getClassLoader(String codebase)
        throws MalformedURLException
    {
        if (codebase != null) {
            codebase = normalizeCodebase(codebase);
        }
        
        return delegate.getClassLoader(codebase);
    }
    
    public String getClassAnnotation(Class type) {
        Object obj = type.getClassLoader();
        if (obj instanceof ConfigurationClassLoader) {
            obj = new ConfigurationClassLoaderAdapter((ConfigurationClassLoader) obj);
        }
        if (obj instanceof ClassLoaderServerAware) {
            ClassLoaderServerAware classLoader = (ClassLoaderServerAware) obj;
            URL urls[] = classLoader.getClassLoaderServerURLs();
            if (null == urls) {
                return delegate.getClassAnnotation(type);
            }
            StringBuffer codebase = new StringBuffer();
            for (int i = 0; i < urls.length; i++) {
                URL url = normalizeURL(urls[i]);
                if (codebase.length() != 0) {
                    codebase.append(' ');
                }
                codebase.append(url);
            }
            return codebase.toString();
        }
        
        return delegate.getClassAnnotation(type);
    }
    
    static String normalizeCodebase(String input)
        throws MalformedURLException
    {
        assert input != null;
        // System.out.println("Input codebase: " + input);
        
        StringBuffer codebase = new StringBuffer();
        StringBuffer working = new StringBuffer();
        StringTokenizer stok = new StringTokenizer(input, " \t\n\r\f", true);
        
        while (stok.hasMoreTokens()) {
            String item = stok.nextToken();
            try {
                URL url = new URL(item);
                // System.out.println("Created URL: " + url);
                
                // If we got this far then item is a valid url, so commit the current
                // buffer and start collecting any trailing bits from where we are now
                
                updateCodebase(working, codebase);
            }
            catch (MalformedURLException ignore) {
                // just keep going & append to the working buffer
            }
            
            working.append(item);
            // System.out.println("Added to working buffer: " + item);
        }
        
        // Handle trailing elements
        updateCodebase(working, codebase);
        
        // System.out.println("Normalized codebase: " + codebase);
        return codebase.toString();
    }
    
    private static void updateCodebase(final StringBuffer working, final StringBuffer codebase)
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
    
    private static class ConfigurationClassLoaderAdapter implements ClassLoaderServerAware {
        private final ConfigurationClassLoader cl;
        
        private ConfigurationClassLoaderAdapter(ConfigurationClassLoader cl) {
            this.cl = cl;
        }

        public URL[] getClassLoaderServerURLs() {
            return cl.getClassLoaderServerURLs();
        }
    }
}
