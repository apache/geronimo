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
package org.apache.geronimo.deployment;

import junit.framework.TestCase;
import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.EditableConfigurationManagerImpl;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationResolver;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.AbstractName;

import javax.sql.DataSource;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Hashtable;

/**
 * @version $Rev: 384686 $ $Date$
 */
public class DeploymentContextTest extends TestCase {
    private byte[] classBytes;

    public void testAddClass() throws Exception {
        File basedir = File.createTempFile("car", "tmp");
        basedir.delete();
        basedir.mkdirs();
        try {
            basedir.deleteOnExit();
            Environment environment = new Environment();
            Artifact configId = new Artifact("foo", "artifact", "1", "car");
            environment.setConfigId(configId);
            Map nameKeys = new HashMap();
            nameKeys.put("domain", "d");
            environment.setProperties(nameKeys);
            Configuration configuration = new Configuration(null,
                    ConfigurationModuleType.CAR,
                    environment,
                    null,
                    null,
                    new ConfigurationResolver(configId, basedir));
            DeploymentContext context = new DeploymentContext(configuration, basedir);
            Enhancer enhancer = new Enhancer();
            enhancer.setInterfaces(new Class[]{DataSource.class});
            enhancer.setCallbackType(MethodInterceptor.class);
            enhancer.setStrategy(new DefaultGeneratorStrategy() {
                public byte[] transform(byte[] b) {
                    classBytes = b;
                    return b;
                }
            });
            enhancer.setClassLoader(new URLClassLoader(new URL[0], this.getClass().getClassLoader()));
            Class type = enhancer.createClass();
            URI location = new URI("cglib/");
            context.addClass(location, type.getName(), classBytes);
            ClassLoader cl = context.getClassLoader();
            Class loadedType = cl.loadClass(type.getName());
            assertTrue(DataSource.class.isAssignableFrom(loadedType));
            assertTrue(type != loadedType);
        } finally {
            recursiveDelete(basedir);
        }
    }

    private void recursiveDelete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                recursiveDelete(files[i]);
            }
        }
        file.delete();
    }
}
