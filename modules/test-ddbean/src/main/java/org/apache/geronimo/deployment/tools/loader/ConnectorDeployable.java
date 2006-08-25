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

package org.apache.geronimo.deployment.tools.loader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.shared.ModuleType;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class ConnectorDeployable extends AbstractDeployable {
    private final ClassLoader rarLoader;

    public ConnectorDeployable(URL moduleURL) throws DDBeanCreateException {
        super(ModuleType.RAR, moduleURL, "META-INF/ra.xml");
        ClassLoader parent = super.getModuleLoader();
        List path = new ArrayList();
        Enumeration e = entries();
        while (e.hasMoreElements()) {
            String entry = (String) e.nextElement();
            if (entry.endsWith(".jar")) {
                path.add(parent.getResource(entry));
            }
        }
        rarLoader = new URLClassLoader((URL[]) path.toArray(new URL[path.size()]), parent);
    }

    protected ClassLoader getModuleLoader() {
        return rarLoader;
    }
}
