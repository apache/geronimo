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

/*
 * This code has been borrowed from the Apache Xerces project. We're copying the code to
 * keep from adding a dependency on Xerces in the Geronimo kernel.
 */
package org.apache.geronimo.system.configuration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * This class represents an encoding.
 *
 * @version $Id$
 */
public class SieveEncodingInfo extends EncodingInfo {

    BAOutputStream checkerStream = null;
    Writer checkerWriter = null;
    String dangerChars = null;

    /**
     * Creates new <code>SeiveEncodingInfo</code> instance.
     *
     * @param dangers A sorted characters that are always printed as character references.
     */
    public SieveEncodingInfo(String mimeName, String javaName,
                             int lastPrintable, String dangers) {
        super(mimeName, javaName, lastPrintable);
        this.dangerChars = dangers;
    }

    /**
     * Creates new <code>SeiveEncodingInfo</code> instance.
     */
    public SieveEncodingInfo(String mimeName, int lastPrintable) {
        this(mimeName, mimeName, lastPrintable, null);
    }

    /**
     * Checks whether the specified character is printable or not.
     *
     * @param ch a code point (0-0x10ffff)
     */
    public boolean isPrintable(int ch) {
        if (this.dangerChars != null && ch <= 0xffff) {
            /**
             * Searches this.dangerChars for ch.
             * TODO: Use binary search.
             */
            if (this.dangerChars.indexOf(ch) >= 0)
                return false;
        }

        if (ch <= this.lastPrintable)
            return true;

        boolean printable = true;
        synchronized (this) {
            try {
                if (this.checkerWriter == null) {
                    this.checkerStream = new BAOutputStream(10);
                    this.checkerWriter = new OutputStreamWriter(this.checkerStream, this.javaName);
                }

                if (ch > 0xffff) {
                    this.checkerWriter.write(((ch-0x10000)>>10)+0xd800);
                    this.checkerWriter.write(((ch-0x10000)&0x3ff)+0xdc00);
                    byte[] result = this.checkerStream.getBuffer();
                    if (this.checkerStream.size() == 2 && result[0] == '?' && result[1] == '?')
                        printable = false;
                } else {
                    this.checkerWriter.write(ch);
                    this.checkerWriter.flush();
                    byte[] result = this.checkerStream.getBuffer();
                    if (this.checkerStream.size() == 1 && result[0] == '?')
                        printable = false;
                }
                this.checkerStream.reset();
            } catch (IOException ioe) {
                printable = false;
            }
        }

        return printable;
    }

    /**
     * Why don't we use the original ByteArrayOutputStream?
     * - Because the toByteArray() method of the ByteArrayOutputStream
     * creates new byte[] instances for each call.
     */
    static class BAOutputStream extends ByteArrayOutputStream {
        BAOutputStream() {
            super();
        }

        BAOutputStream(int size) {
            super(size);
        }

        byte[] getBuffer() {
            return this.buf;
        }
    }

}
