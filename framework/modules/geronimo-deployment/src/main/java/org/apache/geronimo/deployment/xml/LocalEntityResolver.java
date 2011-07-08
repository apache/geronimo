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

package org.apache.geronimo.deployment.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Vector;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogEntry;
import org.apache.xml.resolver.CatalogException;
import org.apache.xml.resolver.CatalogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * OASIS seems to move it around.  Try
 * http://www.oasis-open.org/committees/tc_home.php?wg_abbrev=entity
 * and the list of documents currently at
 * http://www.oasis-open.org/committees/documents.php?wg_abbrev=entity
 * An older version may be at
 * http://www.oasis-open.org/committees/entity/archives/spec-2001-08-01.html
 * and see http://www.oasis-open.org/html/a401.htm
 *
 * @version $Rev$ $Date$
 */
public class LocalEntityResolver implements EntityResolver {
    private static final Logger log = LoggerFactory.getLogger(LocalEntityResolver.class);

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
    private URI localRepositoryURI = null;

    /**
     * Flag indicating if this resolver may return null to signal
     * the parser to open a regular URI connection to the system
     * identifier. Otherwise an exception is thrown.
     */
    private boolean failOnUnresolvable = false;


    public LocalEntityResolver(URI catalogFileURI, URI localRepositoryURI, boolean failOnUnresolvable) {
        this.catalogFileURI = catalogFileURI;
        setLocalRepositoryURI(localRepositoryURI);
        setFailOnUnresolvable(failOnUnresolvable);
        init();
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

    public void setCatalogFileURI(final URI catalogFileURI) {
        this.catalogFileURI = catalogFileURI;
        init();
    }

    public URI getCatalogFileURI() {
        return this.catalogFileURI;
    }

    public URI getLocalRepositoryURI() {
        return localRepositoryURI;
    }

    public void setLocalRepositoryURI(URI string) {
        localRepositoryURI = string;
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
            log.trace("start resolving for " + entityMessageString(publicId, systemId));
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

        String message = "could not resolve " + entityMessageString(publicId, systemId);

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
                log.trace("resolved " + entityMessageString(publicId, systemId) + " using the catalog file. result: " + resolvedSystemId);
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

        if (localRepositoryURI == null) {
            return null;
        }

        String fileName = getSystemIdFileName(systemId);

        if (fileName == null) {
            return null;
        }

        InputStream inputStream = null;
        URI resolvedSystemIDURI;
        try {
            resolvedSystemIDURI = localRepositoryURI.resolve(fileName);
            inputStream = resolvedSystemIDURI.toURL().openStream();
        } catch (IOException e) {
            return null;
        } catch (IllegalArgumentException e) {
            //typically "uri is not absolute"
            return null;
        }
        if (inputStream != null) {
            if (log.isTraceEnabled()) {
                log.trace("resolved " + entityMessageString(publicId, systemId) + "with file relative to " + localRepositoryURI + resolvedSystemIDURI);
            }
            return new InputSource(inputStream);
        } else {
            return null;
        }
        /*
        String resolvedSystemId = null;

        File file = new File(localRepositoryURI, fileName);
        if (file.exists()) {
            resolvedSystemId = file.getAbsolutePath();
            if (log.isTraceEnabled()) {
                log.trace(
                        "resolved "
                        + entityMessageString(publicId, systemId)
                        + "with file relative to "
                        + localRepositoryURI
                        + resolvedSystemId);
            }
            return new InputSource(resolvedSystemId);
        }
        return null;
        */
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
                log.trace("resolved " + entityMessageString(publicId, systemId) + " via file found file on classpath: " + fileName);
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

        StringBuilder buffer = new StringBuilder("entity with publicId '");
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
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("configurable local entity resolver", LocalEntityResolver.class);

        infoFactory.addAttribute("catalogFileURI", URI.class, true);
        infoFactory.addAttribute("localRepositoryURI", URI.class, true);
        infoFactory.addAttribute("failOnUnresolvable", boolean.class, true);

//        infoFactory.addOperation("resolveEntity", new Class[]{String.class, String.class});
//        infoFactory.addOperation("addPublicMapping", new Class[]{String.class, String.class});
//        infoFactory.addOperation("addSystemMapping", new Class[]{String.class, String.class});

        infoFactory.setConstructor(new String[]{"catalogFileURI", "localRepositoryURI", "failOnUnresolvable"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
