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

package org.apache.geronimo.deployment;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.deployment.util.URLInfo;
import org.apache.geronimo.kernel.config.ConfigurationParent;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:35 $
 */
public class URLDeployer extends ModuleDeployer {
    private final List deployers;
    private final Set moduleIDs = new HashSet();

    public URLDeployer(ConfigurationParent parent, URI configID, List deployers, File workingDir) {
        super(parent, configID, workingDir);

        this.deployers = deployers;
    }

    public void addSource(URLInfo source) throws NoDeployerException, DeploymentException {
        String path = source.getUrl().getPath();
        while (path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }
        int end = path.lastIndexOf('/');
        if (end != -1) {
            path = path.substring(end+1);
        }
        if (path.length() == 0) {
            path = "module";
        }
        URI moduleID = null;
        try {
            moduleID = new URI(path);
            int i=0;
            while (moduleIDs.contains(moduleID)) {
                moduleID = new URI(++i + path);
            }
        } catch (URISyntaxException e) {
            throw new DeploymentException("Unable to construct moduleID for URL: "+source.getUrl(), e);
        }
        for (Iterator i = deployers.iterator(); i.hasNext();) {
            ModuleFactory deployer = (ModuleFactory) i.next();
            DeploymentModule module = deployer.getModule(source, moduleID);
            if (module != null) {
                addModule(module);
                return;
            }
        }
        throw new NoDeployerException("No deployer could handle source " + source.getUrl());
    }
}
