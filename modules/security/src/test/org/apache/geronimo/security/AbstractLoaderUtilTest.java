/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.security;

import javax.xml.parsers.DocumentBuilder;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import org.apache.geronimo.deployment.xml.LocalEntityResolver;
import org.apache.geronimo.deployment.xml.ParserFactory;
import org.apache.geronimo.deployment.xml.ParserFactoryImpl;


/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractLoaderUtilTest extends TestCase {

    private static final File repoDir = new File("src/schema");
    private static final File docDir = new File("src/test-data/xml/deployment");
    private static final File catalogFile = new File(docDir, "resolver-catalog.xml");
    protected LocalEntityResolver resolver;
    protected ParserFactory parserFactory;
    protected DocumentBuilder parser;

    protected void setUp() throws Exception {
        setUp(catalogFile.toURI(), repoDir.toURI());
    }

    protected void setUp(URI catalogFileURI, URI docDirectoryURI) throws Exception {
        resolver = new LocalEntityResolver(catalogFileURI, docDirectoryURI, true);
        parserFactory = new ParserFactoryImpl(resolver);
        parser = parserFactory.getParser();
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        resolver = null;
    }

}
