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

package org.apache.geronimo.twiddle.util;

import java.io.PrintWriter;

import org.apache.commons.cli.Options;

import org.apache.geronimo.common.NullArgumentException;

/**
 * A helper to handle command help output.
 *
 * @version <code>$Revision: 1.3 $ $Date: 2004/03/10 10:00:38 $</code>
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
