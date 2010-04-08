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

import java.io.IOException;

/**
 * Abstract interface for an interface between command components
 * and a console.
 */
public interface ConsoleReader {

    /**
     * Print an end-of-line marker.
     *
     * @exception IOException
     */
    void printNewline() throws IOException;

    /**
     * Print a string to the console (without a newline).
     *
     * @param s      The string to print.
     *
     * @exception IOException
     */
    void printString(String s) throws IOException;

    /**
     * Print a line to the console (with a newline).
     *
     * @param s      The string to print.
     *
     * @exception IOException
     */
    void println(String s) throws IOException;

    /**
     * Read a line from the console.
     *
     * @return The next line from the console.
     * @exception IOException
     */
    String readLine() throws IOException;

    /**
     * Read a line from the console with a prompt.
     *
     * @param prompt The prompt string used for the reading.
     *
     * @return The next line from the console.
     * @exception IOException
     */
    String readLine(String prompt) throws IOException;

    /**
     * Flush any pending writes to the console.
     *
     * @exception IOException
     */
    void flushConsole() throws IOException;

    /**
     * Read a line from the console without echo
     * @return
     */
    String readPassword() throws IOException;

    /**
     * Read a line from the console without echo
     * @param prompt The prompt string used for the reading.
     * @return
     */
    String readPassword(String prompt) throws IOException;
}
