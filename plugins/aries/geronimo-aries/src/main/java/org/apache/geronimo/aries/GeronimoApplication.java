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
package org.apache.geronimo.aries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.aries.application.ApplicationMetadata;
import org.apache.aries.application.ApplicationMetadataFactory;
import org.apache.aries.application.DeploymentMetadata;
import org.apache.aries.application.DeploymentMetadataFactory;
import org.apache.aries.application.management.AriesApplication;
import org.apache.aries.application.management.BundleInfo;
import org.apache.aries.application.utils.AppConstants;
import org.apache.aries.application.utils.management.SimpleBundleInfo;
import org.apache.aries.application.utils.manifest.BundleManifest;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:385232 $ $Date$
 */
public class GeronimoApplication implements AriesApplication {
    
    private final ApplicationMetadata applicationMetadata;
    private final Set<BundleInfo> bundleInfo;
    private DeploymentMetadata deploymentMetadata;
    
    public GeronimoApplication(Bundle bundle, 
                               ApplicationMetadataFactory applicationFactory, 
                               DeploymentMetadataFactory deploymentFactory) 
        throws IOException {
        URL applicationMF = bundle.getEntry(AppConstants.APPLICATION_MF);
        applicationMetadata = applicationFactory.parseApplicationMetadata(applicationMF.openStream());
        
        bundleInfo = new HashSet<BundleInfo>();
        Enumeration<URL> e = bundle.findEntries("/", "*", true);
        while (e.hasMoreElements()) {
            URL url = e.nextElement();
            if (url.getPath().endsWith("/")) {
                continue;
            }
            BundleManifest bm = BundleManifest.fromBundle(url.openStream());
            if (bm != null && bm.isValid()) {
                bundleInfo.add(new SimpleBundleInfo(applicationFactory, bm, url.toExternalForm()));
            }
        }
        
        URL deploymentMF = bundle.getEntry(AppConstants.DEPLOYMENT_MF);
        if (deploymentMF != null) {
            deploymentMetadata = deploymentFactory.createDeploymentMetadata(deploymentMF.openStream());
        }
    }

    public ApplicationMetadata getApplicationMetadata() {
        return applicationMetadata;
    }

    public Set<BundleInfo> getBundleInfo() {
        return bundleInfo;
    }

    public DeploymentMetadata getDeploymentMetadata() {
        return deploymentMetadata;
    }

    public boolean isResolved() {
        return (deploymentMetadata != null);
    }

    public void store(File arg0) throws FileNotFoundException, IOException {
        throw new UnsupportedOperationException();
    }

    public void store(OutputStream arg0) throws FileNotFoundException, IOException {
        throw new UnsupportedOperationException();        
    }
   
}
