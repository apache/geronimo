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

package org.apache.geronimo.naming.deployment;

import java.util.Map;

import javax.naming.NamingException;

import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.naming.java.ComponentContextBuilder;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/09 18:03:11 $
 *
 * */
public class ENCConfigBuilder {

    public static void addEnvEntries(EnvEntryType[] envEntries, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < envEntries.length; i++) {
            EnvEntryType envEntry = envEntries[i];
            String name = envEntry.getEnvEntryName().getStringValue();
            String type = envEntry.getEnvEntryType().getStringValue();
            String text = envEntry.getEnvEntryValue().getStringValue();
            try {
                builder.addEnvEntry(name, type, text);
            } catch (NumberFormatException e) {
                throw new DeploymentException("Invalid env-entry value for name: " + name, e);
            } catch (NamingException e) {
                throw new DeploymentException("Invalid env-entry definition for name: " + name, e);
            }
        }

    }

    public static void addResourceRefs(ResourceRefType[] resourceRefs, ClassLoader cl, Map refAdapterMap, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < resourceRefs.length; i++) {
            ResourceRefType resourceRef = resourceRefs[i];
            String name = resourceRef.getResRefName().getStringValue();
            String type = resourceRef.getResType().getStringValue();
            Class iface = null;
            try {
                iface = cl.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
            }
            RefAdapter refAdapter = (RefAdapter) refAdapterMap.get(name);
            try {
                builder.addResourceRef(name, iface, refAdapter);
            } catch (NamingException e) {
                throw new DeploymentException("Invalid env-entry definition for name: " + name, e);
            }
        }

    }
}
