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

package org.apache.geronimo.crypto.asn1.misc;

import org.apache.geronimo.crypto.asn1.*;

/**
 * The NetscapeCertType object.
 * <pre>
 *    NetscapeCertType ::= BIT STRING {
 *         SSLClient               (0),
 *         SSLServer               (1),
 *         S/MIME                  (2),
 *         Object Signing          (3),
 *         Reserved                (4),
 *         SSL CA                  (5),
 *         S/MIME CA               (6),
 *         Object Signing CA       (7) }
 * </pre>
 */
public class NetscapeCertType
    extends DERBitString
{
    public static final int        sslClient        = (1 << 7);
    public static final int        sslServer        = (1 << 6);
    public static final int        smime            = (1 << 5);
    public static final int        objectSigning    = (1 << 4);
    public static final int        reserved         = (1 << 3);
    public static final int        sslCA            = (1 << 2);
    public static final int        smimeCA          = (1 << 1);
    public static final int        objectSigningCA  = (1 << 0);

    /**
     * Basic constructor.
     *
     * @param usage - the bitwise OR of the Key Usage flags giving the
     * allowed uses for the key.
     * e.g. (X509NetscapeCertType.sslCA | X509NetscapeCertType.smimeCA)
     */
    public NetscapeCertType(
        int usage)
    {
        super(getBytes(usage), getPadBits(usage));
    }

    public NetscapeCertType(
        DERBitString usage)
    {
        super(usage.getBytes(), usage.getPadBits());
    }

    public String toString()
    {
        return "NetscapeCertType: 0x" + Integer.toHexString(data[0] & 0xff);
    }
}
