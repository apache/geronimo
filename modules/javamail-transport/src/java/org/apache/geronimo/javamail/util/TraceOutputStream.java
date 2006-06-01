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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.geronimo.mail.util.QuotedPrintableEncoderStream;

/**
 * @version $Rev$ $Date$
 */
public class TraceOutputStream extends FilterOutputStream {
    // the current debug setting
    protected boolean debug = false;

    // the target trace output stream.
    protected OutputStream traceStream;

    /**
     * Construct a debug trace stream.
     * 
     * @param out
     *            The target out put stream.
     * @param traceStream
     *            The side trace stream to which trace data gets written.
     * @param encode
     *            Indicates whether we wish the Trace data to be Q-P encoded.
     */
    public TraceOutputStream(OutputStream out, OutputStream traceStream, boolean debug, boolean encode) {
        super(out);
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
     * Writes <code>len</code> bytes from the specified <code>byte</code>
     * array starting at offset <code>off</code> to this output stream.
     * <p>
     * The <code>write</code> method of <code>FilterOutputStream</code>
     * calls the <code>write</code> method of one argument on each
     * <code>byte</code> to output.
     * <p>
     * Note that this method does not call the <code>write</code> method of
     * its underlying input stream with the same arguments. Subclasses of
     * <code>FilterOutputStream</code> should provide a more efficient
     * implementation of this method.
     * 
     * @param b
     *            the data.
     * @param off
     *            the start offset in the data.
     * @param len
     *            the number of bytes to write.
     * @exception IOException
     *                if an I/O error occurs.
     * @see java.io.FilterOutputStream#write(int)
     */
    public void write(byte b[], int off, int len) throws IOException {
        if (debug) {
            for (int i = 0; i < len; i++) {
                traceStream.write(b[off + i]);
            }
        }
        super.write(b, off, len);
    }

    /**
     * Writes the specified <code>byte</code> to this output stream.
     * <p>
     * The <code>write</code> method of <code>FilterOutputStream</code>
     * calls the <code>write</code> method of its underlying output stream,
     * that is, it performs <tt>out.write(b)</tt>.
     * <p>
     * Implements the abstract <tt>write</tt> method of <tt>OutputStream</tt>.
     * 
     * @param b
     *            the <code>byte</code>.
     * @exception IOException
     *                if an I/O error occurs.
     */
    public void write(int b) throws IOException {
        if (debug) {
            traceStream.write(b);
        }
        super.write(b);
    }
}
