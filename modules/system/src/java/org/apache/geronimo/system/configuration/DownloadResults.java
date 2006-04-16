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

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * Provides the results of a configuration download operation.  This is updated
 * along the way for callers who want to monitor the ongoing progress.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class DownloadResults implements Serializable, DownloadPoller {
    private List installedConfigIDs = new ArrayList();
    private List dependenciesPresent = new ArrayList();
    private List dependenciesInstalled = new ArrayList();
    private String currentFile;
    private String currentMessage;
    private int currentFileProgress = -1;
    private Exception failure;
    private boolean finished;
    private long totalDownloadBytes = 0;

    public void addInstalledConfigID(Artifact dep) {
        installedConfigIDs.add(dep);
    }

    public void addDependencyPresent(Artifact dep) {
        dependenciesPresent.add(dep);
    }

    public void addDependencyInstalled(Artifact dep) {
        dependenciesInstalled.add(dep);
    }

    public void setCurrentFile(String currentFile) {
        this.currentFile = currentFile;
    }

    public void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
    }

    public void setCurrentFilePercent(int currentFileProgress) {
        this.currentFileProgress = currentFileProgress;
    }

    public void setFailure(Exception failure) {
        this.failure = failure;
    }

    public void setFinished() {
        finished = true;
    }

    public void addDownloadBytes(long bytes) {
        totalDownloadBytes += bytes;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isFailed() {
        return failure != null;
    }

    /**
     * The total number of bytes in the archives downloaded from remote
     * repositories.
     */
    public long getTotalDownloadBytes() {
        return totalDownloadBytes;
    }

    /**
     * If the operation failed, the Exception that caused the failure.
     */
    public Exception getFailure() {
        return failure;
    }

    /**
     * Gets the list of the originally requested Config IDs that were
     * successfully installed.  Ordinarily this is not necessary, but
     * it may be important in case of failure midway through, or if the
     * request passed previously downloaded configurations on the command
     * line and the caller doesn't know what the Config IDs are.
     */
    public Artifact[] getInstalledConfigIDs() {
        return (Artifact[]) installedConfigIDs.toArray(new Artifact[installedConfigIDs.size()]);
    }

    /**
     * Gets the dependencies that we've needed but they're already present in
     * the local server so no installation was necessary.
     */
    public Artifact[] getDependenciesPresent() {
        return (Artifact[]) dependenciesPresent.toArray(new Artifact[dependenciesPresent.size()]);
    }

    /**
     * Gets the dependencies that we've successfully downloaded and installed
     * into the local server environment.
     */
    public Artifact[] getDependenciesInstalled() {
        return (Artifact[]) dependenciesInstalled.toArray(new Artifact[dependenciesInstalled.size()]);
    }

    /**
     * Gets the name of the file that is currently being operated on.
     */
    public String getCurrentFile() {
        return currentFile;
    }

    /**
     * Gets a description of the work currently being done.
     */
    public String getCurrentMessage() {
        return currentMessage;
    }

    /**
     * Gets the progress on the current file expressed as a percentage.  This
     * value may be -1 in which case the progress cannot be calculated (e.g. a
     * download where the server doesn't supply the file size up front).
     */
    public int getCurrentFilePercent() {
        return currentFileProgress;
    }
}
