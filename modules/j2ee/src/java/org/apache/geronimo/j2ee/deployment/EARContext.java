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
package org.apache.geronimo.j2ee.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;

/**
 * @version $Rev$ $Date$
 */
public class EARContext extends DeploymentContext {
    private final Map resourceAdapterModules = new HashMap();
    private final Map activationSpecInfos = new HashMap();
    private final String j2eeDomainName;
    private final String j2eeServerName;
    private final String j2eeApplicationName;
    private final ObjectName domainObjectName;
    private final ObjectName serverObjectName;
    private final ObjectName applicationObjectName;

    private final ObjectName transactionContextManagerObjectName;
    private final ObjectName connectionTrackerObjectName;

    private final ObjectName transactedTimerName;
    private final ObjectName nonTransactedTimerName;

    private final EJBRefContext ejbRefContext;

    public EARContext(File baseDir, URI id, ConfigurationModuleType moduleType, URI parentID, Kernel kernel, String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, ObjectName transactionContextManagerObjectName, ObjectName connectionTrackerObjectName, ObjectName transactedTimerName, ObjectName nonTransactedTimerName, EJBRefContext ejbRefContext) throws MalformedObjectNameException, DeploymentException {
        super(baseDir, id, moduleType, parentID, kernel);
        this.j2eeDomainName = j2eeDomainName;
        this.j2eeServerName = j2eeServerName;

        if (j2eeApplicationName == null) {
            j2eeApplicationName = "null";
        }
        this.j2eeApplicationName = j2eeApplicationName;

        Properties domainNameProps = new Properties();
        domainNameProps.put("j2eeType", "J2EEDomain");
        domainNameProps.put("name", j2eeDomainName);
        try {
            domainObjectName = new ObjectName(j2eeDomainName, domainNameProps);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct J2EEDomain ObjectName", e);
        }

        Properties serverNameProps = new Properties();
        serverNameProps.put("j2eeType", "J2EEServer");
        serverNameProps.put("name", j2eeServerName);
        try {
            serverObjectName = new ObjectName(j2eeDomainName, serverNameProps);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct J2EEServer ObjectName", e);
        }

        if (!j2eeApplicationName.equals("null")) {
            Properties applicationNameProps = new Properties();
            applicationNameProps.put("j2eeType", "J2EEApplication");
            applicationNameProps.put("name", j2eeApplicationName);
            applicationNameProps.put("J2EEServer", j2eeServerName);
            try {
                applicationObjectName = new ObjectName(j2eeDomainName, applicationNameProps);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Unable to construct J2EEApplication ObjectName", e);
            }
        } else {
            applicationObjectName = null;
        }
        this.transactionContextManagerObjectName = transactionContextManagerObjectName;
        this.connectionTrackerObjectName = connectionTrackerObjectName;
        this.transactedTimerName = transactedTimerName;
        this.nonTransactedTimerName = nonTransactedTimerName;
        this.ejbRefContext = ejbRefContext;
    }

    public String getJ2EEDomainName() {
        return j2eeDomainName;
    }

    public String getJ2EEServerName() {
        return j2eeServerName;
    }

    public String getJ2EEApplicationName() {
        return j2eeApplicationName;
    }

    public ObjectName getDomainObjectName() {
        return domainObjectName;
    }

    public ObjectName getServerObjectName() {
        return serverObjectName;
    }

    public ObjectName getApplicationObjectName() {
        return applicationObjectName;
    }

    public ObjectName getTransactionContextManagerObjectName() {
        return transactionContextManagerObjectName;
    }

    public ObjectName getConnectionTrackerObjectName() {
        return connectionTrackerObjectName;
    }

    public ObjectName getTransactedTimerName() {
        return transactedTimerName;
    }

    public ObjectName getNonTransactedTimerName() {
        return nonTransactedTimerName;
    }

    public EJBRefContext getEJBRefContext() {
        return ejbRefContext;
    }

    public void addResourceAdapter(String resourceAdapterName, String resourceAdapterModule, Map activationSpecInfoMap) {
        resourceAdapterModules.put(resourceAdapterName, resourceAdapterModule);
        activationSpecInfos.put(resourceAdapterName, activationSpecInfoMap);
    }

    public Object getActivationSpecInfo(String resourceAdapterName, String activationSpecClassName) {
        Map activationSpecInfoMap = (Map) activationSpecInfos.get(resourceAdapterName);
        Object activationSpecInfo = activationSpecInfoMap.get(activationSpecClassName);
        return activationSpecInfo;
    }

    public String getResourceAdapterModule(String resourceAdapterName) {
        return (String) resourceAdapterModules.get(resourceAdapterName);
    }

    public void addManifestClassPath(JarFile earFile, Module module, JarFile jarFile, URI baseUri) throws DeploymentException, URISyntaxException, IOException {
        String classPath = null;
        try {
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                return;
            }
            classPath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
            if (classPath == null) {
                return;
            }
        } catch (IOException e) {
            throw new DeploymentException("Could not get manifest from app client module: " + jarFile.getName());
        }

        for (StringTokenizer tokenizer = new StringTokenizer(classPath, " "); tokenizer.hasMoreTokens();) {
            URI uri = new URI(tokenizer.nextToken());
            if (!uri.getPath().endsWith(".jar")) {
                throw new DeploymentException("Manifest class path entries must end with the .jar extension (J2EE 1.4 Section 8.2)");
            }
            if (uri.isAbsolute()) {
                throw new DeploymentException("Manifest class path entries must be relative (J2EE 1.4 Section 8.2)");
            }

            URI path = baseUri.resolve(uri);
            // todo no need to copy this into a temp file...
            File classPathFile = DeploymentUtil.toFile(earFile, path.getPath());

            // before going to the work of adding this file, let's make sure it really is a jar file
            JarFile classPathJar = null;
            try {
                classPathJar = new JarFile(classPathFile);

                // add this class path jar to the output context
                addInclude(path, classPathFile);

                // add the client jars of this class path jar
                addManifestClassPath(earFile, module, classPathJar, path);
            } catch (IOException e) {
                throw new DeploymentException("Manifest class path entries must be a valid jar file (J2EE 1.4 Section 8.2)", e);
            } finally {
                DeploymentUtil.close(classPathJar);
            }
        }
    }
}
