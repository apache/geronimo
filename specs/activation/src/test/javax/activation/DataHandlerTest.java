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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.activation;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class DataHandlerTest extends TestCase {
    private CommandMap defaultMap;

    public void testObjectInputStream() throws IOException {
        DataHandler handler = new DataHandler("Hello World", "text/plain");
        InputStream is = handler.getInputStream();
        byte[] bytes = new byte[128];
        assertEquals(11, is.read(bytes));
        assertEquals("Hello World", new String(bytes, 0, 11));
    }

    protected void setUp() throws Exception {
        defaultMap = CommandMap.getDefaultCommandMap();
        MailcapCommandMap myMap = new MailcapCommandMap();
        myMap.addMailcap("text/plain;;    x-java-content-handler=" + DummyTextHandler.class.getName());
        CommandMap.setDefaultCommandMap(myMap);
    }

    protected void tearDown() throws Exception {
        CommandMap.setDefaultCommandMap(defaultMap);
    }

    public static class DummyTextHandler implements DataContentHandler {
        public DataFlavor[] getTransferDataFlavors() {
            throw new UnsupportedOperationException();
        }

        public Object getTransferData(DataFlavor df, DataSource ds) throws UnsupportedFlavorException, IOException {
            throw new UnsupportedOperationException();
        }

        public Object getContent(DataSource ds) throws IOException {
            throw new UnsupportedOperationException();
        }

        public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
            os.write(((String)obj).getBytes());
        }
    }
}
