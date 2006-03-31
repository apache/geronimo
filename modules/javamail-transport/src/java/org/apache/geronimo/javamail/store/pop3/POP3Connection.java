/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.javamail.store.pop3;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.mail.MessagingException;
import javax.mail.Session;

/**
 * Represents a connection with the POP3 mail server. The connection is owned by
 * a pop3 store and is only associated with one user who owns the respective
 * POP3Store instance
 * 
 * @version $Rev$ $Date$
 */

public class POP3Connection {

    private Socket soc;

    private Session session;

    private String host;

    private int port;

    private PrintWriter writer;

    private BufferedReader reader;

    POP3Connection(Session session, String host, int port) {

        this.session = session;
        this.host = host;
        this.port = port;
    }

    public void open() throws Exception {
        try {
            soc = new Socket();
            soc.connect(new InetSocketAddress(host, port));

            if (session.getDebug()) {
                session.getDebugOut().println("Connection successful " + this.toString());
            }

            buildInputReader();
            buildOutputWriter();

            // consume the greeting
            if (session.getDebug()) {
                session.getDebugOut().println("Greeting from server " + reader.readLine());
            } else {
                reader.readLine();
            }

        } catch (IOException e) {
            Exception ex = new Exception("Error opening connection " + this.toString(), e);
            throw ex;
        }
    }

    void close() throws Exception {
        try {
            soc.close();
            if (session.getDebug()) {
                session.getDebugOut().println("Connection successfuly closed " + this.toString());
            }

        } catch (IOException e) {
            Exception ex = new Exception("Error closing connection " + this.toString(), e);
            throw ex;
        }

    }

    public synchronized POP3Response sendCommand(POP3Command cmd) throws MessagingException {
        if (soc.isConnected()) {

            // if the underlying output stream is down
            // attempt to rebuild the writer
            if (soc.isOutputShutdown()) {
                buildOutputWriter();
            }

            // if the underlying inout stream is down
            // attempt to rebuild the reader
            if (soc.isInputShutdown()) {
                buildInputReader();
            }

            if (session.getDebug()) {
                session.getDebugOut().println("\nCommand sent " + cmd.getCommand());
            }

            POP3Response res = null;

            // this method supresses IOException
            // but choose bcos of ease of use
            {
                writer.write(cmd.getCommand());
                writer.flush();
                res = POP3ResponseBuilder.buildResponse(session, reader, cmd.isMultiLineResponse());
            }

            return res;
        }

        throw new MessagingException("Connection to Mail Server is lost, connection " + this.toString());
    }

    private void buildInputReader() throws MessagingException {
        try {
            reader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
        } catch (IOException e) {
            throw new MessagingException("Error obtaining input stream " + this.toString(), e);
        }
    }

    private void buildOutputWriter() throws MessagingException {
        try {
            writer = new PrintWriter(new BufferedOutputStream(soc.getOutputStream()));
        } catch (IOException e) {
            throw new MessagingException("Error obtaining output stream " + this.toString(), e);
        }
    }

    public String toString() {
        return "POP3Connection host: " + host + " port: " + port;
    }
}
