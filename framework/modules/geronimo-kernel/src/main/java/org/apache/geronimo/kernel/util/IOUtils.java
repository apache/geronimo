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

package org.apache.geronimo.kernel.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class IOUtils {

    private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

    public static final int DEFAULT_COPY_BUFFER_SIZE = 4096;

    public static void copy(InputStream in, OutputStream out) throws IOException {
        copy(in, out, DEFAULT_COPY_BUFFER_SIZE);
    }

    public static void copy(InputStream in, OutputStream out, int bufferSizeInBytes) throws IOException {
        byte[] buffer = new byte[bufferSizeInBytes];
        int count;
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        out.flush();
    }

    public static void close(Closeable thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        try {
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (int count = inputStream.read(buffer); count >= 0; count = inputStream.read(buffer)) {
                out.write(buffer, 0, count);
            }
            byte[] bytes = out.toByteArray();
            return bytes;
        } finally {
            close(inputStream);
        }
    }

    public static void flush(OutputStream thing) {
        if (thing != null) {
            try {
                thing.flush();
            } catch (Exception ignored) {
            }
        }
    }

    public static void flush(Writer thing) {
        if (thing != null) {
            try {
                thing.flush();
            } catch (Exception ignored) {
            }
        }
    }

    public static final class EmptyInputStream extends InputStream {

        public int read() {
            return -1;
        }

        public int read(byte b[]) {
            return -1;
        }

        public int read(byte b[], int off, int len) {
            return -1;
        }

        public long skip(long n) {
            return 0;
        }

        public int available() {
            return 0;
        }

        public void close() {
        }

        public synchronized void mark(int readlimit) {
        }

        public synchronized void reset() {
        }

        public boolean markSupported() {
            return false;
        }
    }
}
