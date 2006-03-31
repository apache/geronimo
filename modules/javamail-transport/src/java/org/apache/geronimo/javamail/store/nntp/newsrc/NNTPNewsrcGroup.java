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

import java.io.IOException;
import java.io.Writer;

public class NNTPNewsrcGroup {
    // the newsrc database we're part of
    NNTPNewsrc newsrc;

    // the name of the group
    protected String name;

    // the subscription flage
    protected boolean subscribed;

    // the range of already seen articles.
    protected RangeList ranges;

    /**
     * Construct a NNTPNewsrcGroup item associated with a given .newsrc
     * database.
     * 
     * @param newsrc
     *            The owning .newsrc database.
     * @param line
     *            The .newsrc range entries in .newsrc format. These ranges are
     *            parsed to create a set of seen flags.
     * 
     * @return A created NNTPNewsrcGroup item.
     */
    public static NNTPNewsrcGroup parse(NNTPNewsrc newsrc, String line) {
        String groupName = null;
        String ranges = null;

        // subscribed lines have a ':' marker acting as a delimiter
        int marker = line.indexOf(':');

        if (marker != -1) {
            groupName = line.substring(0, marker);
            ranges = line.substring(marker + 1);
            return new NNTPNewsrcGroup(newsrc, groupName, ranges, true);
        }

        // now check for an unsubscribed group
        marker = line.indexOf('!');

        if (marker != -1) {
            groupName = line.substring(0, marker);
            ranges = line.substring(marker + 1);
            return new NNTPNewsrcGroup(newsrc, groupName, ranges, false);
        }

        // must be a comment line
        return null;
    }

    /**
     * Construct a .newsrc group item.
     * 
     * @param newsrc
     *            The owning newsrc database.
     * @param name
     *            The group name.
     * @param newsrcRanges
     *            The initial set of seen ranges for the group (may be null).
     * @param subscribed
     *            The initial group subscription state.
     */
    public NNTPNewsrcGroup(NNTPNewsrc newsrc, String name, String newsrcRanges, boolean subscribed) {
        this.newsrc = newsrc;
        this.name = name;
        this.subscribed = subscribed;
        this.ranges = new RangeList(newsrcRanges);
    }

    /**
     * Get the group name.
     * 
     * @return The String name of the group.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the newsrc subscribed status for an article.
     * 
     * @return The current subscription flag.
     */
    public boolean isSubscribed() {
        return subscribed;
    }

    /**
     * Set the subscription status for an article.
     * 
     * @param flag
     *            The new subscription value.
     */
    public void setSubscribed(boolean flag) {
        // we don't blindly set this to the new value since we only want to
        // resave the newsrc file if
        // something changes.
        if (flag && !subscribed) {
            subscribed = true;
            newsrc.setDirty();
        } else if (!flag && subscribed) {
            subscribed = false;
            newsrc.setDirty();
        }
    }

    /**
     * Test if an article has been seen yet.
     * 
     * @param article
     *            The target article.
     * 
     * @return The seen mark for the article.
     */
    public boolean isArticleSeen(int article) {
        return ranges.isMarked(article);
    }

    /**
     * Mark an article as seen.
     * 
     * @param article
     *            The target article number.
     */
    public void markArticleSeen(int article) {
        ranges.setMarked(article);
        if (ranges.isDirty()) {
            newsrc.setDirty();
        }
    }

    /**
     * Mark an article as unseen.
     * 
     * @param article
     *            The target article number.
     */
    public void markArticleUnseen(int article) {
        ranges.setUnmarked(article);
        if (ranges.isDirty()) {
            newsrc.setDirty();
        }
    }

    /**
     * Save this group definition to a .newsrc file.
     * 
     * @param out
     *            The output writer to send the information to.
     * 
     * @exception IOException
     */
    public void save(Writer out) throws IOException {
        out.write(name);
        out.write(subscribed ? ": " : "! ");
        ranges.save(out);
        // put a terminating line end
        out.write("\r\n");
    }
}
