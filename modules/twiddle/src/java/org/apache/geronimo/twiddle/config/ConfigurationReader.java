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

package org.apache.geronimo.twiddle.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.common.net.URLFactory;

import org.exolab.castor.xml.Unmarshaller;

/**
 * Creates <code>Configuration</code> objects.
 *
 * @version <code>$Revision: 1.8 $ $Date: 2003/09/03 13:50:29 $</code>
 */
public class ConfigurationReader
{
    private static final Log log = LogFactory.getLog(ConfigurationReader.class);
    
    /** The Castor unmarshaller used to tranform XML->Objects */
    protected Unmarshaller unmarshaller;
    
    /**
     * Construct a <code>ConfigurationReader</code>.
     */
    public ConfigurationReader()
    {
        unmarshaller = new Unmarshaller(Configuration.class);
    }
    
    /**
     * Read a configuration instance from a URL.
     *
     * @param url   The URL to read the configuration from.
     * @return      The configuration instance.
     *
     * @throws Exception    Failed to read configuration.
     */
    public Configuration read(final URL url) throws Exception
    {
        if (url == null) {
            throw new NullArgumentException("url");
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Reading: " + url);
        }
        
        return doRead(new BufferedReader(new InputStreamReader(url.openStream())));
    }
    
    /**
     * Read a configuration instance from a string URL specification.
     *
     * @param urlspec   The URL specification.
     * @return          The configuration instance.
     *
     * @throws Exception    Failed to read configuration.
     */
    public Configuration read(final String urlspec) throws Exception
    {
        if (urlspec == null) {
            throw new NullArgumentException("urlspec");
        }
        
        return read(URLFactory.create(urlspec));
    }
    
    /**
     * Read a configuration instance from a file.
     *
     * @param file  The file to read the configuration from.
     * @return      The configuration instance.
     *
     * @throws Exception    Failed to read configuration.
     */
    public Configuration read(final File file) throws Exception
    {
        if (file == null) {
            throw new NullArgumentException("file");
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Reading: " + file);
        }
        
        return doRead(new BufferedReader(new FileReader(file)));
    }
    
    /**
     * Read a configuration instance from a reader.
     *
     * @param reader    The reader to read the configuration from.
     * @return          The configuration instance.
     *
     * @throws Exception    Failed to read configuration.
     */
    public Configuration read(final Reader reader) throws Exception
    {
        if (reader == null) {
            throw new NullArgumentException("reader");
        }
        
        return (Configuration)unmarshaller.unmarshal(reader);
    }
    
    /**
     * Read a configuration instance from a reader and handle closing the
     * reader after the read operation.
     *
     * @param reader    The reader to read the configuration from.
     * @return          The configuration instance.
     *
     * @throws Exception    Failed to read configuration.
     */
    protected Configuration doRead(final Reader reader) throws Exception
    {
        assert reader != null;
        
        Configuration config = null;
        try {
            config = read(reader);
        }
        finally {
            reader.close();
        }
        
        return config;
    }
}
