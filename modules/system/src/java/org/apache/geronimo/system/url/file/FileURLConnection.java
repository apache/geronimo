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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SyncFailedException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sun.net.www.ParseUtil;

/**
 * A URLConnection for the 'file' protocol.
 *
 * <p>Correctly returns headers.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/10 09:59:31 $
 */
public class FileURLConnection extends URLConnection {
    private static final boolean IS_OS_WINDOWS = System.getProperty("os.name").startsWith("Windows");
    private File file;
    private FileDescriptor fd;

    public FileURLConnection(final URL url, final File file) throws MalformedURLException, IOException {
        super(url);

        if (file == null) {
            throw new IllegalArgumentException("file is null");
        }

        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void connect() throws IOException {
        if (connected) {
            return;
        }

        if (!file.exists()) {
            throw new FileNotFoundException(file.toString());
        }

        connected = true;
    }

    /**
     * Return the input stream for the file.
     *
     * <p>Sun's URL connections use buffered streams, so we do too.
     *
     * <p>This impl will return a new stream for each call.
     */
    public InputStream getInputStream() throws IOException {
        if (!connected) {
            connect();
        }

        FileInputStream fis = new FileInputStream(file);
        fd = fis.getFD();

        return new BufferedInputStream(fis);
    }

    /**
     * Return the output stream for the file.
     *
     * <p>Sun's URL connections use buffered streams, so we do too.
     *
     * <p>This impl will return a new stream for each call.
     */
    public OutputStream getOutputStream() throws IOException {
        if (!connected) {
            connect();
        }

        FileOutputStream fos = new FileOutputStream(file);
        fd = fos.getFD();

        return new BufferedOutputStream(fos);
    }

    /**
     * Return the permission for the file.
     *
     * <p>Sun's impl always returns "read", but no reason why we can
     *    not also write to a file URL, so we do.
     */
    public Permission getPermission() throws IOException {
        // Detect if we have read/write perms
        String perms = null;

        if (file.canRead()) {
            perms = "read";
        }
        if (file.canWrite()) {
            if (perms != null) {
                perms += ",write";
            } else {
                perms = "write";
            }
        }

        // File perms need filename to be in system format
        String filename = ParseUtil.decode(url.getPath());
        if (File.separatorChar != '/') {
            filename.replace('/', File.separatorChar);
        }

        return new FilePermission(filename, perms);
    }

    /**
     * Conditionaly sync the underlying file descriptor if we are running
     * on windows, so that the file details update.
     */
    private void maybeSync() {
        if (fd != null && fd.valid()) {
            if (IS_OS_WINDOWS) {
                try {
                    fd.sync();
                } catch (SyncFailedException e) {
                    // ignore... data may be a bit out of sync but who cares?
                }
            }
        }
    }

    /**
     * Always return the last-modified from the file.
     *
     * <p>NOTE: Sun's impl caches this value, so it will appear to never change
     *          even if the underlying file's last-modified has changed.
     */
    public long getLastModified() {
        maybeSync();
        return file.lastModified();
    }

    /**
     * Returns the last modified time of the file.
     */
    public long getDate() {
        return getLastModified();
    }

    /**
     * Returns the length of the file.
     *
     */
    public int getContentLength() {
        maybeSync();

        final long value = file.length();
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new IllegalStateException("Can not safly convert to int: " + value);
        }

        return (int) value;
    }

    /**
     * Returns the content type of the file as mapped by the filename map.
     */
    public String getContentType() {
        return getFileNameMap().getContentTypeFor(file.getName());
    }


    /////////////////////////////////////////////////////////////////////////
    //                                Headers                              //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Get a header field by name.
     *
     * <p>Supported headers:
     * <ul>
     * <li>last-modified
     * <li>content-length
     * <li>content-type
     * <li>date
     * </ul>
     *
     * <p>Hook into our local methods to get headers.  URLConnection
     *    normally goes the other way around.  ie. URLConnection.getDate()
     *    calls getHeaderField('date'), but for file usage this is wasteful
     *    string creation as normally the getHeaderField() will not be called.
     */
    public String getHeaderField(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        String headerName = name.toLowerCase();

        if (headerName.equals("last-modified")) {
            return String.valueOf(getLastModified());
        } else if (headerName.equals("content-length")) {
            return String.valueOf(getContentLength());
        } else if (headerName.equals("content-type")) {
            return getContentType();
        } else if (headerName.equals("date")) {
            return String.valueOf(getDate());
        }

        return super.getHeaderField(name);
    }

    /**
     * Returns supported headers.
     *
     * @see #getHeaderField(java.lang.String)
     */
    public Map getHeaderFields() {
        Map headers = new HashMap();
        String[] headerNames = {
            "last-modified",
            "content-length",
            "content-type",
            "date"
        };

        for (int i = 0; i < headerNames.length; i++) {
            List list = new ArrayList(1);
            list.add(getHeaderField(headerNames[i]));
            headers.put(headerNames[i], Collections.unmodifiableList(list));
        }

        return Collections.unmodifiableMap(headers);
    }

    //
    // TODO: implement these... no one uses these so who cares?
    //

    public String getHeaderFieldKey(final int n) {
        return getHeaderFieldKey(n);
    }

    public String getHeaderField(final int n) {
        return getHeaderField(n);
    }
}
