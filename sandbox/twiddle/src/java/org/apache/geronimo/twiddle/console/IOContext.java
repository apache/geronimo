/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.twiddle.console;

import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;

import org.apache.geronimo.common.NullArgumentException;

/**
 * Abstraction of a collection of input and output streams
 * as well as helper methods to access the corresponding reader/writers.
 *
 * @version <code>$Rev$ $Date$</code>
 */
public class IOContext
{
    /** The input stream. */
    protected InputStream input;
    
    /** The reader (or null if reader was not accessed). */
    protected Reader reader;
    
    /** The output stream. */
    protected OutputStream output;
    
    /** The writer (or null if writer was not accessed). */
    protected PrintWriter writer;
    
    /** The error output stream. */
    protected OutputStream errorOutput;
    
    /** The error writer (or null if writer was not accessed). */
    protected PrintWriter errorWriter;
    
    /**
     * Construct a <code>IOContext</code>.
     *
     * @param input         The input stream.
     * @param output        The output stream.
     * @param errorOutput   The error output stream.
     */
    public IOContext(final InputStream input, final OutputStream output, final OutputStream errorOutput)
    {
        if (input == null) {
            throw new NullArgumentException("input");
        }
        if (output == null) {
            throw new NullArgumentException("output");
        }
        if (errorOutput == null) {
            throw new NullArgumentException("errorOutput");
        }
        
        this.input = input;
        this.output = output;
        this.errorOutput = errorOutput;
    }
    
    /**
     * Construct a <code>IOContext</code> using the output stream for error output.
     *
     * @param input     The input stream.
     * @param output    The output stream.
     */
    public IOContext(final InputStream input, final OutputStream output)
    {
        this(input, output, output);
    }
    
    /**
     * Construct a <code>IOContext</code> using system defaults.
     */
    public IOContext()
    {
        this(System.in, System.out, System.err);
    }
    
    /**
     * Get the input stream.
     *
     * @return The input stream.
     */
    public InputStream getInputStream()
    {
        return input;
    }
    
    /**
     * Get the reader.
     *
     * @return The reader.
     */
    public Reader getReader()
    {
        if (reader == null) {
            reader = new InputStreamReader(input);
        }
        
        return reader;
    }
    
    /**
     * Get the output stream.
     *
     * @return The output stream.
     */
    public OutputStream getOutputStream()
    {
        return output;
    }
    
    /**
     * Get the writer.
     *
     * @return The writer.
     */
    public PrintWriter getWriter()
    {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(output), true);
        }
        
        return writer;
    }
    
    /**
     * Get the error output stream.
     *
     * @return The error output stream.
     */
    public OutputStream getErrorOutputStream()
    {
        return errorOutput;
    }
    
    /**
     * Get the error writer.
     *
     * @return The error writer.
     */
    public PrintWriter getErrorWriter()
    {
        if (errorWriter == null) {
            errorWriter = new PrintWriter(new OutputStreamWriter(errorOutput), true);
        }
        
        return errorWriter;
    }
}
