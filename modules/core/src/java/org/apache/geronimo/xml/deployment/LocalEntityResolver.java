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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogEntry;
import org.apache.xml.resolver.CatalogException;
import org.apache.xml.resolver.CatalogManager;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implementation of EntityResolver that looks to the local filesystem.
 *
 * The implementation tries to resolve an entity via the following steps:
 *
 * <ul>
 *   <li>using a catalog file</li>
 *   <li>using a local repository</li>
 *   <li>using JAR files in Classpath</li>
 * </ul>
 *
 * The catalog resolving is based on the OASIS XML Catalog Standard.
 * (see http://www.oasis-open.org/committees/entity/spec-2001-08-01.html
 * and http://www.oasis-open.org/html/a401.htm)
 *
 * @version $Revision: 1.7 $ $Date: 2004/01/05 00:05:35 $
 */
public class LocalEntityResolver implements EntityResolver {

    private static final String INTERNAL_CATALOG = "resolver-catalog.xml";
    /**
     * used Logger
     */
    private static final Log log = LogFactory.getLog(LocalEntityResolver.class);

    /**
     * The used Catalog Manager
     */
    private final CatalogManager manager = new CatalogManager();

    /**
     * the XML Catalog
     */
    private Catalog catalog = null;

    /**
     * the URI of the catalog file
     */
    private URI catalogFileURI = null;

    /**
     * Local Repository where DTDs and Schemas are located
     */
    private String localRepository = null;

    /**
     * Flag indicating if this resolver may return null to signal
     * the parser to open a regular URI connection to the system
     * identifier. Otherwise an exception is thrown.
     */
    private boolean failOnUnresolvable = false;

    private String internalCatalog;

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setTargetClass(LocalEntityResolver.class);
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("FailOnUnresolvable", true, true, "Should null be returned or an exception thrown when an entity cannot be resolved"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LocalRepository", true, true, "Location of dtds and schemas"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("CatalogFile", true, true, "Location of xml catalog file"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("InternalCatalog", true, false, "Name of catalog file in a schema jar file, for classpath access"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("resolveEntity", new GeronimoParameterInfo[]{
            new GeronimoParameterInfo("PublicID", String.class, "PublicID of entity to resolve"),
            new GeronimoParameterInfo("SystemID", String.class, "SystemID of entity to resolve")},
                GeronimoOperationInfo.ACTION,
                "resolve supplied entity"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("addPublicMapping", new GeronimoParameterInfo[]{
            new GeronimoParameterInfo("PublicID", String.class, "PublicID to map"),
            new GeronimoParameterInfo("URI", String.class, "Actual location of dtd/schema")},
                GeronimoOperationInfo.ACTION,
                "resolve supplied entity"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("addSystemMapping", new GeronimoParameterInfo[]{
            new GeronimoParameterInfo("SystemID", String.class, "SystemID to map"),
            new GeronimoParameterInfo("URI", String.class, "Actual location of dtd/schema")},
                GeronimoOperationInfo.ACTION,
                "resolve supplied entity"));
        mbeanInfo.setAutostart(true);
        return mbeanInfo;
    }

    public LocalEntityResolver(String catalogFile, String localRepository, boolean failOnUnresolvable) {
        this(catalogFile, localRepository, INTERNAL_CATALOG, failOnUnresolvable);
    }

    public LocalEntityResolver(String catalogFile, String localRepository, String internalCatalog, boolean failOnUnresolvable) {
        this.internalCatalog = internalCatalog;
        setLocalRepository(localRepository);
        setFailOnUnresolvable(failOnUnresolvable);
        setCatalogFile(catalogFile);
        LoaderUtil.setEntityResolver(this);
        StorerUtil.setEntityResolver(this);
    }

    /**
     * Sets the setFailOnUnresolvable flag.
     *
     * @param b value (true means that a SAXException is thrown
     * if the entity could not be resolved)
     */
    public void setFailOnUnresolvable(final boolean b) {
        failOnUnresolvable = b;
    }

    public boolean isFailOnUnresolvable() {
        return failOnUnresolvable;
    }

    public void setCatalogFile(final String catalogFile) {

        try {
            URL url = new URL(catalogFile);
            this.catalogFileURI = new URI(url.toExternalForm());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("could not parse url: " + catalogFile);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("could not parse url: " + catalogFile);
        }

        init();
    }

    public String getCatalogFile() {
        if (catalogFileURI != null) {
            return this.catalogFileURI.toString();
        }
        return null;
    }

    public String getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(String string) {
        localRepository = string;
    }

    public String getInternalCatalog() {
        return internalCatalog;
    }

    public void addPublicMapping(final String publicId, final String uri) {

        Vector args = new Vector();
        args.add(publicId);
        args.add(uri);

        addEntry("PUBLIC", args);

    }

    public void addSystemMapping(final String systemId, final String uri) {

        Vector args = new Vector();
        args.add(systemId);
        args.add(uri);

        addEntry("SYSTEM", args);

    }

    /**
     * Attempt to resolve the entity based on the supplied publicId and systemId.
     * First the catalog is queried with both publicId and systemId.
     * Then the local repository is queried with the file name part of the systemId
     * Then the classpath is queried with  the file name part of the systemId.
     *
     * Then, if failOnUnresolvable is true, an exception is thrown: otherwise null is returned.
     * @param publicId
     * @param systemId
     * @return
     * @throws SAXException
     * @throws IOException
     */
    public InputSource resolveEntity(
            final String publicId,
            final String systemId)
            throws SAXException, IOException {

        if (log.isTraceEnabled()) {
            log.trace(
                    "start resolving for "
                    + entityMessageString(publicId, systemId));
        }

        InputSource source = resolveWithCatalog(publicId, systemId);
        if (source != null) {
            return source;
        }

        source = resolveWithRepository(publicId, systemId);
        if (source != null) {
            return source;
        }

        source = resolveWithClasspath(publicId, systemId);
        if (source != null) {
            return source;
        }

        String message =
                "could not resolve " + entityMessageString(publicId, systemId);

        if (failOnUnresolvable) {
            throw new SAXException(message);
        } else {
            log.debug(message);
        }

        return null;
    }

    /**
     * Try to resolve using the catalog file
     *
     * @param publicId the PublicId
     * @param systemId the SystemId
     * @return InputSource if the entity could be resolved. null otherwise
     * @throws MalformedURLException
     * @throws IOException
     */
    InputSource resolveWithCatalog(
            final String publicId,
            final String systemId)
            throws MalformedURLException, IOException {

        if (catalogFileURI == null) {
            return null;
        }

        String resolvedSystemId =
                catalog.resolvePublic(guaranteeNotNull(publicId), systemId);

        if (resolvedSystemId != null) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "resolved "
                        + entityMessageString(publicId, systemId)
                        + " using the catalog file. result: "
                        + resolvedSystemId);
            }
            return new InputSource(resolvedSystemId);
        }

        return null;
    }

    /**
     * Try to resolve using the local repository and only the supplied systemID filename.
     * Any path in the systemID will be removed.
     *
     * @param publicId the PublicId
     * @param systemId the SystemId
     * @return InputSource if the entity could be resolved. null otherwise
     */
    InputSource resolveWithRepository(
            final String publicId,
            final String systemId) {

        if (localRepository == null) {
            return null;
        }

        String fileName = getSystemIdFileName(systemId);

        if (fileName == null) {
            return null;
        }

        String resolvedSystemId = null;

        File file = new File(localRepository, fileName);
        if (file.exists()) {
            resolvedSystemId = file.getAbsolutePath();
            if (log.isTraceEnabled()) {
                log.trace(
                        "resolved "
                        + entityMessageString(publicId, systemId)
                        + "with file relative to "
                        + localRepository
                        + resolvedSystemId);
            }
            return new InputSource(resolvedSystemId);
        }
        return null;
    }

    /**
     * Try to resolve using the the classpath and only the supplied systemID.
     * Any path in the systemID will be removed.
     *
     * @param publicId the PublicId
     * @param systemId the SystemId
     * @return InputSource if the entity could be resolved. null otherwise
     */
    InputSource resolveWithClasspath(
            final String publicId,
            final String systemId) {

        String fileName = getSystemIdFileName(systemId);

        if (fileName == null) {
            return null;
        }

        InputStream in =
                getClass().getClassLoader().getResourceAsStream(fileName);
        if (in != null) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "resolved "
                        + entityMessageString(publicId, systemId)
                        + " via file found file on classpath: "
                        + fileName);
            }
            InputSource is = new InputSource(new BufferedInputStream(in));
            is.setSystemId(systemId);
            return is;
        }

        return null;
    }

    /**
     * Guarantees a not null value
     */
    private String guaranteeNotNull(final String string) {
        return string != null ? string : "";
    }

    /**
     * Returns the SystemIds filename
     *
     * @param systemId SystemId
     * @return filename
     */
    private String getSystemIdFileName(final String systemId) {

        if (systemId == null) {
            return null;
        }

        int indexBackSlash = systemId.lastIndexOf("\\");
        int indexSlash = systemId.lastIndexOf("/");

        int index = Math.max(indexBackSlash, indexSlash);

        String fileName = systemId.substring(index + 1);
        return fileName;
    }

    /**
     * Constructs a debugging message string
     */
    private String entityMessageString(
            final String publicId,
            final String systemId) {

        StringBuffer buffer = new StringBuffer("entity with publicId '");
        buffer.append(publicId);
        buffer.append("' and systemId '");
        buffer.append(systemId);
        buffer.append("'");
        return buffer.toString();

    }

    /**
     * Adds a new Entry to the catalog
     */
    private void addEntry(String type, Vector args) {
        try {
            CatalogEntry entry = new CatalogEntry(type, args);
            catalog.addEntry(entry);
        } catch (CatalogException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads mappings from configuration file
     */
    private void init() {

        if (log.isDebugEnabled()) {
            log.debug("init catalog file " + this.catalogFileURI);
        }

        manager.setUseStaticCatalog(false);
        manager.setCatalogFiles(this.catalogFileURI.toString());
        manager.setIgnoreMissingProperties(true);
        catalog = manager.getCatalog();
        //This is an attempt to use a catalog in gerionimo-schema.jar.  It is not yet clear
        //if it works, and is pretty much untestable until schemas are in a different module.
        if (internalCatalog != null) {
            URL url = getClass().getClassLoader().getResource(internalCatalog);
            if (url == null) {
                log.info("Could not locate internal catalog " + internalCatalog);
            } else {
                try {
                    catalog.parseCatalog(url);
                } catch (IOException e) {
                    log.info("Could not add internal catalog: " + url, e);
                }
            }
        }
    }

}
