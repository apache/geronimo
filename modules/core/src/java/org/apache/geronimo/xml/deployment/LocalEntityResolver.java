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
package org.apache.geronimo.xml.deployment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implementation of EntityResolver that looks to the local filesystem.
 *
 * @jmx:mbean
 *
 * @version $Revision: 1.5 $ $Date: 2003/12/09 04:19:38 $
 */
public class LocalEntityResolver implements EntityResolver, LocalEntityResolverMBean {
    private static final Log log = LogFactory.getLog(LocalEntityResolver.class);
    private File root;
    private String configFile;
    private Properties mappings = new Properties();

    /**
     * @jmx:managed-constructor
     */
    public LocalEntityResolver(File root) {
        this.root = root;
        log.info("root=" + root);
    }

    public LocalEntityResolver() {
        root = null;
        init();
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        InputSource is = null;

                is = resolveEntityLocally(publicId, systemId);
                if (is != null) {
                        return is;
                }
        if (publicId != null || systemId == null) {
            if (log.isDebugEnabled()) {
                log.debug("Not attempting to locally resolve entity S=" + systemId + " P=" + publicId);
            }
            return null;
        }
        String message = null;
        if (log.isDebugEnabled()) {
            message = "Resolving entity S=" + systemId + " P=" + publicId + ": ";
        }
        int index = systemId.lastIndexOf("/");
        String fileName = systemId.substring(index + 1);
        if (root != null) {
            File file = new File(root, fileName);
            if (file.exists()) {
                if (log.isDebugEnabled()) {
                    log.debug(message + "found file relative to " + root);
                }
                is = new InputSource(new BufferedInputStream(new FileInputStream(file)));
                is.setSystemId(systemId);
                return is;
            }
        }
        InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
        if (in != null) {
            if (log.isDebugEnabled()) {
                log.debug(message + "found file on classpath");
            }
            is = new InputSource(new BufferedInputStream(in));
            is.setSystemId(systemId);
            return is;
        }
        if (log.isDebugEnabled()) {
            log.debug(message + "not found");
        }
        return null;
    }

    /**
     * @jmx:managed-attribute
     */
    public void setConfigFile(String configFile) {
        this.configFile = configFile;
        init();
    }

    /**
     * @jmx:managed-attribute
     */
    public String getConfigFile() {
        return this.configFile;
    }

    /**
     * @jmx:managed-operation
     */
    public void addMapping(String publicId, String systemId) {
        if ( publicId == null || systemId == null )
        {
                        if (log.isDebugEnabled()) {
                                log.debug("publicId or systemId are null");
                        }
                        return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Adding entity P=" + publicId + " and its S=" + systemId);
        }
        mappings.put(publicId, systemId);
    }

    /**
     * Resolve entities locally.
     *
     * TODO: Look for the systemIds in jar(s) beside their file representatives
     *
     * @param publicId
     * @param systemId
     * @return input source of the local representative of systemId
     * @throws SAXException
     * @throws IOException
     */
    private InputSource resolveEntityLocally(String publicId, String systemId) throws SAXException, IOException {
        System.out.println("publicId: " + publicId + ", systemId: " + systemId);
                if ( publicId == null || publicId.length() == 0)
                {
                        if (log.isDebugEnabled()) {
                                log.debug("publicId is null or empty; skipping resolving");
                        }
                        return null;
                }
        if (log.isDebugEnabled()) {
            log.debug("Resolving entity locally S=" + systemId + " P=" + publicId);
        }
        String publicIdPath = (String) mappings.get(publicId);
        if (publicIdPath != null && publicIdPath.length() != 0) {
            File file = new File(publicIdPath);
            if (file.exists()) {
                if (log.isDebugEnabled()) {
                    log.debug("found file: " + publicIdPath);
                }
                InputSource is = new InputSource(new BufferedInputStream(new FileInputStream(file)));
                is.setSystemId(systemId);
                return is;
            }
        }
        return null;
    }

    /**
     * Loads mappings from configuration file
     */
    private void init() {
        mappings.clear();
        try {
            if (this.configFile == null || this.configFile.length() == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("No configuration file provided");
                }
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Loading configuration file=" + this.configFile);
            }
            mappings.load(new BufferedInputStream(new FileInputStream(this.configFile)));
        } catch (IOException ioe) {
            if (log.isDebugEnabled()) {
                log.debug("Exception occured: " + ioe.getMessage() + "; ignore it");
            }
        }
    }

    /**
     * @jmx:managed-operation
     */
    public Hashtable showMappings() {
        return mappings;
    }
}
