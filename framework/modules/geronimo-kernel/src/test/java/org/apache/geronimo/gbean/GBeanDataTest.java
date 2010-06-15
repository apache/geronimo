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

package org.apache.geronimo.gbean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.geronimo.gbean.annotation.AnnotationGBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.testsupport.TestSupport;

/**
 * @version $Rev$ $Date$
 */
public class GBeanDataTest extends TestSupport {

//    public void testBackwardCompatibility() throws Exception {
//        FileInputStream fis = new FileInputStream(resolveFile("src/test/data/gbeandata/SERIALIZATION_-1012491431781444074.ser"));
//        ObjectInputStream is = new ObjectInputStream(fis);
//        is.readObject();
//    }

    public void testCurrentSerialization() throws Exception {
        GBeanData beanData = new GBeanData();

        ByteArrayOutputStream memOut = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(memOut);
        os.writeObject(beanData);

        ByteArrayInputStream memIn = new ByteArrayInputStream(memOut.toByteArray());
        ObjectInputStream is = new ObjectInputStream(memIn);
        is.readObject();
    }

    public void testCurrentSerialization2() throws Exception {
        GBeanData beanData = new GBeanData(new AnnotationGBeanInfoBuilder(AnnotationGBean.class).buildGBeanInfo());
        beanData.setServiceInterfaces(new String[] {TestSupport.class.getName()});
        beanData.getServiceProperties().put("foo", "bar");

        ByteArrayOutputStream memOut = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(memOut);
        os.writeObject(beanData);

        ByteArrayInputStream memIn = new ByteArrayInputStream(memOut.toByteArray());
        ObjectInputStream is = new ObjectInputStream(memIn);
        GBeanData beanData2 = (GBeanData) is.readObject();
        assertEquals(1, beanData2.getServiceInterfaces().length);
        assertEquals(TestSupport.class.getName(), beanData2.getServiceInterfaces()[0]);
        assertEquals(1, beanData2.getServiceProperties().size());
        assertEquals("bar", beanData2.getServiceProperties().get("foo"));
    }

    @GBean
    @OsgiService
    public static class AnnotationGBean {}

}
