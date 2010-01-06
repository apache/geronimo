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
package org.apache.geronimo.deployment.hot;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.deployment.cli.DeployUtils;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Meant to be run as a Thread that tracks the contents of a directory.
 * It sends notifications for changes to its immediate children (it
 * will look into subdirs for changes, but will not send notifications
 * for files within subdirectories).  If a file continues to change on
 * every pass, this will wait until it stabilizes before sending an
 * add or update notification (to handle slow uploads, etc.).
 *
 * @version $Rev$ $Date$
 */
public class DirectoryMonitor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DirectoryMonitor.class);

    public static interface Listener {
        /**
         * The directory monitor doesn't take any action unless this method
         * returns true (to avoid deploying before the deploy GBeans are
         * running, etc.).
         */
        boolean isServerRunning();

        /**
         * Checks if the file with same configID is already deployed
         *
         * @return true if the file in question is already available in the
         *         server, false if it should be deployed on the next pass.
         */
        boolean isFileDeployed(File file, String configId);

        /**
         * Called during initialization on previously deployed files.
         *
         * @return The time that the file was deployed.  If the current
         *         version in the directory is newer, the file will be
         *         updated on the first pass.
         */
        long getDeploymentTime(File file, String configId);

        /**
         * Called to indicate that the monitor has fully initialized
         * and will be doing normal deployment operations from now on.
         */
        void started();

        /**
         * Called to check whether a file passes the smell test before
         * attempting to deploy it.
         *
         * @return true if there's nothing obviously wrong with this file.
         *         false if there is (for example, it's clearly not
         *         deployable).
         */
        boolean validateFile(File file, String configId);

        /**
         * @return A configId for the deployment if the addition was processed
         *         successfully (or an empty String if the addition was OK but
         *         the configId could not be determined).  null if the addition
         *         failed, in which case the file will be added again next time
         *         it changes.
         */
        String fileAdded(File file);

        /**
         * @return true if the removal was processed successfully.  If not
         *         the file will be removed again on the next pass.
         */
        boolean fileRemoved(File file, String configId);

        String fileUpdated(File file, String configId);

        /**
         * This method returns the module id of an application deployed in the default group.
         * @return String respresenting the ModuleId if the application is already deployed
         */
        String getModuleId(String config);

    }

    private int pollIntervalMillis;
    private File directory;
    private boolean done = false;
    private Listener listener; // a little cheesy, but do we really need multiple listeners?
    private final Map files = new HashMap();
    private volatile String workingOnConfigId;

    public DirectoryMonitor(File directory, Listener listener, int pollIntervalMillis) {
        this.directory = directory;
        this.listener = listener;
        this.pollIntervalMillis = pollIntervalMillis;
    }

    public int getPollIntervalMillis() {
        return pollIntervalMillis;
    }

    public void setPollIntervalMillis(int pollIntervalMillis) {
        this.pollIntervalMillis = pollIntervalMillis;
    }

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public File getDirectory() {
        return directory;
    }

    /**
     * Warning: changing the directory at runtime will cause all files in the
     * old directory to be removed and all files in the new directory to be
     * added, next time the thread awakens.
     */
    public void setDirectory(File directory) {
        if (!directory.isDirectory() || !directory.canRead()) {
            throw new IllegalArgumentException("Cannot monitor directory " + directory.getAbsolutePath());
        }
        this.directory = directory;
    }

    public synchronized boolean isDone() {
        return done;
    }

    public synchronized void close() {
        this.done = true;
    }

    public void removeModuleId(Artifact id) {
        log.info("Hot deployer notified that an artifact was removed: "+id);
        if(id.toString().equals(workingOnConfigId)) {
            // since the redeploy process inserts a new thread to handle progress,
            // this is called by a different thread than the hot deploy thread during
            // a redeploy, and this check must be executed outside the synchronized
            // block or else it will cause a deadlock!
            return; // don't react to events we generated ourselves
        }
        synchronized(files) {
            for (Iterator it = files.keySet().iterator(); it.hasNext();) {
                String path = (String) it.next();
                FileInfo info = (FileInfo) files.get(path);
                Artifact target = Artifact.create(info.getConfigId());
                if(id.matches(target)) { // need to remove record & delete file
                    File file = new File(path);
                    if(file.exists()) { // if not, probably it's deletion kicked off this whole process
                        log.info("Hot deployer deleting "+id);
                        if(!FileUtils.recursiveDelete(file)) {
                            log.error("Hot deployer unable to delete "+path);
                        }
                        it.remove();
                    }
                }
            }
        }
    }

    public void run() {
        boolean serverStarted = false, initialized = false;
        while (!done) {
            try {
                Thread.sleep(pollIntervalMillis);
            } catch (InterruptedException e) {
                continue;
            }
            try {
                if (listener != null) {
                    if (!serverStarted && listener.isServerRunning()) {
                        serverStarted = true;
                    }
                    if (serverStarted) {
                        if (!initialized) {
                            initialized = true;
                            initialize();
                            listener.started();
                        } else {
                            scanDirectory();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error during hot deployment", e);
            }
        }
    }

    public void initialize() {
        File parent = directory;
        File[] children = parent.listFiles();
        for (int i = 0; i < children.length; i++) {
            File child = children[i];
            if (!child.canRead()) {
                continue;
            }
            FileInfo now = child.isDirectory() ? getDirectoryInfo(child) : getFileInfo(child);
            now.setChanging(false);
            try {
                now.setConfigId(calculateModuleId(child));
                if (listener == null || listener.isFileDeployed(child, now.getConfigId())) {
                    if (listener != null) {
                        now.setModified(listener.getDeploymentTime(child, now.getConfigId()));
                    }
log.info("At startup, found "+now.getPath()+" with deploy time "+now.getModified()+" and file time "+new File(now.getPath()).lastModified());
                    files.put(now.getPath(), now);
                }
            } catch (Exception e) {
                log.error("Unable to scan file " + child.getAbsolutePath() + " during initialization", e);
            }
        }
    }

    /**
     * Looks for changes to the immediate contents of the directory we're watching.
     */
    private void scanDirectory() {
        File parent = directory;
        File[] children = parent.listFiles();
        if (!directory.exists() || children == null) {
            log.error("Hot deploy directory has disappeared!  Shutting down directory monitor.");
            done = true;
            return;
        }
        synchronized (files) {
            Set oldList = new HashSet(files.keySet());
            List actions = new LinkedList();
            for (int i = 0; i < children.length; i++) {
                File child = children[i];
                if (!child.canRead()) {
                    continue;
                }
                FileInfo now = child.isDirectory() ? getDirectoryInfo(child) : getFileInfo(child);
                FileInfo then = (FileInfo) files.get(now.getPath());
                if (then == null) { // Brand new, wait a bit to make sure it's not still changing
                    now.setNewFile(true);
                    files.put(now.getPath(), now);
                    log.debug("New File: " + now.getPath());
                } else {
                    oldList.remove(then.getPath());
                    if (now.isSame(then)) { // File is the same as the last time we scanned it
                        if (then.isChanging()) {
                            log.debug("File finished changing: " + now.getPath());
                            // Used to be changing, now in (hopefully) its final state
                            if (then.isNewFile()) {
                                actions.add(new FileAction(FileAction.NEW_FILE, child, then));
                            } else {
                                actions.add(new FileAction(FileAction.UPDATED_FILE, child, then));
                            }
                            then.setChanging(false);
                        } // else it's just totally unchanged and we ignore it this pass
                    } else if(then.isNewFile() || now.getModified() > then.getModified()) {
                        // The two records are different -- record the latest as a file that's changing
                        // and later when it stops changing we'll do the add or update as appropriate.
                        now.setConfigId(then.getConfigId());
                        now.setNewFile(then.isNewFile());
                        files.put(now.getPath(), now);
                        log.debug("File Changed: " + now.getPath());
                    }
                }
            }
            // Look for any files we used to know about but didn't find in this pass
            for (Iterator it = oldList.iterator(); it.hasNext();) {
                String name = (String) it.next();
                FileInfo info = (FileInfo) files.get(name);
                log.debug("File removed: " + name);
                if (info.isNewFile()) { // Was never added, just whack it
                    files.remove(name);
                } else {
                    actions.add(new FileAction(FileAction.REMOVED_FILE, new File(name), info));
                }
            }
            if (listener != null) {
                // First pass: validate all changed files, so any obvious errors come out first
                for (Iterator it = actions.iterator(); it.hasNext();) {
                    FileAction action = (FileAction) it.next();
                    if (!listener.validateFile(action.child, action.info.getConfigId())) {
                        resolveFile(action);
                        it.remove();
                    }
                }
                // Second pass: do what we're meant to do
                for (Iterator it = actions.iterator(); it.hasNext();) {
                    FileAction action = (FileAction) it.next();
                    try {
                        if (action.action == FileAction.REMOVED_FILE) {
                            workingOnConfigId = action.info.getConfigId();
                            if (action.info.getConfigId() == null || listener.fileRemoved(action.child, action.info.getConfigId())) {
                                files.remove(action.child.getPath());
                            }
                            workingOnConfigId = null;
                        } else if (action.action == FileAction.NEW_FILE) {
                            if (listener.isFileDeployed(action.child, calculateModuleId(action.child))) {
                                workingOnConfigId = calculateModuleId(action.child);
                                String result = listener.fileUpdated(action.child, workingOnConfigId);
                                if (result != null) {
                                    if (!result.equals("")) {
                                        action.info.setConfigId(result);
                                    }
                                    else {
                                        action.info.setConfigId(calculateModuleId(action.child));
                                    }
                                }
                                // remove the previous jar or directory if duplicate
                                File[] childs = directory.listFiles();
                                for (int i = 0; i < childs.length; i++) {
                                    String path = childs[i].getAbsolutePath();
                                    String configId = ((FileInfo)files.get(path)).configId;
                                    if (configId != null && configId.equals(workingOnConfigId) && !action.child.getAbsolutePath().equals(path)) {
                                        File fd = new File(path);
                                        if (fd.isDirectory()) {
                                            log.info("Deleting the Directory: "+path);
                                            if (FileUtils.recursiveDelete(fd))
                                                log.debug("Successfully deleted the Directory: "+path);
                                            else
                                                log.error("Couldn't delete the hot deployed directory="+path);
                                        }
                                        else if (fd.isFile()) {
                                            log.info("Deleting the File: "+path);
                                            if (fd.delete()) {
                                                log.debug("Successfully deleted the File: "+path);
                                            }
                                            else
                                                log.error("Couldn't delete the hot deployed file="+path);
                                        }
                                        files.remove(path);
                                    }
                                }
                                workingOnConfigId = null;
                            }
                            else {
                                String result = listener.fileAdded(action.child);
                                if (result != null) {
                                    if (!result.equals("")) {
                                        action.info.setConfigId(result);
                                    }
                                    else {
                                        action.info.setConfigId(calculateModuleId(action.child));
                                    }
                                }
                            }
                            action.info.setNewFile(false);
                        } else if (action.action == FileAction.UPDATED_FILE) {
                            workingOnConfigId = action.info.getConfigId();
                            String result = listener.fileUpdated(action.child, action.info.getConfigId());
                            FileInfo update = action.info;
                            if (result != null) {
                                if (!result.equals("")) {
                                    update.setConfigId(result);
                                } else {
                                    update.setConfigId(calculateModuleId(action.child));
                                }
                            }
                            workingOnConfigId = null;
                        }
                    } catch (Exception e) {
                        log.error("Unable to " + action.getActionName() + " file " + action.child.getAbsolutePath(), e);
                    } finally {
                        resolveFile(action);
                    }
                }
            }
        }
    }

    private void resolveFile(FileAction action) {
        if (action.action == FileAction.REMOVED_FILE) {
            files.remove(action.child.getPath());
        } else {
            action.info.setChanging(false);
        }
    }

    private String calculateModuleId(File module) {
        String moduleId = null;
        try {
            moduleId = DeployUtils.extractModuleIdFromArchive(module);
        } catch (Exception e) {
            try {
                moduleId = DeployUtils.extractModuleIdFromPlan(module);
            } catch (IOException e2) {
                log.warn("Unable to calculate module ID for file " + module.getAbsolutePath() + " [" + e2.getMessage() + "]");
            }
        }
        if (moduleId == null) {
            int pos = module.getName().lastIndexOf('.');
            moduleId = pos > -1 ? module.getName().substring(0, pos) : module.getName();
            moduleId = listener.getModuleId(moduleId);
        }
        return moduleId;
    }

    /**
     * We don't pay attention to the size of the directory or files in the
     * directory, only the highest last modified time of anything in the
     * directory.  Hopefully this is good enough.
     */
    private FileInfo getDirectoryInfo(File dir) {
        FileInfo info = new FileInfo(dir.getAbsolutePath());
        info.setSize(0);
        info.setModified(getLastModifiedInDir(dir));
        return info;
    }

    private long getLastModifiedInDir(File dir) {
        long value = dir.lastModified();
        File[] children = dir.listFiles();
        long test;
        for (int i = 0; i < children.length; i++) {
            File child = children[i];
            if (!child.canRead()) {
                continue;
            }
            if (child.isDirectory()) {
                test = getLastModifiedInDir(child);
            } else {
                test = child.lastModified();
            }
            if (test > value) {
                value = test;
            }
        }
        return value;
    }

    private FileInfo getFileInfo(File child) {
        FileInfo info = new FileInfo(child.getAbsolutePath());
        info.setSize(child.length());
        info.setModified(child.lastModified());
        return info;
    }

    private static class FileAction {
        private static int NEW_FILE = 1;
        private static int UPDATED_FILE = 2;
        private static int REMOVED_FILE = 3;
        private int action;
        private File child;
        private FileInfo info;

        public FileAction(int action, File child, FileInfo info) {
            this.action = action;
            this.child = child;
            this.info = info;
        }

        public String getActionName() {
            return action == NEW_FILE ? "deploy" : action == UPDATED_FILE ? "redeploy" : "undeploy";
        }
    }

    private static class FileInfo implements Serializable {
        private String path;
        private long size;
        private long modified;
        private boolean newFile;
        private boolean changing;
        private String configId;

        public FileInfo(String path) {
            this.path = path;
            newFile = false;
            changing = true;
        }

        public String getPath() {
            return path;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getModified() {
            return modified;
        }

        public void setModified(long modified) {
            this.modified = modified;
        }

        public boolean isNewFile() {
            return newFile;
        }

        public void setNewFile(boolean newFile) {
            this.newFile = newFile;
        }

        public boolean isChanging() {
            return changing;
        }

        public void setChanging(boolean changing) {
            this.changing = changing;
        }

        public String getConfigId() {
            return configId;
        }

        public void setConfigId(String configId) {
            this.configId = configId;
        }

        public boolean isSame(FileInfo info) {
            if (!path.equals(info.path)) {
                throw new IllegalArgumentException("Should only be used to compare two files representing the same path!");
            }
            return size == info.size && modified == info.modified;
        }
    }
}
