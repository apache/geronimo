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

package javax.activation;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * @version $Revision: 1.2 $ $Date: 2003/12/07 16:11:44 $
 */
public class MimeTypeParameterList {
    private final static String PARAMETER_SEPARATOR = ";";
    private final static String NAME_VALUE_SEPARATOR = "=";

    Map _mimeTypeParameterMap = new HashMap();

    public MimeTypeParameterList() {

    }

    public MimeTypeParameterList(String parameterList) throws MimeTypeParseException {
        parse(parameterList);
    }

    protected void parse(String parameterList) throws MimeTypeParseException {
        if (parameterList == null) {
            return;
        }

        StringTokenizer tokenizer = new StringTokenizer(parameterList, PARAMETER_SEPARATOR);
        while (tokenizer.hasMoreTokens()) {
            String parameter = tokenizer.nextToken();
            if (parameter.length() == 0) {
                continue;
            }
            int eq = parameter.indexOf(NAME_VALUE_SEPARATOR);
            String name = null;
            if (eq > -1) {
                name = parseToken(parameter.substring(0, eq));
            }
            String value = parseToken(parameter.substring(eq + 1));
            if ((name == null || name.length() == 0) && value.length() == 0) {
                continue;
            }
            if (name.length() == 0 || value.length() == 0) {
                throw new MimeTypeParseException("Name or value is Missing");
            }
            set(name, value);

        }

    }

    public int size() {
        return _mimeTypeParameterMap.size();
    }

    public boolean isEmpty() {
        return _mimeTypeParameterMap.isEmpty();
    }

    public String get(String name) {
        return (String) _mimeTypeParameterMap.get(name);
    }

    public void set(String name, String value) {
        name = parseToken(name);
        value = parseToken(value);
        _mimeTypeParameterMap.put(name, value);
    }

    public void remove(String name) {
        _mimeTypeParameterMap.remove(name);
    }

    public Enumeration getNames() {
        return Collections.enumeration(_mimeTypeParameterMap.keySet());
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (Enumeration enum = getNames(); enum.hasMoreElements();) {
            buf.append(PARAMETER_SEPARATOR);
            String name = (String) enum.nextElement();
            buf.append(name).append(NAME_VALUE_SEPARATOR).append(get(name));
        }
        return buf.toString();
    }

    private String parseToken(String token) {
        // TODO it seems to have unauthorized chars
        return removeBlank(token);
    }

    private String removeBlank(String str) {
        StringBuffer buf = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(str);
        while (tokenizer.hasMoreTokens()) {
            buf.append(tokenizer.nextToken());
        }
        return buf.toString();
    }
}