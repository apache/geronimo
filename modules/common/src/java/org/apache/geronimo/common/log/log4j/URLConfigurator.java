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

package org.apache.geronimo.common.log.log4j;

import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.spi.Configurator;
import org.apache.log4j.spi.LoggerRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.common.NullArgumentException;

/**
 * Handles the details of configuring Log4j from a URL.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/29 20:08:45 $
 */
public class URLConfigurator
    implements Configurator
{
    private static final Log log = LogFactory.getLog(URLConfigurator.class);
    
    public static void configure(final URL url)
    {
        new URLConfigurator().doConfigure(url, LogManager.getLoggerRepository());
    }
    
    private boolean isContentXML(final URL url)
    {
        String contentType = null;
        
        // Get the content type to see if it is XML or not
        try {
            URLConnection connection = url.openConnection();
            contentType = connection.getContentType();
            if (log.isTraceEnabled()) {
                log.trace("Content type: " + contentType);
            }
        }
        catch (IOException e) {
            log.warn("Could not determine content type from URL; ignoring", e);
        }
        
        if (contentType != null) {
            return contentType.toLowerCase().endsWith("/xml");
        }
        
        return url.getFile().toLowerCase().endsWith(".xml");
        
        //
        // TODO: Add check for <?xml in content
        //
    }
    
    private Configurator getConfigurator(final URL url)
    {
        if (isContentXML(url)) {
            return new DOMConfigurator();
        }
        
        return new PropertyConfigurator();
    }
    
    public void doConfigure(final URL url, final LoggerRepository repo)
    {
        if (log.isDebugEnabled()) {
            log.debug("Configuring from URL: " + url);
        }
        
        // Get the config delegate and target repository to config with
        Configurator delegate = getConfigurator(url);
        
        if (log.isTraceEnabled()) {
            log.trace("Configuring Log4j using configurator: " + 
                      delegate + ", repository: " + repo);
        }
        
        // Now actually configure Log4j
        delegate.doConfigure(url, repo);
    }
}
