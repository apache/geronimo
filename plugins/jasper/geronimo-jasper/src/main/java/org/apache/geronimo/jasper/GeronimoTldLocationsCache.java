/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.jasper;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;

import javax.servlet.ServletContext;

import org.apache.geronimo.kernel.osgi.BundleResourceFinder;
import org.apache.geronimo.kernel.osgi.DelegatingBundle;
import org.apache.geronimo.kernel.osgi.BundleResourceFinder.ResourceFinderCallback;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.TldLocationsCache;
import org.apache.jasper.xmlparser.ParserUtils;
import org.apache.jasper.xmlparser.TreeNode;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * A container for all tag libraries that are defined "globally"
 * for the web application.
 *
 * Tag Libraries can be defined globally in one of two ways:
 *   1. Via <taglib> elements in web.xml:
 *      the uri and location of the tag-library are specified in
 *      the <taglib> element.
 *   2. Via packaged jar files that contain .tld files
 *      within the META-INF directory, or some subdirectory
 *      of it. The taglib is 'global' if it has the <uri>
 *      element defined.
 *
 * A mapping between the taglib URI and its associated TaglibraryInfoImpl
 * is maintained in this container.
 * Actually, that's what we'd like to do. However, because of the
 * way the classes TagLibraryInfo and TagInfo have been defined,
 * it is not currently possible to share an instance of TagLibraryInfo
 * across page invocations. A bug has been submitted to the spec lead.
 * In the mean time, all we do is save the 'location' where the
 * TLD associated with a taglib URI can be found.
 *
 * When a JSP page has a taglib directive, the mappings in this container
 * are first searched (see method getLocation()).
 * If a mapping is found, then the location of the TLD is returned.
 * If no mapping is found, then the uri specified
 * in the taglib directive is to be interpreted as the location for
 * the TLD of this tag library.
 *
 * This class was copied from tomcat to allow Geronimo
 * to override Jasper's default TldLocationsCache which does not work
 * with Geronimo's MultiParentClassLoader.  Copying was necessary because
 * most of the essential methods and member variables were private.
 */

public class GeronimoTldLocationsCache extends TldLocationsCache {
    private static final Logger log = LoggerFactory.getLogger(GeronimoTldLocationsCache.class);

    /**
     * The types of URI one may specify for a tag library
     */
    public static final int ABS_URI = 0;
    public static final int ROOT_REL_URI = 1;
    public static final int NOROOT_REL_URI = 2;

    private static final String WEB_XML = "/WEB-INF/web.xml";
    private static final String FILE_PROTOCOL = "file:";
    private static final String JAR_FILE_SUFFIX = ".jar";

    /**
     * The mapping of the 'global' tag library URI to the location (resource
     * path) of the TLD associated with that tag library. The location is
     * returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location of the tld.
     */
    private Hashtable<String,String[]> mappings;

    private boolean initialized;
    private ServletContext ctxt;

    //*********************************************************************
    // Constructor and Initilizations

    public GeronimoTldLocationsCache(ServletContext ctxt) {
        super(ctxt);
        this.ctxt = ctxt;
        this.mappings = new Hashtable<String,String[]>();
    }

    /**
     * Sets the list of JARs that are known not to contain any TLDs.
     *
     * @param jarNames List of comma-separated names of JAR files that are
     * known not to contain any TLDs
     */
    public static void setNoTldJars(String jarNames) {
    }

    /**
     * Gets the 'location' of the TLD associated with the given taglib 'uri'.
     *
     * Returns null if the uri is not associated with any tag library 'exposed'
     * in the web application. A tag library is 'exposed' either explicitly in
     * web.xml or implicitly via the uri tag in the TLD of a taglib deployed
     * in a jar file (WEB-INF/lib).
     *
     * @param uri The taglib uri
     *
     * @return An array of two Strings: The first element denotes the real
     * path to the TLD. If the path to the TLD points to a jar file, then the
     * second element denotes the name of the TLD entry in the jar file.
     * Returns null if the uri is not associated with any tag library 'exposed'
     * in the web application.
     */
    public String[] getLocation(String uri) throws JasperException {
        if (!initialized) {
            init();
        }
        return (String[]) mappings.get(uri);
    }

    private void init() throws JasperException {
        if (initialized) return;
        try {
            processWebDotXml();
            Bundle bundle = getBundle();
            if (bundle != null) {
                processWebInf(bundle);
                processClassPath(bundle);
            }
            initialized = true;
        } catch (Exception ex) {
            throw new JasperException(ex);
        }
    }

    private Bundle getBundle() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader instanceof BundleReference) {
            Bundle bundle = ((BundleReference) classLoader).getBundle();
            if (bundle instanceof DelegatingBundle) {
                return ((DelegatingBundle) bundle).getMainBundle();
            } else {
                return bundle;
            }
        }
        return null;
    }
    
    /*
     * Populates taglib map described in web.xml.
     */
    private void processWebDotXml() throws Exception {

        InputStream is = null;

        try {
            // Acquire input stream to web application deployment descriptor
            String altDDName = (String)ctxt.getAttribute(
                                                    Constants.ALT_DD_ATTR);
            URL uri = null;
            if (altDDName != null) {
                try {
                    uri = new URL(FILE_PROTOCOL+altDDName.replace('\\', '/'));
                } catch (MalformedURLException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("file not found: " + altDDName);
                    }
                }
            } else {
                uri = ctxt.getResource(WEB_XML);
                if (uri == null && log.isWarnEnabled()) {
                    log.warn("file not found: " + WEB_XML);
                }
            }

            if (uri == null) {
                return;
            }
            is = uri.openStream();
            InputSource ip = new InputSource(is);
            ip.setSystemId(uri.toExternalForm());

            // Parse the web application deployment descriptor
            TreeNode webtld = null;
            // altDDName is the absolute path of the DD
            if (altDDName != null) {
                webtld = new ParserUtils().parseXMLDocument(altDDName, ip);
            } else {
                webtld = new ParserUtils().parseXMLDocument(WEB_XML, ip);
            }

            // Allow taglib to be an element of the root or jsp-config (JSP2.0)
            TreeNode jspConfig = webtld.findChild("jsp-config");
            if (jspConfig != null) {
                webtld = jspConfig;
            }
            Iterator<TreeNode> taglibs = webtld.findChildren("taglib");
            while (taglibs.hasNext()) {

                // Parse the next <taglib> element
                TreeNode taglib = taglibs.next();
                String tagUri = null;
                String tagLoc = null;
                TreeNode child = taglib.findChild("taglib-uri");
                if (child != null)
                    tagUri = child.getBody();
                child = taglib.findChild("taglib-location");
                if (child != null)
                    tagLoc = child.getBody();

                // Save this location if appropriate
                if (tagLoc == null)
                    continue;
                if (uriType(tagLoc) == NOROOT_REL_URI)
                    tagLoc = "/WEB-INF/" + tagLoc;
                String tagLoc2 = null;
                if (tagLoc.endsWith(JAR_FILE_SUFFIX)) {
                    tagLoc = ctxt.getResource(tagLoc).toString();
                    tagLoc2 = "META-INF/taglib.tld";
                }
                mappings.put(tagUri, new String[] { tagLoc, tagLoc2 });
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable t) {}
            }
        }
    }

    private void processClassPath(Bundle bundle) throws Exception {
        ServiceReference reference = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
        PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);
                
        BundleResourceFinder resourceFinder = new BundleResourceFinder(packageAdmin, bundle, "META-INF/", ".tld");
        resourceFinder.find(new ResourceFinderCallback() {

            public void foundDirectory(Bundle bundle, String basePath, URL url) throws Exception {
                addMapping(url, new String[] {url.getPath(), null});
            }

            public void foundJar(Bundle bundle, String jarName, ZipEntry entry) throws Exception {
                URL jarURL = bundle.getEntry(jarName);
                URL url = new URL("jar:" + jarURL.toString() + "!/" + entry.getName());
                
                addMapping(url, new String[] {jarURL.toString(), entry.getName()});
            }
            
        });
          
        bundle.getBundleContext().ungetService(reference);
    }

    private void processWebInf(Bundle bundle) throws JasperException, IOException {
        Enumeration e = bundle.findEntries("WEB-INF/", "*.tld", true);
        if (e != null) {
            while (e.hasMoreElements()) {
                URL u = (URL) e.nextElement();
                addMapping(u, new String[] {u.getPath(), null});
            }
        }
    }

    private void addMapping(URL url, String[] location) throws JasperException, IOException {
        String path = url.toString();
        InputStream stream = url.openStream();
        String uri = null;
        try {
            uri = getUriFromTld(path, stream);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Throwable t) {
                    // do nothing
                }
            }
        }
        // Add implicit map entry only if its uri is not already
        // present in the map
        if (uri != null && mappings.get(uri) == null) {
            mappings.put(uri, location);
        }
    }
    
    /*
     * Returns the value of the uri element of the given TLD, or null if the
     * given TLD does not contain any such element.
     */
    private String getUriFromTld(String resourcePath, InputStream in)
        throws JasperException
    {
        // Parse the tag library descriptor at the specified resource path
        TreeNode tld = new ParserUtils().parseXMLDocument(resourcePath, in);
        TreeNode uri = tld.findChild("uri");
        if (uri != null) {
            String body = uri.getBody();
            if (body != null)
                return body;
        }

        return null;
    }

}
