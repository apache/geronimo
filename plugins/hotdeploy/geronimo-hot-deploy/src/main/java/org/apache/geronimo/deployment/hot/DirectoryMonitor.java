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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
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
    private final Map<String, FileInfo> files;
    private volatile String workingOnConfigId;
    private final ArrayList <Artifact> toRemove = new ArrayList <Artifact>();
    private File monitorFile;

    public DirectoryMonitor(File directory, Listener listener, int pollIntervalMillis) {
        this(directory, null, listener, pollIntervalMillis);
    }

    public DirectoryMonitor(File directory, File monitorFile, Listener listener, int pollIntervalMillis) {
        this.directory = directory;
        this.listener = listener;
        this.pollIntervalMillis = pollIntervalMillis;
        this.monitorFile = monitorFile;
        this.files = readState();
        persistState();
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

    // The undeployed operation could occur in this hot-deploy thread or in console/cli.
    // The id that go to "toRemove" should be the ones that not undeployed in this hot-deploy thread 
    // BTW, now we support EBA deployment. And if an EBA includes a WAB, which also has a configuration id during EBA deployment, 
    // this method also can be called with the WAB id. And the WAB' so id will also go into the "toRemove" list.
    public void removeModuleId(Artifact id) {
        log.info("Hot deployer notified that an artifact was undeployed: "+id);
        if(id.toString().equals(workingOnConfigId)) {
            // don't react to undelpoy events we generated ourselves
            // because the file update action(i.e. redeploy) will cause the old module to undeploy first
            // we must handle this so that its config id won't go "toRemove" list to avoid the deletion of the new file.
            return; 
        }
        synchronized (toRemove) {
            toRemove.add(id);
        }
    }

    private void doRemoves() {
        synchronized (toRemove) {
            synchronized (files) {
                for (Iterator<Artifact> idItr = toRemove.iterator(); idItr.hasNext();) {
                    Artifact id = idItr.next();
                    for (Iterator<String> filesItr = files.keySet().iterator(); filesItr.hasNext();) {
                        String path = filesItr.next();
                        FileInfo info = files.get(path);
                        if (info.getConfigId() == null){
                            // the file is new added, have not deployed yet, so its config id is not set.
                            continue;
                        }
                        Artifact target = Artifact.create(info.getConfigId());
                        if (id.matches(target)) { // need to remove record & delete file
                            File file = new File(path);
                            if (file.exists()) {  // if not, probably it's deletion kicked off this whole process
                                log.info("Hot deployer deleting " + id);
                                if (!FileUtils.recursiveDelete(file)) {
                                    log.error("Hot deployer unable to delete " + path);
                                }
                                filesItr.remove();
                            }
                        }
                    }
                    // remove the id form the toRemove list no matter if we found it in the "files" list
                    idItr.remove();
                }
            }
        }
    }

    private void persistState() {
        if (monitorFile == null) {
            return;
        }
        
        log.info("Persisting directory monitor state to " + monitorFile.getName());
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(monitorFile));
            outputStream.writeObject(files);
        } catch (IOException ioe) {
            log.warn("Error persisting directory monitor state to " + monitorFile.getName(), ioe);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ioe) {
                    
                }
            }
        }    
    }
    
    @SuppressWarnings("unchecked")
    private Map<String,FileInfo> readState() {
        Map<String,FileInfo> newFiles = null;
        if (monitorFile != null) {
                ObjectInputStream inputStream = null;
                try {
                    inputStream = new ObjectInputStream(new FileInputStream(monitorFile));
                    newFiles = (Map<String,FileInfo>) inputStream.readObject();
                } catch (IOException ex) {
                    log.info("No directory monitor state to be read. This is to be expected on initial start of a new server");
                } catch (ClassNotFoundException cnfe) {
                    log.warn("ClassNotFoundException reading directory monitor state from " + monitorFile.getName(), cnfe);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException ioe) {
                        // ignore
                    }
                }
        }
        if (newFiles == null) {
            newFiles = new HashMap<String,FileInfo>();
        }
        return newFiles;
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
                            doRemoves();
                            scanDirectory();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error during hot deployment", e);
            }
        }
    }

    private void initialize() {
        File parent = directory;
        File[] children = parent.listFiles();
        for (int i = 0; i < children.length; i++) {
            File child = children[i];
            if (!child.canRead()) {
                continue;
            }
            if (child.equals(monitorFile)) {
                continue;
            }
            FileInfo now = child.isDirectory() ? getDirectoryInfo(child) : getFileInfo(child);
            now.setChanging(false);
            try {
                now.setConfigId(calculateModuleId(child));
                if (listener.isFileDeployed(child, now.getConfigId())) {
                    now.setModified(listener.getDeploymentTime(child, now.getConfigId()));
                    log.info("At startup, found "+now.getPath()+" with deploy time "+now.getModified()+" and file time "+new File(now.getPath()).lastModified());
                    files.put(now.getPath(), now);
                }
            } catch (Exception e) {
                log.error("Unable to scan file " + child.getAbsolutePath() + " during initialization", e);
            }
        }
        persistState();
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
            Set<String> oldList = new HashSet<String>(files.keySet());
            List<FileAction> actions = new LinkedList<FileAction>();
            boolean changeMade = false;
            for (int i = 0; i < children.length; i++) {
                File child = children[i];
                if (!child.canRead()) {
                    continue;
                }
                if (child.equals(monitorFile)) {
                    continue;
                }
                FileInfo now = child.isDirectory() ? getDirectoryInfo(child) : getFileInfo(child);
                FileInfo last = files.get(now.getPath());
                if (last == null) { // Brand new, wait a bit to make sure it's not still changing
                    now.setNewFile(true);
                    files.put(now.getPath(), now);
                    changeMade = true;
                    log.debug("New File: " + now.getPath());
                } else {
                    oldList.remove(last.getPath());
                    if (now.isSame(last)) { // File is the same as the last time we scanned it
                        if (last.isChanging()) { // else it's just totally unchanged and we ignore it this pass
                            log.debug("File finished changing: " + now.getPath());
                            // Used to be changing, now in (hopefully) its final state
                            if (last.isNewFile()) {
                                actions.add(new FileAction(FileAction.NEW_FILE, child, last));
                            } else {
                                actions.add(new FileAction(FileAction.UPDATED_FILE, child, last));
                            }
                            last.setChanging(false);
                        } 
                    } else { // is changing, when finish will do the file action accordingly
                        if (last.isNewFile()){ // is adding file
                            now.setNewFile(last.isNewFile());
                        } else { // is replacing file
                            now.setConfigId(last.getConfigId());
                        }
                        files.put(now.getPath(), now);
                        changeMade = true;
                        log.debug("File Changed: " + now.getPath());
                    }
                }
            }
            
            // Look for any files we used to know about but didn't find in this pass
            for (Iterator<String> it = oldList.iterator(); it.hasNext();) {
                String name = it.next();
                FileInfo info = files.get(name);
                log.debug("File removed: " + name);
                if (info.isNewFile()) { // was never deployed, just drop it
                    files.remove(name);
                    changeMade = true;
                } else {
                    actions.add(new FileAction(FileAction.REMOVED_FILE, new File(name), info));
                }
            }
            
            // First pass: validate all changed files, so any obvious errors come out first
            for (Iterator<FileAction> it = actions.iterator(); it.hasNext();) {
                FileAction action = it.next();
                if (!listener.validateFile(action.child, action.info.getConfigId())) {
                    resolveFile(action);
                    it.remove();
                }
            }
                
            // Second pass: do what we're meant to do
            for (Iterator<FileAction> it = actions.iterator(); it.hasNext();) {
                FileAction action = it.next();
                try {
                    if (action.action == FileAction.REMOVED_FILE) {
                        workingOnConfigId = action.info.getConfigId();
                        if (action.info.getConfigId() == null || listener.fileRemoved(action.child, action.info.getConfigId())) {
                            files.remove(action.child.getPath());
                            changeMade = true;
                        }
                        workingOnConfigId = null;
                    } else if (action.action == FileAction.NEW_FILE) {
                        if (listener.isFileDeployed(action.child, calculateModuleId(action.child))) {
                            workingOnConfigId = calculateModuleId(action.child);
                            String result = listener.fileUpdated(action.child, workingOnConfigId);
                            if (result != null) {
                                if (!result.equals("")) {
                                    action.info.setConfigId(result);
                                } else {
                                    action.info.setConfigId(calculateModuleId(action.child));
                                }
                            }
                            // remove the previous jar or directory if duplicate
                            File[] childs = directory.listFiles();
                            for (int i = 0; i < childs.length; i++) {
                                File child = children[i];
                                if (!child.canRead()) {
                                    continue;
                                }
                                if (child.equals(monitorFile)) {
                                    continue;
                                }
                                String path = child.getAbsolutePath();
                                String configId = files.get(path).getConfigId();
                                if (configId != null && configId.equals(workingOnConfigId) && !action.child.getAbsolutePath().equals(path)) {
                                    File fd = new File(path);
                                    if (fd.isDirectory()) {
                                        log.info("Deleting the Directory: " + path);
                                        if (FileUtils.recursiveDelete(fd))
                                            log.debug("Successfully deleted the Directory: " + path);
                                        else
                                            log.error("Couldn't delete the hot deployed directory=" + path);
                                    } else if (fd.isFile()) {
                                        log.info("Deleting the File: " + path);
                                        if (fd.delete()) {
                                            log.debug("Successfully deleted the File: " + path);
                                        } else
                                            log.error("Couldn't delete the hot deployed file=" + path);
                                    }
                                    files.remove(path);
                                    changeMade = true;
                                }
                            }
                            workingOnConfigId = null;
                        } else {
                            String result = listener.fileAdded(action.child);
                            if (result != null) {
                                if (!result.equals("")) {
                                    action.info.setConfigId(result);
                                } else {
                                    action.info.setConfigId(calculateModuleId(action.child));
                                }
                            }
                        }
                        action.info.setNewFile(false);
                        changeMade = true;
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

            if (changeMade) {
                persistState();
            }
        }
    }
    
    private void resolveFile(FileAction action) {
        if (action.action == FileAction.REMOVED_FILE) {
            files.remove(action.child.getPath());
        } 
        
        /* we don't need this, because the setChanging(false) has been called after the NEW_FILE/UPDATED_FILE action object was created.
          else {
            action.info.setChanging(false);
        }*/
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
