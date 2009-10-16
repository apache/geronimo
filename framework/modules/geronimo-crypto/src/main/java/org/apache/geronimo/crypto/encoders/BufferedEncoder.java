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
 * a buffering class to allow translation from one format to another to
 * be done in discrete chunks.
 */
public class BufferedEncoder
{
    protected byte[]        buf;
    protected int           bufOff;

    protected Translator    translator;

    /**
     * @param translator the translator to use.
     * @param bufSize amount of input to buffer for each chunk.
     */
    public BufferedEncoder(
        Translator  translator,
        int         bufSize)
    {
        this.translator = translator;

        if ((bufSize % translator.getEncodedBlockSize()) != 0)
        {
            throw new IllegalArgumentException("buffer size not multiple of input block size");
        }

        buf = new byte[bufSize];
        bufOff = 0;
    }

    public int processByte(
        byte        in,
        byte[]      out,
        int         outOff)
    {
        int         resultLen = 0;

        buf[bufOff++] = in;

        if (bufOff == buf.length)
        {
            resultLen = translator.encode(buf, 0, buf.length, out, outOff);
            bufOff = 0;
        }

        return resultLen;
    }

    public int processBytes(
        byte[]      in,
        int         inOff,
        int         len,
        byte[]      out,
        int         outOff)
    {
        if (len < 0)
        {
            throw new IllegalArgumentException("Can't have a negative input length!");
        }

        int resultLen = 0;
        int gapLen = buf.length - bufOff;

        if (len > gapLen)
        {
            System.arraycopy(in, inOff, buf, bufOff, gapLen);

            resultLen += translator.encode(buf, 0, buf.length, out, outOff);

            bufOff = 0;

            len -= gapLen;
            inOff += gapLen;
            outOff += resultLen;

            int chunkSize = len - (len % buf.length);

            resultLen += translator.encode(in, inOff, chunkSize, out, outOff);

            len -= chunkSize;
            inOff += chunkSize;
        }

        if (len != 0)
        {
            System.arraycopy(in, inOff, buf, bufOff, len);

            bufOff += len;
        }

        return resultLen;
    }
}
