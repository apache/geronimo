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
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.zip.ZipEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.net.URI;

import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import junit.framework.TestCase;

/**
 * 
 * 
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:03 $
 */
public class RunTest extends TestCase {
    private File carFile;

    public void testRun() {

    }

    protected void setUp() throws Exception {
        super.setUp();

        Map gbeans = new HashMap();
        ObjectName objectName = new ObjectName("test:name=MyGBean");
        gbeans.put(objectName, new GBeanMBean(MyGBean.GBEAN_INFO));
        GBeanMBean config = new GBeanMBean(Configuration.GBEAN_INFO);
        config.setAttribute("ID", URI.create("org/apache/geronimo/run-test"));
        config.setReferencePatterns("Parent", null);
        config.setAttribute("ClassPath", Collections.EMPTY_LIST);
        config.setAttribute("GBeanState", Configuration.storeGBeans(gbeans));

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
        Configuration.storeGMBeanState(config, oos);
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
    }
}
