/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.javamail.store.nntp.newsrc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Base class implementation of a standard news reader news rc file. This is
 * used to track newsgroup subscriptions and SEEN flags for articles. This is an
 * abstract class designed for subclasses to bridge to the physical store type
 * used for the newsgroup information.
 */
public abstract class NNTPNewsrc {

    // the group information we've read from the news rc file.
    Map groups = new HashMap();

    // flag to let us know of we need to persist the newsrc file on close.
    boolean dirty = false;

    /**
     * Base class constructor for NNTPNewsrc items. Subclasses provide their own
     * domain-specific intialization.
     */
    protected NNTPNewsrc() {
    }

    /**
     * Load the data from the newsrc file and parse into an instore group
     * database.
     */
    public void load() {
        BufferedReader in = null;

        try {
            in = getInputReader();

            String line = in.readLine();

            while (line != null) {
                // parse the line...this returns null if it's something
                // unrecognized.
                NNTPNewsrcGroup group = NNTPNewsrcGroup.parse(this, line);
                // if it parsed ok, add it to the group list, and potentially to
                // the subscribed list.
                if (group != null) {
                    groups.put(group.getName(), group);
                }

                line = in.readLine();
            }

            in.close();
        } catch (IOException e) {
            // an IOException may mean that the file just doesn't exist, which
            // is fine. We'll ignore and
            // proceed with the information we have.
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Save the newsrc file data back to the original source file.
     * 
     * @exception IOException
     */
    public void save() throws IOException {
        Writer out = getOutputWriter();

        Iterator i = groups.values().iterator();

        while (i.hasNext()) {
            NNTPNewsrcGroup group = (NNTPNewsrcGroup) i.next();
            group.save(out);
        }

        out.close();
    }

    /**
     * Abstract open method intended for sub class initialization. The subclass
     * is responsible for creating the BufferedReaded used to read the .newsrc
     * file.
     * 
     * @return A BufferedReader for reading the .newsrc file.
     * @exception IOException
     */
    abstract public BufferedReader getInputReader() throws IOException;

    /**
     * Abstract open for output method intended for subclass implementation. The
     * subclasses are reponsible for opening the output stream and creating an
     * appropriate Writer for saving the .newsrc file.
     * 
     * @return A Writer target at the .newsrc file save location.
     * @exception IOException
     */
    abstract public Writer getOutputWriter() throws IOException;

    /**
     * Retrieve the newsrc group information for a named group. If the file does
     * not currently include this group, an unsubscribed group will be added to
     * the file.
     * 
     * @param name
     *            The name of the target group.
     * 
     * @return The NNTPNewsrcGroup item corresponding to this name.
     */
    public NNTPNewsrcGroup getGroup(String name) {
        NNTPNewsrcGroup group = (NNTPNewsrcGroup) groups.get(name);
        // if we don't know about this, create a new one and add to the list.
        // This
        // will be an unsubscribed one.
        if (group == null) {
            group = new NNTPNewsrcGroup(this, name, null, false);
            groups.put(name, group);
            // we've added a group, so we need to resave
            dirty = true;
        }
        return group;
    }

    /**
     * Mark this newsrc database as dirty.
     */
    public void setDirty() {
        dirty = true;
    }

    /**
     * Close the newsrc file, persisting it back to disk if the file has
     * changed.
     */
    public void close() {
        if (dirty) {
            try {
                save();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Retrieve the current set of loaded groups.
     * 
     * @return An iterator for traversing the group set.
     */
    public Iterator getGroups() {
        return groups.values().iterator();
    }
}
