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

package javax.activation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @version $Rev$ $Date$
 */
public class URLDataSource implements DataSource {

    private URL url;
    private final static String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /**
     * Creates a URLDataSource from a URL object
     */
    public URLDataSource(URL url) {
        this.url = url;
    }

    /**
     * Returns the value of the URL content-type header field
     */
    public String getContentType() {
        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
        }
        if (connection == null)
            return DEFAULT_CONTENT_TYPE;

        return connection.getContentType();

    }

    /**
     * Returns the file name of the URL object
     */
    public String getName() {
        return url.getFile();
    }

    /**
     * Returns an InputStream obtained from the data source
     */
    public InputStream getInputStream() throws IOException {
        return url.openStream();
    }

    /**
     * Returns an OutputStream obtained from the data source
     */
    public OutputStream getOutputStream() throws IOException {

        URLConnection connection = url.openConnection();
        if (connection == null)
            return null;

        connection.setDoOutput(true); //is it necessary?
        return connection.getOutputStream();
    }

    /**
     * Returns the URL of the data source
     */
    public URL getURL() {
        return url;
    }
}