/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.interop.rmi.iiop.portable;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.geronimo.interop.generator.GenOptions;

/**
 * @version $Rev$ $Date$
 */
public class PortableStubCompilerTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));


    public void testStubCompiler() throws Exception {
        GenOptions genOptions = new GenOptions();
        new File("target/stubs").mkdirs();
        genOptions.setClasspath("target/classes");
        genOptions.setCompile(false);
        genOptions.setGenSrcDir("target/stubs");
        genOptions.setGenerate(true);
        genOptions.setInterfaces(Arrays.asList(new String[]{Foo.class.getName(), Special.class.getName()}));
        genOptions.setLoadclass(true);
        genOptions.setOverwrite(true);
        genOptions.setSimpleIdl(false);
        genOptions.setVerbose(true);

        ClassLoader classLoader = getClass().getClassLoader();
        PortableStubCompiler stubCompiler = new PortableStubCompiler(genOptions, classLoader);

        stubCompiler.generate();
    }

    public void testBasicNameMangler() throws Exception {
        Properties nameManglerProperties = new Properties();
        File file = new File(basedir, "src/test-data/nameMangler.properties");
        nameManglerProperties.load(new FileInputStream(file));

        Set methodSignatures = new HashSet();
        IiopOperation[] iiopOperations = PortableStubCompiler.createIiopOperations(Foo.class);
        for (int i = 0; i < iiopOperations.length; i++) {
            IiopOperation iiopOperation = iiopOperations[i];
            Method method = iiopOperation.getMethod();
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

            assertTrue("Method not present in name mangler properties: " + methodSignature, nameManglerProperties.containsKey(methodSignature));
            assertEquals(nameManglerProperties.getProperty(methodSignature), iiopOperation.getName());
        }

        assertEquals("Did not match all methods", nameManglerProperties.keySet(), methodSignatures);
    }

    public void testSpecialNameMangler() throws Exception {
        Properties nameManglerProperties = new Properties();
        File file = new File(basedir, "src/test-data/specialNameMangler.properties");
        nameManglerProperties.load(new FileInputStream(file));

        Set methodSignatures = new HashSet();
        IiopOperation[] iiopOperations = PortableStubCompiler.createIiopOperations(Special.class);
        for (int i = 0; i < iiopOperations.length; i++) {
            IiopOperation iiopOperation = iiopOperations[i];
            Method method = iiopOperation.getMethod();
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

            assertTrue("Method not present in name mangler properties: " + methodSignature, nameManglerProperties.containsKey(methodSignature));
            assertEquals(nameManglerProperties.getProperty(methodSignature), iiopOperation.getName());
        }

        assertEquals("Did not match all methods", nameManglerProperties.keySet(), methodSignatures);
    }
}
