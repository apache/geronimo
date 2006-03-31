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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.geronimo.mail.util.QuotedPrintableEncoderStream;

/**
 * @version $Rev$ $Date$
 */
public class TraceInputStream extends FilterInputStream {
    // the current debug setting
    protected boolean debug = false;

    // the target trace output stream.
    protected OutputStream traceStream;

    /**
     * Construct a debug trace stream.
     * 
     * @param in
     *            The source input stream.
     * @param traceStream
     *            The side trace stream to which trace data gets written.
     * @param encode
     *            Indicates whether we wish the Trace data to be Q-P encoded.
     */
    public TraceInputStream(InputStream in, OutputStream traceStream, boolean debug, boolean encode) {
        super(in);
        this.debug = debug;
        if (encode) {
            this.traceStream = new QuotedPrintableEncoderStream(traceStream);
        } else {
            this.traceStream = traceStream;
        }
    }

    /**
     * Set the current setting of the debug trace stream debug flag.
     * 
     * @param d
     *            The new debug flag settings.
     */
    public void setDebug(boolean d) {
        debug = d;
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream into
     * an array of bytes. This method blocks until some input is available.
     * <p>
     * This method simply performs <code>in.read(b, off, len)</code> and
     * returns the result.
     * 
     * @param b
     *            the buffer into which the data is read.
     * @param off
     *            the start offset of the data.
     * @param len
     *            the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or
     *         <code>-1</code> if there is no more data because the end of the
     *         stream has been reached.
     * @exception IOException
     *                if an I/O error occurs.
     * @see java.io.FilterInputStream#in
     */
    public int read(byte b[], int off, int len) throws IOException {
        int count = in.read(b, off, len);
        if (debug && count > 0) {
            traceStream.write(b, off, count);
        }
        return count;
    }

    /**
     * Reads the next byte of data from this input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the
     * stream has been reached, the value <code>-1</code> is returned. This
     * method blocks until input data is available, the end of the stream is
     * detected, or an exception is thrown.
     * <p>
     * This method simply performs <code>in.read()</code> and returns the
     * result.
     * 
     * @return the next byte of data, or <code>-1</code> if the end of the
     *         stream is reached.
     * @exception IOException
     *                if an I/O error occurs.
     * @see java.io.FilterInputStream#in
     */
    public int read() throws IOException {
        int b = in.read();
        if (debug) {
            traceStream.write(b);
        }
        return b;
    }
}
