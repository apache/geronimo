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
package org.apache.geronimo.deployment.hot;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.geronimo.deployment.cli.DeployUtils;
import org.apache.geronimo.common.DeploymentException;
import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Meant to be run as a Thread that tracks the contents of a directory.
 * It sends notifications for changes to its immediate children (it
 * will look into subdirs for changes, but will not send notifications
 * for files within subdirectories).  If a file continues to change on
 * every pass, this will wait until it stabilizes before sending an
 * add or update notification (to handle slow uploads, etc.).
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class DirectoryMonitor implements Runnable {
    private static final Log log = LogFactory.getLog(DirectoryMonitor.class);
    public static interface Listener {
        /**
         * The directory monitor doesn't take any action unless this method
         * returns true (to avoid deploying before the deploy GBeans are
         * running, etc.).
         */
        boolean isServerRunning();
        /**
         * Called during initialization on all files in the hot deploy
         * directory.
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
         * @return true if the addition was processed successfully.  If not
         *         the file will be added again next time it changes.
         */
        boolean fileAdded(File file);
        /**
         * @return true if the removal was processed successfully.  If not
         *         the file will be removed again on the next pass.
         */
        boolean fileRemoved(File file, String configId);

        void fileUpdated(File file, String configId);
    }

    private int pollIntervalMillis;
    private File directory;
    private boolean done = false;
    private Listener listener; // a little cheesy, but do we really need multiple listeners?
    private Map files = new HashMap();

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
        if(!directory.isDirectory() || !directory.canRead()) {
            throw new IllegalArgumentException("Cannot monitor directory "+directory.getAbsolutePath());
        }
        this.directory = directory;
    }

    public synchronized boolean isDone() {
        return done;
    }

    public synchronized void close() {
        this.done = true;
    }

    public void run() {
        while(!done) {
            try {
                Thread.sleep(pollIntervalMillis);
            } catch (InterruptedException e) {
                continue;
            }
            if(listener != null && listener.isServerRunning()) {
                scanDirectory();
            }
        }
    }

    public void initialize() {
        File parent = directory;
        File[] children = parent.listFiles();
        for (int i = 0; i < children.length; i++) {
            File child = children[i];
            if(!child.canRead()) {
                continue;
            }
            FileInfo now = child.isDirectory() ? getDirectoryInfo(child) : getFileInfo(child);
            now.setChanging(false);
            try {
                now.setConfigId(calculateModuleId(child));
                if(listener == null || listener.isFileDeployed(child, now.getConfigId())) {
                    if(listener != null) {
                        now.setModified(listener.getDeploymentTime(child, now.getConfigId()));
                    }
                    files.put(now.getPath(), now);
                }
            } catch (Exception e) {
                log.error("Unable to scan file "+child.getAbsolutePath()+" during initialization");
            }
        }
    }

    /**
     * Looks for changes to the immediate contents of the directory we're watching.
     */
    private void scanDirectory() {
        File parent = directory;
        File[] children = parent.listFiles();
        if(!directory.exists() || children == null) {
            log.error("Hot deploy directory has disappeared!  Shutting down directory monitor.");
            done = true;
            return;
        }
        HashSet oldList = new HashSet(files.keySet());
        for (int i = 0; i < children.length; i++) {
            File child = children[i];
            if(!child.canRead()) {
                continue;
            }
            FileInfo now = child.isDirectory() ? getDirectoryInfo(child) : getFileInfo(child);
            FileInfo then = (FileInfo) files.get(now.getPath());
            if(then == null) { // Brand new, wait a bit to make sure it's not still changing
                now.setNewFile(true);
                files.put(now.getPath(), now);
                log.debug("New File: "+now.getPath());
            } else {
                oldList.remove(then.getPath());
                if(now.isSame(then)) { // File is the same as the last time we scanned it
                    if(then.isChanging()) {
                        try {
                            log.debug("File finished changing: "+now.getPath());
                            // Used to be changing, now in (hopefully) its final state
                            if(then.isNewFile()) {
                                then.setConfigId(calculateModuleId(child));
                                if(listener == null || listener.fileAdded(child)) {
                                    then.setNewFile(false);
                                }
                            } else {
                                if(listener != null) listener.fileUpdated(child, then.getConfigId());
                            }
                            then.setChanging(false);
                        } catch (Exception e) {
                            log.error("Unable to deploy file "+child.getAbsolutePath(), e);
                        }
                    } // else it's just totally unchanged and we ignore it this pass
                } else {
                    // The two records are different -- record the latest as a file that's changing
                    // and later when it stops changing we'll do the add or update as appropriate.
                    now.setConfigId(then.getConfigId());
                    now.setNewFile(then.isNewFile());
                    files.put(now.getPath(), now);
                    log.debug("File Changed: "+now.getPath());
                }
            }
        }
        // Look for any files we used to know about but didn't find in this pass
        for (Iterator it = oldList.iterator(); it.hasNext();) {
            String name = (String) it.next();
            FileInfo info = (FileInfo) files.get(name);
            log.debug("File removed: "+name);
            if(info.isNewFile() || listener == null || listener.fileRemoved(new File(name), info.getConfigId())) {
                files.remove(name);
            }
        }
    }

    private static String calculateModuleId(File module) throws IOException, DeploymentException {
        String moduleId = DeployUtils.extractModuleIdFromArchive(module);
        if(moduleId == null) {
            int pos = module.getName().lastIndexOf('.');
            moduleId = pos > -1 ? module.getName().substring(0, pos) : module.getName();
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
            if(!child.canRead()) {
                continue;
            }
            if(child.isDirectory()) {
                test = getLastModifiedInDir(child);
            } else {
                test = child.lastModified();
            }
            if(test > value) {
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
            if(!path.equals(info.path)) {
                throw new IllegalArgumentException("Should only be used to compare two files representing the same path!");
            }
            return size == info.size && modified == info.modified;
        }
    }
}
