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

package org.apache.geronimo.crypto.asn1.x509;

import org.apache.geronimo.crypto.asn1.DERBitString;

/**
 * The KeyUsage object.
 * <pre>
 *    id-ce-keyUsage OBJECT IDENTIFIER ::=  { id-ce 15 }
 *
 *    KeyUsage ::= BIT STRING {
 *         digitalSignature        (0),
 *         nonRepudiation          (1),
 *         keyEncipherment         (2),
 *         dataEncipherment        (3),
 *         keyAgreement            (4),
 *         keyCertSign             (5),
 *         cRLSign                 (6),
 *         encipherOnly            (7),
 *         decipherOnly            (8) }
 * </pre>
 */
public class KeyUsage
    extends DERBitString
{
    public static final int        digitalSignature = (1 << 7);
    public static final int        nonRepudiation   = (1 << 6);
    public static final int        keyEncipherment  = (1 << 5);
    public static final int        dataEncipherment = (1 << 4);
    public static final int        keyAgreement     = (1 << 3);
    public static final int        keyCertSign      = (1 << 2);
    public static final int        cRLSign          = (1 << 1);
    public static final int        encipherOnly     = (1 << 0);
    public static final int        decipherOnly     = (1 << 15);

    /**
     * Basic constructor.
     *
     * @param usage - the bitwise OR of the Key Usage flags giving the
     * allowed uses for the key.
     * e.g. (KeyUsage.keyEncipherment | KeyUsage.dataEncipherment)
     */
    public KeyUsage(
        int usage)
    {
        super(getBytes(usage), getPadBits(usage));
    }

    public KeyUsage(
        DERBitString usage)
    {
        super(usage.getBytes(), usage.getPadBits());
    }

    public String toString()
    {
        if (data.length == 1)
        {
            return "KeyUsage: 0x" + Integer.toHexString(data[0] & 0xff);
        }
        return "KeyUsage: 0x" + Integer.toHexString((data[1] & 0xff) << 8 | (data[0] & 0xff));
    }
}
