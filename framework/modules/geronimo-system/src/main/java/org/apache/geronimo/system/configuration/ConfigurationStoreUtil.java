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
package org.apache.geronimo.system.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for dealing with checksums (hashes) of files in the
 * configuration store.
 *
 * @version $Rev$ $Date$
 */
public class ConfigurationStoreUtil {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationStoreUtil.class);

    public static void writeChecksumFor(File file) throws IOException {
        // check if the sum already exists
        File sumFile = new File(file.getParentFile(), file.getName() + ".sha1");
        if (sumFile.exists()) {
            throw new IOException("Sum file already exists");
        }

        // calculate the checksum
        String actualChecksum;
        try {
            actualChecksum = calculateChecksum(file, "SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw (IOException)new IOException("SHA-1 algorithm not available").initCause(e);
        }

        // write it
        FileWriter writer = new FileWriter(sumFile);
        try {
            writer.write(actualChecksum);
        } finally {
            try {
                writer.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static boolean verifyChecksum(File file) {
        String expectedChecksum = getExpectedChecksum(file);
        if (expectedChecksum == null) {
            // log message already printed
            return false;
        }

        String actualChecksum = getActualChecksum(file);
        if (actualChecksum == null) {
            // log message already printed
            return false;
        }


        if (!actualChecksum.equals(expectedChecksum)) {
            log.warn("Configuration file was modified: " + file.getAbsolutePath());
            return false;
        }

        return true;
    }

    public static String getExpectedChecksum(File file) {
        File sumFile = new File(file.getParentFile(), file.getName() + ".sha1");
        if (!sumFile.exists()) {
            log.warn("Checksum file not found: " + sumFile.getAbsolutePath());
            return null;
        }
        if (!sumFile.canRead()) {
            log.warn("Checksum file is not readable: " + sumFile.getAbsolutePath());
            return null;
        }
        LineNumberReader lineNumberReader = null;
        try {
            lineNumberReader = new LineNumberReader(new FileReader(sumFile));
            String expectedChecksum = lineNumberReader.readLine();
            if (expectedChecksum == null) {
                log.error("Checksum file was empty: " + sumFile.getAbsolutePath());
                return null;
            }
            return expectedChecksum.trim();
        } catch (IOException e) {
            log.error("Unable to read checksum file: " + sumFile.getAbsolutePath(), e);
        } finally {
            if (lineNumberReader != null) {
                try {
                    lineNumberReader.close();
                } catch (IOException ignored) {
                }
            }

        }
        return null;
    }

    public static String getActualChecksum(File file) {
        return getActualChecksum(file, "SHA-1");
    }
    public static String getActualChecksum(File file, String algorithm) {
        try {
            return calculateChecksum(file, algorithm);
        } catch (Exception e) {
            log.error("Unable to calculate checksum for configuration file: " + file.getAbsolutePath(), e);
        }
        return null;
    }

    private static String calculateChecksum(File file, String algorithm) throws NoSuchAlgorithmException, IOException {

        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            
            MessageDigest digester = MessageDigest.getInstance(algorithm);
            digester.reset();

            byte buf[] = new byte[4096];
            int len = 0;

            while ((len = stream.read(buf, 0, 1024)) != -1) {
                digester.update(buf, 0, len);
            }

            String actualChecksum = encode(digester.digest());
            return actualChecksum;
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static String encode(byte[] binaryData) {
        if (binaryData.length != 16 && binaryData.length != 20) {
            int bitLength = binaryData.length * 8;
            throw new IllegalArgumentException("Unrecognised length for binary data: " + bitLength + " bits");
        }

        String retValue = "";

        for (int i = 0; i < binaryData.length; i++) {
            String t = Integer.toHexString(binaryData[i] & 0xff);

            if (t.length() == 1) {
                retValue += ("0" + t);
            } else {
                retValue += t;
            }
        }

        return retValue.trim();
    }

    public static class ChecksumOutputStream extends OutputStream {
        private final OutputStream out;
        private MessageDigest digester;

        public ChecksumOutputStream(OutputStream out) throws IOException {
            this.out = out;
            try {
                digester = MessageDigest.getInstance("SHA-1");
                digester.reset();
            } catch (NoSuchAlgorithmException e) {
                throw (IOException)new IOException("SHA-1 algorithm not available").initCause(e);
            }
        }

        public String getChecksum() {
            String actualChecksum = encode(digester.digest());
            return actualChecksum;
        }

        public void write(int b) throws IOException {
            digester.update((byte) b);
            out.write(b);
        }

        public void write(byte[] b) throws IOException {
            digester.update(b);
            out.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            digester.update(b, off, len);
            out.write(b, off, len);
        }

        public void flush() throws IOException {
            out.flush();
        }

        public void close() throws IOException {
            out.close();
        }
    }
}
