/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.javamail.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An implementation of an OutputStream that performs MIME linebreak
 * canonicalization and "byte-stuff" so that data content does not get mistaken
 * for a message data-end marker (CRLF.CRLF)l
 * 
 * @version $Rev$ $Date$
 */
public class MIMEOutputStream extends OutputStream {

    // the wrappered output stream.
    protected OutputStream out;

    // last character we handled...used to recongnize line breaks.
    protected int lastWrite = -1;

    // a flag to indicate we've just processed a line break. This is used for
    // byte stuffing purposes. This
    // is initially true, because if the first character of the content is a
    // period, we need to byte-stuff
    // immediately.
    protected boolean atLineBreak = true;

    /**
     * Create an output stream that writes to the target output stream.
     * 
     * @param out
     *            The wrapped output stream.
     */
    public MIMEOutputStream(OutputStream out) {
        this.out = out;
    }

    // in order for this to work, we only need override the single character
    // form, as the others
    // funnel through this one by default.
    public void write(int ch) throws IOException {
        // if this is a CR character, always write out a full sequence, and
        // remember that we just did this.
        if (ch == '\r') {
            out.write((byte) '\r');
            out.write((byte) '\n');
            // we've just taken a break;
            atLineBreak = true;
        }
        // if this is a new line, then we need to determine if this is a loner
        // or part of a CRLF sequence.
        else if (ch == '\n') {
            // is this a lone ranger?
            if (lastWrite != '\r') {
                // write the full CRLF sequence.
                out.write((byte) '\r');
                out.write((byte) '\n');
            }
            // regardless of whether we wrote something or not, we're still at a
            // line break.
            atLineBreak = true;
        }
        // potential byte-stuffing situation?
        else if (ch == '.') {
            // ok, this is a potential stuff situation. Did we just have a line
            // break? Double up the character.
            if (atLineBreak) {
                out.write('.');
            }
            out.write('.');
            atLineBreak = false;
        } else {
            // just write this out and flip the linebreak flag.
            out.write(ch);
            atLineBreak = false;
        }
        // remember this last one for CRLF tracking purposes.
        lastWrite = ch;
    }
}
