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
package org.apache.geronimo.javamail.transport.nntp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class StringListInputStream extends InputStream {
    // the list of lines we're reading from
    protected List lines;

    // the next line to process.
    protected int nextLine = 0;

    // current buffer of bytes to read from
    byte[] buffer;

    // current offset within the buffer;
    int offset;

    // indicator that we've left off at a split between the CR and LF of a line
    // break.
    boolean atLineBreak = false;

    public StringListInputStream(List lines) throws IOException {
        this.lines = lines;
        nextLine = 0;
        buffer = null;
        offset = 0;
        atLineBreak = false;

        // if we have at least one line in the list, get the bytes now.
        if (lines.size() > 0) {
            nextBuffer();
        }
    }

    /**
     * Just override the single byte read version, which handles all of the
     * lineend markers correctly.
     * 
     * @return The next byte from the stream or -1 if we've hit the EOF.
     */
    public int read() throws IOException {
        // leave off at the split between a line?
        if (atLineBreak) {
            // flip this off and return the second line end character. Also step
            // to the next line.
            atLineBreak = false;
            nextBuffer();
            return '\n';
        }
        // gone past the end? Got an EOF
        if (buffer == null) {
            return -1;
        }

        // reach the end of the line?
        if (offset >= buffer.length) {
            // we're now working on a virtual linebreak
            atLineBreak = true;
            return '\r';
        }
        // just return the next byte
        return buffer[offset++];

    }

    /**
     * Step to the next buffer of string data.
     * 
     * @exception IOException
     */
    protected void nextBuffer() throws IOException {
        // give an eof check.
        if (nextLine >= lines.size()) {
            buffer = null;
        } else {
            try {
                String next = (String) lines.get(nextLine++);
                buffer = next.getBytes("US-ASCII");

            } catch (UnsupportedEncodingException e) {
                throw new IOException("Invalid string encoding");
            }
        }

        offset = 0;
    }
}
