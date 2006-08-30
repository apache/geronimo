/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.activation.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractHandler extends TestCase {
    protected DataContentHandler dch;
    protected String mimeType;

    public void testGetContent() throws Exception {
        final byte[] bytes = "Hello World".getBytes();
        DataSource ds = new DataSource() {
            public InputStream getInputStream() {
                return new ByteArrayInputStream(bytes);
            }

            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException();
            }

            public String getContentType() {
                throw new UnsupportedOperationException();
            }

            public String getName() {
                throw new UnsupportedOperationException();
            }
        };
        Object o = dch.getContent(ds);
        assertEquals("Hello World", o);
    }

    public void testWriteTo() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dch.writeTo("Hello World", mimeType, baos);
        assertEquals("Hello World", baos.toString());
    }
}
