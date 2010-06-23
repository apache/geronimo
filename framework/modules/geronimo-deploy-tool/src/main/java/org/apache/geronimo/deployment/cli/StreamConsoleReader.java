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
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface for an interface between command components
 * and a console.
 */
public class StreamConsoleReader implements ConsoleReader {

    private static final Logger logger = LoggerFactory.getLogger(StreamConsoleReader.class);

    protected BufferedReader keyboard;

    protected PrintWriter console;

    private jline.ConsoleReader jlineConsoleReader;

    private boolean jlineConsoleEnabled = true;

    public StreamConsoleReader(InputStream in, PrintStream out) {
        this(in, new PrintWriter(out, true));
    }

    public StreamConsoleReader(InputStream in, PrintWriter out) {
        try {
            jlineConsoleReader = new jline.ConsoleReader(in, out);
        } catch (IOException e) {
            logger.warn("Fail to create jline console, some features like password mask will be disabled", e);
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
            jlineConsoleReader.printNewline();
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
            jlineConsoleReader.printString(s);
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
            jlineConsoleReader.printString(s);
            jlineConsoleReader.printNewline();
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
            jlineConsoleReader.flushConsole();
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
