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

package org.apache.geronimo.system.url.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import sun.net.www.ParseUtil;

/**
 * A protocol handler for the 'file' protocol.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/27 00:43:44 $
 */
public class Handler extends URLStreamHandler {
    protected void parseURL(final URL url, final String s, final int i, final int j) {
        super.parseURL(url, s.replace(File.separatorChar, '/'), i, j);
    }

    /**
     * Open a connection to the file.
     *
     * <p>NOTE: Sun's impl attempts to translate into a 'ftp' URL which is dumb
     *          so fix that by removing it ;-)
     */
    public URLConnection openConnection(final URL url) throws IOException {
        String path = ParseUtil.decode(url.getPath());
        path = path.replace('/', File.separatorChar).replace('|', ':');
        File file = new File(path);

        // Handle the hostname of the URL if given, puke if not valid
        String hostname = url.getHost();
        if (hostname == null ||
                hostname.equals("") ||
                hostname.equals("~") ||
                hostname.equals("localhost") ||
                file.exists()) {
            return new FileURLConnection(url, file);
        }

        throw new FileNotFoundException("Invalid host specification: " + url);
    }
}
