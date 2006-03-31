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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.FetchProfile;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.geronimo.javamail.store.nntp.newsrc.NNTPNewsrcGroup;
import org.apache.geronimo.javamail.transport.nntp.NNTPReply;

/**
 * The NNTP implementation of the javax.mail.Folder Note that only INBOX is
 * supported in NNTP
 * <p>
 * <url>http://www.faqs.org/rfcs/rfc1939.html</url>
 * </p>
 * 
 * @see javax.mail.Folder
 * 
 * @version $Rev$ $Date$
 */
public class NNTPGroupFolder extends NNTPFolder {

    // holders for status information returned by the GROUP command.
    protected int firstArticle = -1;

    protected int lastArticle = -1;

    // retrieved articles, mapped by article number.
    Map articles;

    // information stored in the newsrc group.
    NNTPNewsrcGroup groupInfo;

    /**
     * Construct a "real" folder representing an NNTP news group.
     * 
     * @param parent
     *            The parent root folder.
     * @param store
     *            The Store this folder is attached to.
     * @param name
     *            The folder name.
     * @param groupInfo
     *            The newsrc group information attached to the newsrc database.
     *            This contains subscription and article "SEEN" information.
     */
    protected NNTPGroupFolder(NNTPRootFolder parent, NNTPStore store, String name, NNTPNewsrcGroup groupInfo) {
        super(store);
        // the name and the full name are the same.
        this.name = name;
        this.fullName = name;
        // set the parent appropriately.
        this.parent = parent = parent;
        this.groupInfo = groupInfo;
    }

    /**
     * Ping the server and update the group count, first, and last information.
     * 
     * @exception MessagingException
     */
    private void updateGroupStats() throws MessagingException {
        // ask the server for information about the group. This is a one-line
        // reponse with status on
        // the group, if it exists.
        NNTPReply reply = connection.sendCommand("GROUP " + name);

        // explicitly not there?
        if (reply.getCode() == NNTPReply.NO_SUCH_NEWSGROUP) {
            throw new FolderNotFoundException(this, "Folder does not exist on server: " + reply);
        } else if (reply.getCode() != NNTPReply.GROUP_SELECTED) {
            throw new MessagingException("Error requesting group information: " + reply);
        }

        // we've gotten back a good response, now parse out the group specifics
        // from the
        // status response.

        StringTokenizer tokenizer = new StringTokenizer(reply.getMessage());

        // we should have a least 3 tokens here, in the order "count first
        // last".

        // article count
        if (tokenizer.hasMoreTokens()) {
            String count = tokenizer.nextToken();
            try {
                messageCount = Integer.parseInt(count);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        // first article number
        if (tokenizer.hasMoreTokens()) {
            String first = tokenizer.nextToken();
            try {
                firstArticle = Integer.parseInt(first);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        // last article number.
        if (tokenizer.hasMoreTokens()) {
            String last = tokenizer.nextToken();
            try {
                lastArticle = Integer.parseInt(last);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
    }

    /**
     * Test to see if this folder actually exists. This pings the server for
     * information about the GROUP and updates the article count and index
     * information.
     * 
     * @return true if the newsgroup exists on the server, false otherwise.
     * @exception MessagingException
     */
    public boolean exists() throws MessagingException {

        try {
            // update the group statistics. If the folder doesn't exist, we'll
            // get an exception that we
            // can turn into a false reply.
            updateGroupStats();
            // updated ok, so it must be there.
            return true;
        } catch (FolderNotFoundException e) {
            return false;
        }
    }

    /**
     * Ping the NNTP server to check if a newsgroup has any new messages.
     * 
     * @return True if the server has new articles from the last time we
     *         checked. Also returns true if this is the first time we've
     *         checked.
     * @exception MessagingException
     */
    public boolean hasNewMessages() throws MessagingException {
        int oldLast = lastArticle;
        updateGroupStats();

        return lastArticle > oldLast;
    }

    /**
     * Open the folder for use. This retrieves article count information from
     * the server.
     * 
     * @exception MessagingException
     */
    public void openFolder() throws MessagingException {
        // update the group specifics, especially the message count.
        updateGroupStats();

        // get a cache for retrieve articles
        articles = new HashMap();
    }

    /**
     * Close the folder, which also clears out the article caches.
     * 
     * @exception MessagingException
     */
    public void closeFolder() throws MessagingException {
        // get ride of any retrieve articles, and flip over the open for
        // business sign.
        articles = null;
    }

    /**
     * Checks wether the message is in cache, if not will create a new message
     * object and return it.
     * 
     * @see javax.mail.Folder#getMessage(int)
     */
    public Message getMessage(int msgNum) throws MessagingException {
        // Can only be performed on an Open folder
        checkOpen();

        // get an object form to look up in the retrieve messages list (oh how I
        // wish there was
        // something like Map that could use integer keys directly!).
        Integer key = new Integer(msgNum);
        NNTPMessage message = (NNTPMessage) articles.get(key);
        if (message != null) {
            // piece of cake!
            return message;
        }

        // we need to suck a message down from the server.
        // but first, make sure the group is still valid.
        updateGroupStats();

        // just send a STAT command to this message. Right now, all we want is
        // existance proof. We'll
        // retrieve the other bits when requested.
        NNTPReply reply = connection.sendCommand("STAT " + Integer.toString(msgNum));
        if (reply.getCode() != NNTPReply.REQUEST_TEXT_SEPARATELY) {
            throw new MessagingException("Error retrieving article from NNTP server: " + reply);
        }

        // we need to parse out the message id.
        String response = reply.getMessage();

        int idStart = response.indexOf('<');
        int idEnd = response.indexOf('>');

        message = new NNTPMessage(this, (NNTPStore) store, msgNum, response.substring(idStart + 1, idEnd));

        // add this to the article cache.
        articles.put(key, message);

        return message;
    }

    /**
     * Retrieve all articles in the group.
     * 
     * @return An array of all messages in the group.
     */
    public Message[] getMessages() throws MessagingException {
        // we're going to try first with XHDR, which will allow us to retrieve
        // everything in one shot. If that
        // fails, we'll fall back on issing STAT commands for the entire article
        // range.
        NNTPReply reply = connection.sendCommand("XHDR Message-ID " + Integer.toString(firstArticle) + "-"
                + Integer.toString(lastArticle), NNTPReply.HEAD_FOLLOWS);

        List messages = new ArrayList();

        if (reply.getCode() == NNTPReply.HEAD_FOLLOWS) {
            List lines = reply.getData();

            for (int i = 0; i < lines.size(); i++) {
                String line = (String) lines.get(i);

                try {
                    int pos = line.indexOf(' ');
                    int articleID = Integer.parseInt(line.substring(0, pos));
                    String messageID = line.substring(pos + 1);
                    Integer key = new Integer(articleID);
                    // see if we have this message cached, If not, create it.
                    Message message = (Message) articles.get(key);
                    if (message == null) {
                        message = new NNTPMessage(this, (NNTPStore) store, key.intValue(), messageID);
                        articles.put(key, message);
                    }

                    messages.add(message);

                } catch (NumberFormatException e) {
                    // should never happen, but just skip this entry if it does.
                }
            }
        } else {
            // grumble, we need to stat each article id to see if it
            // exists....lots of round trips.
            for (int i = firstArticle; i <= lastArticle; i++) {
                try {
                    messages.add(getMessage(i));
                } catch (MessagingException e) {
                    // just assume if there is an error, it's because the
                    // message id doesn't exist.
                }
            }
        }

        return (Message[]) messages.toArray(new Message[0]);
    }

    /**
     * @see javax.mail.Folder#fetch(javax.mail.Message[],
     *      javax.mail.FetchProfile)
     * 
     * The JavaMail API recommends that this method be overrident to provide a
     * meaningfull implementation.
     */
    public void fetch(Message[] msgs, FetchProfile fp) throws MessagingException {
        // Can only be performed on an Open folder
        checkOpen();

        for (int i = 0; i < msgs.length; i++) {
            Message msg = msgs[i];
            // we can only perform this operation for NNTPMessages.
            if (msg == null || !(msg instanceof NNTPMessage)) {
                // we can't fetch if it's the wrong message type
                continue;
            }

            // fetching both the headers and body?
            if (fp.contains(FetchProfile.Item.ENVELOPE) && fp.contains(FetchProfile.Item.CONTENT_INFO)) {

                // retrive everything
                ((NNTPMessage) msg).loadArticle();
            }
            // headers only?
            else if (fp.contains(FetchProfile.Item.ENVELOPE)) {
                ((NNTPMessage) msg).loadHeaders();
            } else if (fp.contains(FetchProfile.Item.CONTENT_INFO)) {
                ((NNTPMessage) msg).loadContent();
            }
        }
    }

    /**
     * Return the subscription status of this folder.
     * 
     * @return true if the folder is marked as subscribed, false for
     *         unsubscribed.
     */
    public boolean isSubscribed() {
        return groupInfo.isSubscribed();
    }

    /**
     * Set or clear the subscription status of a file.
     * 
     * @param flag
     *            The new subscription state.
     */
    public void setSubscribed(boolean flag) {
        groupInfo.setSubscribed(flag);
    }

    /**
     * Return the "seen" state for an article in a folder.
     * 
     * @param article
     *            The article number.
     * 
     * @return true if the article is marked as seen in the newsrc file, false
     *         for unseen files.
     */
    public boolean isSeen(int article) {
        return groupInfo.isArticleSeen(article);
    }

    /**
     * Set the seen state for an article in a folder.
     * 
     * @param article
     *            The article number.
     * @param flag
     *            The new seen state.
     */
    public void setSeen(int article, boolean flag) {
        if (flag) {
            groupInfo.markArticleSeen(article);
        } else {
            groupInfo.markArticleUnseen(article);
        }
    }
}
