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
 * @version <code>$Revision: 1.2 $ $Date: 2004/02/25 09:59:17 $</code>
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
