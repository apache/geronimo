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
 * @version $Revision: 1.1 $ $Date: 2004/01/30 20:11:18 $
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
