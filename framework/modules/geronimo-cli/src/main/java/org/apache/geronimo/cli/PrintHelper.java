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
package org.apache.geronimo.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * This code is borrowed from commons-cli <code>org.apache.commons.cli.HelpFormatter</code> class. Its authors are 
 * Slawek Zachcial and John Keyes (john at integralsource.com). This class has been slightly updated to meet specific
 * requirements.
 * 
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class PrintHelper {

    public static String reformat(String source, int indent, int width) {
        int endCol = width;
        if (endCol == 0) {
            endCol = DEFAULT_WIDTH;
        }
        if(endCol-indent < 10) {
            throw new IllegalArgumentException("Need at least 10 spaces for " +
                "printing, but indent=" + indent + " and endCol=" + endCol);
        }
        StringBuilder buf = new StringBuilder((int)(source.length()*1.1));
        String prefix = indent == 0 ? "" : buildIndent(indent);
        try {
            BufferedReader in = new BufferedReader(new StringReader(source));
            String line;
            int pos;
            while((line = in.readLine()) != null) {
                if(buf.length() > 0) {
                    buf.append('\n');
                }
                while(line.length() > 0) {
                    line = prefix + line;
                    if(line.length() > endCol) {
                        pos = line.lastIndexOf(' ', endCol);
                        if(pos < indent) {
                            pos = line.indexOf(' ', endCol);
                            if(pos < indent) {
                                pos = line.length();
                            }
                        }
                        buf.append(line.substring(0, pos)).append('\n');
                        if(pos < line.length()-1) {
                            line = line.substring(pos+1);
                        } else {
                            break;
                        }
                    } else {
                        buf.append(line).append("\n");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new AssertionError("This should be impossible");
        }
        return buf.toString();
    }

    private static String buildIndent(int indent) {
        StringBuilder buf = new StringBuilder(indent);
        for(int i=0; i<indent; i++) {
            buf.append(' ');
        }
        return buf.toString();
    }

    public static final int DEFAULT_WIDTH = 76;
    public static final int DEFAULT_LEFT_PAD = 1;
    public static final int DEFAULT_DESC_PAD = 3;
    public static final String DEFAULT_SYNTAX_PREFIX = "usage: ";
    public static final String DEFAULT_OPT_PREFIX = "-";
    public static final String DEFAULT_LONG_OPT_PREFIX = "--";
    public static final String DEFAULT_ARG_NAME = "arg";

    private final OutputStream outputStream;
    public int defaultWidth;
    public int defaultLeftPad;
    public int defaultDescPad;
    public String defaultSyntaxPrefix;
    public String defaultNewLine;
    public String defaultOptPrefix;
    public String defaultLongOptPrefix;
    public String defaultArgName;

    public PrintHelper(OutputStream outputStream) {
        if (null == outputStream) {
            throw new IllegalArgumentException("outputStream is required");
        }
        this.outputStream = outputStream;
        
        defaultWidth = DEFAULT_WIDTH;
        defaultLeftPad = DEFAULT_LEFT_PAD;
        defaultDescPad = DEFAULT_DESC_PAD;
        defaultSyntaxPrefix = DEFAULT_SYNTAX_PREFIX;
        defaultNewLine = System.getProperty("line.separator");
        defaultOptPrefix = DEFAULT_OPT_PREFIX;
        defaultLongOptPrefix = DEFAULT_LONG_OPT_PREFIX;
        defaultArgName = DEFAULT_ARG_NAME;
    }

    public void printHelp(String cmdLineSyntax, String header, Options options, String footer, boolean autoUsage) {
        printHelp(defaultWidth, cmdLineSyntax, header, options, footer, autoUsage);
    }

    public void printHelp(int width,
            String cmdLineSyntax,
            String header,
            Options options,
            String footer,
            boolean autoUsage) {
        PrintWriter pw = new PrintWriter(outputStream);
        printHelp(pw, width, cmdLineSyntax, header, options, defaultLeftPad, defaultDescPad, footer, autoUsage);
        pw.flush();
    }

    public void printHelp(PrintWriter pw,
            int width,
            String cmdLineSyntax,
            String header,
            Options options,
            int leftPad,
            int descPad,
            String footer,
            boolean autoUsage) throws IllegalArgumentException {
        if (cmdLineSyntax == null || cmdLineSyntax.length() == 0) {
            throw new IllegalArgumentException("cmdLineSyntax not provided");
        }

        if (autoUsage) {
            printUsage(pw, width, cmdLineSyntax, options);
        } else {
            printUsage(pw, width, cmdLineSyntax);
        }

        if (header != null && header.trim().length() > 0) {
            printWrapped(pw, width, header);
        }
        printOptions(pw, width, options, leftPad, descPad);
        if (footer != null && footer.trim().length() > 0) {
            printWrapped(pw, width, footer);
        }
    }

    public void printUsage(PrintWriter pw, int width, String app, Options options) {
        // create a list for processed option groups
        ArrayList list = new ArrayList();

        StringBuilder optionsBuff = new StringBuilder();
        
        // temp variable
        Option option;

        // iterate over the options
        for (Iterator i = options.getOptions().iterator(); i.hasNext();) {
            // get the next Option
            option = (Option) i.next();

            // check if the option is part of an OptionGroup
            OptionGroup group = options.getOptionGroup(option);

            // if the option is part of a group and the group has not already
            // been processed
            if (group != null && !list.contains(group)) {

                // add the group to the processed list
                list.add(group);

                // get the names of the options from the OptionGroup
                Collection names = group.getNames();

                optionsBuff.append("[");

                // for each option in the OptionGroup
                for (Iterator iter = names.iterator(); iter.hasNext();) {
                    optionsBuff.append(iter.next());
                    if (iter.hasNext()) {
                        optionsBuff.append("|");
                    }
                }
                optionsBuff.append("] ");
            } else if (group == null) {
                // if the Option is not part of an OptionGroup
                // if the Option is not a required option
                if (!option.isRequired()) {
                    optionsBuff.append("[");
                }

                if (!" ".equals(option.getOpt())) {
                    optionsBuff.append("-").append(option.getOpt());
                } else {
                    optionsBuff.append("--").append(option.getLongOpt());
                }

                if (option.hasArg()) {
                    optionsBuff.append(" ");
                }

                // if the Option has a value
                if (option.hasArg()) {
                    optionsBuff.append(option.getArgName());
                }

                // if the Option is not a required option
                if (!option.isRequired()) {
                    optionsBuff.append("]");
                }
                optionsBuff.append(" ");
            }
        }
        
        app = app.replace("$options", optionsBuff.toString());

        // call printWrapped
        printWrapped(pw, width, app.indexOf(' ') + 1, app);
    }

    public void printUsage(PrintWriter pw, int width, String cmdLineSyntax) {
        int argPos = cmdLineSyntax.indexOf(' ') + 1;
        printWrapped(pw, width, defaultSyntaxPrefix.length() + argPos, defaultSyntaxPrefix + cmdLineSyntax);
    }

    public void printOptions(PrintWriter pw, int width, Options options, int leftPad, int descPad) {
        StringBuilder sb = new StringBuilder();
        renderOptions(sb, width, options, leftPad, descPad, true);
        pw.println(sb.toString());
    }
    
    public void printOptions(PrintWriter pw, Options options) {
        StringBuilder sb = new StringBuilder();
        renderOptions(sb, defaultWidth, options, defaultLeftPad, defaultDescPad, true);
        pw.println(sb.toString());
    }
    
    public void printOptionsNoDesc(PrintWriter pw, Options options) {
        StringBuilder sb = new StringBuilder();
        renderOptions(sb, defaultWidth, options, defaultLeftPad, defaultDescPad, false);
        pw.println(sb.toString());
    }

    public void printWrapped(PrintWriter pw, int width, String text) {
        printWrapped(pw, width, 0, text);
    }

    public void printWrapped(PrintWriter pw, int width, int nextLineTabStop, String text) {
        StringBuilder sb = new StringBuilder(text.length());
        renderWrappedText(sb, width, nextLineTabStop, text);
        pw.println(sb.toString());
    }

    protected StringBuilder renderOptions(StringBuilder sb, int width, Options options, int leftPad, int descPad, boolean displayDesc) {
        final String lpad = createPadding(leftPad);
        final String dpad = createPadding(descPad);

        //first create list containing only <lpad>-a,--aaa where -a is opt and --aaa is
        //long opt; in parallel look for the longest opt string
        //this list will be then used to sort options ascending
        int max = 0;
        StringBuilder optBuf;
        List prefixList = new ArrayList();
        Option option;
        List optList = new ArrayList(options.getOptions());
        Collections.sort(optList, new StringBuilderComparator());
        for (Iterator i = optList.iterator(); i.hasNext();) {
            option = (Option) i.next();
            optBuf = new StringBuilder(8);

            if (option.getOpt().equals(" ")) {
                optBuf.append(lpad).append("   " + defaultLongOptPrefix).append(option.getLongOpt());
            } else {
                optBuf.append(lpad).append(defaultOptPrefix).append(option.getOpt());
                if (option.hasLongOpt()) {
                    optBuf.append(',').append(defaultLongOptPrefix).append(option.getLongOpt());
                }

            }

            if (option.hasArg()) {
                if (option.hasArgName()) {
                    optBuf.append(" <").append(option.getArgName()).append('>');
                } else {
                    optBuf.append(' ');
                }
            }

            prefixList.add(optBuf);
            max = optBuf.length() > max ? optBuf.length() : max;
        }
        int x = 0;
        for (Iterator i = optList.iterator(); i.hasNext();) {
            option = (Option) i.next();
            optBuf = new StringBuilder(prefixList.get(x++).toString());

            if (optBuf.length() < max) {
                optBuf.append(createPadding(max - optBuf.length()));
            }
            optBuf.append(dpad);
            
            if (displayDesc) {
                optBuf.append(option.getDescription());
            }
            int nextLineTabStop = max + descPad;
            renderWrappedText(sb, width, nextLineTabStop, optBuf.toString());
            if (i.hasNext()) {
                sb.append(defaultNewLine);
                if (displayDesc) {
                    sb.append(defaultNewLine);
                }
            }
        }

        return sb;
    }

    protected StringBuilder renderWrappedText(StringBuilder sb, int width, int nextLineTabStop, String text) {
        int pos = findWrapPos(text, width, 0);
        if (pos == -1) {
            sb.append(rtrim(text));
            return sb;
        } else {
            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }

        //all following lines must be padded with nextLineTabStop space characters
        final String padding = createPadding(nextLineTabStop);

        while (true) {
            text = padding + text.substring(pos).trim();
            pos = findWrapPos(text, width, 0);
            if (pos == -1) {
                sb.append(text);
                return sb;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }

    }

    protected int findWrapPos(String text, int width, int startPos) {
        int pos = -1;
        // the line ends before the max wrap pos or a new line char found
        if (((pos = text.indexOf('\n', startPos)) != -1 && pos <= width)
                || ((pos = text.indexOf('\t', startPos)) != -1 && pos <= width)) {
            return pos;
        } else if ((startPos + width) >= text.length()) {
            return -1;
        }

        //look for the last whitespace character before startPos+width
        pos = startPos + width;
        char c;
        while (pos >= startPos && (c = text.charAt(pos)) != ' ' && c != '\n' && c != '\r') {
            --pos;
        }
        //if we found it - just return
        if (pos > startPos) {
            return pos;
        } else {
            //must look for the first whitespace chearacter after startPos + width
            pos = startPos + width;
            while (pos <= text.length() && (c = text.charAt(pos)) != ' ' && c != '\n' && c != '\r') {
                ++pos;
            }
            return pos == text.length() ? -1 : pos;
        }
    }

    protected String createPadding(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; ++i) {
            sb.append(' ');
        }
        return sb.toString();
    }

    protected String rtrim(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }

        int pos = s.length();
        while (pos >= 0 && Character.isWhitespace(s.charAt(pos - 1))) {
            --pos;
        }
        return s.substring(0, pos);
    }

    private static class StringBuilderComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            String str1 = stripPrefix(o1.toString());
            String str2 = stripPrefix(o2.toString());
            return (str1.compareTo(str2));
        }

        private String stripPrefix(String strOption) {
            // Strip any leading '-' characters
            int iStartIndex = strOption.lastIndexOf('-');
            if (iStartIndex == -1) {
                iStartIndex = 0;
            }
            return strOption.substring(iStartIndex);

        }
    }

}
