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

package org.apache.geronimo.test.protocol.mockproto;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * A mock input stream for mockproto urls.
 *
 * @version $Rev$ $Date$
 */
public final class MockInputStream
    extends ByteArrayInputStream
{
    private MockURLConnection conn;

    public MockInputStream(MockURLConnection conn, String s) {
        super(s.getBytes());
        this.conn = conn;
    }

    public void close() throws IOException {
        conn.close();
    }
}
