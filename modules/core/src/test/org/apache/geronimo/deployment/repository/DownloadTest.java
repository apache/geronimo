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
package org.apache.geronimo.deployment.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.geronimo.common.net.protocol.Protocols;

import org.apache.geronimo.test.protocol.mockproto.Handler;
import org.apache.geronimo.test.protocol.mockproto.MockURLConnection;

/**
 * 
 * 
 * @version $Revision: 1.2 $ $Date: 2003/08/30 12:32:32 $
 */
public class DownloadTest
    extends TestCase
{
    private File localRoot;
    private File fakeRemoteRoot;
    private ComponentRepository repo;
    private ComponentDescription desc;
    private ComponentDescription desc2;
    private ComponentDescription desc3;
    private MockURLConnection urlConnection;
    private File testArchive;
    
    protected void setUp() throws Exception {
        Protocols.appendHandlerPackage("org.apache.geronimo.test.protocol");
        
        File temp = new File(System.getProperty("java.io.tmpdir"));
        localRoot = new File(temp, "localRepo");

        desc = new ComponentDescription("product", "1.1.1", "myproduct/product-1.1.1.txt");
        fakeRemoteRoot = new File(temp, "remoteRepo");
        testArchive = new File(fakeRemoteRoot, desc.getLocation());
        testArchive.getParentFile().mkdirs();
        testArchive.createNewFile();
        FileOutputStream fos = new FileOutputStream(testArchive);
        PrintWriter writer = new PrintWriter(fos);
        writer.println("Hello World");
        writer.flush();
        writer.close();

        String location = "commons-logging/jars/commons-logging-1.0.1.jar";
        desc2 = new ComponentDescription("commons-logging", "1.0.1", location);
        desc3 = new ComponentDescription("nosuchfile", "noversion", "nolocation");

        repo = new ComponentRepository(localRoot);
        repo.addRemoteRoot(fakeRemoteRoot.toURL());

        String urlspec = "mockproto://www.ibiblio.org/maven/";
        urlConnection =  Handler.registerURL(urlspec + location, "This is test file content");
        repo.addRemoteRoot(new URL(urlspec));
    }

    protected void tearDown() throws Exception {
        Protocols.setHandlerPackages(Protocols.getSystemHandlerPackages());
        
        recursiveDelete(localRoot);
        recursiveDelete(fakeRemoteRoot);
    }

    private void recursiveDelete(File root) throws Exception {
        File[] files = root.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                recursiveDelete(file);
            } else {
                file.delete();
            }
        }
        root.delete();
    }
    
    public void testLocalCopy() throws Exception {
        assertTrue(repo.ensureLocal(desc));

        File localFile = new File(localRoot, desc.getLocation());
        assertTrue(localFile.exists());

        long ts = localFile.lastModified();
        sleep(50);

        assertTrue(repo.ensureLocal(desc));
        assertTrue(ts == localFile.lastModified());

        repo.removeLocal(desc);
        assertFalse(localFile.exists());
    }

    public void testRemoteCopy() throws Exception {
        assertTrue(repo.ensureLocal(desc2));

        File localFile = new File(localRoot, desc2.getLocation());
        assertTrue(localFile.exists());

        long ts = localFile.lastModified();
        sleep(50);

        assertTrue(repo.ensureLocal(desc));
        assertTrue(ts == localFile.lastModified());

        repo.removeLocal(desc2);
        assertFalse(localFile.exists());

        assertTrue("Was expecting clean up of resources",
                urlConnection.getState() == MockURLConnection.STATE_CLOSED);
    }

    public void testNotFound() throws Exception {
        assertFalse(repo.ensureLocal(desc3));
        File localFile = new File(localRoot, desc3.getLocation());
        assertFalse(localFile.exists());
        assertTrue("Was expecting clean up",
                ( urlConnection.getState() == MockURLConnection.STATE_CLOSED 
                 || urlConnection.getState() == MockURLConnection.STATE_INIT));
    }

    private void sleep(long naptime) {
        try {
            Thread.sleep(naptime);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
