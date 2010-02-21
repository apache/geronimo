/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.connector.wrapper.outbound;

import java.net.URI;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.basic.BasicKernel;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.wrapper.outbound.GenericConnectionManagerGBean;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public class GenericConnectionManagerGBeanSerializationTest extends TestCase {

    public void testSerialization() throws Exception {
        BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", null, null);

        Kernel kernel = new BasicKernel("test", bundleContext);
        kernel.boot();
        AbstractName abstractName = new AbstractName(URI.create("foo/bar/1/car?name=ConnectionManager"));
        GBeanData data = new GBeanData(abstractName, GenericConnectionManagerGBean.class);
        data.setAttribute("transactionSupport", NoTransactions.INSTANCE);
        data.setAttribute("pooling", new SinglePool(10, 0, 5000, 5, false, false, true));
        kernel.loadGBean(data, bundleContext);
        kernel.startGBean(abstractName);
        Object cm = kernel.getGBean(abstractName);
        assertTrue(cm instanceof GenericConnectionManagerGBean);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(cm);
        out.flush();
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bais);
        Object cm2 = in.readObject();
        assertSame(cm, cm2);
    }
}
