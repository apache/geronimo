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

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * TODO Decide if we need an entity resolver, and if we do, move this test to where it is.
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/22 22:56:21 $
 */
public class LocalEntityResolverTest extends AbstractLoaderUtilTest {

    private static final File docDir = new File("src/test-data/xml/deployment");
    private static final File catalogFile = new File(docDir, "resolver-catalog.xml");

    private static final String NON_EXISTING_DTD = "-//Keine Ahnung, Inc.//DTD Nonesense 1.0//DE";

    protected void setUp() throws Exception {
        setUp(catalogFile.toURI(), null);
    }

    public void testEntityResolverCatalogPublicId() throws Exception {

        File expected = new File(catalogFile.getParent(), "../../../test-data/xml/deployment/sun/dtd/web-app_2_3.dtd");

        String resolved = resolver.resolveEntity("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN", "c:\\work\\dd\\web.dtd").getSystemId();

        URI resolvedURI = new URI(resolved);
        assertEquals(expected.toURI().normalize(), resolvedURI);

        resolved = resolver.resolveEntity("-//OASIS//DTD DocBook XML V4.1.2//EN", "c:\\work\\dummy\\sonstwo.dtd").getSystemId();
        resolvedURI = new URI(resolved);

        assertEquals(new URI("file:///n:/share/doctypes/docbook/xml/docbookx.dtd"), resolvedURI);

    }

    public void testEntityResolverCatalogSystemId() throws Exception {

        File expected = new File(catalogFile.getParent(), "docbook/xml/docbookx.dtd");

        String resolved = resolver.resolveEntity(NON_EXISTING_DTD, "urn:x-oasis:docbook-xml-v4.1.2").getSystemId();

        URI real = new URI(resolved);

        assertEquals(expected.toURI(), real);

        resolved = resolver.resolveEntity(NON_EXISTING_DTD, "urn:x-oasis:docbook-xml-v4.1.0").getSystemId();
        URI resolvedURI = new URI(resolved);

        assertEquals(new URI("file:/home/kkoehler/share/doctypes/docbook/xml/docbookx.dtd"), resolvedURI);

    }

    public void testNotFoundInCatalog() throws Exception {

        resolver.setFailOnUnresolvable(false);
        assertNull(resolver.resolveEntity(NON_EXISTING_DTD, "urn:x-oasis:docbook-xml-v4.1.1"));

    }

    public void testFailOnUnresolvable() throws IOException {

        resolver.setFailOnUnresolvable(true);

        try {
            resolver.resolveEntity(NON_EXISTING_DTD, "urn:x-oasis:docbook-xml-v4.1.1");
            fail("no exception is thrown");
        } catch (SAXException e) {
            //exception is desired
        }

    }

    public void testAddPublicEntry() throws Exception {

        resolver.addPublicMapping(NON_EXISTING_DTD, "file:/home/kkoehler/geronimo/geronimo.dtd");
        InputSource source = resolver.resolveEntity(NON_EXISTING_DTD, "http://incubator.apache.org/geronimo/geronimo.dtd");
        assertNotNull(source);
        URI resolvedSystemId = new URI(source.getSystemId());
        assertEquals(new URI("file:/home/kkoehler/geronimo/geronimo.dtd"), resolvedSystemId);

    }

    public void testStaticCatalog() throws Exception {

        resolver.addPublicMapping(NON_EXISTING_DTD, "file:/home/kkoehler/geronimo/geronimo.dtd");
        InputSource source = resolver.resolveEntity(NON_EXISTING_DTD, "http://incubator.apache.org/geronimo/geronimo.dtd");
        assertNotNull(source);

        tearDown();
        setUp();

        resolver.setFailOnUnresolvable(false);
        source = resolver.resolveEntity(NON_EXISTING_DTD, "http://incubator.apache.org/geronimo/geronimo.dtd");
        assertNull(source);

    }

    public void testAddSystemEntry() throws Exception {

        resolver.addSystemMapping("urn:x-oasis:docbook-xml-v4.0.9", "file:/home/kkoehler/geronimo/geronimo.dtd");
        InputSource source = resolver.resolveEntity(NON_EXISTING_DTD, "urn:x-oasis:docbook-xml-v4.0.9");
        assertNotNull(source);

    }

    //TODO fix this test
    public void XtestLocalRepository() throws Exception {

        File repo = new File(docDir, "localresolverrepository");
        resolver.setLocalRepositoryURI(new URI(repo.getAbsolutePath()));

        InputSource source = resolver.resolveEntity(NON_EXISTING_DTD, "c:\\work\\some.xsd");
        assertNotNull(source);

        source = resolver.resolveEntity(NON_EXISTING_DTD, "/home/kkoehler/some.xsd");
        assertNotNull(source);

        resolver.setFailOnUnresolvable(false);
        source = resolver.resolveEntity(NON_EXISTING_DTD, "not existing");
        assertNull(source);

    }

    public void testNotFoundWithLocalRepository() throws Exception {

        File repo = new File(docDir, "localresolverrepository");
        resolver.setLocalRepositoryURI(repo.getAbsoluteFile().toURI());

        resolver.setFailOnUnresolvable(false);
        InputSource source = resolver.resolveEntity(NON_EXISTING_DTD, "http://incubator.apache.org/geronimo/geronimo.dtd");
        assertNull(source);

    }

    public void testNullParameters() throws Exception {

        resolver.setFailOnUnresolvable(false);
        InputSource source = resolver.resolveEntity(null, "http://incubator.apache.org/geronimo/geronimo.dtd");
        assertNull(source);

        source = resolver.resolveEntity(null, "urn:x-oasis:docbook-xml-v4.1.0");
        assertNotNull(source);

        source = resolver.resolveEntity(NON_EXISTING_DTD, null);
        assertNull(source);

        source = resolver.resolveEntity("-//OASIS//DTD DocBook XML V4.1.2//EN", null);
        assertNotNull(source);

        source = resolver.resolveEntity(null, null);
        assertNull(source);

    }

}
