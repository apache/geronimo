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
import java.util.Set;

import org.apache.aries.application.ApplicationMetadata;
import org.apache.aries.application.DeploymentMetadata;
import org.apache.aries.application.management.AriesApplication;
import org.apache.aries.application.management.BundleInfo;

/**
 * @version $Rev:385232 $ $Date$
 */
public class GeronimoApplication implements AriesApplication {
    
    private final ApplicationMetadata applicationMetadata;
    private final Set<BundleInfo> bundleInfo;
    private DeploymentMetadata deploymentMetadata;
    
    public GeronimoApplication(ApplicationMetadata applicationMetadata, 
                               DeploymentMetadata deploymentMetadata, 
                               Set<BundleInfo> bundleInfo) {
        this.applicationMetadata = applicationMetadata;
        this.deploymentMetadata = deploymentMetadata;
        this.bundleInfo = bundleInfo;
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