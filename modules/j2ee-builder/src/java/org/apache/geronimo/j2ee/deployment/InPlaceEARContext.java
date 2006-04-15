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
import java.net.URL;
import java.util.Collection;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;

/**
 * @version $Rev:386276 $ $Date: 2006-03-25 13:13:46 +1100 (Sat, 25 Mar 2006) $
 */
public class InPlaceEARContext extends EARContext {

    public InPlaceEARContext(File baseDir, 
            File inPlaceConfigurationDir, 
            Environment environment, 
            ConfigurationModuleType moduleType, 
            AbstractName baseName, 
            EARContext parent) throws DeploymentException {
        super(baseDir,
                inPlaceConfigurationDir,
                environment,
                moduleType,
                baseName,
                parent);
    }

    public InPlaceEARContext(File baseDir,
    		File inPlaceConfigurationDir,
    		Environment environment,
    		ConfigurationModuleType moduleType, 
    		Naming naming, 
    		Collection repositories, 
    		Collection configurationStores, 
    		AbstractNameQuery serverName, 
    		AbstractName baseName, 
    		AbstractNameQuery transactionContextManagerObjectName, 
    		AbstractNameQuery connectionTrackerObjectName, 
    		AbstractNameQuery transactedTimerName, 
    		AbstractNameQuery nonTransactedTimerName, 
    		AbstractNameQuery corbaGBeanObjectName, 
    		RefContext refContext) throws DeploymentException {
    	super(baseDir,
    			inPlaceConfigurationDir,
    			environment, 
    			moduleType, 
    			naming, 
    			repositories, 
    			configurationStores, 
    			serverName, 
    			baseName, 
    			transactionContextManagerObjectName, 
    			connectionTrackerObjectName, 
    			transactedTimerName, 
    			nonTransactedTimerName, 
    			corbaGBeanObjectName,
    			refContext);
	}

	public void addIncludeAsPackedJar(URI targetPath, JarFile jarFile) throws IOException {
        configuration.addToClassPath(targetPath);
	}

    public void addInclude(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
    	configuration.addToClassPath(targetPath);
    }

    public void addInclude(URI targetPath, URL source) throws IOException {
        configuration.addToClassPath(targetPath);
    }

    public void addInclude(URI targetPath, File source) throws IOException {
        configuration.addToClassPath(targetPath);
    }

    public void addFile(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
    }

    public void addFile(URI targetPath, URL source) throws IOException {
    }

    public void addFile(URI targetPath, File source) throws IOException {
    }

    public void addFile(URI targetPath, String source) throws IOException {
    }
}
