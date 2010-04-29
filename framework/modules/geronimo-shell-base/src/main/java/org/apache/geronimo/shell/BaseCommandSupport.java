/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.shell;

import java.io.IOException;
import java.util.Set;

import org.apache.felix.karaf.shell.console.OsgiCommandSupport;
import org.apache.geronimo.deployment.cli.ConsoleReader;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.osgi.framework.ServiceReference;

/**
 * @version $Rev$ $Date$
 */

public abstract class BaseCommandSupport extends OsgiCommandSupport implements ConsoleReader {

    /**
     * Print an end-of-line marker.
     *
     * @exception IOException
     */
    public void printNewline() throws IOException {
        session.getConsole().println();
    }

    /**
     * Write a line of output to the command shell session.
     *
     * @param data   The line to write.
     */
    public void println(String data) {
        session.getConsole().println(data);
    }


    /**
     * Write a string of output to the command shell session.
     *
     * @param data   The line to write.
     */
    public void printString(String data) {
        session.getConsole().print(data);
    }

    /**
     * Read a line of output from the command shell session.
     *
     * @return The next line from the session input.
     * @exception IOException
     */
    public String readLine() throws IOException {
        return readLine(null, '\0');
    }

    /**
     * Utility method for issuing shell prompts.
     *
     * @param prompt The prompt string.
     *
     * @return The string return result.
     */
    public String readLine(String msg) throws IOException {
        return readLine(msg, '\0');
    }
    
    private String readLine(String msg, char mask) throws IOException {
        StringBuffer sb = new StringBuffer();
        if (msg != null) {
            session.getConsole().print(msg);
            session.getConsole().flush();
        }
        for (;;) {
            int c = session.getKeyboard().read();
            if (c < 0) {
                return null;
            }
            if (c == 8 || c == 127) {
                int size = sb.length();
                if (size == 0) {
                    continue;
                }
                session.getConsole().print("\b \b");
                session.getConsole().flush();               
                sb.delete(size - 1, size);
            } else if (c == '\r' || c == '\n') {  
                session.getConsole().println();
                session.getConsole().flush();
                break;
            } else {
                session.getConsole().print(mask == '\0' ? (char)c : mask);
                session.getConsole().flush();
                sb.append((char) c);
            }
        }
        return sb.toString();
    }

    /**
     * Flush any pending writes to the console.
     *
     * @exception IOException
     */
    public void flushConsole() throws IOException {
        session.getConsole().flush();
    }

    @Override
    public String readPassword() throws IOException {
       return readLine(null, '*');
    }

    @Override
    public String readPassword(String prompt) throws IOException {
        return readLine(prompt, '*');
    }
    
    public Kernel getKernel() {
        ServiceReference reference = bundleContext.getServiceReference(Kernel.class.getName());
        Kernel kernel = null;
        if (reference != null) {
            kernel = getService(Kernel.class, reference);
        }
        return kernel;
    }
    
    public boolean isEmbedded() {
        return isEmbedded(getKernel());
    }
    
    public boolean isEmbedded(Kernel kernel) {
        if (kernel != null) {
            Set deamon = kernel.listGBeans(new AbstractNameQuery("org.apache.geronimo.system.main.EmbeddedDaemon"));
            return !deamon.isEmpty();
        } else {
            return false;
        }  
    }
}
