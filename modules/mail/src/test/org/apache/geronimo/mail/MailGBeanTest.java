/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.mail;

import javax.mail.Session;
import javax.mail.Store;
import javax.management.ObjectName;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;


/**
 * @version $Rev: $ $Date: $
 */
public class MailGBeanTest extends TestCase {

    private Kernel kernel;
    private ObjectName mailName;
    private static final String KERNEL_NAME = "testKernel";

    public void testProxy() throws Exception {
        Object proxy = kernel.invoke(mailName, "$getResource");

        assertNotNull(proxy);
        assertTrue(proxy instanceof Session);

//        Store store = ((Session) proxy).getStore();
//        assertNotNull(store);

        kernel.stopGBean(mailName);
    }

    protected void setUp() throws Exception {
        kernel = new Kernel(KERNEL_NAME, "test.domain");
        kernel.boot();

        GBeanMBean cmf = new GBeanMBean(MailGBean.getGBeanInfo());
        cmf.setAttribute("useDefault", new Boolean(true));
        cmf.setAttribute("properties", new Properties());
        mailName = ObjectName.getInstance("geronimo.server:J2EEServer=geronimo,J2EEApplication=null,J2EEType=JavaMailResource,name=default");
        kernel.loadGBean(mailName, cmf);

        kernel.startGBean(mailName);
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(mailName);
        kernel.shutdown();
    }
}
