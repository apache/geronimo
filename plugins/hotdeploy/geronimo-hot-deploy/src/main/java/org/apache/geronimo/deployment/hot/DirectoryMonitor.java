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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

    private int pollIntervalMillis;
    private File directory;
    private boolean done = false;
    private MonitorListener listener; // a little cheesy, but do we really need multiple listeners?
    // This Map records the files in the hot deploy folder. 
    // The FileInfo's configId could be null, which represents it was not successfully deployed 
    // through this hot deployer.
    private Map<String, FileInfo> fileRecords;
    private final ArrayList <String> toRemove = new ArrayList <String>();
    private File monitorFile;

    public DirectoryMonitor(File directory, MonitorListener listener, int pollIntervalMillis) {
        this(directory, null, listener, pollIntervalMillis);
    }

    public DirectoryMonitor(File directory, File monitorFile, MonitorListener listener, int pollIntervalMillis) {
        this.directory = directory;
        this.listener = listener;
        this.pollIntervalMillis = pollIntervalMillis;
        this.monitorFile = monitorFile;
    }

    public int getPollIntervalMillis() {
        return pollIntervalMillis;
    }

    public void setPollIntervalMillis(int pollIntervalMillis) {
        this.pollIntervalMillis = pollIntervalMillis;
    }

    public MonitorListener getMonitorListener() {
        return listener;
    }

    public void setMonitorListener(MonitorListener listener) {
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

    /* 
     * The undeployed operation could occur in this hot-deploy thread or in console/cli.
     * The id that go to "toRemove" should be the ones that not undeployed in this hot-deploy thread. 
     */
    public void removeFile(String configId) {
        log.info("Hot deployer notified that an artifact was undeployed: " + configId);
        synchronized (toRemove) {
            toRemove.add(configId);
        }
    }

    private void doRemoves() {
        synchronized (toRemove) {
            synchronized (fileRecords) {
                boolean changeMade = false;
                for (Iterator<String> idItr = toRemove.iterator(); idItr.hasNext();) {
                    String configId = idItr.next();
                    for (Iterator<String> filesItr = fileRecords.keySet().iterator(); filesItr.hasNext();) {
                        String path = filesItr.next();
                        FileInfo info = fileRecords.get(path);
                        if (info.getConfigId() == null) {
                            // the file is new added, have not deployed yet, so its config id is not set.
                            continue;
                        }
                        if (configId.equals(info.getConfigId())) { // need to remove record & delete file
                            File file = new File(path);
                            if (file.exists()) {  // if not, probably it's deletion kicked off this whole process
                                log.info("Hot deployer deleting " + path);
                                if (!FileUtils.recursiveDelete(file)) {
                                    log.error("Hot deployer unable to delete " + path);
                                }
                            }
                            filesItr.remove();
                            changeMade = true;
                        }
                    }
                    // remove the id form the toRemove list no matter if we found it in the "files" list
                    idItr.remove();
                }
                if (changeMade) {
                    persistState();
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
            outputStream.writeObject(fileRecords);
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
    private Map<String, FileInfo> readState() {
        Map<String, FileInfo> newFiles = null;
        if (monitorFile != null) {
            ObjectInputStream inputStream = null;
            try {
                inputStream = new ObjectInputStream(new FileInputStream(monitorFile));
                newFiles = (Map<String, FileInfo>) inputStream.readObject();
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
            newFiles = new HashMap<String, FileInfo>();
        }
        return newFiles;
    }
    
    
    public void run() {
        boolean initialized = false;
        while (!done) {
            try {
                Thread.sleep(pollIntervalMillis);
            } catch (InterruptedException e) {
                continue;
            }
            try {
                if (listener != null) {
                    if (!initialized) {
                        initialized = true;
                        initialize();
                    } else {
                        doRemoves();
                        scanDirectory();
                    }
                }
            } catch (Exception e) {
                log.error("Error during hot deployment", e);
            }
        }
    }

    private void initialize() {
        fileRecords = readState();
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
        synchronized (fileRecords) {
            Set<String> oldList = new HashSet<String>(fileRecords.keySet());
            
            Collection<FileInfo> addedFiles = new ArrayList<FileInfo>();
            Collection<FileInfo> modifiedFiles = new ArrayList<FileInfo>();
            Collection<FileInfo> deletedFiles = new ArrayList<FileInfo>();

            boolean changeMade = false;
            for (int i = 0; i < children.length; i++) {
                File child = children[i];
                if (!child.canRead()) {
                    continue;
                }
                if (child.equals(monitorFile)) {
                    continue;
                }
                FileInfo now = getFileInfo(child);
                FileInfo last = fileRecords.get(now.getPath());
                if (last == null) { // Brand new, wait a bit to make sure it's not still changing
                    now.setNewFile(true);
                    fileRecords.put(now.getPath(), now);
                    changeMade = true;
                    log.debug("New File: " + now.getPath());
                } else {
                    oldList.remove(last.getPath());
                    if (now.isSame(last)) { // File is the same as the last time we scanned it
                        if (last.isChanging()) { // else it's just totally unchanged and we ignore it this pass
                            log.debug("File finished changing: " + now.getPath());
                            // Used to be changing, now in (hopefully) its final state
                            if (last.isNewFile()) {
                                addedFiles.add(last);
                                last.setNewFile(false);
                            } else {
                                modifiedFiles.add(last);
                            }
                            last.setChanging(false);
                            changeMade = true;
                        } 
                    } else { // is changing, when finish will do the file action accordingly
                        if (last.isNewFile()) { // is adding file
                            now.setNewFile(last.isNewFile());
                        } else { // is replacing file
                            now.setConfigId(last.getConfigId());
                        }
                        fileRecords.put(now.getPath(), now);
                        changeMade = true;
                        log.debug("File Changed: " + now.getPath());
                    }
                }
            }
            
            // Look for any files we used to know about but didn't find in this pass
            for (Iterator<String> it = oldList.iterator(); it.hasNext();) {
                String name = it.next();
                FileInfo info = fileRecords.remove(name);
                changeMade = true;
                log.debug("File removed: " + name);
                deletedFiles.add(info);
            }
                        
            validate(addedFiles, false);
            validate(modifiedFiles, false);
            
            if (!addedFiles.isEmpty() || !modifiedFiles.isEmpty() || !deletedFiles.isEmpty()) {
                log.debug("Added files: {}", addedFiles);
                log.debug("Modified files: {}", modifiedFiles);
                log.debug("Deleted files: {}", deletedFiles);
                try {
                    listener.scanComplete(addedFiles, modifiedFiles, deletedFiles);
                } catch (Throwable e) {
                    log.warn("Error calling scanComplete()", e);
                }
            }
            
            if (changeMade) {
                persistState();
            }
        }
    }
    
    private void validate(Collection<FileInfo> updatedFiles, boolean delete) {
        for (Iterator<FileInfo> it = updatedFiles.iterator(); it.hasNext();) {
            FileInfo info = it.next();
            if (!listener.validateFile(new File(info.getPath()), info.getConfigId())) {
                it.remove();
                if (delete) {
                    fileRecords.remove(info.getPath());
                }
            }
        }
    }
   
    private FileInfo getFileInfo(File file) {
        FileInfo info = new FileInfo(file.getAbsolutePath());
        info.setSize(file.isDirectory() ? 0 : file.length());
        info.setModified(getLastModified(file));
        return info;
    }
    
    /**
     * We don't pay attention to the size of the directory or files in the
     * directory, only the highest last modified time of anything in the
     * directory.  Hopefully this is good enough.
     */
    public static long getLastModified(File file) {
        long value = file.lastModified();
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            long test;
            for (int i = 0; i < children.length; i++) {
                File child = children[i];
                if (!child.canRead()) {
                    continue;
                }
                test = getLastModified(child);
                if (test > value) {
                    value = test;
                }
            }
        }
        return value;
    }

    public static interface MonitorListener {
        
        /**
         * Called to check whether a file passes the smell test before
         * attempting to deploy it.
         *
         * @return true if there's nothing obviously wrong with this file.
         *         false if there is (for example, it's clearly not
         *         deployable).
         */
        boolean validateFile(File file, String configId);
        
        void scanComplete(Collection<FileInfo> addedFiles, Collection<FileInfo> modifiedFiles, Collection<FileInfo> deletedFiles);

    }
    
    public static class FileInfo implements Serializable {
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
