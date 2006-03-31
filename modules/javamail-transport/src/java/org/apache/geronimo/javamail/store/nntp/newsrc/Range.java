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

/**
 * Represent a single Range in a newsrc file. A Range can be either a single
 * number (start == end) or a span of article numbers.
 */
public class Range {
    // the low end of the range
    int start;

    // the high end of the range (start and end are inclusive);
    int end;

    /**
     * Construct a Range item for a single digit range.
     * 
     * @param spot
     *            The location of the singleton.
     */
    public Range(int spot) {
        this(spot, spot);
    }

    /**
     * Construct a Range item.
     * 
     * @param start
     *            The starting point of the Range.
     * @param end
     *            The Range end point (which may be equal to the starting
     *            point).
     */
    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Parse a section of a .newsrc range string into a single Range item. The
     * range is either a single number, or a pair of numbers separated by a
     * hyphen.
     * 
     * @param range
     *            The range string.
     * 
     * @return A constructed Range item, or null if there is a parsing error.
     */
    static public Range parse(String range) {
        // a range from a newsrc file is either a single number or in the format
        // 'nnnn-mmmm'. We need
        // to figure out which type this is.
        int marker = range.indexOf('-');

        try {
            if (marker != -1) {
                String rangeStart = range.substring(0, marker).trim();
                String rangeEnd = range.substring(marker + 1).trim();

                int start = Integer.parseInt(rangeStart);
                int end = Integer.parseInt(rangeEnd);

                if (start >= 0 && end >= 0) {
                    return new Range(start, end);
                }
            } else {
                // use the entire token
                int start = Integer.parseInt(range);
                // and start and the end are the same
                return new Range(start, start);

            }
        } catch (NumberFormatException e) {
        }
        // return null for any bad values
        return null;
    }

    /**
     * Get the starting point for the Range.
     * 
     * @return The beginning of the mark range.
     */
    public int getStart() {
        return start;
    }

    /**
     * Set the starting point for a Range.
     * 
     * @param start
     *            The new start value.
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Get the ending point for the Range.
     * 
     * @return The end of the mark range.
     */
    public int getEnd() {
        return end;
    }

    /**
     * Set the ending point for a Range.
     * 
     * @param end
     *            The new end value.
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * Test if a range contains a point value.
     * 
     * @param target
     *            The article location to test.
     * 
     * @return True if the target is between the start and end values,
     *         inclusive.
     */
    public boolean contains(int target) {
        return target >= start && target <= end;
    }

    /**
     * Test if one range is completely contained within another Range.
     * 
     * @param other
     *            The other test range.
     * 
     * @return true if the other start and end points are contained within this
     *         range.
     */
    public boolean contains(Range other) {
        return contains(other.getStart()) && contains(other.getEnd());
    }

    /**
     * Tests if two ranges overlap
     * 
     * @param other
     *            The other test range.
     * 
     * @return true if the start or end points of either range are contained
     *         within the range of the other.
     */
    public boolean overlaps(Range other) {
        return other.contains(start) || other.contains(end) || contains(other.getStart()) || contains(other.getEnd());
    }

    /**
     * Test if two ranges exactly abutt each other.
     * 
     * @param other
     *            The other Range to test.
     * 
     * @return true if the end of one range abutts the start of the other range.
     */
    public boolean abutts(Range other) {
        return other.getStart() == end + 1 || other.getEnd() == start - 1;
    }

    /**
     * Tests if a single point abutts either the start or end of this Range.
     * 
     * @param article
     *            The point to test.
     * 
     * @return true if test point is equal to start - 1 or end + 1.
     */
    public boolean abutts(int article) {
        return article == start - 1 || article == end + 1;
    }

    /**
     * Test if a point is below the test Range.
     * 
     * @param article
     *            The point to test.
     * 
     * @return true if the entire range is less than the test point.
     */
    public boolean lessThan(int article) {
        return end < article;
    }

    /**
     * Test if another Range is less than this Range.
     * 
     * @param other
     *            The other Range to test.
     * 
     * @return true if the other Range lies completely below this Range.
     */
    public boolean lessThan(Range other) {
        return end < other.start;
    }

    /**
     * Test if a point is above the test Range.
     * 
     * @param article
     *            The point to test.
     * 
     * @return true if the entire range is greater than the test point.
     */
    public boolean greaterThan(int article) {
        return start > article;
    }

    /**
     * Test if another Range is greater than this Range.
     * 
     * @param other
     *            The other Range to test.
     * 
     * @return true if the other Range lies completely below this Range.
     */
    public boolean greaterThan(Range other) {
        return start > other.end;
    }

    /**
     * Merge another Range into this one. Merging will increase the bounds of
     * this Range to encompass the entire span of the two. If the Ranges do not
     * overlap, the newly created range will include the gap between the two.
     * 
     * @param other
     *            The Range to merge.
     */
    public void merge(Range other) {
        if (other.start < start) {
            start = other.start;
        }

        if (other.end > end) {
            end = other.end;
        }
    }

    /**
     * Split a range at a given split point. Splitting will truncate at the
     * split location - 1 and return a new range beginning at location + 1; This
     * code assumes that the split location is at neither end poing.
     * 
     * @param location
     *            The split location. Location must be in the range start <
     *            location < end.
     * 
     * @return A new Range object for the split portion of the range.
     */
    public Range split(int location) {
        int newEnd = end;

        end = location - 1;

        return new Range(location + 1, newEnd);
    }

    /**
     * Save an individual range element to a newsrc file. The range is expressed
     * either as a single number, or a hypenated pair of numbers.
     * 
     * @param out
     *            The output writer used to save the data.
     * 
     * @exception IOException
     */
    public void save(Writer out) throws IOException {
        // do we have a single data point range?
        if (start == end) {
            out.write(Integer.toString(start));
        } else {
            out.write(Integer.toString(start));
            out.write("-");
            out.write(Integer.toString(end));
        }
    }

    /**
     * Convert a Range into String form. Used mostly for debugging.
     * 
     * @return The String representation of the Range.
     */
    public String toString() {
        if (start == end) {
            return Integer.toString(start);
        } else {
            return Integer.toString(start) + "-" + Integer.toString(end);
        }
    }
}
