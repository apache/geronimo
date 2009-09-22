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
import java.io.Writer;
import java.nio.CharBuffer;

/**
 * A writer that will replace certain keyword (&lt;/body&gt;) with a given string in
 * the output.
 * @version $Rev$, $Date$
 */
public class SubstituteWriter extends Writer {

    // Default internal buffer size
    private static final int BUFFER_SIZE = 4086;

    // Threshold for processing buffered content
    private static final int PROCESS_THRESHOLD = 128;

    private String substitute = null;

    private String outputCharset = null;

    private OutputStream stream = null;

    private CharBuffer cb = CharBuffer.allocate(BUFFER_SIZE);

    private boolean found = false;

    public SubstituteWriter(String substitute, String outputCharset, OutputStream os) {
        if (substitute == null || outputCharset == null || os == null) {
            throw new NullPointerException();
        }
        this.substitute = substitute;
        this.outputCharset = outputCharset;
        this.stream = os;
    }

    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (!found) {
                cb.flip();
                found = SubstituteUtil.processSubstitute(cb, substitute, true, outputCharset, stream);
            }
            stream.close();
        }
    }

    @Override
    public void flush() throws IOException {
        synchronized (lock) {
            if (!found) {
                // Try to write out as much as possible, but some text might be
                // still buffered in order to handle broken keyword
                cb.flip();
                found = SubstituteUtil.processSubstitute(cb, substitute, false, outputCharset, stream);
            }
            stream.flush();
        }
    }

    @Override
    public void write(char[] content, int offset, int length) throws IOException {
        synchronized (lock) {
            // Skip replacement if already found one occurrence
            if (found) {
                String s = new String(content, offset, length);
                stream.write(s.getBytes(outputCharset));
                return;
            }
            // Process the content in batches
            int boundary = offset + length;
            int batchOffset = offset;
            int batchLength;
            while (batchOffset < boundary) {
                if (found) {
                    stream.write(new String(content, batchOffset, boundary - batchOffset).getBytes(outputCharset));
                    break;
                } else {
                    if (boundary - batchOffset < cb.remaining()) {
                        batchLength = boundary - batchOffset;
                    } else {
                        batchLength = cb.remaining();
                    }
                    cb.put(content, batchOffset, batchLength);
                    if (cb.position() > PROCESS_THRESHOLD) {
                        cb.flip();
                        found = SubstituteUtil.processSubstitute(cb, substitute, false, outputCharset, stream);
                    }
                    batchOffset = batchOffset + batchLength;
                }
            }
        }
    }

    public void reset() {
        synchronized (lock) {
            cb.clear();
            found = false;
        }
    }
}
