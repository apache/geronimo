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

package org.apache.geronimo.common.net.protocol.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.TestCase;
import org.apache.geronimo.common.net.protocol.Protocols;
import org.apache.geronimo.common.net.protocol.URLStreamHandlerFactory;

/**
 * Unit test for the 'file' protocol.
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:57:05 $
 */
public class FileProtocolTest
    extends TestCase
{
    static {
        //
        // Have to install factory to make sure that our file handler is used
        // and not Sun's
        //
        Protocols.prependHandlerPackage("org.apache.geronimo.common.net.protocol");
        URLStreamHandlerFactory factory = new URLStreamHandlerFactory();
        URL.setURLStreamHandlerFactory(factory);
    }

    protected File file;
    protected URL fileURL;

    protected void setUp() throws Exception {
        file = File.createTempFile("FileProtocolTest", ".tmp");
        fileURL = file.toURI().toURL();
    }

    protected void tearDown() throws Exception {
        file.delete();
    }

    public void testCreateURL() throws Exception {
        new URL("file:/some/file");
    }

    public void testURLConnectionType() throws Exception {
        URL url = new URL("file:/some/file");
        URLConnection c = url.openConnection();
        assertEquals(FileURLConnection.class, c.getClass());
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

    protected void writeSomeBytes(final File file, final int count) throws IOException {
        OutputStream output = new FileOutputStream(file);
        try {
            writeSomeBytes(output, count);
        } finally {
            output.close();
        }
    }

    protected void writeSomeBytes(final OutputStream output, final int count) throws IOException {
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
        File file = File.createTempFile("FileProtocolTest", ".xml");
        try {
            URLConnection c = file.toURI().toURL().openConnection();
            assertEquals("application/xml", c.getContentType());
        } finally {
            file.delete();
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
        File foo = File.createTempFile("TestFileLength", ".tmp");
        FileOutputStream fos = new FileOutputStream(foo);
        OutputStream out = new BufferedOutputStream(fos);
        try {
            out.write(new byte[10]);
            out.flush();
            // out.close();
            fos.getFD().sync(); // this is required on Windows for foo.length to be updated
            assertEquals(10, foo.length());
        } finally {
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
