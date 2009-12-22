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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Abstract interface for an interface between command components
 * and a console.
 */
public class StreamConsoleReader implements ConsoleReader {
    // the standard streams to write to
    protected BufferedReader keyboard;
    protected PrintWriter console;

    public StreamConsoleReader(InputStream in, PrintWriter out) {
        keyboard = new BufferedReader(new InputStreamReader(in));
        console = out;
    }

    public StreamConsoleReader(InputStream in, OutputStream out) {
        this(in, new PrintWriter(new OutputStreamWriter(out)));
    }

    public StreamConsoleReader(InputStream in, Writer out) {
        this(in, new PrintWriter(out, true));
    }

    /**
     * Print an end-of-line marker.
     *
     * @exception IOException
     */
    public void printNewline() throws IOException {
        console.println();
    }

    /**
     * Print a string to the console (without a newline).
     *
     * @param s      The string to print.
     *
     * @exception IOException
     */
    public void printString(String s) throws IOException {
        console.print(s);
    }

    /**
     * Print a line to the console (with a newline).
     *
     * @param s      The string to print.
     *
     * @exception IOException
     */
    public void println(String s) throws IOException {
        console.println(s);
    }


    /**
     * Read a line from the console.
     *
     * @return The next line from the console.
     * @exception IOException
     */
    public String readLine() throws IOException {
        return keyboard.readLine();
    }


    /**
     * Read a line from the console with a prompt.
     *
     * @param prompt The prompt string used for the reading.
     *
     * @return The next line from the console.
     * @exception IOException
     */
    public String readLine(String prompt) throws IOException {
        printString(prompt);
        flushConsole();
        return readLine();
    }


    /**
     * Flush any pending writes to the console.
     *
     * @exception IOException
     */
    public void flushConsole() throws IOException {
        console.flush();
    }
}

