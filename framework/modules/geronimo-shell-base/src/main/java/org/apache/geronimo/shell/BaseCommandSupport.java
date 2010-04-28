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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
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

    private PrintWriter printWriter = null;
    private BufferedReader lineReader = null;


    /**
     * Create printWriter and lineReader for the session
     *
     */
    private void init(){

        if (printWriter == null)
            printWriter = new PrintWriter(session.getConsole(), true);

        if (lineReader == null)
            lineReader = new BufferedReader(new InputStreamReader(session.getKeyboard()));
    }



    /**
     * Print an end-of-line marker.
     *
     * @exception IOException
     */
    public void printNewline() throws IOException {
        init();
        printWriter.println();
    }


    /**
     * Write a line of output to the command shell session.
     *
     * @param data   The line to write.
     */
    public void println(String data) {
        init();
        printWriter.println(data);
    }


    /**
     * Write a string of output to the command shell session.
     *
     * @param data   The line to write.
     */
    public void printString(String data) {
        init();
        printWriter.print(data);
    }


    /**
     * Read a line of output from the command shell session.
     *
     * @return The next line from the session input.
     * @exception IOException
     */
    public String readLine() throws IOException {
        init();
        return lineReader.readLine();
    }


    /**
     * Utility method for issuing shell prompts.
     *
     * @param prompt The prompt string.
     *
     * @return The string return result.
     */
    public String readLine(String prompt) throws IOException {
        printString(prompt);
        return readLine();
    }


    /**
     * Flush any pending writes to the console.
     *
     * @exception IOException
     */
    public void flushConsole() throws IOException {
        init();
        printWriter.flush();
    }

    @Override
    public String readPassword() throws IOException {
       return readLine();
    }

    @Override
    public String readPassword(String prompt) throws IOException {
        return readLine(prompt);
    }
    
    public Kernel getKernel() {
        ServiceReference reference = bundleContext.getServiceReference(Kernel.class.getName());
        Kernel kernel = getService(Kernel.class, reference);
        return kernel;
    }
    
    public boolean isEmbedded(Kernel kernel) {
        Set deamon = kernel.listGBeans(new AbstractNameQuery("org.apache.geronimo.system.main.EmbeddedDaemon"));
        return !deamon.isEmpty();
    }
}
