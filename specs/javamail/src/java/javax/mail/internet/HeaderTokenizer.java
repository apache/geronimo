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

package javax.mail.internet;
/**
 * @version $Rev$ $Date$
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
