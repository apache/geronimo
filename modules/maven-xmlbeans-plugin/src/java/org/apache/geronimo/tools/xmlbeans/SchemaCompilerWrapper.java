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

package org.apache.geronimo.tools.xmlbeans;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;


/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/10 23:06:31 $
 *
 * */
public class SchemaCompilerWrapper {

    public static void CompileSchemas(String sourceDir, String sourceSchemas, String xmlConfigs, String targetDir, String catalogLocation) throws Exception {
        List schemas = new ArrayList();
        File base = new File(sourceDir);
        for (StringTokenizer st = new StringTokenizer(sourceSchemas, ","); st.hasMoreTokens();) {
            String schemaName = st.nextToken();
            schemas.add(new File(base, schemaName));
        }
        List configs = new ArrayList();

        if (xmlConfigs != null) {
            for (StringTokenizer st = new StringTokenizer(xmlConfigs, ","); st.hasMoreTokens();) {
                String configName = st.nextToken();
                configs.add(new File(configName));
            }
        }
        EntityResolver entityResolver = null;
        if (catalogLocation != null) {
            CatalogManager catalogManager = CatalogManager.getStaticManager();
            catalogManager.setCatalogFiles(catalogLocation);
            entityResolver = new PassThroughResolver(new CatalogResolver());
        }
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setBaseDir(null);
        params.setXsdFiles((File[])schemas.toArray(new File[] {}));
        params.setWsdlFiles(new File[] {});
        params.setJavaFiles(new File[] {});
        params.setConfigFiles((File[])configs.toArray(new File[] {}));
        params.setClasspath(new File[] {});
        params.setOutputJar(null);
        params.setName(null);
        params.setSrcDir(new File(targetDir));
        params.setClassesDir(new File(targetDir));
        params.setCompiler(null);
        params.setJar(null);
        params.setMemoryInitialSize(null);
        params.setMemoryMaximumSize(null);
        params.setNojavac(true);
        params.setQuiet(false);
        params.setVerbose(true);
        params.setDownload(true);
        params.setNoUpa(true);
        params.setNoPvr(true);
        params.setDebug(true);
        params.setErrorListener(new ArrayList());
        params.setRepackage(null);
        params.setExtensions(null);
        params.setJaxb(false);
        params.setMdefNamespaces(null);
        params.setEntityResolver(entityResolver);

        boolean result = SchemaCompiler.compile(params);
        if (!result) {
            Collection errors = params.getErrorListener();
            for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
                Object o = (Object) iterator.next();
                System.out.println("xmlbeans error: " + o);
            }
            throw new Exception("Schema compilation failed");
        }

    }

    private static class PassThroughResolver implements EntityResolver {

        private final EntityResolver delegate;

        public PassThroughResolver(EntityResolver delegate) {
            this.delegate = delegate;
        }
        public InputSource resolveEntity(String publicId,
                                         String systemId)
                throws SAXException, IOException {
            InputSource is = delegate.resolveEntity(publicId, systemId);
            if (is != null) {
                return is;
            }
            System.out.println("Could not resolve publicId: " + publicId + ", systemId: " + systemId + " from catalog");
            return new InputSource(systemId);
        }

    }
}
