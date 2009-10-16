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

package org.apache.geronimo.crypto.crypto.params;

public class DESedeParameters
    extends DESParameters
{
    /*
     * DES-EDE Key length in bytes.
     */
    static public final int DES_EDE_KEY_LENGTH = 24;

    public DESedeParameters(
        byte[]  key)
    {
        super(key);

        if (isWeakKey(key, 0, key.length))
        {
            throw new IllegalArgumentException("attempt to create weak DESede key");
        }
    }

    /**
     * return true if the passed in key is a DES-EDE weak key.
     *
     * @param key bytes making up the key
     * @param offset offset into the byte array the key starts at
     * @param length number of bytes making up the key
     */
    public static boolean isWeakKey(
        byte[]  key,
        int     offset,
        int     length)
    {
        for (int i = offset; i < length; i += DES_KEY_LENGTH)
        {
            if (DESParameters.isWeakKey(key, i))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * return true if the passed in key is a DES-EDE weak key.
     *
     * @param key bytes making up the key
     * @param offset offset into the byte array the key starts at
     */
    public static boolean isWeakKey(
        byte[]  key,
        int     offset)
    {
        return isWeakKey(key, offset, key.length - offset);
    }
}
