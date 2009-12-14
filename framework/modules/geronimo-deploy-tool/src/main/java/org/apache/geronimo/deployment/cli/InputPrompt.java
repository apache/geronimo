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
import java.io.InputStream;
import java.io.Writer;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;

/**
 * Prompts a user for input; optionally masking.
 *
 * <p>
 * Uses the <a href="http://jline.sf.net">JLine</a> library to provide a rich user experence.
 *
 * @version $Rev$ $Date$
 */
public class InputPrompt
{
    private ConsoleReader reader;

    public InputPrompt(final InputStream in, final Writer out) throws IOException {
        this.reader = new StreamConsoleReader(in, out);
    }

    public InputPrompt(final InputStream in, final OutputStream out) throws IOException {
        this(in, new PrintWriter(new OutputStreamWriter(out), true));
    }

    /**
     * Displays the prompt, grabs the input.
     */
    public String getInput(final String prompt, final Character mask) throws IOException {
        // since jline is not getting used any more, the character mask doesn't work.
        return reader.readLine(prompt);
    }

    public String getInput(final String prompt, final char mask) throws IOException {
        return getInput(prompt, new Character(mask));
    }

    public String getInput(final String prompt) throws IOException {
        return getInput(prompt, null);
    }

    /**
     * Displays the prompt, grabs the input masking with '*'.
     */
    public String getPassword(final String prompt) throws IOException {
        return getInput(prompt, '*');
    }
}