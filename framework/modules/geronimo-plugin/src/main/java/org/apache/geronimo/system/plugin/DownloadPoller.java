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

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.MissingDependencyException;

/**
 * An interface for callers who want to monitor the progress of an installation.
 * These are all callbacks sent by the server.
 *
 * @see PluginInstaller
 *
 * @version $Rev$ $Date$
 */
public interface DownloadPoller {
    /**
     * Notes a configuration that was removed because it was obsoleted by a
     * newly-installed configuration.
     */
    void addRemovedConfigID(Artifact obsolete);

    /**
     * Notes that a configuration passed as an argument to the install
     * operation was successfully installed.  This will only be called on
     * the original arguments to the install command, not on anything
     * installed because it was a dependency.
     */
    void addInstalledConfigID(Artifact target);

    /**
     * Notes that a configuration was restarted as a result of the
     * current operation.  This usually means that it depended on a
     * configuration that was obsoleted (removed), so it shut down when
     * the remove happened, and was started up again after the replacement
     * was installed.
     */
    void addRestartedConfigID(Artifact target);

    /**
     * Provides details on why a plugin was not installed.
     * @param e MissingDependencyException containing info on 
     */
    void addSkippedConfigID(MissingDependencyException e);

    /**
     * Notes that the current install operation found a dependency, and that
     * dependency was satisfied by an artifact already available in the
     * current server environment.
     */
    void addDependencyPresent(Artifact dep);

    /**
     * Notes that the current install operation found a dependency, and that
     * dependency was downloaded from a remote repository and installed into
     * the local server environment.
     */
    void addDependencyInstalled(Artifact dep);

    /**
     * Indicates which file the configuration installer is working on at the
     * moment.  Mainly for purposes of user feedback during asynchronous
     * requests.
     */
    void setCurrentFile(String currentFile);

    /**
     * Describes the current operation status as a text message.  Mainly for
     * purposes of user feedback during asynchronous requests.
     */
    void setCurrentMessage(String currentMessage);

    /**
     * Gives the percent complete for a file currently being downloaded.
     * Mainly for purposes of user feedback during asynchronous requests.
     * This may be -1 if the download server does not supply the file size in
     * advance.
     */
    void setCurrentFilePercent(int currentFileProgress);

    /**
     * Called at the end of a file download with the number of bytes downloaded
     * in the current operation.  This can be used to calculate a rough
     * transfer rate (the time between setCurrentFile and setDownloadBytes) as
     * well as if the caller wants to total the size of all downloads for the
     * current installation.
     */
    void addDownloadBytes(long bytes);

    /**
     * Indicates that a failure was encountered during the installation
     * operation.  Any failure is currently treated as fatal -- the installer
     * will not attempt to complete additional tasks after a failure.
     */
    void setFailure(Exception failure);

    /**
     * Always called when the operation is complete, regardless of whether
     * there was a failure or not.
     */
    void setFinished();
}
