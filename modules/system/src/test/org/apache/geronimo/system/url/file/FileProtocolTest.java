/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.system.url.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.geronimo.system.url.GeronimoURLFactory;

import junit.framework.TestCase;

/**
 * Unit test for the 'file' protocol.
 *
 * @version $Rev$ $Date$
 */
public class FileProtocolTest extends TestCase {
    static {
        //
        // Have to install factory to make sure that our file handler is used
        // and not Sun's
        //
        GeronimoURLFactory.install();
    }

    private File file;
    private URL fileURL;

    protected void setUp() throws Exception {
        try {
            file = File.createTempFile("FileProtocolTest", ".tmp");
            fileURL = file.toURI().toURL();
        } catch (Exception e) {
            if (file != null) {
                file.delete();
            }
            throw e;
        }
    }

    protected void tearDown() throws Exception {
        if (file != null) {
            file.delete();
        }
    }

    public void testCreateURL() throws Exception {
        new URL("file:/some/file");
    }

    public void testURLConnectionType() throws Exception {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("foo", "bar");
            URL url = new URL(tempFile.toURL().toExternalForm());
            URLConnection c = url.openConnection();
            assertEquals(FileURLConnection.class, c.getClass());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    public void testFileToURL() throws Exception {
        URL url = file.toURL();
        URLConnection c = url.openConnection();
        assertEquals(FileURLConnection.class, c.getClass());
    }

    public void testGetLastModified() throws Exception {
        URLConnection c = fileURL.openConnection();
        assertEquals(file.lastModified(), c.getLastModified());
        file.setLastModified(System.currentTimeMillis());
        assertEquals(file.lastModified(), c.getLastModified());
    }

    public void testGetDate() throws Exception {
        URLConnection c = fileURL.openConnection();
        assertEquals(file.lastModified(), c.getDate());
        file.setLastModified(System.currentTimeMillis());
        assertEquals(file.lastModified(), c.getDate());
    }

    private void writeSomeBytes(final File file, final int count) throws IOException {
        OutputStream output = new FileOutputStream(file);
        try {
            writeSomeBytes(output, count);
        } finally {
            output.close();
        }
    }

    private void writeSomeBytes(final OutputStream output, final int count) throws IOException {
        output.write(new byte[count]);
        output.flush();
    }

    public void testGetContentLength() throws Exception {
        int length = 0;
        URLConnection c = fileURL.openConnection();
        assertEquals(file.length(), c.getContentLength());

        length += 8;
        writeSomeBytes(file, length);
        assertEquals(length, file.length());
        assertEquals(file.length(), c.getContentLength());

        length += 1;
        writeSomeBytes(file, length);
        assertEquals(length, file.length());
        assertEquals(file.length(), c.getContentLength());

        length += 10;
        writeSomeBytes(file, length);
        assertEquals(length, file.length());
        assertEquals(file.length(), c.getContentLength());

        length *= 2;
        writeSomeBytes(file, length);
        assertEquals(length, file.length());
        assertEquals(file.length(), c.getContentLength());
    }

    public void testGetContentType() throws Exception {
        File file = null;
        try {
            file = File.createTempFile("FileProtocolTest", ".xml");
            URLConnection c = file.toURI().toURL().openConnection();
            assertEquals("application/xml", c.getContentType());
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    public void testGetInputStream() throws Exception {
        URLConnection c = fileURL.openConnection();
        InputStream input = c.getInputStream();

        int length = 8;
        writeSomeBytes(file, length);
        assertEquals(length, input.available());

        length *= 8;
        writeSomeBytes(file, length);
        assertEquals(length, input.available());

        try {
            input.close();
            input.read();
            fail("Expected IOException");
        } catch (IOException e) {
            // OK
        }
    }

    public void testSyncFDUpdatesFileLength() throws Exception {
        File foo = null;
        OutputStream out = null;
        try {
            foo = File.createTempFile("TestFileLength", ".tmp");
            FileOutputStream fos = new FileOutputStream(foo);
            out = new BufferedOutputStream(fos);

            out.write(new byte[10]);
            out.flush();
            // out.close();
            fos.getFD().sync(); // this is required on Windows for foo.length to be updated
            assertEquals(10, foo.length());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
            foo.delete();
        }
    }

    public void testGetOutputStream() throws Exception {
        URLConnection c = fileURL.openConnection();
        OutputStream output = c.getOutputStream();

        int length = 8;
        writeSomeBytes(output, length);

        // Do not check file length, may fail on windows
        // assertEquals(length, file.length());

        // This should work, as the connection should sync the fd
        assertEquals(length, c.getContentLength());

        writeSomeBytes(output, length);

        // Do not check file length, may fail on windows
        // assertEquals(length * 2, file.length());

        // This should work, as the connection should sync the fd
        assertEquals(length * 2, c.getContentLength());

        try {
            output.close();
            writeSomeBytes(output, 1);
            fail("Expected IOException");
        } catch (IOException e) {
            // OK
        }
    }
}
