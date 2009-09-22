/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" 
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.apache.geronimo.console.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;

import javax.servlet.ServletOutputStream;

/**
 * A output stream that will replace certain keyword (&lt;/body&gt;) with a
 * given string in the output.
 * @version $Rev$, $Date$
 */
public class SubstituteResponseOutputStream extends ServletOutputStream {

    private static final int BUFFER_SIZE = 4086;

    private static final int PROCESS_THRESHOLD = 128;

    // Console use UTF-8 encoding. In UTF-8, max length for one character is 4.
    private static final int MALFORMED_INPUT_MAX_LENGTH = 4;

    private String substitute = null;

    private String outputCharset = null;

    private OutputStream stream = null;

    private CharsetDecoder cd = null;

    private ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);

    private CharBuffer cb = CharBuffer.allocate(BUFFER_SIZE);

    private boolean found = false;

    /**
     * Construct a output stream which will scan the input and do substitution
     * for the keyword "&lt;/body&gt;".
     * 
     * @param substitute
     *            The text that will replace "&lt;/body&gt;"
     * @param outputCharset
     *            The charset that will be used to encode the output
     * @param os
     *            The output stream
     */
    public SubstituteResponseOutputStream(String substitute, String outputCharset, OutputStream os) {
        if (substitute == null || outputCharset == null || os == null) {
            throw new NullPointerException();
        }
        this.substitute = substitute;
        this.outputCharset = outputCharset;
        this.stream = os;
        Charset cs = Charset.forName(outputCharset);
        cd = cs.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    /*
     * Decode the bytes in the byte buffer and append to the char buffer.
     * 
     * The byte & char buffer should be ready for "put" before entering this
     * method. They are still ready for "put" after exiting this method.
     * 
     * @param flushBuffer Whether to flush out all the contents in the byte
     * buffer
     * 
     * @throws IOException
     */
    private void decodeBuffer(boolean flushBuffer) throws IOException {
        if (!flushBuffer && bb.position() < PROCESS_THRESHOLD) {
            return;
        }
        bb.flip();
        cd.reset();
        if (bb.hasRemaining()) {
            CoderResult cr = cd.decode(bb, cb, true);
            cd.flush(cb);
            if (cr.isMalformed()) {
                // Move the tail bytes to the head
                int tailLength = bb.remaining();
                if (tailLength >= MALFORMED_INPUT_MAX_LENGTH) {
                    // We only expect the bytes of one character to be broken
                    throw new MalformedInputException(tailLength);
                }
            }
            bb.compact();
        } else {
            bb.clear();
        }
    }

    /*
     * Process the bytes in the buffer. This involves: 1. Decode the bytes and
     * put into the char buffer; 2. Scan the characters in the buffer and do
     * replacement.
     * 
     * The byte & char buffer should be ready for "put" before entering this
     * method. They are still ready for "put" after exiting this method.
     * 
     * @param flushBuffer Whether to flush out all the contents in the buffer
     * @param endOfInput Whether this is the end of input
     * 
     * @throws IOException
     */
    private void processBuffer(boolean flushBuffer, boolean endOfInput) throws IOException {
        decodeBuffer(flushBuffer || endOfInput);
        if (!endOfInput && !flushBuffer && cb.position() < PROCESS_THRESHOLD) {
            return;
        }
        cb.flip();
        found = SubstituteUtil.processSubstitute(cb, substitute, endOfInput, outputCharset, stream);
        // Write the tail bytes in the byte buffer if required
        if (bb.position() > 0 && (found || endOfInput)) {
            bb.flip();
            while (bb.hasRemaining()) {
                stream.write(bb.get());
            }
            bb.clear();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException {
        if (found) {
            stream.write(b);
        } else {
            bb.put((byte) b);
            processBuffer(false, false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(byte[] content, int offset, int length) throws IOException {
        if (found) {
            stream.write(content, offset, length);
            return;
        }
        int boundary = offset + length;
        int batchOffset = offset;
        int batchLength;
        while (batchOffset < boundary) {
            if (found) {
                stream.write(content, batchOffset, boundary - batchOffset);
                break;
            } else {
                if (boundary - batchOffset < bb.remaining()) {
                    batchLength = boundary - batchOffset;
                } else {
                    batchLength = bb.remaining();
                }
                bb.put(content, batchOffset, batchLength);
                processBuffer(false, false);
                batchOffset = batchOffset + batchLength;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#print(String)
     */
    @Override
    public void print(String s) throws IOException {
        if (found) {
            super.print(s);
            return;
        }
        if (bb.position() > 0) {
            // Process the buffered bytes
            decodeBuffer(true);
            if (bb.position() > 0) {
                // There are still malformed input remaining, even though we
                // are going to write a string. This should not happen.
                throw new MalformedInputException(bb.position());
            }
        }
        // Process the string without encoding/decoding overhead
        char[] content = s.toCharArray();
        int offset = 0;
        int length;
        while (offset < content.length) {
            if (found) {
                super.print(s.substring(offset));
                break;
            } else {
                if (content.length - offset < cb.remaining()) {
                    length = content.length - offset;
                } else {
                    length = cb.remaining();
                }
                cb.put(content, offset, length);
                processBuffer(false, false);
                offset = offset + length;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close() throws IOException {
        if (!found) {
            processBuffer(true, true);
        }
        stream.flush();
        stream.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush() throws IOException {
        if (!found) {
            // Try to write out as much as possible, but some text might be
            // still buffered in order to handle broken keyword
            processBuffer(true, false);
        }
        stream.flush();
    }

    /**
     * Reset this output stream. Clear the buffers.
     */
    public void reset() {
        bb.clear();
        cb.clear();
        found = false;
    }
}
