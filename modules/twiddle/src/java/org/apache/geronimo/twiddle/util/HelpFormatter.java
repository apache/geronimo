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

package org.apache.geronimo.twiddle.util;

import java.io.PrintWriter;

import org.apache.commons.cli.Options;

import org.apache.geronimo.common.NullArgumentException;

/**
 * A helper to handle command help output.
 *
 * @version <code>$Revision: 1.4 $ $Date: 2003/08/16 15:14:12 $</code>
 */
public class HelpFormatter
    extends org.apache.commons.cli.HelpFormatter
{
    /** Platform dependent line separator. */
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    /** Terminal width. */
    public static final int TERMINAL_WIDTH = 80;
    
    /** The writer to print help to. */
    protected PrintWriter out;
    
    /**
     * Construct a <code>HelpFormatter</code>
     *
     * @param out   The writer to print to.
     */
    public HelpFormatter(final PrintWriter out)
    {
        if (out == null) {
            throw new NullArgumentException("out");
        }
        
        this.out = out;
    }
    
    /**
     * Print help.
     *
     * @param desc      The help desctiption, or null for none.
     * @param usage     The command usage/syntax.
     * @param header    The options header, or null for the default.
     * @param options   The command line options.
     * @param footer    The options footer, or null for the default.
     */
    public void print(final String desc, final String usage, String header,
                      final Options options, String footer)
    {
        if (usage == null) {
            throw new NullArgumentException("usage");
        }
        if (options == null) {
            throw new NullArgumentException("options");
        }
        
        if (desc != null) {
            out.println(desc);
            out.println();
        }
        
        if (header == null) {
            header = LINE_SEPARATOR + "Options:";
        }
        
        if (footer == null) {
            footer = LINE_SEPARATOR;
        }
        
        printHelp(out, TERMINAL_WIDTH, usage, header, options, 2, 4, footer);
        out.println();
        out.flush();
    }
    
    /**
     * Print help.
     *
     * @param desc      The help desctiption, or null for none.
     * @param usage     The command usage/syntax.
     * @param options   The command line options.
     */
    public void print(final String desc, final String usage, final Options options)
    {
        print(desc, usage, null, options, null);
    }
    
    /**
     * Print help.
     *
     * @param usage     The command usage/syntax.
     * @param options   The command line options.
     */
    public void print(final String usage, final Options options)
    {
        print(null, usage, null, options, null);
    }
}
