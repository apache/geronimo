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
import java.io.*;
import javax.mail.*;
import java.util.*;
/**
 * @version $Revision: 1.1 $ $Date: 2003/08/16 01:55:48 $
 */
public class InternetHeaders {
    // TODO This is probably bad, managing two copies of the same data structure
    // (maybe)
    private Map _headers = new HashMap();
    private List _lines = new LinkedList();
    public InternetHeaders() {
    }
    public InternetHeaders(InputStream in) throws MessagingException {
        load(in);
    }
    public void load(InputStream in) throws MessagingException {
        // try and figure out what format it's in?
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null && !line.equals("")) {
                addHeaderLine(line);
            }
        } catch (IOException e) {
            throw new MessagingException(e.getMessage());
        }
    }
    private static final String[] STRING_ARRAY = new String[0];
    public String[] getHeader(String name) {
        List list = getHeaderList(name);
        if (list == null) {
            return null;
        } else {
            return (String[]) list.toArray(STRING_ARRAY);
        }
    }
    private List getHeaderList(String name) {
        List list = (List) _headers.get(name);
        return list;
    }
    public String getHeader(String name, String delimiter) {
        List list = getHeaderList(name);
        if (list == null || list.isEmpty()) {
            return null;
        } else {
            Iterator it = list.iterator();
            StringBuffer result = new StringBuffer();
            boolean first = true;
            while (it.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    if (delimiter == null) {
                        break;
                    } else {
                        result.append(delimiter);
                    }
                }
                Header header = (Header) it.next();
                result.append(header.getValue());
            }
            return result.toString();
        }
    }
    public void setHeader(String name, String value) {
        List list = new LinkedList();
        list.add(new Header(name, value));
        _headers.put(name, list);
    }
    public void addHeader(String name, String value) {
        List list = (List) _headers.get(name);
        if (list == null) {
            list = new LinkedList();
            _headers.put(name, list);
        }
        list.add(new Header(name, value));
    }
    public void removeHeader(String name) {
        _headers.remove(name);
    }
    public Enumeration getAllHeaders() {
        return Collections.enumeration(_headers.keySet());
    }
    public Enumeration getMatchingHeaders(String[] names) {
        return getHeaders(names, _headers);
    }
    private Enumeration getHeaders(String[] names, Map map) {
        List result = new LinkedList();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            List list = (List) map.get(name);
            if (list != null) {
                result.addAll(list);
            }
        }
        return Collections.enumeration(result);
    }
    public Enumeration getNonMatchingHeaders(String[] names) {
        Map copy = new HashMap(_headers);
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            copy.remove(name);
        }
        return getHeaders(names, copy);
    }
    public void addHeaderLine(String line) {
        _lines.add(line);
        // figure out if it's a header? Who knows?
    }
    public Enumeration getAllHeaderLines() {
        return Collections.enumeration(_lines);
    }
    public Enumeration getMatchingHeaderLines(String[] names) {
        return getLinesList(names, true);
    }
    private Enumeration getLinesList(String[] names, boolean match) {
        Iterator it = _lines.iterator();
        List result = new LinkedList();
        while (it.hasNext()) {
            String line = (String) it.next();
            String upper = line.toUpperCase();
            for (int i = 0; i < names.length; i++) {
                String name = (names[i] + ":").toUpperCase();
                if (upper.startsWith(name) == match) {
                    result.add(line);
                }
            }
        }
        return Collections.enumeration(result);
    }
    public Enumeration getNonMatchingHeaderLines(String[] names) {
        return getLinesList(names, false);
    }
}
