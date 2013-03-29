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
import java.io.IOException;
import java.io.InputStream;

import org.apache.aries.application.management.AriesApplication;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactTypeHandler;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.util.FileUtils;

/**
 * @version $Rev:385232 $ $Date$
 */
public class EBAArtifactTypeHandler implements ArtifactTypeHandler {

    private ApplicationInstaller installer;
    
    public EBAArtifactTypeHandler(ApplicationInstaller installer) {
        this.installer = installer;
    }

    @Override
    public void install(InputStream source, int size, Artifact artifactId, FileWriteMonitor monitor, File target) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void install(File source, Artifact artifactId, FileWriteMonitor monitor, File target) throws IOException {
        // copy config.ser & other generated stuff during deployment
        FileUtils.recursiveCopy(source, target);
        
        AriesApplication app = installer.lookupApplication(artifactId);
        // app will be null for in-place deployment
        if (app != null) {
            // copy the contents on the application
            installer.storeApplication(app, target);
        } 
    }

}
