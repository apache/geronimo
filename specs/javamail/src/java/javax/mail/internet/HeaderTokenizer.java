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
//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.mail.internet;
/**
 * @version $Revision: 1.2 $ $Date: 2003/09/04 02:14:40 $
 */
public class HeaderTokenizer {
    public static class Token {
        // Constant values from J2SE 1.4 API Docs (Constant values)
        public static final int ATOM = -1;
        public static final int COMMENT = -3;
        public static final int EOF = -4;
        public static final int QUOTEDSTRING = -2;
        private int _type;
        private String _value;
        public Token(int type, String value) {
            _type = type;
            _value = value;
        }
        public int getType() {
            return _type;
        }
        public String getValue() {
            return _value;
        }
        public String toString() {
            String type = "Unknown";
            switch (_type) {
                case ATOM :
                    type = "Atom";
                    break;
                case COMMENT :
                    type = "Comment";
                    break;
                case EOF :
                    type = "EOF";
                    break;
                case QUOTEDSTRING :
                    type = "String";
                    break;
            }
            return "[" + type + "] \"" + getValue() + "\"";
        }
    }
    private static final Token EOF = new Token(Token.EOF, null);
    // characters not allowed in MIME
    public static final String MIME = "[]()<>@.:,;\\\" \t?=";
    // charaters not allowed in RFC822
    public static final String RFC822 = "[]()<>@.:,;\\\" \t";
    private static final String WHITE = " \t\n\r";
    private String _delimiters;
    private String _header;
    private boolean _skip;
    private int pos;
    public HeaderTokenizer(String header) {
        this(header, RFC822);
    }
    public HeaderTokenizer(String header, String delimiters) {
        this(header, delimiters, true);
    }
    public HeaderTokenizer(
        String header,
        String delimiters,
        boolean skipComments) {
        _skip = skipComments;
        _header = header;
        _delimiters = delimiters;
    }
    public String getRemainder() {
        return _header.substring(pos);
    }
    public Token next() throws ParseException {
        return readToken();
    }
    public Token peek() throws ParseException {
        int start = pos;
        try {
            return readToken();
        } finally {
            pos = start;
        }
    }
    /**
     * @return
     */
    private Token readAtomicToken() {
        // skip to next delimiter
        int start = pos;
        while (++pos < _header.length()
            && _delimiters.indexOf(_header.charAt(pos)) == -1);
        return new Token(Token.ATOM, _header.substring(start, pos));
    }
    private Token readToken() throws ParseException {
        if (pos >= _header.length()) {
            return EOF;
        } else {
            char c = _header.charAt(pos);
            if (c == '(') {
                Token comment = readUntil(')',Token.COMMENT);
                if (_skip) {
                    return readToken();
                } else {
                    return comment;
                }
            } else if (c == '\"') {
                    return readUntil('\"',Token.QUOTEDSTRING);
            } else if (WHITE.indexOf(c) != -1) {
                eatWhiteSpace();
                return readToken();
            } else if (_delimiters.indexOf(c) != -1) {
                pos++;
                return new Token(Token.ATOM, String.valueOf(c));
            } else {
                return readAtomicToken();
            }
        }
    }
    /**
     * @return
     */
    private Token readUntil(char end, int type) {
        int start = ++pos;
        // skip to end of comment/string
        while (++pos < _header.length()
            && _header.charAt(pos) != end);
        String value = _header.substring(start,pos++);
        return new Token(type,value);
    }
    /**
     * @return
     */
    private void eatWhiteSpace() {
        // skip to end of whitespace
        while (++pos < _header.length()
            && WHITE.indexOf(_header.charAt(pos)) != -1);
    }
}
