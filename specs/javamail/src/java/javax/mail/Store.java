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

package javax.mail;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.mail.event.FolderEvent;
import javax.mail.event.FolderListener;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;
/**
 * @version $Rev$ $Date$
 */
public abstract class Store extends Service {
    private static final Folder[] FOLDER_ARRAY = new Folder[0];
    private List _folderListeners = new LinkedList();
    private List _storeListeners = new LinkedList();
    protected Store(Session session, URLName name) {
        super(session,name);
    }
    public void addFolderListener(FolderListener listener) {
        _folderListeners.add(listener);
    }
    public void addStoreListener(StoreListener listener) {
        _storeListeners.add(listener);
    }
    public abstract Folder getDefaultFolder() throws MessagingException;
    public abstract Folder getFolder(String name) throws MessagingException;
    public abstract Folder getFolder(URLName name) throws MessagingException;
    public Folder[] getPersonalNamespaces() throws MessagingException {
        return new Folder[] { getDefaultFolder()};
    }
    public Folder[] getSharedNamespaces() throws MessagingException {
        return FOLDER_ARRAY;
    }
    public Folder[] getUserNamespaces(String name) throws MessagingException {
        return FOLDER_ARRAY;
    }
    protected void notifyFolderListeners(int type, Folder folder) {
        Iterator it = _folderListeners.iterator();
        FolderEvent event = new FolderEvent(this, folder, type);
        while (it.hasNext()) {
            FolderListener listener = (FolderListener) it.next();
            event.dispatch(listener);
        }
    }
    protected void notifyFolderRenamedListeners(
        Folder oldFolder,
        Folder newFolder) {
        Iterator it = _folderListeners.iterator();
        FolderEvent event =
            new FolderEvent(this, oldFolder, newFolder, FolderEvent.RENAMED);
        while (it.hasNext()) {
            FolderListener listener = (FolderListener) it.next();
            event.dispatch(listener);
        }
    }
    protected void notifyStoreListeners(int type, String message) {
        Iterator it = _storeListeners.iterator();
        StoreEvent event = new StoreEvent(this, type, message);
        while (it.hasNext()) {
            StoreListener listener = (StoreListener) it.next();
            listener.notification(event);
        }
    }
    public void removeFolderListener(FolderListener listener) {
        _folderListeners.remove(listener);
    }
    public void removeStoreListener(StoreListener listener) {
        _storeListeners.remove(listener);
    }
}
