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
package org.apache.geronimo.deployment.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.zip.ZipException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Scanner that searches a remote site using WebDAV looking for deployments.
 * Sub-collections that themselves contain a Manifest are
 * assumed to be deployable; others may be recursed into
 *
 * @todo we should cache results between scans to reduce network traffic
 * @version $Revision: 1.5 $ $Date: 2003/08/23 09:07:11 $
 */
public class WebDAVScanner implements Scanner {
    private final URL base;
    private final HostConfiguration hostConfig;
    private final boolean recurse;
    private final HttpClient httpClient;
    private final SAXParser parser;
    private final DavHandler handler = new DavHandler();

    /**
     * Constructor taking a base URL and whether to recurse into sub-collections.
     * @param base the base URL on the WebDAV Server
     * @param recurse if true, the scan will recurse into sub-collections
     */
    public WebDAVScanner(URL base, boolean recurse) {
        assert base.toString().endsWith("/");
        this.base = base;
        this.recurse = recurse;

        // all this so we can get a connection to close...
        try {
            hostConfig = new HostConfiguration();
            URI uri = new URI(base.toString());
            hostConfig.setHost(uri);
            httpClient = new HttpClient();
            httpClient.setHostConfiguration(hostConfig);
        } catch (URIException e) {
            throw new IllegalArgumentException("Invalid URL " + base);
        }

        // set up the parser for the response
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
        try {
            parser = saxParserFactory.newSAXParser();
        } catch (Exception e) {
            throw new AssertionError("Unable to allocate SAXParser: " + e.getMessage());
        }
    }

    public synchronized Set scan() throws IOException {
        try {
            Set result = new HashSet();
            LinkedList toScan = new LinkedList();
            toScan.add(base);
            while (!toScan.isEmpty()) {
                URL scanURL = (URL) toScan.removeFirst();
                Set scanResult = scanCollection(scanURL);
                for (Iterator i = scanResult.iterator(); i.hasNext();) {
                    URL url = (URL) i.next();
                    if (scanURL.equals(url)) {
                        // ignore the collection we scanned
                        continue;
                    }
                    URLType type = getType(url);
                    if (type == URLType.COLLECTION) {
                        if (recurse) {
                            toScan.addLast(url);
                        }
                    } else {
                        result.add(new URLInfo(url, type));
                    }
                }
                handler.result.clear();
            }
            return result;
        } finally {
            handler.result.clear();
            HttpConnection conn = httpClient.getHttpConnectionManager().getConnection(hostConfig);
            conn.close();
            httpClient.getHttpConnectionManager().releaseConnection(conn);
        }
    }

    /**
     * See if the specified URL has a manifest
     * @param url the URL to check
     * @return true if the URL has a manifest
     * @throws IOException if there was a problem talking to the server
     */
    private URLType getType(URL url) throws IOException {
        if (url.toString().endsWith("/")) {
            URL metaInfURL = new URL(url, "META-INF/MANIFEST.MF");
            HttpMethod getMethod = new GetMethod(metaInfURL.toString());
            try {
                int status = httpClient.executeMethod(getMethod);
                return status == 200 ? URLType.UNPACKED_ARCHIVE : URLType.COLLECTION;
            } finally {
                getMethod.releaseConnection();
            }
        } else {
            URL jarURL = new URL("jar:" + url.toString() + "!/");
            JarURLConnection jarConnection = (JarURLConnection) jarURL.openConnection();
            try {
                jarConnection.getManifest();
                return URLType.PACKED_ARCHIVE;
            } catch (ZipException e) {
                return URLType.RESOURCE;
            }
        }
    }

    /**
     * Scan the supplied URL for all first-level members. We do not do a deep
     * scan as we do not want to recurse into entries with META-INFs
     * @param collection the collection to scan
     * @return a Set<URL> of members found
     * @throws IOException if there was a problem talking to the server
     */
    private Set scanCollection(URL collection) throws IOException {
        assert collection.toString().endsWith("/");
        HttpMethod method = new PropfindMethod(collection.toString());
        try {
            method.setFollowRedirects(true);
            method.setRequestHeader("DAV", "1");
            method.setRequestHeader("Depth", "1");
            int status = httpClient.executeMethod(method);
            if (status != 207) {
                throw new IOException("WebDAV request returned status " + status);
            }

            InputStream is = method.getResponseBodyAsStream();
            parser.parse(is, handler);
            return handler.result;
        } catch (SAXException e) {
            throw new IOException("Unable to parse response");
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * The WebDAV PROPFIND method - see RFC2518
     */
    private static final class PropfindMethod extends HttpMethodBase {
        public PropfindMethod(String uri) {
            super(uri);
        }

        public String getName() {
            return "PROPFIND";
        }
    }

    /**
     * SAX event parser that extracts the contents of the DAV:href elements
     * it the response
     */
    private final class DavHandler extends DefaultHandler {
        private final Set result = new HashSet();
        private final StringBuffer filename = new StringBuffer();
        private boolean capture;

        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes)
                throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if ("DAV:".equals(uri) && "href".equals(localName)) {
                capture = true;
            }
        }

        public void characters(char ch[], int start, int length)
                throws SAXException {
            super.characters(ch, start, length);
            if (capture) {
                filename.append(ch, start, length);
            }
        }

        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
            if ("DAV:".equals(uri) && "href".equals(localName)) {
                capture = false;
                try {
                    result.add(new URL(base, filename.toString()));
                } catch (MalformedURLException e) {
                    throw new SAXException(e);
                }
                filename.setLength(0);
            }
        }
    }
}
