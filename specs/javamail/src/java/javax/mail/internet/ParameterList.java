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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
// Represents lists in things like
// Content-Type: text/plain;charset=klingon
//
// The ;charset=klingon is the parameter list, may have more of them with ';'
/**
 * @version $Revision: 1.1 $ $Date: 2003/08/16 01:55:48 $
 */
public class ParameterList {
    private Map _parameters = new HashMap();
    private static final String _separator = ";";
    private static final String _divider = "=";
    public ParameterList() {
    }
    public ParameterList(String list) throws ParseException {
        // TODO Parse List and add to parameters
        if (list != null) {
            int pos;
            int last = 0;
            list = list + ";"; // HACK so that it picks up the last one
            while ((pos = list.indexOf(_separator, last)) != -1) {
                if (pos == -1) {
                    pos = list.length();
                }
                String string = list.substring(last, pos);
                int div = string.indexOf(_divider);
                if (div == -1) {
                    throw new ParseException("Cannot parse " + string);
                } else {
                    set(
                        string.substring(0, div),
                        string.substring(div + _divider.length()));
                }
                last = pos + _separator.length();
            }
        }
    }
    public int size() {
        return _parameters.size();
    }
    public String get(String name) {
        return (String) _parameters.get(name);
    }
    public void set(String name, String value) {
        _parameters.put(name.trim(), value.trim());
    }
    public void remove(String name) {
        _parameters.remove(name);
    }
    public Enumeration getNames() {
        return Collections.enumeration(_parameters.keySet());
    }
    public String toString() {
        Iterator it = _parameters.entrySet().iterator();
        StringBuffer result = new StringBuffer();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            result.append(
                entry.getKey()
                    + _divider
                    + " "
                    + entry.getValue()
                    + _separator);
        }
        return result.toString();
        // TODO Return in same list as parsed format
    }
    public String toString(int lineBreak) {
        // figure out where to break the line
        String answer = toString();
        if (answer.length() > lineBreak) {
            // convert it to substring
            // TODO Implement
            return "";
        } else {
            return answer;
        }
    }
}
