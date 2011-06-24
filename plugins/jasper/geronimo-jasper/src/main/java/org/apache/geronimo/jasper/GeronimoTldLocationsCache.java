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
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.servlet.ServletContext;

import org.apache.xbean.osgi.bundle.util.BundleResourceFinder;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.xbean.osgi.bundle.util.BundleResourceFinder.ResourceFinderCallback;
import org.apache.xbean.osgi.bundle.util.jar.BundleJarFile;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JarResource;
import org.apache.jasper.compiler.TldLocation;
import org.apache.jasper.compiler.TldLocationsCache;
import org.apache.jasper.compiler.WebXml;
import org.apache.jasper.xmlparser.ParserUtils;
import org.apache.jasper.xmlparser.TreeNode;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final String WEB_INF = "/WEB-INF/";
    private static final String WEB_INF_LIB = "/WEB-INF/lib/";
    private static final String JAR_EXT = ".jar";
    private static final String TLD_EXT = ".tld";

    /**
     * The mapping of the 'global' tag library URI to the location (resource
     * path) of the TLD associated with that tag library. The location is
     * returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location of the tld.
     */
    private Hashtable<String, TldLocation> mappings;

    private boolean initialized;
    private ServletContext ctxt;

    //*********************************************************************
    // Constructor and Initilizations

    public GeronimoTldLocationsCache(ServletContext ctxt) {
        super(ctxt);
        this.ctxt = ctxt;
        this.mappings = new Hashtable<String, TldLocation>();
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
    public TldLocation getLocation(String uri) throws JasperException {
        if (!initialized) {
            init();
        }
        return mappings.get(uri);
    }

    private void init() throws JasperException {
        if (initialized) return;
        try {
            tldScanWebXml();
            tldScanResourcePaths(WEB_INF);

            Bundle bundle = BundleUtils.getContextBundle(true);
            if (bundle != null) {
                tldScanClassPath(bundle);
                tldScanGlobal(bundle);
            }
            initialized = true;
        } catch (Exception ex) {
            throw new JasperException(ex);
        }
    }
    
    /*
     * Populates taglib map described in web.xml.
     * 
     * This is not kept in sync with o.a.c.startup.TldConfig as the Jasper only
     * needs the URI to TLD mappings from scan web.xml whereas TldConfig needs
     * to scan the actual TLD files.
     */    
    private void tldScanWebXml() throws Exception {

        WebXml webXml = null;
        try {
            webXml = new WebXml(ctxt);
            if (webXml.getInputSource() == null) {
                return;
            }
            
            // Parse the web application deployment descriptor
            TreeNode webtld = null;
            webtld = new ParserUtils().parseXMLDocument(webXml.getSystemId(),
                    webXml.getInputSource());

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
                TldLocation location;
                if (tagLoc.endsWith(JAR_EXT)) {
                    location = new TldLocation("META-INF/taglib.tld", ctxt.getResource(tagLoc).toString());
                } else {
                    location = new TldLocation(tagLoc);
                }
                mappings.put(tagUri, location);
            }
        } finally {
            if (webXml != null) {
                webXml.close();
            }
        }
    }
    
    /*
     * Scans the web application's sub-directory identified by startPath,
     * along with its sub-directories, for TLDs and adds an implicit map entry
     * to the taglib map for any TLD that has a <uri> element.
     *
     * Initially, rootPath equals /WEB-INF/. The /WEB-INF/classes and
     * /WEB-INF/lib sub-directories are excluded from the search, as per the
     * JSP 2.0 spec.
     * 
     * Keep code in sync with o.a.c.startup.TldConfig
     */
    private void tldScanResourcePaths(String startPath)
            throws Exception {

        Set<String> dirList = ctxt.getResourcePaths(startPath);
        if (dirList != null) {
            Iterator<String> it = dirList.iterator();
            while (it.hasNext()) {
                String path = it.next();
                if (!path.endsWith(TLD_EXT)
                        && (path.startsWith(WEB_INF_LIB)
                                || path.startsWith("/WEB-INF/classes/"))) {
                    continue;
                }
                if (path.endsWith(TLD_EXT)) {
                    if (path.startsWith("/WEB-INF/tags/") &&
                            !path.endsWith("implicit.tld")) {
                        continue;
                    }
                    InputStream stream = ctxt.getResourceAsStream(path);
                    tldScanStream(stream, new TldLocation(path));
                } else {
                    tldScanResourcePaths(path);
                }
            }
        }
    }    
    
    private void tldScanClassPath(Bundle bundle) throws Exception {
        ServiceReference reference = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
        PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);
                
        BundleResourceFinder resourceFinder = new BundleResourceFinder(packageAdmin, bundle, "META-INF/", ".tld");
        resourceFinder.find(new ResourceFinderCallback() {

            public boolean foundInDirectory(Bundle bundle, String basePath, URL url) throws Exception {
                tldScanStream(url, new TldLocation(url.getPath()));
                return true;
            }

            public boolean foundInJar(Bundle bundle, String jarName, ZipEntry entry, InputStream in) throws Exception {
                URL jarURL = bundle.getEntry(jarName); 
                tldScanStream(in, new TldLocation(entry.getName(), jarURL.toExternalForm()));
                return true;
            }
            
        });
          
        bundle.getBundleContext().ungetService(reference);
    }
   
    private void tldScanGlobal(Bundle bundle) throws JasperException, IOException, InvalidSyntaxException {
        BundleContext bundleContext = bundle.getBundleContext();
        ServiceReference reference = bundleContext.getServiceReference(TldRegistry.class.getName());
        if (reference != null) {
            TldRegistry tldRegistry = (TldRegistry) bundleContext.getService(reference);
            for (TldProvider.TldEntry entry : tldRegistry.getDependentTlds(bundle)) {
                URL url = entry.getURL();
                if (entry.getJarUrl() != null) { 
                    tldScanStream(url, new TldLocation(entry.getName(), entry.getJarUrl().toExternalForm()));
                } else {
                    tldScanStream(url, new TldLocation(entry.getName(), new BundleJarResource(entry.getBundle())));
                }
            }
            bundleContext.ungetService(reference);            
        }
    }
    
    private void tldScanStream(URL url, TldLocation location) throws IOException {
        InputStream in = url.openStream();
        tldScanStream(in, location);
    }
    
    /*
     * Scan the TLD contents in the specified input stream and add any new URIs
     * to the map.
     * 
     * @param resourcePath  Path of the resource
     * @param entryName     If the resource is a JAR file, the name of the entry
     *                      in the JAR file
     * @param stream        The input stream for the resource
     * @throws IOException
     */
    private void tldScanStream(InputStream stream, TldLocation location) throws IOException {
        String path = (location.getJarResource() == null) ?  location.getName() : location.getJarResource().getUrl();
        try {
            // Parse the tag library descriptor at the specified resource path
            String uri = null;

            TreeNode tld =
                new ParserUtils().parseXMLDocument(path, stream);
            TreeNode uriNode = tld.findChild("uri");
            if (uriNode != null) {
                String body = uriNode.getBody();
                if (body != null)
                    uri = body;
            }

            // Add implicit map entry only if its uri is not already
            // present in the map
            if (uri != null && mappings.get(uri) == null) {
                mappings.put(uri, location);
            }
        } catch (JasperException e) {
            // Hack - makes exception handling simpler
            throw new IOException(e);
        } finally {
            try { stream.close(); } catch (IOException ignore) {}
        }
    }
    
    private static class BundleJarResource implements JarResource {
               
        private Bundle bundle;
        private URL url;
        
        public BundleJarResource(Bundle bundle) {           
            this.bundle = bundle;
            this.url = bundle.getEntry("/");
        }
        
        public JarFile getJarFile() throws IOException {
            return new BundleJarFile(bundle);
        }
        
        public String getUrl() {
            return url.toExternalForm();
        }
        
        public URL getEntry(String name) {
            return bundle.getEntry(name);
        }
    }
}
