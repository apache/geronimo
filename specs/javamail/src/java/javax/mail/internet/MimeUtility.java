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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
// encodings include "base64", "quoted-printable", "7bit", "8bit" and "binary".
// In addition, "uuencode" is also supported. The 
/**
 * @version $Rev$ $Date$
 */
public class MimeUtility {
    private MimeUtility(){
    }

    // From J2SE 1.4 API Docs (Constant Values)
    public static final int ALL = -1;
    public static InputStream decode(InputStream in, String encoding)
        throws MessagingException {
        // TODO - take account of encoding
        return in;
    }
    public static String decodeText(String word)
        throws UnsupportedEncodingException {
        // TODO - take account of encoding
        return word;
    }
    public static String decodeWord(String word)
        throws ParseException, UnsupportedEncodingException {
        // TODO - take account of encoding
        return word;
    }
    public static OutputStream encode(OutputStream out, String encoding)
        throws MessagingException {
        // TODO - take account of encoding
        return out;
    }
    public static OutputStream encode(
        OutputStream out,
        String encoding,
        String filename)
        throws MessagingException {
        // TODO - take account of encoding
        return out;
    }
    public static String encodeText(String word)
        throws UnsupportedEncodingException {
        // TODO - take account of encoding
        return word;
    }
    public static String encodeText(
        String word,
        String characterset,
        String encoding)
        throws UnsupportedEncodingException {
        // TODO - take account of encoding
        return word;
    }
    public static String encodeWord(String word)
        throws UnsupportedEncodingException {
        // TODO - take account of encoding
        return word;
    }
    public static String encodeWord(
        String word,
        String characteset,
        String encoding)
        throws UnsupportedEncodingException {
        // TODO - take account of encoding
        return word;
    }
    public static String getDefaultJavaCharset() {
        return "utf-8";
    }
    public static String getEncoding(DataHandler handler) {
        // TODO figure what type of data it is
        return "binary";
    }
    public static String getEncoding(DataSource source) {
        // TODO figure what type of data it is
        return "binary";
    }
    public static String javaCharset(String charset) {
        // TODO Perform translations as appropriate        
        return charset;
    }
    public static String mimeCharset(String charset) {
        // TODO Perform translations as appropriate
        return charset;
    }
    public static String quote(String word, String specials) {
        // TODO Check for specials
        return word;
    }
}
