/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.2 $ $Date: 2003/09/02 03:47:40 $
 */
public class FileProtocolTest
        extends TestCase {
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

    public void testFoo() throws Exception {
        File foo = File.createTempFile("TestFileLength", ".tmp");
        FileOutputStream fos = new FileOutputStream(foo);
        OutputStream out = new BufferedOutputStream(fos);
        try {
            out.write(new byte[10]);
            out.flush();
//            out.close();
            fos.getFD().sync(); // this is required on Windows for foo.length to be updated
            assertEquals(10, foo.length());
        } finally {
            foo.delete();
        }
    }

    /*
     * This test fails on Windows because File.length() is not updated until the
     * OutputStream is closed or the underlying FileDescriptor sync()'ed
     */
    public void XtestGetOutputStream() throws Exception {
        URLConnection c = fileURL.openConnection();
        OutputStream output = c.getOutputStream();

        int length = 8;
        writeSomeBytes(output, length);
        output.close();
        assertEquals(length, file.length());
        assertEquals(length, c.getContentLength());

        writeSomeBytes(output, length);
        assertEquals(length * 2, file.length());
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
