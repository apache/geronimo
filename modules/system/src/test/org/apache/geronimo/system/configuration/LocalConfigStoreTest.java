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
import java.util.Collections;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManagerImpl;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.management.State;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class LocalConfigStoreTest extends TestCase {
    private File root;
    private URL source;
    private File sourceFile;
    private URI uri;
    private Kernel kernel;
    private byte[] state;
    private ObjectName gbeanName1;
    private ObjectName gbeanName2;
    private ObjectName storeName;
    private ConfigurationManager configurationManager;

    public void testInstall() throws Exception {
        kernel.invoke(storeName, "install", new Object[] {source}, new String[] {"java.net.URL"});
        assertTrue(new File(root, "1/META-INF/config.ser").exists());
    }

    public void testReInstall() throws Exception {
        kernel.invoke(storeName, "install", new Object[] {source}, new String[] {"java.net.URL"});
        kernel.invoke(storeName, "install", new Object[] {source}, new String[] {"java.net.URL"});
        assertTrue(new File(root, "2/META-INF/config.ser").exists());
        kernel.invoke(storeName, "install", new Object[] {source}, new String[] {"java.net.URL"});
        assertTrue(new File(root, "3/META-INF/config.ser").exists());
        kernel.invoke(storeName, "install", new Object[] {source}, new String[] {"java.net.URL"});
        assertTrue(new File(root, "4/META-INF/config.ser").exists());
        kernel.invoke(storeName, "install", new Object[] {source}, new String[] {"java.net.URL"});
        assertTrue(new File(root, "5/META-INF/config.ser").exists());
        kernel.invoke(storeName, "install", new Object[] {source}, new String[] {"java.net.URL"});
        assertTrue(new File(root, "6/META-INF/config.ser").exists());
    }

    public void testUpdateConfig() throws Exception {
        // install the config
        kernel.invoke(storeName, "install", new Object[] {source}, new String[] {"java.net.URL"});

        // load and start the config
        ObjectName configName = configurationManager.load(uri);
        configurationManager.start(configName);

        // make sure the config and the enabled gbean are running
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(configName));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName1));

        // make sure the config and the disabled gbean are NOT running
        assertEquals(State.STOPPED_INDEX, kernel.getGBeanState(gbeanName2));

        // set the value
        kernel.setAttribute(gbeanName1, "value", "9900990099");
        assertEquals("9900990099", kernel.getAttribute(gbeanName1, "value"));

        kernel.invoke(configName, "saveState");
        // stop and unload the config
        kernel.stopGBean(configName);
        kernel.unloadGBean(configName);

        // assure it was unloaded
        assertFalse(kernel.isLoaded(configName));

        // now reload and restart the config
        configName = configurationManager.load(uri);
        configurationManager.start(configName);

        // make sure the value was reloaded correctly
        assertEquals("9900990099", kernel.getAttribute(gbeanName1, "value"));

        // stop and unload the config
        kernel.stopGBean(configName);
        kernel.unloadGBean(configName);
    }

    protected void setUp() throws Exception {
        try {
            kernel = KernelFactory.newInstance().createKernel("test.kernel");
            kernel.boot();

            gbeanName1 = new ObjectName("geronimo.test:name=MyMockGMBean1");
            GBeanData mockBean1 = new GBeanData(gbeanName1, MockGBean.getGBeanInfo());
            mockBean1.setAttribute("value", "1234");

            gbeanName2 = new ObjectName("geronimo.test:name=MyMockGMBean2");
            GBeanData mockBean2 = new GBeanData(gbeanName2, MockGBean.getGBeanInfo());
            mockBean2.setAttribute("gbeanEnabled", Boolean.FALSE);
            mockBean2.setAttribute("value", "1234");

            state = Configuration.storeGBeans(new GBeanData[] {mockBean1, mockBean2});

            root = new File(System.getProperty("java.io.tmpdir") + "/config-store");
            recursiveDelete(root);
            root.mkdir();

            storeName = new ObjectName("geronimo.test:j2eeType=ConfigurationStore,name=LocalConfigStore");
            GBeanData store = new GBeanData(storeName, LocalConfigStore.getGBeanInfo());
            store.setAttribute("root", root.toURI());
            kernel.loadGBean(store, getClass().getClassLoader());
            kernel.startGBean(storeName);

            ObjectName configurationManagerName = new ObjectName(":j2eeType=ConfigurationManager,name=Basic");
            GBeanData configurationManagerData = new GBeanData(configurationManagerName, ConfigurationManagerImpl.GBEAN_INFO);
            configurationManagerData.setReferencePatterns("Stores", Collections.singleton(store.getName()));
            kernel.loadGBean(configurationManagerData, getClass().getClassLoader());
            kernel.startGBean(configurationManagerName);
            configurationManager = (ConfigurationManager) kernel.getProxyManager().createProxy(configurationManagerName, ConfigurationManager.class);

            uri = new URI("test");
            GBeanData gbean = new GBeanData(Configuration.getConfigurationObjectName(uri), Configuration.GBEAN_INFO);
            gbean.setAttribute("id", uri);
            gbean.setAttribute("gBeanState", state);


            sourceFile = File.createTempFile("test", ".car");
            source = sourceFile.toURL();
            JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(sourceFile)));
            jos.putNextEntry(new ZipEntry("META-INF/config.ser"));
            ObjectOutputStream oos = new ObjectOutputStream(jos);
            gbean.writeExternal(oos);
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
        recursiveDelete(root);
        kernel.shutdown();
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
