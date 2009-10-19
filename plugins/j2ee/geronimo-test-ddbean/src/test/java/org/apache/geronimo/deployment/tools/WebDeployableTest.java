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

package org.apache.geronimo.deployment.tools;

import java.net.URL;
import java.net.URLClassLoader;

import org.apache.geronimo.deployment.tools.loader.WebDeployable;
import org.apache.geronimo.kernel.osgi.MockBundle;
import org.osgi.framework.Bundle;
import junit.framework.TestCase;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class WebDeployableTest extends TestCase {
    private ClassLoader classLoader;

    public void testWebClasspath() throws Exception {
        URL resource = classLoader.getResource("deployables/war1.war");
        ClassLoader cl = new URLClassLoader(new URL[] {resource});
        Bundle bundle = new MockBundle(cl, resource.toString(), 0L);
        WebDeployable deployable = new WebDeployable(bundle);
    }

    protected void setUp() throws Exception {
        classLoader = Thread.currentThread().getContextClassLoader();
    }
}
