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
package org.apache.geronimo.kernel;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.LocalConfigStore;
import org.apache.geronimo.kernel.management.State;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/01/14 22:16:38 $
 */
public class ConfigTest extends TestCase {
    private ObjectName gbeanName1;
    private File configRoot;
    private File tmpDir;
    private GBeanInfo storeInfo;
    private Kernel kernel;
    private MBeanServer mbServer;
    private byte[] state;
    private ObjectName gbeanName2;

    public void testOfflineConfig() throws Exception {
        GBeanMBean config = new GBeanMBean(Configuration.GBEAN_INFO);
        config.setAttribute("ID", new URI("test"));
        config.setEndpointPatterns("Parent", null);
    }

    public void testOnlineConfig() throws Exception {
        GBeanMBean config = new GBeanMBean(Configuration.GBEAN_INFO);
        config.setAttribute("ID", new URI("test"));
        config.setEndpointPatterns("Parent", null);
        config.setAttribute("ClassPath", Collections.EMPTY_LIST);
        config.setAttribute("GBeanState", state);
        ObjectName configName = (ObjectName) mbServer.invoke(Kernel.KERNEL, "load", new Object[]{config, null}, new String[]{GBeanMBean.class.getName(), URL.class.getName()});
        mbServer.invoke(configName, "startRecursive", null, null);

        assertEquals(new Integer(State.RUNNING.toInt()), mbServer.getAttribute(configName, "state"));
        assertNotNull(mbServer.getAttribute(configName, "ClassLoader"));

        assertEquals(new Integer(State.RUNNING.toInt()), mbServer.getAttribute(gbeanName1, "state"));
        assertEquals("1234", mbServer.getAttribute(gbeanName1, "Value"));
        assertEquals("no endpoint", mbServer.invoke(gbeanName1, "checkEndpoint", null, null));
        //assertEquals("endpointCheck", mbServer.invoke(gbeanName2, "checkEndpoint", null, null));

        mbServer.invoke(configName, "stop", null, null);
        try {
            mbServer.getAttribute(gbeanName1, "Value");
            fail();
        } catch (InstanceNotFoundException e) {
            // ok
        }
        assertEquals(new Integer(State.STOPPED.toInt()), mbServer.getAttribute(configName, "state"));
        mbServer.invoke(Kernel.KERNEL, "unload", new Object[]{configName}, new String[]{ObjectName.class.getName()});
        assertFalse(mbServer.isRegistered(configName));
    }

    protected void setUp() throws Exception {
        tmpDir = new File(System.getProperty("java.io.tmpdir"));
        configRoot = new File(tmpDir, "config-store");
        storeInfo = LocalConfigStore.getGBeanInfo();
        configRoot.mkdir();

        kernel = new Kernel("geronimo", storeInfo, configRoot);
        kernel.boot();
        mbServer = kernel.getMBeanServer();

        gbeanName1 = new ObjectName("geronimo.test:name=MyMockGMBean1");
        GBeanMBean mockBean1 = new GBeanMBean(MockGBean.getGBeanInfo());
        mockBean1.setAttribute("Value", "1234");
        mockBean1.setAttribute("Name", "child");
        gbeanName2 = new ObjectName("geronimo.test:name=MyMockGMBean2");
        GBeanMBean mockBean2 = new GBeanMBean(MockGBean.getGBeanInfo());
        mockBean2.setAttribute("Value", "5678");
        mockBean2.setAttribute("Name", "Parent");
        mockBean2.setEndpointPatterns("MockEndpoint", Collections.singleton(gbeanName1));

        Map gbeans = new HashMap();
        gbeans.put(gbeanName1, mockBean1);
        gbeans.put(gbeanName2, mockBean2);
        state = Configuration.storeGBeans(gbeans);
    }

    protected void tearDown() throws Exception {
        mbServer = null;
        kernel.shutdown();
        recursiveDelete(configRoot);
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
