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

package org.apache.geronimo.javamail.store.nntp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.mail.Folder;
import javax.mail.MessagingException;

import org.apache.geronimo.javamail.store.nntp.newsrc.NNTPNewsrcGroup;
import org.apache.geronimo.javamail.transport.nntp.NNTPReply;
import org.apache.geronimo.mail.util.SessionUtil;

/**
 * The base NNTP implementation of the javax.mail.Folder This is a base class
 * for both the Root NNTP server and each NNTP group folder.
 * 
 * @see javax.mail.Folder
 * 
 * @version $Rev$
 */
public class NNTPRootFolder extends NNTPFolder {
    protected static final String NNTP_LISTALL = "mail.nntp.listall";

    /**
     * Construct the NNTPRootFolder.
     * 
     * @param store
     *            The owning Store.
     * @param name
     *            The folder name (by default, this is the server host name).
     * @param fullName
     *            The fullName to use for this server (derived from welcome
     *            string).
     */
    protected NNTPRootFolder(NNTPStore store, String name, String fullName) {
        super(store);

        this.name = name;
        this.fullName = fullName;
    }

    /**
     * List the subfolders. For group folders, this is a meaningless so we throw
     * a MethodNotSupportedException.
     * 
     * @param pattern
     *            The folder pattern string.
     * 
     * @return Never returns.
     * @exception MessagingException
     */
    public synchronized Folder[] list(String pattern) throws MessagingException {
        // the pattern specfied for javamail uses two wild card characters, "%"
        // and "*". The "%" matches
        // and character except hierarchy separators. Since we have a flag
        // hierarchy, "%" and "*" are
        // essentially the same. If we convert the "%" into "*", we can just
        // treat this as a wildmat
        // formatted pattern and pass this on to the server rather than having
        // to read everything and
        // process the strings on the client side.

        pattern = pattern.replace('%', '*');

        // if we're not supposed to list everything, then just filter the list
        // of subscribed groups.
        if (SessionUtil.getBooleanProperty(NNTP_LISTALL, false)) {
            return filterActiveGroups(pattern);
        } else {
            return filterSubscribedGroups(pattern);
        }
    }

    /**
     * Retrieve the list of subscribed folders that match the given pattern
     * string.
     * 
     * @param pattern
     *            The pattern string used for the matching
     * 
     * @return An array of matching folders from the subscribed list.
     */
    public Folder[] listSubscribed(String pattern) throws MessagingException {
        // the pattern specfied for javamail uses two wild card characters, "%"
        // and "*". The "%" matches
        // and character except hierarchy separators. Since we have a flag
        // hierarchy, "%" and "*" are
        // essentially the same. If we convert the "%" into "*", we can just
        // treat this as a wildmat
        // formatted pattern and pass this on to the server rather than having
        // to read everything and
        // process the strings on the client side.

        pattern = pattern.replace('%', '*');

        return filterSubscribedGroups(pattern);
    }

    /**
     * Retrieve the list of matching groups from the NNTP server using the LIST
     * ACTIVE command. The server does the wildcard matching for us.
     * 
     * @param pattern
     *            The pattern string (in wildmat format) used to match.
     * 
     * @return An array of folders for the matching groups.
     */
    protected Folder[] filterActiveGroups(String pattern) throws MessagingException {
        NNTPReply reply = connection.sendCommand("LIST ACTIVE " + pattern, NNTPReply.LIST_FOLLOWS);

        // if the LIST ACTIVE command isn't supported,
        if (reply.getCode() == NNTPReply.COMMAND_NOT_RECOGNIZED) {
            // only way to list all is to retrieve all and filter.
            return filterAllGroups(pattern);
        } else if (reply.getCode() != NNTPReply.LIST_FOLLOWS) {
            throw new MessagingException("Error retrieving group list from NNTP server: " + reply);
        }

        // get the response back from the server and process each returned group
        // name.
        List groups = reply.getData();

        Folder[] folders = new Folder[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            folders[i] = getFolder(getGroupName((String) groups.get(i)));
        }
        return folders;
    }

    /**
     * Retrieve a list of all groups from the server and filter on the names.
     * Not recommended for the usenet servers, as there are over 30000 groups to
     * process.
     * 
     * @param pattern
     *            The pattern string used for the selection.
     * 
     * @return The Folders for the matching groups.
     */
    protected Folder[] filterAllGroups(String pattern) throws MessagingException {
        NNTPReply reply = connection.sendCommand("LIST", NNTPReply.LIST_FOLLOWS);

        if (reply.getCode() != NNTPReply.LIST_FOLLOWS) {
            throw new MessagingException("Error retrieving group list from NNTP server: " + reply);
        }

        // get the response back from the server and process each returned group
        // name.
        List groups = reply.getData();

        WildmatMatcher matcher = new WildmatMatcher(pattern);

        List folders = new ArrayList();
        for (int i = 0; i < groups.size(); i++) {
            String name = getGroupName((String) groups.get(i));
            // does this match our pattern? Add to the list
            if (matcher.matches(name)) {
                folders.add(getFolder(name));
            }
        }
        return (Folder[]) folders.toArray(new Folder[0]);
    }

    /**
     * Return the set of groups from the newsrc subscribed groups list that
     * match a given filter.
     * 
     * @param pattern
     *            The selection pattern.
     * 
     * @return The Folders for the matching groups.
     */
    protected Folder[] filterSubscribedGroups(String pattern) throws MessagingException {
        Iterator groups = ((NNTPStore) store).getNewsrcGroups();

        WildmatMatcher matcher = new WildmatMatcher(pattern);

        List folders = new ArrayList();
        while (groups.hasNext()) {
            NNTPNewsrcGroup group = (NNTPNewsrcGroup) groups.next();
            if (group.isSubscribed()) {
                // does this match our pattern? Add to the list
                if (matcher.matches(group.getName())) {
                    folders.add(getFolder(group.getName()));
                }
            }
        }
        return (Folder[]) folders.toArray(new Folder[0]);
    }

    /**
     * Utility method for extracting a name from a group list response.
     * 
     * @param response
     *            The response string.
     * 
     * @return The group name.
     */
    protected String getGroupName(String response) {
        int blank = response.indexOf(' ');
        return response.substring(0, blank).trim();
    }

    /**
     * Return whether this folder can hold just messages or also subfolders.
     * Only the root folder can hold other folders, so it will need to override.
     * 
     * @return Always returns Folder.HOLDS_FOLDERS.
     * @exception MessagingException
     */
    public int getType() throws MessagingException {
        return HOLDS_FOLDERS;
    }

    /**
     * Get a new folder from the root folder. This creates a new folder, which
     * might not actually exist on the server. If the folder doesn't exist, an
     * error will occur on folder open.
     * 
     * @param name
     *            The name of the requested folder.
     * 
     * @return A new folder object for this folder.
     * @exception MessagingException
     */
    public Folder getFolder(String name) throws MessagingException {
        // create a new group folder and return
        return new NNTPGroupFolder(this, (NNTPStore) store, name, ((NNTPStore) store).getNewsrcGroup(name));
    }

    /**
     * Utility class to do Wildmat pattern matching on folder names.
     */
    class WildmatMatcher {
        // middle match sections...because these are separated by wildcards, if
        // they appear in
        // sequence in the string, it is a match.
        List matchSections = new ArrayList();

        // just a "*" match, so everything is true
        boolean matchAny = false;

        // no wildcards, so this must be an exact match.
        String exactMatch = null;

        // a leading section which must be at the beginning
        String firstSection = null;

        // a trailing section which must be at the end of the string.
        String lastSection = null;

        /**
         * Create a wildmat pattern matcher.
         * 
         * @param pattern
         *            The wildmat pattern to apply to string matches.
         */
        public WildmatMatcher(String pattern) {
            int section = 0;

            // handle the easy cases first

            // single wild card?
            if (pattern.equals("*")) {
                matchAny = true;
                return;
            }

            // find the first wild card
            int wildcard = pattern.indexOf('*');

            // no wild card at all?
            if (wildcard == -1) {
                exactMatch = pattern;
                return;
            }

            // pattern not begin with a wildcard? We need to pull off the
            // leading section
            if (!pattern.startsWith("*")) {
                firstSection = pattern.substring(0, wildcard);
                section = wildcard + 1;
                // this could be "yada*", so we could be done.
                if (section >= pattern.length()) {
                    return;
                }
            }

            // now parse off the middle sections, making sure to handle the end
            // condition correctly.
            while (section < pattern.length()) {
                // find the next wildcard position
                wildcard = pattern.indexOf('*', section);
                if (wildcard == -1) {
                    // not found, we're at the end of the pattern. We need to
                    // match on the end.
                    lastSection = pattern.substring(section);
                    return;
                }
                // we could have a null section, which we'll just ignore.
                else if (wildcard == section) {
                    // step over the wild card
                    section++;
                } else {
                    // pluck off the next section
                    matchSections.add(pattern.substring(section, wildcard));
                    // step over the wild card character and check if we've
                    // reached the end.
                    section = wildcard + 1;
                }
            }
        }

        /**
         * Test if a name string matches to parsed wildmat pattern.
         * 
         * @param name
         *            The name to test.
         * 
         * @return true if the string matches the pattern, false otherwise.
         */
        public boolean matches(String name) {

            // handle the easy cases first

            // full wildcard? Always matches
            if (matchAny) {
                return true;
            }

            // required exact matches are easy.
            if (exactMatch != null) {
                return exactMatch.equals(name);
            }

            int span = 0;

            // must match the beginning?
            if (firstSection != null) {
                // if it doesn't start with that, it can't be true.
                if (!name.startsWith(firstSection)) {
                    return false;
                }

                // we do all additional matching activity from here.
                span = firstSection.length();
            }

            // scan for each of the sections along the string
            for (int i = 1; i < matchSections.size(); i++) {
                // if a section is not found, this is false

                String nextMatch = (String) matchSections.get(i);
                int nextLocation = name.indexOf(nextMatch, span);
                if (nextLocation == -1) {
                    return false;
                }
                // step over that one
                span = nextMatch.length() + nextLocation;
            }

            // we've matched everything up to this point, now check to see if
            // need an end match
            if (lastSection != null) {
                // we need to have at least the number of characters of the end
                // string left, else this fails.
                if (name.length() - span < lastSection.length()) {
                    return false;
                }

                // ok, make sure we end with this string
                return name.endsWith(lastSection);
            }

            // no falsies, this must be the truth.
            return true;
        }
    }
}
