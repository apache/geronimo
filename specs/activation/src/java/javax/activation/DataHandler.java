/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.activation;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @version $Revision: 1.5 $ $Date: 2003/09/04 01:00:02 $
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
