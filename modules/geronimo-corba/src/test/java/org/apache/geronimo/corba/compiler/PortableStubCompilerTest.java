/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.geronimo.corba.util.Util;

/**
 * @version $Rev: 451647 $ $Date: 2006-09-30 12:33:02 -0700 (Sat, 30 Sep 2006) $
 */
public class PortableStubCompilerTest extends TestCase {

    public void testBeanPropertiesNameMangler() throws Exception {
        assertMangling("beanPropertiesNameMangler.properties", BeanProperties.class);
    }

    public void testBasicNameMangler() throws Exception {
        assertMangling("nameMangler.properties", Foo.class);
    }

    public void testSpecialNameMangler() throws Exception {
        assertMangling("specialNameMangler.properties", Special.class);
    }

    private void assertMangling(String propertiesFile, Class intf) throws IOException {
        Properties nameManglerProperties = new Properties();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(propertiesFile);
        if (in == null) {
            fail("couldn't find resource: " + propertiesFile);
        }
        nameManglerProperties.load(in);

        boolean failed = false;
        Set methodSignatures = new HashSet();
        Map methodToOperation = Util.mapMethodToOperation(intf);
        for (Iterator iterator = methodToOperation.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Method method = (Method) entry.getKey();
            String operation = (String) entry.getValue();
            String methodSignature = method.getName() + "(";

            Class[] parameterTypes = method.getParameterTypes();
            for (int j = 0; j < parameterTypes.length; j++) {
                Class parameterType = parameterTypes[j];
                String arrayBrackets = "";
                while (parameterType.isArray()) {
                    arrayBrackets += "[]";
                    parameterType = parameterType.getComponentType();
                }
                methodSignature += parameterType.getName() + arrayBrackets;
            }
            methodSignature += ")";
            methodSignatures.add(methodSignature);

            String expected = nameManglerProperties.getProperty(methodSignature);
            if (expected == null || !expected.equals(operation)) {
                System.out.println("Expected: " + expected);
                System.out.println("  Actual: " + operation);
                System.out.println();
                failed = true;
            }
        }

        if (!nameManglerProperties.keySet().equals(methodSignatures)) {
            Set extraProperties = new HashSet(nameManglerProperties.keySet());
            extraProperties.removeAll(methodSignatures);
            Set missingProperties = new HashSet(methodSignatures);
            missingProperties.removeAll(nameManglerProperties.keySet());
            fail("extraProperties=" + extraProperties + ", missingProperties=" + missingProperties);
        }

        if (failed) {
            fail();
        }
    }
}
