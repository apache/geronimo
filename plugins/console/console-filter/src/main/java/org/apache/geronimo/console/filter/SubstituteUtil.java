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
import java.nio.CharBuffer;
import java.util.regex.Pattern;

/**
 * @version $Rev$, $Date$
 */
public class SubstituteUtil {

    private final static String KEYWORD = "</body>";

    private final static Pattern SEARCH_PATTERN = Pattern.compile("(?i)" + KEYWORD);

    /**
     * Scan the characters in the buffer and do replacement.
     * 
     * The char buffer should be ready for "get" before entering this method,
     * and will change to be ready for "put" after exiting this method.
     * 
     * @param cb
     *            The char buffer to be scanned
     * @param replacement
     *            The replacement string that will be used to replace found
     *            occurrences of the keyword
     * @param endOfInput
     *            Whether this is the last batch to scan
     * @param outputCharset
     *            The charset to encode the output
     * @param os
     *            The output stream to receive the output
     * @return true if the keyword is found, otherwise false
     * @throws IOException
     */
    public static boolean processSubstitute(CharBuffer cb, String replacement, boolean endOfInput, String outputCharset, OutputStream os) throws IOException {
        int remaining = cb.remaining();
        if (remaining == 0) {
            cb.clear();
            return false;
        }
        String result = SEARCH_PATTERN.matcher(cb).replaceFirst(replacement);
        cb.clear();
        // For simplicity, we assume that the length of the replacement is
        // different from the keyword, which is definitely true in Geronimo
        // admin console's case
        if (result.length() != remaining) {
            // Replacement happened, we've got a match
            os.write(result.getBytes(outputCharset));
            return true;
        } else if (endOfInput) {
            // End of input, write everything out
            os.write(result.getBytes(outputCharset));
            return false;
        } else {
            // Push back the last N chars so that we don't break a keyword
            char tail = result.charAt(result.length() - 1);
            if (tail != ' ' && tail != '\n' && tail != '\r') {
                int textTailLength = Math.min(result.length(), KEYWORD.length() - 1);
                int textTailOffset = result.length() - textTailLength;
                cb.put(result.substring(textTailOffset));
                if (textTailOffset > 0) {
                    os.write(result.substring(0, textTailOffset).getBytes(outputCharset));
                }
            } else {
                os.write(result.getBytes(outputCharset));
            }
            return false;
        }
    }
}
