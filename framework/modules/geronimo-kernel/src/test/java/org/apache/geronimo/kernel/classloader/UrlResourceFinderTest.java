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

package org.apache.geronimo.kernel.classloader;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import org.apache.geronimo.testsupport.TestSupport;

/**
 * @version $Rev$ $Date$
 */
public class UrlResourceFinderTest extends TestSupport {
    private File jarFile;
    private Manifest manifest;
    private Attributes resourceAttributes;
    private File alternateJarFile;
    private File testResource;

    /**
     * There are 2 "jars" with a "resource" inside.  Make sure the enumeration has exactly 2 elements and
     * that hasMoreElements() doesn't advance the iterator.
     *
     * @throws Exception
     */
    public void testResourceEnumeration() throws Exception {
        URL jar1 = new File(BASEDIR, "src/test/data/resourceFinderTest/jar1/").toURL();
        URL jar2 = new File(BASEDIR, "src/test/data/resourceFinderTest/jar2/").toURL();
        UrlResourceFinder resourceFinder = new UrlResourceFinder(new URL[]{jar1, jar2});

        Enumeration enumeration = resourceFinder.findResources("resource");

        // resource1
        assertTrue(enumeration.hasMoreElements());
        assertTrue(enumeration.hasMoreElements());
        URL resource1 = (URL) enumeration.nextElement();
        assertNotNull(resource1);
        assertEquals("resource1", toString(resource1.openStream()));

        // resource2
        assertTrue(enumeration.hasMoreElements());
        assertTrue(enumeration.hasMoreElements());
        URL resource2 = (URL) enumeration.nextElement();
        assertNotNull(resource2);
        assertEquals("resource2", toString(resource2.openStream()));
        assertFalse(enumeration.hasMoreElements());
    }

    public void testDirectoryResource() throws Exception {
        URL jar = new File(BASEDIR, "src/test/data/resourceFinderTest/jar1/").toURL();
        UrlResourceFinder resourceFinder = new UrlResourceFinder(new URL[]{jar});

        ResourceHandle resource = resourceFinder.getResource("resource");
        assertNotNull(resource);

        // handle.getBytes()
        assertEquals("resource1", new String(resource.getBytes()));

        // handle.getInputStream()
        assertEquals("resource1", toString(resource.getInputStream()));

        // handle.getUrl()
        URL url = resource.getUrl();
        assertEquals("resource1", toString(url.openStream()));

        // copy the url and verify we can still get the data
        URL copyUrl = new URL(url.toExternalForm());
        assertEquals("resource1", toString(copyUrl.openStream()));

        // resourceFinder.findResource
        URL directUrl = resourceFinder.findResource("resource");
        assertEquals("resource1", toString(directUrl.openStream()));
        assertEquals("resource1", toString(new URL(directUrl.toExternalForm()).openStream()));

        // handle.getContentLength()
        assertEquals("resource1".length(), resource.getContentLength());

        // handle.getName()
        assertEquals("resource", resource.getName());

        // handle.getAttributes()
        assertNull(resource.getAttributes());

        // handle.getManifest()
        assertNull(resource.getManifest());
    }

    public void testDirectoryResourceScope() throws Exception {
        URL jar = new File(BASEDIR, "src/test/data/resourceFinderTest/jar1/").toURL();
        UrlResourceFinder resourceFinder = new UrlResourceFinder(new URL[]{jar});

        ResourceHandle resource = resourceFinder.getResource("../jar2/resource");
        assertNull(resource);
    }
    
    public void testJarResource() throws Exception {
        URL jar = jarFile.toURL();
        UrlResourceFinder resourceFinder = new UrlResourceFinder(new URL[]{jar});

        ResourceHandle resource = resourceFinder.getResource("resource");
        assertNotNull(resource);

        // handle.getBytes()
        assertEquals("resource3", new String(resource.getBytes()));

        // handle.getInputStream()
        assertEquals("resource3", toString(resource.getInputStream()));

        // handle.getUrl()
        URL url = resource.getUrl();
        assertEquals("resource3", toString(url.openStream()));

        // copy the url and verify we can still get the data
        URL copyUrl = new URL(url.toExternalForm());
        assertEquals("resource3", toString(copyUrl.openStream()));

        // resourceFinder.findResource
        URL directUrl = resourceFinder.findResource("resource");
        assertEquals("resource3", toString(directUrl.openStream()));
        assertEquals("resource3", toString(new URL(directUrl.toExternalForm()).openStream()));

        // handle.getContentLength()
        assertEquals("resource3".length(), resource.getContentLength());

        // handle.getName()
        assertEquals("resource", resource.getName());

        // handle.getAttributes()
        assertEquals(resourceAttributes, resource.getAttributes());

        // handle.getManifest()
        assertEquals(manifest, resource.getManifest());
    }

    public void testAddURL() throws Exception {
        URL jar1 = new File(BASEDIR, "src/test/data/resourceFinderTest/jar1/").toURL();
        UrlResourceFinder resourceFinder = new UrlResourceFinder(new URL[]{jar1});

        Enumeration enumeration = resourceFinder.findResources("resource");

        // resource1
        assertTrue(enumeration.hasMoreElements());
        assertTrue(enumeration.hasMoreElements());
        URL resource1 = (URL) enumeration.nextElement();
        assertNotNull(resource1);
        assertEquals("resource1", toString(resource1.openStream()));
        assertFalse(enumeration.hasMoreElements());

        // addUrl
        URL jar2 = new File(BASEDIR, "src/test/data/resourceFinderTest/jar2/").toURL();
        resourceFinder.addUrl(jar2);

        // getResource should find the first jar only
        ResourceHandle resource = resourceFinder.getResource("resource");
        assertNotNull(resource);
        assertEquals("resource1", new String(resource.getBytes()));

        // findResource should find the first jar only
        resource1 = resourceFinder.findResource("resource");
        assertEquals("resource1", toString(resource1.openStream()));

        // findResouces should see both jars
        enumeration = resourceFinder.findResources("resource");

        // resource1
        assertTrue(enumeration.hasMoreElements());
        assertTrue(enumeration.hasMoreElements());
        resource1 = (URL) enumeration.nextElement();
        assertNotNull(resource1);
        assertEquals("resource1", toString(resource1.openStream()));
        assertTrue(enumeration.hasMoreElements());

        // resource2
        assertTrue(enumeration.hasMoreElements());
        assertTrue(enumeration.hasMoreElements());
        URL resource2 = (URL) enumeration.nextElement();
        assertNotNull(resource2);
        assertEquals("resource2", toString(resource2.openStream()));
        assertFalse(enumeration.hasMoreElements());
    }

    public void testConcurrentAddURL() throws Exception {
        URL jar1 = new File(BASEDIR, "src/test/data/resourceFinderTest/jar1/").toURL();
        URL jar2 = new File(BASEDIR, "src/test/data/resourceFinderTest/jar2/").toURL();
        UrlResourceFinder resourceFinder = new UrlResourceFinder(new URL[]{jar1, jar2});

        Enumeration enumeration = resourceFinder.findResources("resource");

        // resource1
        assertTrue(enumeration.hasMoreElements());
        assertTrue(enumeration.hasMoreElements());
        URL resource1 = (URL) enumeration.nextElement();
        assertNotNull(resource1);
        assertEquals("resource1", toString(resource1.openStream()));
        assertTrue(enumeration.hasMoreElements());

        //
        // addURL
        //
        URL newJar = jarFile.toURL();
        resourceFinder.addUrl(newJar);

        // new resources should be available
        // getResource should find the first jar only
        ResourceHandle jar3Resouce = resourceFinder.getResource("jar3");
        assertNotNull(jar3Resouce);
        assertEquals("jar3", new String(jar3Resouce.getBytes()));

        // findResource should find the first jar only
        URL jar3Url = resourceFinder.findResource("jar3");
        assertEquals("jar3", toString(jar3Url.openStream()));

        //
        // enumeration from above should still be valid, but only see the resources available at the time it was created
        //

        // resource2
        assertTrue(enumeration.hasMoreElements());
        assertTrue(enumeration.hasMoreElements());
        URL resource2 = (URL) enumeration.nextElement();
        assertNotNull(resource2);
        assertEquals("resource2", toString(resource2.openStream()));
        assertFalse(enumeration.hasMoreElements());
    }

    public void testDirectoryDestroy() throws Exception {
        URL jar = new File(BASEDIR, "src/test/data/resourceFinderTest/jar1/").toURL();
        UrlResourceFinder resourceFinder = new UrlResourceFinder(new URL[]{jar});
        assertDestroyed(resourceFinder, "resource1", null);
    }

    public void testJarDestroy() throws Exception {
        URL jar = jarFile.toURL();
        UrlResourceFinder resourceFinder = new UrlResourceFinder(new URL[]{jar});
        assertDestroyed(resourceFinder, "resource3", manifest);
    }

    public void testUrlCopy() throws Exception {
        URL jar = jarFile.toURL();
        UrlResourceFinder resourceFinder = new UrlResourceFinder(new URL[]{jar});

        // get the resource
        URL resource = resourceFinder.findResource("resource");
        assertNotNull(resource);
        assertEquals("resource3", toString(resource.openStream()));

        // copy resource with string
        URL stringCopy = new URL(resource.toExternalForm());
        assertEquals("resource3", toString(stringCopy.openStream()));

        // copy resource perserving the url handler
        URL handlerCopy = new URL(resource, resource.toExternalForm());
        assertEquals("resource3", toString(handlerCopy.openStream()));

        // access the other resource using the original url as a starting point
        URL other = new URL(resource, "jar3");
        assertEquals("jar3", toString(other.openStream()));
    }

    public void testUrlAccess() throws Exception {
        URL jar = jarFile.toURL();
        UrlResourceFinder resourceFinder = new UrlResourceFinder(new URL[]{jar});

        // get geronimo url from the resource finder
        URL geronimoUrl = resourceFinder.findResource("resource");
        assertNotNull(geronimoUrl);
        assertEquals("resource3", toString(geronimoUrl.openStream()));

        // get a system url by copying the url by string
        URL systemUrl = new URL(geronimoUrl.toExternalForm());
        assertEquals("resource3", toString(systemUrl.openStream()));

        // verify both can see the jar3 file withing the jar file
        assertEquals("jar3", toString(new URL(systemUrl, "jar3").openStream()));
        assertEquals("jar3", toString(new URL(geronimoUrl, "jar3").openStream()));

        // verify both can see the jar3 file withing the jar file using a full url spec
        String mainEntry = "jar:" + jarFile.toURL().toExternalForm() + "!/jar3";
        assertEquals("jar3", toString(new URL(systemUrl, mainEntry).openStream()));
        assertEquals("jar3", toString(new URL(geronimoUrl, mainEntry).openStream()));

        // verify both throw a FileNotFoundExcetion for an unknown file
        try {
            new URL(systemUrl, "unknown").openStream();
            fail("Expected a FileNotFoundException");
        } catch (FileNotFoundException expected) {
        }
        try {
            new URL(geronimoUrl, "unknown").openStream();
            fail("Expected a FileNotFoundException");
        } catch (FileNotFoundException expected) {
        }

        // verify both can see the alternate jar
        String alternateEntry = "jar:" + alternateJarFile.toURL().toExternalForm() + "!/jar4";
        assertEquals("jar4", toString(new URL(systemUrl, alternateEntry).openStream()));
        assertEquals("jar4", toString(new URL(geronimoUrl, alternateEntry).openStream()));

        // verify both throw a FileNotFoundExcetion for an unknown entry in the alternate file
        String alternateUnknownEntry = "jar:" + alternateJarFile.toURL().toExternalForm() + "!/unknown";
        try {
            new URL(systemUrl, alternateUnknownEntry).openStream();
            fail("Expected a FileNotFoundException");
        } catch (FileNotFoundException expected) {
        }
        try {
            new URL(geronimoUrl, alternateUnknownEntry).openStream();
            fail("Expected a FileNotFoundException");
        } catch (FileNotFoundException expected) {
        }

        // verify both work an excepton for a non-jar entry
        assertEquals("testResource", toString(new URL(systemUrl, testResource.toURL().toExternalForm()).openStream()));
        assertEquals("testResource", toString(new URL(geronimoUrl, testResource.toURL().toExternalForm()).openStream()));

        // verify both fail for a spec without a !/
        String badEntry = "jar:" + alternateJarFile.toURL().toExternalForm();
        try {
            new URL(systemUrl, badEntry).openStream();
            fail("Expected a FileNotFoundException");
        } catch (MalformedURLException expected) {
        }
        try {
            new URL(geronimoUrl, badEntry).openStream();
            fail("Expected a FileNotFoundException");
        } catch (MalformedURLException expected) {
        }

        // verify both throw FileNotFoundException for a nested jar file
        badEntry = "jar:" + alternateJarFile.toURL().toExternalForm() + "!/foo.jar!/bar";
        try {
            new URL(systemUrl, badEntry).openStream();
            fail("Expected a FileNotFoundException");
        } catch (FileNotFoundException expected) {
        }
        try {
            new URL(geronimoUrl, badEntry).openStream();
            fail("Expected a FileNotFoundException");
        } catch (FileNotFoundException expected) {
        }
    }

    public void assertDestroyed(UrlResourceFinder resourceFinder, String resourceValue, Manifest expectedManifest) throws Exception {
        ResourceHandle resource = resourceFinder.getResource("resource");
        assertNotNull(resource);
        assertEquals(resourceValue, new String(resource.getBytes()));

        // handle.getUrl()
        URL url = resource.getUrl();
        assertEquals(resourceValue, toString(url.openStream()));

        // copy the url and verify we can still get the data
        URL copyUrl = new URL(url.toExternalForm());
        assertEquals(resourceValue, toString(copyUrl.openStream()));

        // resourceFinder.findResource
        URL directUrl = resourceFinder.findResource("resource");
        assertEquals(resourceValue, toString(directUrl.openStream()));
        URL directUrlCopy = new URL(directUrl.toExternalForm());
        assertEquals(resourceValue, toString(directUrlCopy.openStream()));

        // destroy
        resourceFinder.destroy();

        // getResource always returns null
        assertNull(resourceFinder.getResource("resource"));

        // findResource always returns null
        assertNull(resourceFinder.findResource("resource"));

        // findResources always returns an empty enumeration
        assertFalse(resourceFinder.findResources("resource").hasMoreElements());

        // existing url may not work
        try {
            assertEquals(resourceValue, toString(url.openStream()));
        } catch (IllegalStateException expected) {
        } catch (IOException expected) {
        }
        try {
            assertEquals(resourceValue, toString(directUrl.openStream()));
        } catch (IllegalStateException expected) {
        } catch (IOException expected) {
        }

        // the copied urls will work since they are proviced by the vm
        assertEquals(resourceValue, toString(copyUrl.openStream()));
        assertEquals(resourceValue, toString(directUrlCopy.openStream()));

        // existing resource handle may not work since the location was closed
        assertEquals("resource", resource.getName());
        try {
            if (expectedManifest != null) {
                assertEquals(expectedManifest.getAttributes("resource"), resource.getAttributes());
            }
        } catch (IllegalStateException expected) {
        }
        try {
            assertEquals(expectedManifest, resource.getManifest());
        } catch (IllegalStateException expected) {
        }
        try {
            assertEquals(resourceValue, toString(resource.getUrl().openStream()));
        } catch (IllegalStateException expected) {
        }
        try {
            assertEquals(resourceValue, toString(resource.getInputStream()));
        } catch (IllegalStateException expected) {
        } catch (IOException expected) {
        }
        try {
            assertEquals(resourceValue, new String(resource.getBytes()));
        } catch (IllegalStateException expected) {
        } catch (IOException expected) {
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        //
        // Build a simple Jar file to test with
        //
        manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mainAttributes.putValue("food", "nacho");
        resourceAttributes = new Attributes();
        resourceAttributes.putValue("drink", "margarita");
        manifest.getEntries().put("resource", resourceAttributes);

        File targetDir = new File(BASEDIR, "target");
        jarFile = new File(targetDir, "resourceFinderTest.jar");
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(jarFile), manifest);
        jarOutputStream.putNextEntry(new ZipEntry("resource"));
        jarOutputStream.write("resource3".getBytes());
        jarOutputStream.putNextEntry(new ZipEntry("jar3"));
        jarOutputStream.write("jar3".getBytes());
        IoUtil.close(jarOutputStream);

        alternateJarFile = new File(targetDir, "alternate.jar");
        log.debug(alternateJarFile.getAbsolutePath());
        jarOutputStream = new JarOutputStream(new FileOutputStream(alternateJarFile), manifest);
        jarOutputStream.putNextEntry(new ZipEntry("resource"));
        jarOutputStream.write("resource4".getBytes());
        jarOutputStream.putNextEntry(new ZipEntry("jar4"));
        jarOutputStream.write("jar4".getBytes());
        IoUtil.close(jarOutputStream);

        testResource = new File(targetDir, "testResource");
        FileOutputStream fileOutputStream = new FileOutputStream(testResource);
        fileOutputStream.write("testResource".getBytes());
        IoUtil.close(fileOutputStream);
    }

    protected void tearDown() throws Exception {
        jarFile.delete();
        super.tearDown();
    }

    private static String toString(InputStream in) throws IOException {
        try {
            byte[] bytes = IoUtil.getBytes(in);
            String string = new String(bytes);
            return string;
        } finally {
            IoUtil.close(in);
        }
    }
}
