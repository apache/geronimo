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

package org.apache.geronimo.rmi;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.File;

import java.util.StringTokenizer;

import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;

/**
 * An implementation of {@link RMIClassLoaderSpi} which provides normilzation
 * of codebase URLs and delegates to the default {@link RMIClassLoaderSpi}.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/30 10:22:52 $
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
}
