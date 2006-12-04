/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * This class represents an encoding.
 *
 * @version $Id$
 */
public class EncodingInfo {

    String name;
    String javaName;
    int lastPrintable;

    /**
     * Creates new <code>EncodingInfo</code> instance.
     */
    public EncodingInfo(String mimeName, String javaName, int lastPrintable) {
        this.name = mimeName;
        this.javaName = javaName == null ? mimeName : javaName;
        this.lastPrintable = lastPrintable;
    }

    /**
     * Creates new <code>EncodingInfo</code> instance.
     */
    public EncodingInfo(String mimeName, int lastPrintable) {
        this(mimeName, mimeName, lastPrintable);
    }

    /**
     * Returns a MIME charset name of this encoding.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a writer for this encoding based on
     * an output stream.
     *
     * @return A suitable writer
     * @exception UnsupportedEncodingException There is no convertor
     *  to support this encoding
     */
    public Writer getWriter(OutputStream output)
        throws UnsupportedEncodingException {
        if (this.javaName == null)
            return new OutputStreamWriter(output);
        return new OutputStreamWriter(output, this.javaName);
    }
    /**
     * Checks whether the specified character is printable or not.
     *
     * @param ch a code point (0-0x10ffff)
     */
    public boolean isPrintable(int ch) {
        return ch <= this.lastPrintable;
    }
}
