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

package javax.mail.event;
import javax.mail.Folder;
/**
 * @version $Rev$ $Date$
 */
public class FolderEvent extends MailEvent {
    public static final int CREATED = 1;
    public static final int DELETED = 2;
    public static final int RENAMED = 3;
    protected transient Folder folder;
    protected transient Folder newFolder;
    protected int type;
    public FolderEvent(
        Object source,
        Folder oldFolder,
        Folder newFolder,
        int type) {
        super(source);
        folder = oldFolder;
        this.newFolder = newFolder;
        this.type = type;
        if (type != CREATED && type != DELETED && type != RENAMED) {
            throw new IllegalArgumentException("Unknown type " + type);
        }
    }
    public FolderEvent(Object source, Folder folder, int type) {
        this(source, folder, null, type);
    }
    public void dispatch(Object listener) {
        // assume that it is the right listener type
        FolderListener l = (FolderListener) listener;
        if (type == CREATED) {
            l.folderCreated(this);
        } else if (type == DELETED) {
            l.folderDeleted(this);
        } else if (type == RENAMED) {
            l.folderRenamed(this);
        } else {
            throw new IllegalArgumentException("Unknown type " + type);
        }
    }
    public Folder getFolder() {
        return folder;
    }
    public Folder getNewFolder() {
        return newFolder;
    }
    public int getType() {
        return type;
    }
}
