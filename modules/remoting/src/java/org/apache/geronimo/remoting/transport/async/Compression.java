/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:20 $
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
