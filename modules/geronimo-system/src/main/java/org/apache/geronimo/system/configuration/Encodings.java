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

/**
 * Provides information about encodings. Depends on the Java runtime
 * to provides writers for the different encodings, but can be used
 * to override encoding names and provide the last printable character
 * for each encoding.
 *
 * @version $Id$
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 */
public class Encodings
{


    /**
     * The last printable character for unknown encodings.
     */
    static final int DefaultLastPrintable = 0x7F;

    /**
     * @param encoding a MIME charset name, or null.
     */
    static EncodingInfo getEncodingInfo(String encoding) {
        if (encoding == null)
            return new EncodingInfo(null, DefaultLastPrintable);
        for (int i = 0;  i < _encodings.length;  i++) {
            if (_encodings[i].name.equalsIgnoreCase(encoding))
                return _encodings[i];
        }
        return new SieveEncodingInfo(encoding, DefaultLastPrintable);
    }

    static final String JIS_DANGER_CHARS
    = "\\\u007e\u007f\u00a2\u00a3\u00a5\u00ac"
    +"\u2014\u2015\u2016\u2026\u203e\u203e\u2225\u222f\u301c"
    +"\uff3c\uff5e\uffe0\uffe1\uffe2\uffe3";

    /**
     * Constructs a list of all the supported encodings.
     */
    private static final EncodingInfo[] _encodings = new EncodingInfo[] {
        new EncodingInfo("ASCII", 0x7F),
        new EncodingInfo("US-ASCII", 0x7F),
        new EncodingInfo("ISO-8859-1", 0xFF),
        new EncodingInfo("ISO-8859-2", 0xFF),
        new EncodingInfo("ISO-8859-3", 0xFF),
        new EncodingInfo("ISO-8859-4", 0xFF),
        new EncodingInfo("ISO-8859-5", 0xFF),
        new EncodingInfo("ISO-8859-6", 0xFF),
        new EncodingInfo("ISO-8859-7", 0xFF),
        new EncodingInfo("ISO-8859-8", 0xFF),
        new EncodingInfo("ISO-8859-9", 0xFF),
        /**
         * Does JDK's converter supprt surrogates?
         * A Java encoding name "UTF-8" is suppoted by JDK 1.2 or later.
         */
        new EncodingInfo("UTF-8", "UTF8", 0x10FFFF),
        /**
         * JDK 1.1 supports "Shift_JIS" as an alias of "SJIS".
         * But JDK 1.2 treats "Shift_JIS" as an alias of "MS932".
         * The JDK 1.2's behavior is invalid against IANA registrations.
         */
        new SieveEncodingInfo("Shift_JIS", "SJIS", 0x7F, JIS_DANGER_CHARS),
        /**
         * "MS932" is supported by JDK 1.2 or later.
         */
        new SieveEncodingInfo("Windows-31J", "MS932", 0x7F, JIS_DANGER_CHARS),
        new SieveEncodingInfo("EUC-JP", null, 0x7F, JIS_DANGER_CHARS),
        new SieveEncodingInfo("ISO-2022-JP", null, 0x7F, JIS_DANGER_CHARS),
    };
}
