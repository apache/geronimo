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

package org.apache.geronimo.kernel.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.jmx.GBeanMBean;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class RunTest extends TestCase {
    private File carFile;

    public void testRun() {

    }

    protected void setUp() throws Exception {
        super.setUp();

        try {
            Map gbeans = new HashMap();
            ObjectName objectName = new ObjectName("test:name=MyGBean");
            gbeans.put(objectName, new GBeanMBean(MyGBean.GBEAN_INFO));
            GBeanMBean config = new GBeanMBean(Configuration.GBEAN_INFO);
            config.setAttribute("ID", URI.create("org/apache/geronimo/run-test"));
            config.setReferencePatterns("Parent", null);
            config.setAttribute("classPath", Collections.EMPTY_LIST);
            config.setAttribute("gBeanState", Configuration.storeGBeans(gbeans));

            carFile = File.createTempFile("run", ".car");
            Manifest manifest = new Manifest();
            Attributes attrs = manifest.getMainAttributes();
            attrs.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
            attrs.putValue("Geronimo-GBean", objectName.toString());
            attrs.putValue(Attributes.Name.MAIN_CLASS.toString(), Run.class.getName());
            attrs.putValue(Attributes.Name.CLASS_PATH.toString(), "geronimo-kernel-DEV.jar commons-logging-1.0.3.jar cglib-full-2.0-RC2.jar");
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(carFile), manifest);
            jos.putNextEntry(new ZipEntry("META-INF/config.ser"));
            ObjectOutputStream oos = new ObjectOutputStream(jos);
            config.getGBeanData().writeExternal(oos);
            oos.flush();
            jos.closeEntry();
            jos.putNextEntry(new ZipEntry("org/apache/geronimo/kernel/config/MyGBean.class"));
            byte[] buffer = new byte[4096];
            InputStream is = MyGBean.class.getClassLoader().getResourceAsStream("org/apache/geronimo/kernel/config/MyGBean.class");
            int count;
            while ((count = is.read(buffer)) > 0) {
                jos.write(buffer, 0, count);
            }
            jos.closeEntry();
            jos.close();
        } catch (Exception e) {
            if (carFile != null) {
                carFile.delete();
            }
            throw e;
        }
    }

    protected void tearDown() throws Exception {
        if (carFile != null) {
            carFile.delete();
        }
    }
}
