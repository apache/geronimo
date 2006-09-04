/*
 *  Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.mavenplugins.geronimo;

import java.io.File;

import org.apache.tools.ant.taskdefs.Expand;

import org.codehaus.plexus.util.FileUtils;

/**
 * Common assembly install support.
 *
 * @version $Rev$ $Date$
 */
public abstract class InstallerMojoSupport
    extends ServerMojoSupport
{
    /**
     * Enable forced install refresh.
     *
     * @parameter expression="${refresh}" default-value="false"
     */
    protected boolean refresh = false;

    protected void doInstall() throws Exception {
        // Check if there is a newer archive or missing marker to trigger assembly install
        File installMarker = new File(installDir, ".installed");
        boolean refresh = this.refresh; // don't override config state with local state

        if (!refresh) {
            if (!installMarker.exists()) {
                refresh = true;
            }
            else if (installArchive.lastModified() > installMarker.lastModified()) {
                log.debug("Detected new assembly archive");
                refresh = true;
            }
        }
        else {
            log.debug("User requested installation refresh");
        }

        if (refresh) {
            if (installDir.exists()) {
                log.debug("Removing: " + installDir);
                FileUtils.forceDelete(installDir);
            }
        }

        // Install the assembly
        if (!installMarker.exists()) {
            log.info("Installing assembly...");

            Expand unzip = (Expand)createTask("unzip");
            unzip.setSrc(installArchive);
            unzip.setDest(outputDirectory);
            unzip.execute();

            installMarker.createNewFile();
        }
        else {
            log.debug("Assembly already installed");
        }
    }
}
