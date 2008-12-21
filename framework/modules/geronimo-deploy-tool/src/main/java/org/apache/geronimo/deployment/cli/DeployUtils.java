/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.deployment.cli;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Properties;

import jline.ConsoleReader;

import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.deployment.plugin.ConfigIDExtractor;

/**
 * Various helpers for deployment.
 *
 * @version $Rev$ $Date$
 */
public class DeployUtils extends ConfigIDExtractor {
    
    private final static String DEFAULT_URI = "deployer:geronimo:jmx";
    private final static String DEFAULT_SECURE_URI = "deployer:geronimo:jmxs";
    
    /**
     * Split up an output line so it indents at beginning and end (to fit in a
     * typical terminal) and doesn't break in the middle of a word.
     *
     * @param source The unformatted String
     * @param indent The number of characters to indent on the left
     * @param endCol The maximum width of the entire line in characters,
     *               including indent (indent 10 with endCol 70 results
     *               in 60 "usable" characters).
     */
    public static String reformat(String source, int indent, int endCol) {
        if (endCol - indent < 10) {
            throw new IllegalArgumentException("This is ridiculous!");
        }
        StringBuffer buf = new StringBuffer((int) (source.length() * 1.1));
        String prefix = indent == 0 ? "" : buildIndent(indent);
        try {
            BufferedReader in = new BufferedReader(new StringReader(source));
            String line;
            int pos;
            while ((line = in.readLine()) != null) {
                if (buf.length() > 0) {
                    buf.append('\n');
                }
                while (line.length() > 0) {
                    line = prefix + line;
                    if (line.length() > endCol) {
                        pos = line.lastIndexOf(' ', endCol);
                        if (pos < indent) {
                            pos = line.indexOf(' ', endCol);
                            if (pos < indent) {
                                pos = line.length();
                            }
                        }
                        buf.append(line.substring(0, pos)).append('\n');
                        if (pos < line.length() - 1) {
                            line = line.substring(pos + 1);
                        } else {
                            break;
                        }
                    } else {
                        buf.append(line).append("\n");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw (AssertionError) new AssertionError("This should be impossible").initCause(e);
        }
        return buf.toString();
    }

    public static void println(String line, int indent, ConsoleReader consoleReader) throws IOException {
        int endCol = consoleReader.getTermwidth();
        int start = consoleReader.getCursorBuffer().cursor;
        if (endCol - indent < 10) {
            throw new IllegalArgumentException("This is ridiculous!");
        }
//        StringBuffer buf = new StringBuffer((int)(source.length()*1.1));
        String prefix = indent == 0 ? "" : buildIndent(indent);
        int pos;
        while (line.length() > 0) {
            if (start == 0) {
                line = prefix + line;
            }
            if (line.length() > endCol - start) {
                pos = line.lastIndexOf(' ', endCol - start);
                if (pos < indent) {
                    pos = line.indexOf(' ', endCol - start);
                    if (pos < indent) {
                        pos = line.length();
                    }
                }
                consoleReader.printString(line.substring(0, pos));
                consoleReader.printNewline();
                if (pos < line.length() - 1) {
                    line = line.substring(pos + 1);
                } else {
                    break;
                }
                start = 0;
            } else {
                consoleReader.printString(line);
                consoleReader.printNewline();
                break;
            }
        }
    }

    public static void printTo(String string, int col, ConsoleReader consoleReader) throws IOException {
        consoleReader.printString(string);
        for (int i = string.length(); i < col; i++) {
            consoleReader.printString(" ");
        }
    }

    private static String buildIndent(int indent) {
        StringBuffer buf = new StringBuffer(indent);
        for (int i = 0; i < indent; i++) {
            buf.append(' ');
        }
        return buf.toString();
    }
    
    public static String getConnectionURI(String host, Integer port, boolean secure) {
        if (host == null) {
            host = "localhost";
        }
        if (port == null) {
            port = new Integer(1099);
        }
        String uri = (secure) ? DEFAULT_SECURE_URI : DEFAULT_URI;
        uri += "://" + host + ":" + port;
        return uri;
    }
    
    public static SavedAuthentication readSavedCredentials(String uri) throws IOException {
        SavedAuthentication auth = null;
        InputStream in;
        
        // First check for .geronimo-deployer on class path (e.g. packaged in deployer.jar)
        in = DeployUtils.class.getResourceAsStream("/.geronimo-deployer");
        // If not there, check in home directory
        if (in == null) {
            File authFile = new File(System.getProperty("user.home"), ".geronimo-deployer");
            if (authFile.exists() && authFile.canRead()) {
                try {
                    in = new BufferedInputStream(new FileInputStream(authFile));
                } catch (FileNotFoundException e) {
                    // ignore
                }
            }
        }
        
        if (in != null) {
            try {
                Properties props = new Properties();
                props.load(in);
                String encrypted = props.getProperty("login." + uri);
                if (encrypted != null) {
                    if (encrypted.startsWith("{Plain}")) {
                        int pos = encrypted.indexOf("/");
                        String user = encrypted.substring(7, pos);
                        String password = encrypted.substring(pos + 1);
                        auth = new SavedAuthentication(uri, user, password.toCharArray());
                    } else {
                        Object o = EncryptionManager.decrypt(encrypted);
                        if (o == encrypted) {
                            throw new IOException("Unknown encryption used in saved login file");
                        } else {
                            auth = (SavedAuthentication) o;
                        }
                    }
                }
            } catch (IOException e) {
                throw new IOException("Unable to read authentication from saved login file: " + e.getMessage());
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    // ingore
                }
            }
        }
        
        return auth;
    }
    
    public final static class SavedAuthentication implements Serializable {

        private static final long serialVersionUID = -3127576258038677899L;
        
        private String uri;
        private String user;
        private char[] password;

        public SavedAuthentication(String uri, String user, char[] password) {
            this.uri = uri;
            this.user = user;
            this.password = password;
        }
        
        public String getURI() {
            return this.uri;
        }
        
        public String getUser() {
            return this.user;
        }
        
        public char[] getPassword() {
            return this.password;
        }
    }

}
