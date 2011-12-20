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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import jline.UnsupportedTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface for an interface between command components
 * and a console.
 */
public class StreamConsoleReader implements ConsoleReader {

    private static final Logger logger = LoggerFactory.getLogger(StreamConsoleReader.class);

    protected BufferedReader keyboard;

    protected PrintStream console;

    private jline.console.ConsoleReader jlineConsoleReader;

    private boolean jlineConsoleEnabled = true;

    public StreamConsoleReader(InputStream in, PrintStream out) {
        try {
            if ("jline.UnsupportedTerminal".equals(System.getProperty("jline.terminal"))) {
                jlineConsoleReader = new jline.console.ConsoleReader(in, out, new UnsupportedTerminal());
            } else {
                jlineConsoleReader = new jline.console.ConsoleReader(in, out);
            }
        } catch (Throwable e) {
            logger.warn("Fail to create jline console, some features like password mask will be disabled, you might change the log level to debug to show the detailed information");
            if (logger.isDebugEnabled()) {
                logger.debug("Fail to create jline console, some features like password mask will be disabled", e);
            }
            jlineConsoleEnabled = false;
            keyboard = new BufferedReader(new InputStreamReader(in));
            console = out;
        }
    }

    /**
     * Print an end-of-line marker.
     *
     * @exception IOException
     */
    @Override
    public void printNewline() throws IOException {
        if (jlineConsoleEnabled) {
            jlineConsoleReader.println();
        } else {
            console.println();
        }
    }

    /**
     * Print a string to the console (without a newline).
     *
     * @param s      The string to print.
     *
     * @exception IOException
     */
    @Override
    public void printString(String s) throws IOException {
        if (jlineConsoleEnabled) {
            jlineConsoleReader.print(s);
        } else {
            console.print(s);
        }
    }

    /**
     * Print a line to the console (with a newline).
     *
     * @param s      The string to print.
     *
     * @exception IOException
     */
    @Override
    public void println(String s) throws IOException {
        if (jlineConsoleEnabled) {
            jlineConsoleReader.print(s);
            jlineConsoleReader.println();
        } else {
            console.println(s);
            console.println();
        }
    }

    /**
     * Read a line from the console.
     *
     * @return The next line from the console.
     * @exception IOException
     */
    @Override
    public String readLine() throws IOException {
        if (jlineConsoleEnabled) {
            return jlineConsoleReader.readLine();
        } else {
            return keyboard.readLine();
        }
    }

    /**
     * Read a line from the console with a prompt.
     *
     * @param prompt The prompt string used for the reading.
     *
     * @return The next line from the console.
     * @exception IOException
     */
    @Override
    public String readLine(String prompt) throws IOException {
        if (jlineConsoleEnabled) {
            return jlineConsoleReader.readLine(prompt);
        } else {
            printString(prompt);
            flushConsole();
            return keyboard.readLine();
        }
    }

    /**
     * Flush any pending writes to the console.
     *
     * @exception IOException
     */
    @Override
    public void flushConsole() throws IOException {
        if (jlineConsoleEnabled) {
             jlineConsoleReader.flush();
        } else {
            console.flush();
        }
    }

    @Override
    public String readPassword() throws IOException {
        if (jlineConsoleEnabled) {
            return jlineConsoleReader.readLine('*');
        } else {
            return keyboard.readLine();
        }
    }

    @Override
    public String readPassword(String prompt) throws IOException {
        if (jlineConsoleEnabled) {
            return jlineConsoleReader.readLine(prompt, '*');
        } else {
            printString(prompt);
            flushConsole();
            return keyboard.readLine();
        }
    }
}
