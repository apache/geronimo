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

package org.apache.geronimo.plugin.assembly;

import java.io.File;
import java.net.URI;

import org.apache.geronimo.system.configuration.LocalConfigStore;

/**
 * JellyBean that installs configuration artifacts into a LocalConfigurationStore
 *
 * @version $Rev: 156292 $ $Date: 2005-03-05 18:48:02 -0800 (Sat, 05 Mar 2005) $
 */
public class LocalConfigInstaller {
    private File root;
    private File artifact;

    public File getRoot() {
        return root;
    }

    public void setRoot(File root) {
        this.root = root;
    }

    public File getArtifact() {
        return artifact;
    }

    public void setArtifact(File artifact) {
        this.artifact = artifact;
    }

    public void execute() throws Exception {
        LocalConfigStore store = new LocalConfigStore(root);
        store.doStart();
        try {
            URI uri = store.install(artifact.toURL());
            System.out.println("Installed configuration " + uri);
        } finally{
            store.doStop();
        }
    }
}
