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

package org.apache.geronimo.test.protocol.mockproto;

import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

/**
 * URL connection for the 'mockproto' protocol.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:30 $
 */
public final class MockURLConnection
    extends URLConnection
{
    private String urlContents;
    private int state;

    public static int STATE_INIT = 0;
    public static int STATE_OPENED = 1;
    public static int STATE_CLOSED = 2;

    public MockURLConnection(URL url, String urlContents) {
        super(url);
        this.urlContents = urlContents;
        this.state = STATE_INIT;
    }

    public void connect() throws IOException {
        this.state = STATE_OPENED;
    }

    public InputStream getInputStream() {
        return new MockInputStream(this, urlContents);
    }

    public void close() throws IOException {
        if (this.state != STATE_CLOSED) {
            this.state = STATE_CLOSED;
        }
        else {
            throw new IOException("Closing an already closed connection");
        }
    }

    public int getState() {
        return this.state;
    }
}
