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
package javax.mail.internet;

import java.io.IOException;
import java.io.OutputStream;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import javax.mail.MessagingException;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class MimeMultipartTest extends TestCase {
    private CommandMap defaultMap;

    public void testWriteTo() throws MessagingException, IOException {
        MimeMultipart mp = new MimeMultipart();
        MimeBodyPart part1 = new MimeBodyPart();
        part1.setContent("Hello World", "text/plain");
        mp.addBodyPart(part1);
        MimeBodyPart part2 = new MimeBodyPart();
        part2.setContent("Hello Again", "text/plain");
        mp.addBodyPart(part2);
        mp.writeTo(System.out);
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
            return new DataFlavor[0];  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Object getTransferData(DataFlavor df, DataSource ds) throws UnsupportedFlavorException, IOException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Object getContent(DataSource ds) throws IOException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
            os.write(((String)obj).getBytes());
        }
    }

    public static class DummyMultipartHandler implements DataContentHandler {
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[0];  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Object getTransferData(DataFlavor df, DataSource ds) throws UnsupportedFlavorException, IOException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Object getContent(DataSource ds) throws IOException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
            os.write(((String)obj).getBytes());
        }
    }
}
