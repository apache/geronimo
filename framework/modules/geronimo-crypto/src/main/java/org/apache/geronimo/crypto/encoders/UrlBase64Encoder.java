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

package org.apache.geronimo.crypto.encoders;

/**
 * Convert binary data to and from UrlBase64 encoding.  This is identical to
 * Base64 encoding, except that the padding character is "." and the other
 * non-alphanumeric characters are "-" and "_" instead of "+" and "/".
 * <p>
 * The purpose of UrlBase64 encoding is to provide a compact encoding of binary
 * data that is safe for use as an URL parameter. Base64 encoding does not
 * produce encoded values that are safe for use in URLs, since "/" can be
 * interpreted as a path delimiter; "+" is the encoded form of a space; and
 * "=" is used to separate a name from the corresponding value in an URL
 * parameter.
 */
public class UrlBase64Encoder extends Base64Encoder
{
    public UrlBase64Encoder()
    {
        encodingTable[encodingTable.length - 2] = (byte) '-';
        encodingTable[encodingTable.length - 1] = (byte) '_';
        padding = (byte) '.';
        // we must re-create the decoding table with the new encoded values.
        initialiseDecodingTable();
    }
}
