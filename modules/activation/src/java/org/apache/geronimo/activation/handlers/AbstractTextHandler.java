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
package org.apache.geronimo.activation.handlers;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;

/**
 * @version $Rev$ $Date$
 */
public class AbstractTextHandler implements DataContentHandler {
    private final DataFlavor flavour;

    public AbstractTextHandler(DataFlavor flavour) {
        this.flavour = flavour;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {flavour};
    }

    public Object getTransferData(DataFlavor dataFlavor, DataSource dataSource) throws UnsupportedFlavorException, IOException {
        return flavour.equals(dataFlavor) ? getContent(dataSource) : null;
    }

    public Object getContent(DataSource ds) throws IOException {
        // todo handle encoding
        Reader reader = new InputStreamReader(ds.getInputStream());
        StringBuffer result = new StringBuffer(1024);
        char[] buffer = new char[32768];
        int count;
        while ((count = reader.read(buffer)) != -1) {
            result.append(buffer, 0, count);
        }
        return result.toString();
    }

    public void writeTo(Object o, String mimeType, OutputStream os) throws IOException {
        String s;
        if (o instanceof String) {
            s = (String) o;
        } else if (o != null) {
            s = o.toString();
        } else {
            return;
        }
        // todo handle encoding
        OutputStreamWriter writer = new OutputStreamWriter(os);
        writer.write(s);
        writer.flush();
    }
}
