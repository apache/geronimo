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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
// encodings include "base64", "quoted-printable", "7bit", "8bit" and "binary".
// In addition, "uuencode" is also supported. The 
/**
 * @version $Revision: 1.1 $ $Date: 2003/08/16 01:55:48 $
 */
public class MimeUtility {
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
