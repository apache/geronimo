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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:22 $
 */
public class DataHandler implements Transferable {
    private DataSource _ds;
    public DataHandler(DataSource ds) {
        _ds = ds;
    }

    public DataHandler(Object data, String type) {
        _ds = new ObjectDataSource(data, type);
    }

    static class ObjectDataSource implements DataSource {

        private Object _data;
        private String _type;
        public Object getContent() {
            return _data;
        }
        /**
         * Store an object as a data source type
         * @param data the object
         * @param type the mimeType
         */
        public ObjectDataSource(Object data, String type) {
            _data = data;
            _type = type;
        }

        /* (non-Javadoc)
         * @see javax.activation.DataSource#getInputStream()
         */
        public InputStream getInputStream() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.activation.DataSource#getOutputStream()
         */
        public OutputStream getOutputStream() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.activation.DataSource#getContentType()
         */
        public String getContentType() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.activation.DataSource#getName()
         */
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

    }
    public DataHandler(URL url) {
        /*@todo implement*/
    }

    public DataSource getDataSource() {
        return _ds;
    }

    public String getName() {
        /*@todo implement*/
        return null;
    }

    public String getContentType() {
        return _ds.getContentType();
    }

    public InputStream getInputStream() throws IOException {
        return _ds.getInputStream();
    }

    public void writeTo(OutputStream os) throws IOException {
        // TODO implement
    }

    public OutputStream getOutputStream() throws IOException {
        return _ds.getOutputStream();
    }

    public synchronized DataFlavor[] getTransferDataFlavors() {
        /*@todo implement*/
        return null;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        /*@todo implement*/
        return false;
    }

    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
        /*@todo implement*/
        return null;
    }

    public synchronized void setCommandMap(CommandMap commandMap) {
        /*@todo implement*/
    }

    public CommandInfo[] getPreferredCommands() {
        /*@todo implement*/
        return null;
    }

    public CommandInfo[] getAllCommands() {
        /*@todo implement*/
        return null;
    }

    public CommandInfo getCommand(String cmdName) {
        /*@todo implement*/
        return null;
    }

    public Object getContent() throws IOException {
        if (_ds instanceof ObjectDataSource) {
            return ((ObjectDataSource) _ds).getContent();
        } else {
            // TODO not yet implemented
            throw new IOException("TODO Not yet implemented");
        }
    }

    public Object getBean(CommandInfo cmdinfo) {
        /*@todo implement*/
        return null;
    }

    public static synchronized void setDataContentHandlerFactory(DataContentHandlerFactory newFactory) {
    }
}
