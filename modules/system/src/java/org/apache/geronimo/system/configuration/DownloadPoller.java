/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.system.configuration;

import org.apache.geronimo.kernel.repository.Artifact;

/**
 * An interface for callers who want to monitor the progress of an installation.
 * These are all callbacks sent by the server.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public interface DownloadPoller {
    void addDependencyPresent(Artifact dep);

    void addDependencyInstalled(Artifact dep);

    void setCurrentFile(String currentFile);

    void setCurrentMessage(String currentMessage);

    void setCurrentFilePercent(int currentFileProgress);

    void addDownloadBytes(long bytes);

    void setFailure(Exception failure);

    /**
     * This will be called even in the event of a failure.
     */
    void setFinished();
}
