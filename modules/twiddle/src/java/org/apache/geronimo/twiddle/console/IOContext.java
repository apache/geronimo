/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version <code>$Id: IOContext.java,v 1.2 2003/08/13 15:18:48 jdillon Exp $</code>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
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
