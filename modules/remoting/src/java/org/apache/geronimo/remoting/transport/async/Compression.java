/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.geronimo.remoting.transport.async;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to add compression at the transport level.
 *
 * @version $Revision: 1.1 $ $Date: 2003/11/16 05:27:28 $
 */
public class Compression {

    private static final Log log = LogFactory.getLog(Compression.class);

    /**
     * Compresses the input data.
     * @returns null if compression results in larger output.
     */
    static public byte[] compress(byte[] input, int compressionLevel) {
        Deflater deflater = new Deflater(compressionLevel);
        deflater.setInput(input, 0, input.length);
        deflater.finish();
        byte[] buff = new byte[input.length + 50];
        int wsize = deflater.deflate(buff);

        int compressedSize = deflater.getTotalOut();

        // Did this data compress well?
        if (deflater.getTotalIn() != input.length)
            return null;
        if (compressedSize >= input.length - 4)
            return null;

        byte[] output = new byte[compressedSize + 4];
        System.arraycopy(buff, 0, output, 4, compressedSize);
        output[0] = (byte) (input.length >> 24);
        output[1] = (byte) (input.length >> 16);
        output[2] = (byte) (input.length >> 8);
        output[3] = (byte) (input.length);
        return output;
    }

    /**
     * Un-compresses the input data.
     * @throws IOException if the input is not valid.
     */
    static public byte[] uncompress(byte[] input) throws IOException {
        try {
            int uncompressedSize =
                (((input[0] & 0xff) << 24)
                    + ((input[1] & 0xff) << 16)
                    + ((input[2] & 0xff) << 8)
                    + ((input[3] & 0xff)));

            Inflater inflater = new Inflater();
            inflater.setInput(input, 4, input.length - 4);
            inflater.finished();

            byte[] out = new byte[uncompressedSize];
            inflater.inflate(out);

            inflater.reset();
            return out;

        } catch (DataFormatException e) {
            throw new IOException("Input Stream is corrupt: " + e);
        }
    }

}
