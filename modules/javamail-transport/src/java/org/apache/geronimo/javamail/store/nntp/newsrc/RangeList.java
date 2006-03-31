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
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Manage a list of ranges values from a newsrc file.
 */
public class RangeList {
    boolean dirty = false;

    ArrayList ranges = new ArrayList();

    /**
     * Create a RangeList instance from a newsrc range line. Values are saved as
     * a comma separated set of range values. A range value is either a single
     * number or a hypenated pair of numbers.
     * 
     * @param line
     *            The newsrc range line.
     */
    public RangeList(String line) {

        // we might be creating an first time list, so nothing to parse.
        if (line != null) {
            // ranges are comma delimited tokens
            StringTokenizer tokenizer = new StringTokenizer(line, ",");

            while (tokenizer.hasMoreTokens()) {
                String rangeString = (String) tokenizer.nextToken();
                rangeString = rangeString.trim();
                if (rangeString.length() != 0) {
                    Range range = Range.parse(rangeString);
                    if (range != null) {
                        insert(range);
                    }
                }
            }
        }
        // make sure we start out in a clean state. Any changes from this point
        // will flip on the dirty flat.
        dirty = false;
    }

    /**
     * Insert a range item into our list. If possible, the inserted range will
     * be merged with existing ranges.
     * 
     * @param newRange
     *            The new range item.
     */
    public void insert(Range newRange) {
        // first find the insertion point
        for (int i = 0; i < ranges.size(); i++) {
            Range range = (Range) ranges.get(i);
            // does an existing range fully contain the new range, we don't need
            // to insert anything.
            if (range.contains(newRange)) {
                return;
            }

            // does the current one abutt or overlap with the inserted range?
            if (range.abutts(newRange) || range.overlaps(newRange)) {
                // rats, we have an overlap...and it is possible that we could
                // overlap with
                // the next range after this one too. Therefore, we merge these
                // two ranges together,
                // remove the place where we're at, and then recursively insert
                // the larger range into
                // the list.
                dirty = true;
                newRange.merge(range);
                ranges.remove(i);
                insert(newRange);
                return;
            }

            // ok, we don't touch the current one at all. If it is completely
            // above
            // range we're adding, we can just poke this into the list here.
            if (newRange.lessThan(range)) {
                dirty = true;
                ranges.add(i, newRange);
                return;
            }
        }
        dirty = true;
        // this is easy (and fairly common)...we just tack this on to the end.
        ranges.add(newRange);
    }

    /**
     * Test if a given article point falls within one of the contained Ranges.
     * 
     * @param article
     *            The test point.
     * 
     * @return true if this falls within one of our current mark Ranges, false
     *         otherwise.
     */
    public boolean isMarked(int article) {
        for (int i = 0; i < ranges.size(); i++) {
            Range range = (Range) ranges.get(i);
            if (range.contains(article)) {
                return true;
            }
            // we've passed the point where a match is possible.
            if (range.greaterThan(article)) {
                return false;
            }
        }
        return false;
    }

    /**
     * Mark a target article as having been seen.
     * 
     * @param article
     *            The target article number.
     */
    public void setMarked(int article) {
        // go through the insertion logic.
        insert(new Range(article, article));
    }

    /**
     * Clear the seen mark for a target article.
     * 
     * @param article
     *            The target article number.
     */
    public void setUnmarked(int article) {
        for (int i = 0; i < ranges.size(); i++) {
            Range range = (Range) ranges.get(i);
            // does this fall within an existing range? We don't need to do
            // anything here.
            if (range.contains(article)) {
                // ok, we've found where to insert, now to figure out how to
                // insert
                // article is at the beginning of the range. We can just
                // increment the lower
                // bound, or if this is a single element range, we can remove it
                // entirely.
                if (range.getStart() == article) {
                    if (range.getEnd() == article) {
                        // piece of cake!
                        ranges.remove(i);
                    } else {
                        // still pretty easy.
                        range.setStart(article + 1);
                    }
                } else if (range.getEnd() == article) {
                    // pretty easy also
                    range.setEnd(article - 1);
                } else {
                    // split this into two ranges and insert the trailing piece
                    // after this.
                    Range section = range.split(article);
                    ranges.add(i + 1, section);
                }
                dirty = true;
                return;
            }
            // did we find a point where any articles are greater?
            if (range.greaterThan(article)) {
                // nothing to do
                return;
            }
        }
        // didn't find it at all. That was easy!
    }

    /**
     * Save this List of Ranges out to a .newsrc file. This creates a
     * comma-separated list of range values from each of the Ranges.
     * 
     * @param out
     *            The target output stream.
     * 
     * @exception IOException
     */
    public void save(Writer out) throws IOException {
        // we have an empty list
        if (ranges.size() == 0) {
            return;
        }

        Range range = (Range) ranges.get(0);
        range.save(out);

        for (int i = 1; i < ranges.size(); i++) {
            out.write(",");
            range = (Range) ranges.get(i);
            range.save(out);
        }
    }

    /**
     * Return the state of the dirty flag.
     * 
     * @return True if the range list information has changed, false otherwise.
     */
    public boolean isDirty() {
        return dirty;
    }
}
