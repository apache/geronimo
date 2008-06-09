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
package org.apache.geronimo.system.plugin;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.MissingDependencyException;

/**
 * Provides the results of a configuration download operation.  This is updated
 * along the way for callers who want to monitor the ongoing progress.
 *
 * @version $Rev$ $Date$
 */
public class DownloadResults implements Serializable, DownloadPoller {
    private List<Artifact> removedConfigIDs = new ArrayList<Artifact>();
    private List<Artifact> restartedConfigIDs = new ArrayList<Artifact>();
    private List<Artifact> installedConfigIDs = new ArrayList<Artifact>();
    private List<Artifact> dependenciesPresent = new ArrayList<Artifact>();
    private List<Artifact> dependenciesInstalled = new ArrayList<Artifact>();
    private List<MissingDependencyException> skippedPlugins = new ArrayList<MissingDependencyException>();
    private String currentFile;
    private String currentMessage;
    private int currentFileProgress = -1;
    private Exception failure;
    private boolean finished;
    private long totalDownloadBytes = 0;

    public synchronized DownloadResults duplicate() {
        DownloadResults other = new DownloadResults();
        other.removedConfigIDs.addAll(removedConfigIDs);
        other.restartedConfigIDs.addAll(restartedConfigIDs);
        other.installedConfigIDs.addAll(installedConfigIDs);
        other.dependenciesPresent.addAll(dependenciesPresent);
        other.dependenciesInstalled.addAll(dependenciesInstalled);
        other.skippedPlugins.addAll(skippedPlugins);
        other.currentFile = currentFile;
        other.currentMessage = currentMessage;
        other.currentFileProgress = currentFileProgress;
        other.failure = failure;
        other.finished = finished;
        other.totalDownloadBytes = totalDownloadBytes;
        return other;
    }

    public synchronized void addInstalledConfigID(Artifact dep) {
        installedConfigIDs.add(dep);
    }

    public synchronized void addRemovedConfigID(Artifact obsolete) {
        removedConfigIDs.add(obsolete);
    }

    public synchronized void addRestartedConfigID(Artifact target) {
        restartedConfigIDs.add(target);
    }

    public void addSkippedConfigID(MissingDependencyException e) {
        skippedPlugins.add(e);
    }

    public synchronized void addDependencyPresent(Artifact dep) {
        dependenciesPresent.add(dep);
    }

    public synchronized void addDependencyInstalled(Artifact dep) {
        dependenciesInstalled.add(dep);
    }

    public synchronized void setCurrentFile(String currentFile) {
        this.currentFile = currentFile;
    }

    public synchronized void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
    }

    public synchronized void setCurrentFilePercent(int currentFileProgress) {
        this.currentFileProgress = currentFileProgress;
    }

    public synchronized void setFailure(Exception failure) {
        this.failure = failure;
    }

    public synchronized void setFinished() {
        finished = true;
    }

    public synchronized void addDownloadBytes(long bytes) {
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
    public List<Artifact> getInstalledConfigIDs() {
        return Collections.unmodifiableList(installedConfigIDs);
    }

    public List<Artifact> getRemovedConfigIDs() {
        return Collections.unmodifiableList(removedConfigIDs);
    }

    public List<Artifact> getRestartedConfigIDs() {
        return Collections.unmodifiableList(restartedConfigIDs);
    }

    public List<MissingDependencyException> getSkippedPlugins() {
        return Collections.unmodifiableList(skippedPlugins);
    }

    /**
     * Gets the dependencies that we've needed but they're already present in
     * the local server so no installation was necessary.
     */
    public List<Artifact> getDependenciesPresent() {
        return Collections.unmodifiableList(dependenciesPresent);
    }

    /**
     * Gets the dependencies that we've successfully downloaded and installed
     * into the local server environment.
     */
    public List<Artifact> getDependenciesInstalled() {
        return Collections.unmodifiableList(dependenciesInstalled);
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
