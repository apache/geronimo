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

package org.apache.geronimo.system.configuration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.config.Configuration;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class LocalConfigStoreTest extends TestCase {
    private File root;
    private LocalConfigStore store;
    private URL source;
    private File sourceFile;
    private URI uri;

    public void testInstall() throws Exception {
        store.install(source);
        assertTrue(new File(root, "1/META-INF/config.ser").exists());
        assertEquals(new File(root, "1").toURL(), store.getBaseURL(uri));
        GBeanMBean config = store.getConfiguration(uri);
        assertEquals(uri, config.getAttribute("ID"));
    }

    public void testReInstall() throws Exception {
        store.install(source);
        store.install(source);
        assertTrue(new File(root, "2/META-INF/config.ser").exists());
        store.install(source);
        assertTrue(new File(root, "3/META-INF/config.ser").exists());
        store.install(source);
        assertTrue(new File(root, "4/META-INF/config.ser").exists());
        store.install(source);
        assertTrue(new File(root, "5/META-INF/config.ser").exists());
        store.install(source);
        assertTrue(new File(root, "6/META-INF/config.ser").exists());
    }

    protected void setUp() throws Exception {
        try {
            root = new File(System.getProperty("java.io.tmpdir") + "/config-store");
            root.mkdir();

            store = new LocalConfigStore(root);
            store.doStart();

            GBeanMBean gbean = new GBeanMBean(Configuration.GBEAN_INFO);
            uri = new URI("test");
            gbean.setAttribute("ID", uri);
            sourceFile = File.createTempFile("test", ".car");
            source = sourceFile.toURL();
            JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(sourceFile)));
            jos.putNextEntry(new ZipEntry("META-INF/config.ser"));
            ObjectOutputStream oos = new ObjectOutputStream(jos);
            Configuration.storeGMBeanState(gbean, oos);
            oos.flush();
            jos.closeEntry();
            jos.close();
        } catch (Exception e) {
            if (sourceFile != null) {
                sourceFile.delete();
            }
            throw e;
        }
    }

    protected void tearDown() throws Exception {
        if (sourceFile != null) {
            sourceFile.delete();
        }
        store.doStop();
        store = null;
        recursiveDelete(root);
    }

    private static void recursiveDelete(File root) throws Exception {
        File[] files = root.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    recursiveDelete(file);
                } else {
                    file.delete();
                }
            }
        }
        root.delete();
    }
}
